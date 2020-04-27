package fr.upem.net.tcp.nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.frame.FrameVisitor;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.SelectReaderOpcode;

/**
 * <p>
 * The ChatHack will let a client communicate with a server and other clients.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class ChatHack {
	
	private static enum State {
		DONE, WAITING_PUBLIC_CONNECTION, SENDING_REPONSE, WATING_REPONSE, YES, NO, NONE;
	}
	
	private static class Action {
		private final BlockingQueue<String> actions = new LinkedBlockingQueue<>();
		private final Map<String, SelectionKey> map = new HashMap<>();
		private final ReentrantLock lock = new ReentrantLock();
		private final Condition condition = lock.newCondition();
		private State state = State.WAITING_PUBLIC_CONNECTION;
		private String privateConnexion;
		private String firstPrivateMessage;
		private boolean isDone = false;
		
		
		public void setPrivateConnexion(String privateConnexion) {
			lock.lock();
			try {
				this.privateConnexion = privateConnexion;
				state = State.NONE;
			} finally {
				lock.unlock();
			}

		}
		
		private void privateMessageOrFile(String action, ChatHack client) throws IOException {
			firstPrivateMessage = action;
			String[] s = action.split(" ", 2);
			String log = s[0].substring(1);
			if (map.containsKey(log) && !map.get(log).isValid()) {
				logger.info("Client "+log+ " does not exist");
				map.remove(log);
			}
			if (log.equals(client.login)) {
				logger.info("Cannot create private connexion with yourself");
			}
			else if (!map.containsKey(log)) {
				state = State.SENDING_REPONSE;
				client.sendPrivateConnectionRequest(log);
			} else {
				String text = " ";
				if (s.length == 2 && !s[1].isBlank()) {
					text = s[1];
					sendingMessageOrFile(client, action, log, text);
				}
			} 
		}

		public void treat(ChatHack client) throws IOException, InterruptedException {
			if (!client.uniqueKey.isValid()) {
				logger.log(Level.SEVERE, "server is closed");
				end(client);	
			}
			if (state == State.WAITING_PUBLIC_CONNECTION) {
				client.sendPublicConnectionRequest();
				state = State.NONE;
			}
			var action = actions.poll();
			if (action == null) {
				return;
			}
			if (state == State.DONE) {
				state = State.NONE;
				return;
			}
			if (action.startsWith("^Z") || action.startsWith("^z")) {
				String[] s = action.split(" ", 2);
				if (s.length == 2) {
					if (!map.containsKey(s[1]) || !map.get(s[1]).isValid()) {
						return;
					}
					logger.info("Deconneted to client "+s[1]);
					client.sendDeconnexionRequest(map.get(s[1]), client.login);
					map.remove(s[1]);
				} else {
					client.sendDeconnexionRequest(client.uniqueKey, client.login);
				}
			}
			else if (action.startsWith("@") || action.startsWith("/")) {
				privateMessageOrFile(action, client);
			} else {
				if (state == State.NONE)
					client.sendGlobalMessage(action);
			}
		}
		
		private Frame createFileFrame(Path path, String login, String fileName) throws IOException {
			try (FileChannel fc = FileChannel.open(Path.of(path.toString(), fileName), StandardOpenOption.READ)) {
				ByteBuffer bb = ByteBuffer.allocate((int)fc.size());
				while (bb.hasRemaining()) {
					if (fc.read(bb) == -1) {
						break;
					}
				}
				bb.flip();
				var data = Data.createDataPrivateFile(StandardOperation.PRIVATE_FILE, login, fileName, bb);
				return Frame.createFramePrivateFile(data);
			}
		}
		
		private void sendingMessageOrFile(ChatHack client, String message, String login, String text) throws IOException {
			state = State.NONE;
			SelectionKey key = map.get(login);
			System.out.println("ici:" + map.keySet() + " " + key.isValid());
			if (key == null || key.attachment() == null || !key.isValid() ) {
				map.remove(login);
				System.out.println("--- HERE ---");
				return;
			} 
			Context context = (Context)key.attachment();
			Frame frame;
			if (message.startsWith("@")) {
				var data = Data.createDataPrivateMessage(StandardOperation.PRIVATE_MESSAGE, client.login, text);
				frame = Frame.createFramePrivateMessage(data);
			} else {
				frame = createFileFrame(client.path, client.login, text);
			}
			context.queueMessage(frame.buffer());
		}
		
		private byte sendingReponse() throws InterruptedException {
			lock.lock();
			try {
				state = State.WATING_REPONSE;
				while (state != State.YES && state != State.NO) {
					condition.await();
				}
				condition.signal();
				return (byte) (state == State.YES ? 0 : 1);
			} finally {
				lock.unlock();
			}
		}
		
		private void isWaitingReponse(ChatHack client, String reponse) {
			lock.lock();
			try {
				if (state == State.WATING_REPONSE) {
					if (reponse.equals("O") || reponse.equals("o")) {
						state = State.YES;
					} else if (reponse.equals("N") || reponse.equals("n")) {
						state = State.NO;
					} else {
						logger.info("ENTER O/N");
					}
					condition.signal();
				} 
			} finally {
				lock.unlock();
			}
		}
		
		private void put(String key, SelectionKey value) {
			lock.lock();
			try {
				map.put(key, value);
				state = State.NONE;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * @return the isDone
		 */
		public boolean isDone(ChatHack client) {
			lock.lock();
			try {
				if (isDone) {
					client.selector.wakeup();
				}
				return isDone;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * @param isDone the isDone to set
		 * @throws IOException a {@link IOException}
		 */
		public void end(ChatHack client) throws IOException {
			lock.lock();
			try {
				this.isDone = true;
				client.thread.interrupt();
				client.serverSocketChannel.close();
			} finally {
				lock.unlock();
			}
		}
		
	}
	
	private static class Context {
		final private SelectionKey selectionKey;
		final private SocketChannel socketChannel;
		final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
		final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
		final private BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
		final private Reader<Data> reader;
		private boolean closed = false;
		final private FrameVisitor fv;

		private Context(ChatHack client, SelectionKey selectionKey, FrameVisitor fv) {
			this.selectionKey = selectionKey;
			socketChannel = (SocketChannel) selectionKey.channel();
			reader = SelectReaderOpcode.create(bbin);
			this.fv = fv;
		}

		public static Context createPublicClient(ChatHack client, SelectionKey selectionKey) {
			Objects.requireNonNull(client);
			Objects.requireNonNull(selectionKey);
			FrameVisitor fv = new FrameVisitor().

			when(Data.DataAck.class, d -> {
				try {
					switch (d.request()) {
						case CONNEXION:
							logger.info("Connected to: " + client.sc.getRemoteAddress());
							break;
						case DECONNEXION:
							logger.info("Deconnexion to server: "+ client.sc.getRemoteAddress());
							client.action.end(client);
							break;
						default:
							throw new AssertionError();
					}
				} catch (IOException e) { }
				return null;}).

			when(Data.DataError.class, d -> {
				
				try {
					switch (d.requestCode()) {
						case CONNEXION:
							logger.info("Connection failed");
							client.action.end(client);
							break;
						case PRIVATE_CONNEXION:
							logger.info("failed private connexion demand ");
							client.action.state = State.NONE;
							break;
						default:
							throw new AssertionError("Unexpected value: " + d.requestCode());
					}
				} catch (IOException e) { }
				
				return null;}).
			
			when(Data.DataGlobalClient.class, d -> {
				System.out.println(d.login()+":"+d.message());
				return null;}).
			when(Data.DataPrivateConnectionRequested.class, d -> {
				if (selectionKey.attachment() == null) {
					client.action.state = State.DONE;
					return null;
				}
				Frame frame = null;
				try {
					logger.info(d.secondClient()+" wants to start a private conversation enter O/N");
					byte s = client.action.sendingReponse();
					var data = Data.createDataPrivateConnectionReponse(d.opcode(), (byte)3, d.firstClient(), d.secondClient(), s);
					frame = Frame.createFramePrivateConnectionReponse(data);
					Context context = (Context)selectionKey.attachment();
					if (context == null) {
						return null;
					}
					context.queueMessage(frame.buffer());
					client.action.state = State.DONE;
				} catch (InterruptedException e) {
					
				}

				return frame;}).
			
			when(Data.DataPrivateConnectionReponse.class, d -> {
				Context context = (Context)selectionKey.attachment();
				if (context == null) {
					return null;
				}
				if (d.state() == 0) {
					logger.info(d.secondClient()+" accepted the demand");
					
					var data = Data.createDataPrivateConnectionAccepted(StandardOperation.PRIVATE_CONNEXION, (byte)5, d.firstClient(), d.secondClient(), 
							client.serverSocketChannel.socket().getLocalPort(), client.serverSocketChannel.socket().getInetAddress().getHostName(),
							System.currentTimeMillis());
					Frame frame = Frame.createFramePrivateConnectionAccepted(data);
					context.queueMessage(frame.buffer());
					client.action.setPrivateConnexion(d.secondClient());
					
					
				} else {
					logger.info(d.secondClient()+" denied the demand");
					client.action.state = State.NONE;
				}
				return null;}).
			
			when(Data.DataPrivateConnectionAccepted.class, d -> {
				logger.info("Client "+d.secondClient()+ " with the token "+d.token());
				SocketAddress sa = new InetSocketAddress(d.host(), d.port());
				try {
				
					SocketChannel sc = SocketChannel.open();
					sc.configureBlocking(false);
					sc.connect(sa);
					var key = sc.register(client.selector, SelectionKey.OP_CONNECT);
					key.attach(Context.createPrivateClient(client, key));
					client.action.put(d.secondClient(), key);
				} catch (IOException e) { }
				return null;
			});

			return new Context(client, selectionKey, fv);
		}

		public static Context createPrivateClient(ChatHack client, SelectionKey selectionKey) {
			Objects.requireNonNull(client);
			Objects.requireNonNull(selectionKey);
			FrameVisitor fv = new FrameVisitor().
			
			when(Data.DataPrivateMessage.class, d -> {
				System.out.println("private message from "+d.login()+" : "+d.message());
				SelectionKey k = client.action.map.get(d.login());
				if (k == null || k.attachment() == null) {
					return null;
				}
				var data = Data.createDataPrivateAck(StandardOperation.ACK, StandardOperation.PRIVATE_MESSAGE, client.login);
				Frame frame = Frame.createFramePrivateAck(data);
				((Context) k.attachment()).queueMessage(frame.buffer());
				return null;}).
			
			when(Data.DataPrivateFile.class, d -> {
				Path path = Path.of(client.path.toString(), d.fileName());
				SelectionKey k = client.action.map.get(d.login());
				if (k == null || k.attachment() == null) {
					return null;
				}
				if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
					var data = Data.createDataError(StandardOperation.ERROR, StandardOperation.PRIVATE_FILE);
					Frame frame = Frame.createFrameError(data);
					((Context) k.attachment()).queueMessage(frame.buffer());
					return frame;
					
				}
				try (FileChannel fc = FileChannel.open(path , StandardOpenOption.CREATE
															, StandardOpenOption.TRUNCATE_EXISTING
															, StandardOpenOption.WRITE)) {
					ByteBuffer bb = d.buffer();
					while (bb.hasRemaining()) {
						if (fc.write(bb) == -1) {
							break;
						}
					}
					logger.info("Received file "+d.fileName()+" from "+d.login());
				} catch (IOException e) { }
				
				var data = Data.createDataPrivateAck(StandardOperation.ACK, StandardOperation.PRIVATE_FILE, client.login);
				Frame frame = Frame.createFramePrivateAck(data);
				((Context) k.attachment()).queueMessage(frame.buffer());
				return frame;}).
			
			when(Data.DataDeconnexion.class, d -> {
				client.action.map.remove(d.login());
				logger.info("Deconnected to "+d.login());
				return null;
			}).
			
			when(Data.DataPrivateAck.class, d -> {
				switch (d.request()) {
					case PRIVATE_FILE:
						System.out.println(d.login() + " received the file");
						break;
					case PRIVATE_MESSAGE:
						System.out.println(d.login() + " read the message");
						break;
					default:
						throw new IllegalArgumentException("Unexpected value: " + d);
				}
				return null;}).
			
			when(Data.DataError.class, d -> {
				logger.info("Private Error");
				return null;
			});

			return new Context(client, selectionKey, fv);
		}


		/**
		 * Add a message to the message queue, tries to fill bbOut and updateInterestOps
		 *
		 * @param bb a {@link ByteBuffer}
		 */
		private void queueMessage(ByteBuffer bb) {
			
			queue.add(bb);
			processOut();
			updateInterestOps();
			
		}
		
		private void transferBytes(ByteBuffer src, ByteBuffer dst) {
			while (src.hasRemaining() && dst.hasRemaining()) {
				dst.put(src.get());
			}
		} 
		
		/**
		 * Try to fill bbout from the message queue
		 *
		 */
		private void processOut() {
			while (!queue.isEmpty() && bbout.hasRemaining()) {
				transferBytes(queue.peek(), bbout); 
				if (!queue.peek().hasRemaining()) {
					queue.poll();
				}
				//bbout.put(queue.poll());
			}
		}

		/**
		 * Process the content of bbin
		 *
		 * The convention is that bbin is in write-mode before the call to process and
		 * after the call
		 * 
		 * @throws IOException
		 *
		 */
		private void processIn() throws IOException {
			for (;;) {
				try {
					Reader.ProcessStatus status = reader.process();
					switch (status) {
						case DONE:
							Data frame = reader.get();
							fv.call(frame);
							reader.reset();
							break;
						case REFILL:
							return;
						case ERROR:
							logger.info("request fail sending error frame");
							//queueMessage(fv.call(Data.createDataError(StandardOperation.ERROR, (byte)1)));
							silentlyClose();
							return;
						}
				} catch(IllegalArgumentException e) {
					logger.info("inexistant package format.");
				}

			}
		}

		/**
		 * Performs the read action on sc
		 *
		 * The convention is that both buffers are in write-mode before the call to
		 * doRead and after the call
		 *
		 * @throws IOException
		 */
		private void doRead() throws IOException {
			
			if (socketChannel.read(bbin) == -1) {
				closed = true;
			}
			processIn();
			updateInterestOps();
		}

		/**
		 * Performs the write action on sc
		 *
		 * The convention is that both buffers are in write-mode before the call to
		 * doWrite and after the call
		 *
		 * @throws IOException
		 */
		private void doWrite() throws IOException {
			bbout.flip();
			socketChannel.write(bbout);
			bbout.compact();
			processOut();
			updateInterestOps();
		}

		private void doConnect() throws IOException {
			if (!socketChannel.finishConnect()) {
				return;
			}
			updateInterestOps();
		}

		/**
		 * Update the interestOps of the key looking only at values of the boolean
		 * closed and of both ByteBuffers.
		 *
		 * The convention is that both buffers are in write-mode before the call to
		 * updateInterestOps and after the call. Also it is assumed that process has
		 * been be called just before updateInterestOps.
		 */
		private void updateInterestOps() {
			int ops = 0;
			if (bbin.hasRemaining() && !closed) {
				ops |= SelectionKey.OP_READ;
			}
			if (bbout.position() != 0) {
				ops |= SelectionKey.OP_WRITE;
			}
			if (ops == 0) {
				silentlyClose();
			} else {
				selectionKey.interestOps(ops);
			}
		}

		private void silentlyClose() {
			try {
				socketChannel.close();
				
			} catch (IOException e) {
				// ignore exception
			}
		}
	}

	final private static int BUFFER_SIZE = 1_024;
	final private static Logger logger = Logger.getLogger(ChatHack.class.getName());
	private final ServerSocketChannel serverSocketChannel;
	private final SocketChannel sc;
	private final Selector selector;
	private final SocketAddress serverAddress;
	private SelectionKey uniqueKey;
	private final Path path;
	private final String login;
	private final Optional<String> password;
	private final Action action;
	private Thread thread;
	

	/**
	 * Constructs a ChatHack with it's path, clientPort, host, port, login and password.
	 * @param path a {@link Path}
	 * @param clientPort a int
	 * @param host a {@link String}
	 * @param port a int
	 * @param login a {@link String}
	 * @param password a {@link Optional} of {@link String}
	 * @throws IOException a {@link IOException}.
	 */
	private ChatHack(Path path, int clientPort, String host, int port, String login, Optional<String> password) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		selector = Selector.open();
		sc = SocketChannel.open();
		serverAddress = new InetSocketAddress(host, port);
		serverSocketChannel.bind(new InetSocketAddress(clientPort));
		action = new Action();
		this.path = path;
		this.login = login;
		this.password = password;

	}

	/**
	 * <p>Factory to create ChatHack, it will also attribute a free random port number to identify our client.</p>
	 * The path will determine the directory in which there will be the files our client wants acess to echange.<br>
	 * The hostname and port are use to link the client to a server.<br>
	 * The login and password are used to identify a client but also allow the client to connect with the server.<br>
	 * @param path a {@link Path}
	 * @param hostname a {@link String}
	 * @param port a int.
	 * @param login a {@link String}
	 * @param password a {@link Optional} of {@link String}.
	 * @return ChatHack
	 * @throws IOException a {@link IOException}.
	 */
	public static ChatHack create(Path path, String hostname, int port, String login, Optional<String> password) throws IOException {
		Objects.requireNonNull(path);
		Objects.requireNonNull(hostname);
		Objects.requireNonNull(login);
		Objects.requireNonNull(password);
		int clientPort;
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			clientPort =  socket.getLocalPort();
		}
		return new ChatHack(path, clientPort, hostname, port, login, password);

	}

	private void sendAction() throws InterruptedException {
		while (!Thread.interrupted() && !action.isDone(this)) {
			try (Scanner scanner = new Scanner(System.in)) {
				while (scanner.hasNextLine()) {
					String message = scanner.nextLine();
					action.actions.put(message);
					action.isWaitingReponse(this, message);
					selector.wakeup();
				}
			}
		}
	}

	private void sendPublicConnectionRequest() {
		if (uniqueKey.attachment() == null) {
			return;
		}
		Context context = (Context) uniqueKey.attachment();
		byte connexion = (byte) (password.isEmpty() ? 1 : 0);
		var data = Data.createDataConnectionClient(StandardOperation.CONNEXION, connexion, login, password);
		Frame frame = Frame.createFrameConnection(data);
		context.queueMessage(frame.buffer());
	}
	
	private void sendGlobalMessage(String message) {
		if (uniqueKey.attachment() == null) {
			return;
		}
		Context context = (Context)uniqueKey.attachment();
		var data = Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, login, message);
		Frame frame = Frame.createFrameGlobal(data);
		context.queueMessage(frame.buffer());
	}	
	
	private void sendPrivateConnectionRequest(String secondClient) {
		if (uniqueKey.attachment() == null) {
			return;
		}
		Context context = (Context)uniqueKey.attachment();
		var data = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, (byte)1, login, secondClient);
		Frame frame = Frame.createFramePrivateConnectionRequested(data);
		context.queueMessage(frame.buffer());
	}
	
	private void sendDeconnexionRequest(SelectionKey key, String login) {
		if (key.attachment() == null) {
			return;
		}
		Context context = (Context)key.attachment();
		var data = Data.createDataDeconnexion(StandardOperation.DECONNEXION, login);
		Frame frame = Frame.createFrameDeconnexion(data);
	
		context.queueMessage(frame.buffer());
	}
	
	

	/**
	 * <p>Will launch the client and allow it to exchange with the server and other clients.</p>
	 * Will start another thread to get typing information and send Frame respectively.
	 * @throws IOException a {@link IOException}.
	 * @throws InterruptedException a {@link InterruptedException}.
	 */
	public void launch() throws IOException, InterruptedException {
		thread = new Thread(() -> {
			try {
				sendAction();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		});
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		sc.configureBlocking(false);
		sc.connect(serverAddress);
		uniqueKey=sc.register(selector, SelectionKey.OP_CONNECT);
		uniqueKey.attach(Context.createPublicClient(this, uniqueKey));
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		thread.start();
		while (!Thread.interrupted() && !action.isDone(this)) {
			if (!uniqueKey.isValid()) {
				logger.log(Level.SEVERE, "Interruption of Client - Server connection");
				Thread.currentThread().interrupt();
				return;
			}
			selector.select();
			processSelectedKeys(selectedKeys);
			action.treat(this);
			selectedKeys.clear();
		}
	}

	private void processSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException, InterruptedException {
		for (SelectionKey key : selectedKeys) {
			try {
				if (key.isValid() && key.isAcceptable()) {
					doAccept(key);
				}
				if (key.isValid() && key.isConnectable()) {
					((Context)key.attachment()).doConnect();
				}
				if (key.isValid() && key.isWritable()) {
					((Context)key.attachment()).doWrite();
				}
				if (key.isValid() && key.isReadable()) {
					((Context)key.attachment()).doRead();
				}
			} catch(SocketException e) {
				key.cancel();
			}
			
		}
	}

	private void doAccept(SelectionKey selectionKey) throws IOException, InterruptedException {
		SocketChannel sc = serverSocketChannel.accept();
		if (sc != null) {
			sc.configureBlocking(false);
			SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ);
			clientKey.attach(Context.createPrivateClient(this, clientKey));
			action.put(action.privateConnexion, clientKey);
			action.actions.put(action.firstPrivateMessage);
		}
	}

	private static void usage() {
		System.out.println("Usage : ChatHack path, host, port, login and maybe password");
	}

	/**
	 * 	<p>Will take up to 4 or 5 arguments in following order.</p>
	 * 	<ul>
	 * 		<li>a valid path to a directory</li>
	 * 		<li>the host of the server</li>
	 * 		<li>the server port</li>
	 * 		<li>the client login</li>
	 * 		<li>password not necessary in some cases</li>
	 * 	</ul>
	 * @param args a array of {@link String}
	 * @throws NumberFormatException a {@link NumberFormatException}
	 * @throws IOException a {@link IOException}
	 * @throws InterruptedException a {@link InterruptedException}
	 */
	public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException {
		if (args.length != 4 && args.length != 5) {
			usage();
			return;
		}
		Path path = Path.of(args[0]);
		String host = args[1];
		int port = Integer.parseInt(args[2]);
		String login = args[3];
		Optional<String> password = Optional.empty();
		if (args.length == 5) {
			password = Optional.of(args[4]);
		}
		create(path, host, port, login, password).launch();

	}
}
