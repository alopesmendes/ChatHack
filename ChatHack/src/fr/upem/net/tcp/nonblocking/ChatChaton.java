package fr.upem.net.tcp.nonblocking;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
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

public class ChatChaton {
	
	private static enum State {
		DONE, SENDING_REPONSE, WATING_REPONSE, YES, NO, NONE;
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
		//private String login;
		
		public Context(ChatChaton client, SelectionKey selectionKey) {
			this.selectionKey = selectionKey;
			socketChannel = (SocketChannel) selectionKey.channel();
			reader = SelectReaderOpcode.create(bbin);

			fv = new FrameVisitor().
					
			when(Data.DataAck.class, d -> {
				switch (d.request()) {
					case CONNEXION:
						try {
							logger.info("Connected to: " + socketChannel.getRemoteAddress());
						} catch (IOException e) {
						
						}
						break;
	
					default:
						throw new AssertionError();
				}
				return null;
			}).
			
			when(Data.DataConnectionClient.class, d -> Frame.createFrameConnection(d)).

			when(Data.DataError.class, d -> {
				logger.info("Error detected");
				client.lock.lock();
				try {
					client.state = State.NONE;
				} finally {
					client.lock.unlock();
				}
				//silentlyClose();
				return Frame.createFrameError(d);}).
			
			when(Data.DataGlobalClient.class, d -> {
				Frame frame = Frame.createFrameGlobal(d);
				System.out.println(d.login()+":"+d.message());
				return frame;}).
			
			when(Data.DataPrivateConnectionRequested.class, d -> {
				Frame frame = Frame.createFramePrivateConnectionRequested(d);
				if (d.step()==1) {
					queueMessage(frame.buffer());
					client.selector.wakeup();
				} else {
					client.lock.lock();
					try {
						logger.info(d.secondClient()+" wants to start a private conversation enter O/N");
						client.state = State.WATING_REPONSE;
						while (client.state != State.NO && client.state != State.YES) {
							client.condition.await();
						}
						byte s = (byte) (client.state == State.YES ? 0 : 1);
						var data = Data.createDataPrivateConnectionReponse(d.opcode(), (byte)3, d.firstClient(), d.secondClient(), s);
						frame = Frame.createFramePrivateConnectionReponse(data);
						queueMessage(frame.buffer());
						client.selector.wakeup();
						client.state = State.NONE;
					} catch (InterruptedException e) {
						client.thread.interrupt();
					} finally {
						client.lock.unlock();
					}
				}
				return frame;}).
			
			when(Data.DataPrivateConnectionReponse.class, d -> {
				if (d.state()==0) {
					logger.info(d.secondClient()+" accepted the demand");
					var data = Data.createDataPrivateConnectionAccepted(StandardOperation.PRIVATE_CONNEXION, (byte)5, d.firstClient(), d.secondClient(), client.clientPort, "localhost", System.currentTimeMillis());
					Frame frame = Frame.createFramePrivateConnectionAccepted(data);
					queueMessage(frame.buffer());
					client.selector.wakeup();
					
					client.lock.lock();
					try {
						client.state = State.NONE;
						client.privateConnexion = d.secondClient();
					} finally {
						client.lock.unlock();
					}
					return frame;	
				} else {
					logger.info(d.secondClient()+" denied the demand");
					var data = Data.createDataError(StandardOperation.ERROR, StandardOperation.PRIVATE_CONNEXION);
					Frame frame = Frame.createFrameError(data);
					queueMessage(frame.buffer());
					client.selector.wakeup();
					return frame;}}).
			
			when(Data.DataPrivateConnectionAccepted.class, d -> {
				logger.info("Client "+d.secondClient()+ " with the token "+d.token());
				SocketAddress sa = new InetSocketAddress(d.host(), d.port());
				try {
				
					SocketChannel sc = SocketChannel.open();
					sc.configureBlocking(false);
					sc.connect(sa);
					var key = sc.register(client.selector, SelectionKey.OP_CONNECT);
					Context context = new Context(client, key);
					client.map.put(d.secondClient(), context);
					client.state = State.NONE;
					//client.privateConnexion = d.login();
					key.attach(context);
					//var data = Data.createDataPrivateConnectionConnect(StandardOperation.PRIVATE_CONNEXION, (byte)7, d.login(), d.token());
					//Frame frame = Frame.createFramePrivateConnectionConnect(data);
					//context.queueMessage(frame.buffer());
				} catch (IOException e) { }
				
				return null;}).
			when(Data.DataPrivateConnectionConnect.class, d -> {
				System.out.println("Client "+d.login()+" with the token "+d.token());
				return null;}).
			
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
			// TODO
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
	final private static Logger logger = Logger.getLogger(ChatChaton.class.getName());
	private final SocketChannel sc;
	private final Selector selector;
	private SelectionKey uniqueKey;
	private final SocketAddress serverAdress;
	private final String login;
	private String privateConnexion;
	private final Optional<String> password;
	private Thread thread;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private State state = State.NONE;
	private Map<String, Context> map = new HashMap<>();
	private final ServerSocketChannel serverSocketChannel;
	private final Path path;
	private final int clientPort;
	
	
	public ChatChaton(Path path, int clientPort, String hostname, int port, String login, Optional<String> password) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		selector = Selector.open();
		sc = SocketChannel.open();
		serverAdress = new InetSocketAddress(hostname, port);
		serverSocketChannel.bind(new InetSocketAddress(clientPort));
		this.login = login;
		this.password = password;
		this.path = path;
		this.clientPort = clientPort;
	}

	private void sendPublicConnectionRequest() throws InterruptedException {
		Context context = (Context) uniqueKey.attachment();
		if (context == null) {
			return;
		}
		ByteBuffer loginEncode = StandardCharsets.UTF_8.encode(login);
		ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
		byte typeConnexion = (byte) (password.isEmpty() ? 1 : 0);
		bb.put(StandardOperation.CONNEXION.opcode()).put(typeConnexion);
		bb.putInt(loginEncode.remaining());
		bb.put(loginEncode);
		if (password.isPresent()) {
			ByteBuffer passwordEncode = StandardCharsets.UTF_8.encode(password.get());
			bb.putInt(passwordEncode.remaining());
			bb.put(passwordEncode);
		}
		bb.flip();
		lock.lock();
		try {
			context.queueMessage(bb);
			selector.wakeup();
		} finally {
			lock.unlock();
		}
	}
	
	private void waitReponse(String reponse) {
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
			} else if (state == State.NONE) {
				Context context = (Context) uniqueKey.attachment();
				var data = Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, login, reponse);
				Frame frame = Frame.createFrameGlobal(data);
				context.queueMessage(frame.buffer());
				selector.wakeup();
			}
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
	
	private void sendingReponse(String log) {
		state = State.SENDING_REPONSE;
		Context context = (Context) uniqueKey.attachment();
		var data = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, (byte)1, login, log);
		Frame frame = Frame.createFramePrivateConnectionRequested(data);
		context.queueMessage(frame.buffer());
		selector.wakeup();
	}
	
	private Frame createFileFrame(String login, String fileName) throws IOException {
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
	
	private void sendingMessageOrFile(String message, String login, String text) throws IOException {
		state = State.NONE;
		Context context = map.get(login);
		Frame frame;
		if (message.startsWith("@")) {
			var data = Data.createDataPrivateMessage(StandardOperation.PRIVATE_MESSAGE, this.login, text);
			frame = Frame.createFramePrivateMessage(data);
		} else {
			frame = createFileFrame(this.login, text);
		}
		context.queueMessage(frame.buffer());
		selector.wakeup();
	}
	
	private void send() throws InterruptedException, IOException {
		while (!Thread.interrupted()) {
			try (Scanner scanner = new Scanner(System.in)) {
				while (scanner.hasNextLine()) {
					String message = scanner.nextLine();
					if (message.startsWith("@") || message.startsWith("/") ) {
						String[] s = message.split(" ", 2);
						String l = s[0].substring(1);
						if (!map.containsKey(l)) {
							sendingReponse(l);
						} else {
							sendingMessageOrFile(message, l, s[1]);
						}
					} else {
						waitReponse(message);
					}
				}
			}
		}
	}

	public void launch() throws IOException {
		thread = new Thread(() -> {
			try {
				sendPublicConnectionRequest();
				send();
			} catch (InterruptedException e) {
				return;
			} catch (IOException e) {
				return;
			}
		});
		
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		sc.configureBlocking(false);
		sc.connect(serverAdress);
		uniqueKey=sc.register(selector, SelectionKey.OP_CONNECT);
		uniqueKey.attach(new Context(this, uniqueKey));
		thread.start();	
		while (!Thread.interrupted()) {
			try {
				selector.select(this::treatKey);
			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
		}
	}

	private void doAccept(SelectionKey key) throws IOException {
		SocketChannel sc = serverSocketChannel.accept();
		if (sc != null) {
			sc.configureBlocking(false);
			SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ);
			Context context = new Context(this, clientKey);
			clientKey.attach(context);
			map.put(privateConnexion, context);
		}
	}

	private void treatKey(SelectionKey key) {
		try {
			if (key.isValid() && key.isAcceptable()) {
				doAccept(key);
			}
		} catch (IOException ioe) {
			// lambda call in select requires to tunnel IOException
			throw new UncheckedIOException(ioe);
		}
		try {
			if (key.isValid() && key.isConnectable()) {
				((Context) key.attachment()).doConnect();
			}
			if (key.isValid() && key.isWritable()) {
				((Context) key.attachment()).doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				((Context) key.attachment()).doRead();
			}
		} catch (IOException e) {
			logger.log(Level.INFO, "Connection closed with client due to IOException", e);
			silentlyClose(key);
		}
	}

	private void silentlyClose(SelectionKey key) {
		Channel sc = (Channel) key.channel();
		try {
			sc.close();
		} catch (IOException e) {
			// ignore exception
		}
	}

	private static void usage() {
		System.out.println("Usage : ChatChaton port");
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 5 && args.length != 6) {
			usage();
			return;
		}
		
		Path path = Path.of(args[0]);
		int port = Integer.parseInt(args[1]);
		String host = args[2];
		int ip = Integer.parseInt(args[3]);
		String login = args[4];
		Optional<String> password = Optional.empty();
		if (args.length == 6) {
			password = Optional.of(args[5]);
		}
		new ChatChaton(path, port, host, ip, login, password).launch();
	}

}
