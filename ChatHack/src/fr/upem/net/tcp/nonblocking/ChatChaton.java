package fr.upem.net.tcp.nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.frame.FrameVisitor;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.SelectReaderOpcode;

public class ChatChaton {
	static private int BUFFER_SIZE = 1_024;
	static private Logger logger = Logger.getLogger(ChatChaton.class.getName());
	private final SocketChannel sc;
	private final Selector selector;
	private SelectionKey uniqueKey;
	private final SocketAddress serverAddress;
	private final ByteBuffer bbin = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final ByteBuffer bbout = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
	private final ReentrantLock lock = new ReentrantLock();
	private boolean cancel;
	private final FrameVisitor fv;
	private final Reader<Data> reader = SelectReaderOpcode.create(bbin);
	private final String login;
	private Thread thread;
	private final Optional<String> password;
	
	public ChatChaton(String host, int port, String login, Optional<String> password) throws IOException {
		serverAddress = new InetSocketAddress(host, port);
		sc = SocketChannel.open();
		selector = Selector.open();
		this.login = login;
		this.password = password;
		fv = new FrameVisitor().when(Data.DataGlobalServer.class, d -> {
			Frame frame = Frame.createFrameGlobal(d);
			brodcast(frame.buffer());
			return frame;}).
		when(Data.DataGlobalClient.class, d -> Frame.createFrameGlobal(d)).
		when(Data.DataError.class, d -> Frame.createFrameError(d)).
		when(Data.DataConnectionClient.class, d -> {
			Frame frame = Frame.createFrameConnection(d);
			return frame;}).
		when(Data.DataConnectionServerMdpReponse.class, d -> {
			Frame frame = Frame.createFrameConnectMdpServer(d);
			if (d.getOpcode() == (byte)0) {
				logger.info("Connection to server failed");
				silentlyClose();
				thread.interrupt();
			} else if (d.getOpcode() == (byte)1) {
				logger.info("Connnected to:"+serverAddress);
			} else {
				logger.info("Error of byte");
				thread.interrupt();
				throw new IllegalArgumentException("byte is "+d.getOpcode()+" expetected 1 or 0");
			}
			
			return frame;
		});
	}
	
	private void sendPublicConnectionRequest() throws InterruptedException {
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
			queue.put(bb);
			selector.wakeup();
		} finally {
			lock.unlock();
		}
	}
	
	public void launch() throws IOException {
		thread = new Thread(() -> {
			try {
				sendPublicConnectionRequest();
				send();
			} catch (InterruptedException e) {
				logger.info("interruped sending");
				return;
			}
		});
		thread.start();
		sc.configureBlocking(false);
		sc.connect(serverAddress);
		uniqueKey=sc.register(selector, SelectionKey.OP_CONNECT|SelectionKey.OP_READ|SelectionKey.OP_WRITE);
		
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		while (!Thread.interrupted()) {
			selector.select();
			treatActions();
			processSelectedKeys(selectedKeys);
			selectedKeys.clear();
		}
	}

	private void processSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException {
		for (SelectionKey key : selectedKeys) {
			if (key.isValid() && key.isConnectable()) {
				doConnect();
			}
			if (key.isValid() && key.isWritable()) {
				doWrite();
			}
			if (key.isValid() && key.isReadable()) {
				doRead();
			}
		}
	}


	private void doConnect() throws IOException {
		if (!sc.finishConnect()){
			return;
		}
		updateInterestsOps();
	}
	
	private void brodcast(ByteBuffer bb) {
		try {
			int fLimit = bb.limit();
			if (bb.remaining()< 2*Byte.BYTES) {
				return;
			}
			bb.get();
			bb.get();
			if (bb.remaining()<Integer.BYTES) {
				bb.limit(fLimit);
				return;
			}
			int size = bb.getInt();
			bb.limit(bb.position()+ size);
			String login = StandardCharsets.UTF_8.decode(bb).toString();
			bb.limit(fLimit);
			if (bb.remaining()<Integer.BYTES) {
				bb.limit(fLimit);
				return;
			}
			size = bb.getInt();
			bb.limit(bb.position()+size);
			String text = StandardCharsets.UTF_8.decode(bb).toString();
			bb.limit(fLimit);
			System.out.println(login+":"+text);
			//System.out.println(b1+" "+b2+" "+StandardCharsets.UTF_8.decode(bb));
		} finally {
			bb.flip();
		}
	}
	
	private void processIn() {
		for (;;) {
			Reader.ProcessStatus status = reader.process();
			switch (status) {
			case DONE:
				Data value = reader.get();
				fv.call(value);
				reader.reset();
				break;
			case REFILL:
				return;
			case ERROR:
				logger.info("bad package");	
				//silentlyClose();
				return;
			}
		}
	}

	private void processOut() {
		while (!queue.isEmpty() && queue.peek().hasRemaining()) {
			ByteBuffer bb = queue.remove();
			bbout.put( bb );
		}
	}

	private void doRead() throws IOException {
		if (sc.read(bbin) == -1) {
			cancel = true;
		}
		processIn();
		updateInterestsOps();
	}

	private void doWrite() throws IOException {
		bbout.flip();
		sc.write(bbout);
		bbout.compact();
		processOut();
		updateInterestsOps();
	}
	
	private void treatActions() throws IOException {
		lock.lock();
		try {
			processOut();
			updateInterestsOps();
		} finally {
			lock.unlock();
		}
	}

	private void updateInterestsOps() {
		var interestOps = 0;
		if (bbin.hasRemaining() && !cancel) {
			interestOps |= SelectionKey.OP_READ;
		}
		if (bbout.position() != 0) {
			interestOps |= SelectionKey.OP_WRITE;
		}

		if (interestOps == 0) {
			silentlyClose();
		}
		uniqueKey.interestOps(interestOps);
	}

	private void silentlyClose() {
		try {
			sc.close();
		} catch (IOException e) {
			// ignore exception
		}
	}

	private void send() throws InterruptedException {
		while (!Thread.interrupted()) {
			try (Scanner scanner = new Scanner(System.in)) {
				while (scanner.hasNextLine()) {
					String message = scanner.nextLine();
					Frame frame = fv.call(Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, (byte)1, message));
					lock.lock();
					try {
						queue.put(frame.buffer());
						selector.wakeup();
					} finally {
						lock.unlock();
					}
				}
			}
		}
	}

	private static void usage() {
		System.out.println("Usage : ChatChaton port");
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 3 && args.length != 4) {
			usage();
			return;
		}
		String host = args[0];
		int ip = Integer.parseInt(args[1]);
		String login = args[2];
		Optional<String> password = Optional.empty();
		if (args.length == 4) {
			password = Optional.of(args[3]);
		}
		new ChatChaton(host, ip, login, password).launch();
	}
}
