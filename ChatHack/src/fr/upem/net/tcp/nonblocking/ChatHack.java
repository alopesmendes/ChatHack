package fr.upem.net.tcp.nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
import java.util.logging.Logger;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.frame.FrameVisitor;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.SelectReaderOpcode;

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
		
		
		public void setPrivateConnexion(String privateConnexion) {
			lock.lock();
			try {
				this.privateConnexion = privateConnexion;
				state = State.NONE;
			} finally {
				lock.unlock();
			}

		}

		public void treat(ChatHack client) throws IOException {
			if (state == State.WAITING_PUBLIC_CONNECTION) {
				client.sendPublicConnectionRequest();
				state = State.NONE;
			}
			var action = actions.poll();
			if (action == null) {
				return;
			}
			if (action.startsWith("@") || action.startsWith("/")) {
				String[] s = action.split(" ", 2);
				String log = s[0].substring(1);
				if (!map.containsKey(log)) {
					state = State.SENDING_REPONSE;
					client.sendPrivateConnectionRequest(log);
				} else {
					String text = s[1];
					sendingMessageOrFile(client, action, log, text);
				} 
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
			if (key == null || key.attachment() == null || !key.isValid()) {
				
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
				switch (d.request()) {
					case CONNEXION:
						try {
							logger.info("Connected to: " + client.sc.getRemoteAddress());
						} catch (IOException e) { }
						break;

					default:
						throw new AssertionError();
					}
					return null;}).

			when(Data.DataError.class, d -> {
				logger.info("Error detected");
				return null;}).
			
			when(Data.DataGlobalClient.class, d -> {
				System.out.println(d.login()+":"+d.message());
				return null;}).
			when(Data.DataPrivateConnectionRequested.class, d -> {
				if (selectionKey.attachment() == null) {
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
					var data = Data.createDataError(StandardOperation.ERROR, StandardOperation.PRIVATE_CONNEXION);
					Frame frame = Frame.createFrameError(data);
					context.queueMessage(frame.buffer());
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
				return null;}).
			
			when(Data.DataPrivateFile.class, d -> {
				Path path = Path.of(client.path.toString(), d.fileName());
				try (FileChannel fc = FileChannel.open(path , StandardOpenOption.CREATE
															, StandardOpenOption.TRUNCATE_EXISTING
															, StandardOpenOption.WRITE)) {
					ByteBuffer bb = d.buffer();
					while (bb.hasRemaining()) {
						if (fc.write(bb) == -1) {
							break;
						}
					}
					logger.info("Received file "+d.fileName());
				} catch (IOException e) {
					
				}
				Frame frame = Frame.createFramePrivateFile(d);
				return frame;
			});

			return new Context(client, selectionKey, fv);
		}


		/**
		 * Add a message to the message queue, tries to fill bbOut and updateInterestOps
		 *
		 * @param bb
		 */
		private void queueMessage(ByteBuffer bb) {
			try {
				queue.add(bb);
				processOut();
				updateInterestOps();
			} finally {
				bb.flip();
			}
		}

		/**
		 * Try to fill bbout from the message queue
		 *
		 */
		private void processOut() {
			while (!queue.isEmpty() && bbout.remaining() >= queue.peek().remaining()) {
				bbout.put(queue.poll());
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
	

	public ChatHack(Path path, int clientPort, String host, int port, String login, Optional<String> password) throws IOException {
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
		while (!Thread.interrupted()) {
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
	
	

	public void launch() throws IOException {
		Thread thread = new Thread(() -> {
			try {
				//sendPublicConnectionRequest();
				sendAction();
			} catch (InterruptedException e) {
				
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
		while (!Thread.interrupted()) {
			selector.select();
			processSelectedKeys(selectedKeys);
			action.treat(this);
			selectedKeys.clear();
		}
	}

	private void processSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException {
		for (SelectionKey key : selectedKeys) {
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
		}
	}

	private void doAccept(SelectionKey selectionKey) throws IOException {
		SocketChannel sc = serverSocketChannel.accept();
		if (sc != null) {
			sc.configureBlocking(false);
			SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ);
			clientKey.attach(Context.createPrivateClient(this, clientKey));
			action.put(action.privateConnexion, clientKey);
		}
	}

	private static void usage() {
		System.out.println("Usage : ChatHack path, host, port, login and maybe password");
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
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
