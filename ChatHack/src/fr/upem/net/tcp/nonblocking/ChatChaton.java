package fr.upem.net.tcp.nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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

	public ChatChaton(String host, int port, String login) throws IOException {
		serverAddress = new InetSocketAddress(host, port);
		sc = SocketChannel.open();
		selector = Selector.open();
		fv = new FrameVisitor().when(Data.DataGlobalServer.class, d -> Frame.createFrameGlobal(d)).
		when(Data.DataGlobalClient.class, d -> Frame.createFrameGlobal(d)).
		when(Data.DataError.class, d -> Frame.createFrameError(d)).
		when(Data.DataConnectionClient.class, d -> Frame.createFrameConnection(d));
	}
	
	public void launch() throws IOException {
		var t = new Thread(() -> {
			try {
				send();
			} catch (InterruptedException e) {
				logger.info("interruped sending");
				return;
			}
		});
		t.start();
		sc.configureBlocking(false);
		sc.connect(serverAddress);
		logger.info("Connnected to:"+serverAddress);
		uniqueKey=sc.register(selector, SelectionKey.OP_CONNECT);
		
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
				brodcast(fv.call(value).buffer());
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
		if (args.length != 3) {
			usage();
			return;
		}
		String host = args[0];
		int ip = Integer.parseInt(args[1]);
		String login = args[2];
		new ChatChaton(host, ip, login).launch();
	}
}
