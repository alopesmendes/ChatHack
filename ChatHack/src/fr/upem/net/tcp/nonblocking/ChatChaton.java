package fr.upem.net.tcp.nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
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
	private final ByteBuffer login;
	//private Reader messageReader = new MessageReader(bbin);

	public ChatChaton(String host, int port, String login) throws IOException {
		serverAddress = new InetSocketAddress(host, port);
		sc = SocketChannel.open();
		selector = Selector.open();
		this.login = encodeText(login);
	}
	
	private ByteBuffer encodeText(String login) {
		ByteBuffer encode = StandardCharsets.UTF_8.encode(login);
		ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES+encode.remaining());
		bb.putInt(encode.remaining()).put(encode);
		bb.flip();
		return bb;
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
		uniqueKey=sc.register(selector, SelectionKey.OP_CONNECT|SelectionKey.OP_READ|SelectionKey.OP_WRITE);
		sendLogin();
		treatActions();
		
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		while (!Thread.interrupted()) {
			selector.select();
			treatActions();
			processSelectedKeys(selectedKeys);
			selectedKeys.clear();
		}
	}
	
	private void sendLogin() {
		lock.lock();
		try {
			ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES+Long.BYTES+login.remaining());
			bb.put((byte)2).putLong(0).put(login);
			bb.flip();
			login.flip();
			queue.put(bb);
			selector.wakeup();
		} catch (InterruptedException e) {
			return;
		} finally {
			lock.unlock();
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
	
	/*private String textSize(ByteBuffer bb) {
		int size = bb.getInt();
		bb.limit(bb.position()+size);
		return StandardCharsets.UTF_8.decode(bb).toString();
	}*/

	private void processIn() {
		/*for (;;) {
			Reader.ProcessStatus status = messageReader.process();
			switch (status) {
			case DONE:
				ByteBuffer value = (ByteBuffer) messageReader.get();
				int oldLimit = value.limit();
				String login = textSize(value);
				value.limit(oldLimit);
				String text = textSize(value);
				
				logger.info(login+"->"+text);
				messageReader.reset();
				break;
			case REFILL:
				return;
			case ERROR:
				silentlyClose(uniqueKey);
				return;
			}
		}*/
	}

	private void processOut() {
		while (!queue.isEmpty() && bbout.remaining() >= queue.peek().remaining()) {
			ByteBuffer bb = queue.remove();
			bbout.put( bb );
		}
	}

	private void doRead() throws IOException {
		logger.info("read");
		if (sc.read(bbin) == -1) {
			cancel = true;
		}
		processIn();
		updateInterestsOps();
	}

	private void doWrite() throws IOException {
		logger.info("write");
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
			silentlyClose(uniqueKey);
		}
		uniqueKey.interestOps(interestOps);
	}

	private void silentlyClose(SelectionKey key) {
		Channel sc = (Channel) key.channel();
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
					String num = scanner.nextLine();
					ByteBuffer encode = encodeText(num);
					ByteBuffer bb = ByteBuffer.allocate(encode.remaining()+login.remaining());
					lock.lock();
					try {
						bb.put(login).put(encode);
						bb.flip();
						login.flip();
						queue.put(bb);
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
