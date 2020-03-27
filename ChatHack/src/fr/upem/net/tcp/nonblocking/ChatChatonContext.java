package fr.upem.net.tcp.nonblocking;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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

public class ChatChatonContext {
	
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
		
		public Context(ChatChatonContext client, SelectionKey selectionKey) {
			this.selectionKey = selectionKey;
			socketChannel = (SocketChannel) selectionKey.channel();
			reader = SelectReaderOpcode.create(bbin);

			fv = new FrameVisitor().
			when(Data.DataConnectionClient.class, d ->Frame.createFrameConnection(d)).

			when(Data.DataError.class, d -> {
				logger.info("Error detected");
				client.lock.lock();
				try {
					client.state = State.NONE;
				} finally {
					client.lock.unlock();
				}
				return Frame.createFrameError(d);}).
			
			when(Data.DataGlobalClient.class, d -> {
				Frame frame = Frame.createFrameGlobal(d);
				queueMessage(frame.buffer());
				client.selector.wakeup();
				return frame;}).
			
			when(Data.DataConnectionServerMdpReponse.class, d -> {
				Frame frame = Frame.createFrameConnectMdpServer(d);
				if (d.getOpcode() == (byte)0) {
					logger.info("Connection to server failed");
					silentlyClose();
					client.thread.interrupt();
				} else if (d.getOpcode() == (byte)1) {
					
				} else {
					logger.info("Error of byte");
					client.thread.interrupt();
					throw new IllegalArgumentException("byte is "+d.getOpcode()+" expetected 1 or 0");
				}
				return frame;}).
			
			when(Data.DataGlobalServer.class, d -> {
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
						logger.info(d.login()+" wants to start a private conversation enter O/N");
						client.state = State.WATING_REPONSE;
						while (client.state != State.NO && client.state != State.YES) {
							client.condition.await();
						}
						byte s = (byte) (client.state == State.YES ? 0 : 1);
						var data = Data.createDataPrivateConnectionReponse(d.opcode(), (byte)3, d.login(), s);
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
					logger.info(d.login()+" accepted the demand");
					var data = Data.createDataPrivateConnectionAccepted(StandardOperation.PRIVATE_CONNEXION, (byte)5, d.login(), 4646, "localhost", System.currentTimeMillis());
					Frame frame = Frame.createFramePrivateConnectionAccepted(data);
					queueMessage(frame.buffer());
					client.selector.wakeup();
					
					client.lock.lock();
					try {
						client.state = State.NONE;
						client.privateConnexion = d.login();
					} finally {
						client.lock.unlock();
					}
					return frame;	
				} else {
					logger.info(d.login()+" denied the demand");
					var data = Data.createDataError(StandardOperation.ERROR, (byte)2);
					Frame frame = Frame.createFrameError(data);
					queueMessage(frame.buffer());
					client.selector.wakeup();
					return frame;}}).
			
			when(Data.DataPrivateConnectionAccepted.class, d -> {
				SocketAddress sa = new InetSocketAddress(d.host(), d.port());
				
				try {
					SocketChannel sc = SocketChannel.open();
					sc.configureBlocking(false);
					sc.connect(sa);
					var key = sc.register(client.selector, SelectionKey.OP_CONNECT);
					Context context = new Context(client, key);
					client.map.put(d.login(), context);
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
				System.out.println("private message from "+d.login()+":"+d.message());
				return null;
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
			logger.info("Connected to: " + socketChannel.getLocalAddress());
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
	final private static Logger logger = Logger.getLogger(ChatChatonContext.class.getName());
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

	public ChatChatonContext(int clientPort, String hostname, int port, String login, Optional<String> password) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		selector = Selector.open();
		sc = SocketChannel.open();
		serverAdress = new InetSocketAddress(hostname, port);
		serverSocketChannel.bind(new InetSocketAddress(clientPort));
		this.login = login;
		this.password = password;
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
				if (reponse.contentEquals("o")) {
					state = State.YES;
				} else if (reponse.contentEquals("n")) { 
					state = State.NO;
				} else {
					logger.info("ENTER O/N");
				}
			} else if (state == State.NONE) {
				Context context = (Context) uniqueKey.attachment();
				Data data = Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, (byte)1, reponse);
				context.fv.call(data);
			}
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
	
	private void send() throws InterruptedException {
		while (!Thread.interrupted()) {
			try (Scanner scanner = new Scanner(System.in)) {
				while (scanner.hasNextLine()) {
					String message = scanner.nextLine();
					if (message.startsWith("@")) {
						
						String[] s = message.split(" ", 2);
						String l = s[0].substring(1);
						if (!map.containsKey(l)) {
							state = State.SENDING_REPONSE;
							Context context = (Context) uniqueKey.attachment();
							Data data = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, (byte)1, s[0].substring(1));
							context.fv.call(data);
						} else {
							state = State.NONE;
							String text = s[1];
							Context context = map.get(l);
							var data = Data.createDataPrivateMessage(StandardOperation.PRIVATE_MESSAGE, login, text);
							Frame frame = Frame.createFramePrivateMessage(data);
							context.queueMessage(frame.buffer());
							selector.wakeup();
						}
					} else if (message.startsWith("/")) {
						
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
			map.put(privateConnexion, context);
			clientKey.attach(context);
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
		if (args.length != 4 && args.length != 5) {
			usage();
			return;
		}
		int port = Integer.parseInt(args[0]);
		String host = args[1];
		int ip = Integer.parseInt(args[2]);
		String login = args[3];
		Optional<String> password = Optional.empty();
		if (args.length == 5) {
			password = Optional.of(args[4]);
		}
		new ChatChatonContext(port, host, ip, login, password).launch();
	}

}
