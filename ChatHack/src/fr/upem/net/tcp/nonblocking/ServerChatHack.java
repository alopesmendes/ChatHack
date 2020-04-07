package fr.upem.net.tcp.nonblocking;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.Data.DataConnectionClient;
import fr.upem.net.tcp.frame.Data.DataConnectionServerMdp;
import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.frame.FrameVisitor;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.SelectReaderOpcode;

public class ServerChatHack {

	static private class DataMdp {
		private final SelectionKey key;
		private final Data.DataConnectionClient client;
		private final Data.DataConnectionServerMdp mdp;
		/**
		 * @param key
		 * @param client
		 * @param mdp
		 */
		private DataMdp(SelectionKey key, DataConnectionClient client, DataConnectionServerMdp mdp) {
			this.key = key;
			this.client = client;
			this.mdp = mdp;
		}
		
		


	}

	static private class Context {

		final private SelectionKey key;
		final private SocketChannel sc;
		final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
		final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
		final private BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
		//final private ServerChatHack server;
		final private Reader<Data> reader;
		private boolean closed = false;
		final private FrameVisitor fv;


		private Context(ServerChatHack server, SelectionKey key, FrameVisitor fv) {
			this.key = key;
			this.sc = (SocketChannel) key.channel();
			reader = SelectReaderOpcode.create(bbin);
			this.fv = fv;
		}

		private static Context contextServerMdp(ServerChatHack server, SelectionKey key) {
			FrameVisitor fv = new FrameVisitor().

					when(Data.DataConnectionServerMdpReponse.class, d -> {
						Frame frame = Frame.createFrameAck(Data.createDataAck(StandardOperation.ACK, StandardOperation.CONNEXION));
						if (!server.requestMap.containsKey(d.getId())) {
							logger.info("ERROR no such client demand a connection.");
							return frame;
						}

						DataMdp dataMdp = server.requestMap.get(d.getId());
						if (dataMdp.key.attachment() != null) {
							try {
								
								SocketChannel sc = (SocketChannel) dataMdp.key.channel();
								sc.configureBlocking(false);
								var k = sc.register(server.selector, SelectionKey.OP_READ);
								k.attach(contextClient(server, k));
								server.loginMap.put(dataMdp.client.login(), k);
								if 	((dataMdp.mdp.connexion() == 1 && d.getOpcode() == 1)
									|| (dataMdp.mdp.connexion() == 2 && d.getOpcode() == 0)){
									
									((Context)k.attachment()).queueMessage(frame);
								} else {
									
									((Context)k.attachment()).queueMessage(errorFrame(StandardOperation.CONNEXION));
									((Context)k.attachment()).closed = true;
								}
							} catch (ClosedChannelException e) {
								return null;
							} catch (IOException e) {
								return null;
							}
						}
						return frame;});

			return new Context(server, key, fv);
		}

		private static Context contextClient(ServerChatHack server, SelectionKey key) {
			FrameVisitor fv = new FrameVisitor().
					when(Data.DataConnectionClient.class, d -> {
						if (server.loginMap.containsKey(d.login()) && server.loginMap.get(d.login()).isValid()) {
							((Context)key.attachment()).queueMessage(errorFrame(StandardOperation.CONNEXION));
							((Context)key.attachment()).closed = true;
							return null;
						}
						server.loginMap.put(d.login(), key);
						Data.DataConnectionServerMdp data = Data.createDataConnectionServerMdp(d);
						Frame frame = Frame.createFrameConnectionMdp(data);
						Context context = (Context) server.uniqueKey.attachment();
						context.queueMessage(frame);
						server.requestMap.put(data.getId(), new DataMdp(key, d, data));
						key.cancel();
						return frame;}).

					when(Data.DataGlobalClient.class, d -> {
						Frame frame = Frame.createFrameGlobal(d);
						server.broadcast(frame);
						return frame;}).
					
					when(Data.DataPrivateConnectionRequested.class, d -> {
						
						SelectionKey requestKey = server.loginMap.get(d.secondClient());
						if (requestKey == null || !requestKey.isValid() || requestKey.attachment() == null) {
							 SelectionKey senderKey = server.loginMap.get(d.firstClient());
							 Frame error = errorFrame(StandardOperation.PRIVATE_CONNEXION);
							 if (senderKey == null || !senderKey.isValid() || senderKey.attachment() == null) {
								 return error;
							 }
							 Context context = (Context) senderKey.attachment();
							 context.queueMessage(error);
							 return error;
						}
						Context context = (Context) requestKey.attachment();
						var data = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, (byte)2, d.secondClient(), d.firstClient());
						Frame frame = Frame.createFramePrivateConnectionRequested(data);
						context.queueMessage(frame);
						return frame;}).
					
					when(Data.DataPrivateConnectionReponse.class, d -> {
						SelectionKey requestKey = server.loginMap.get(d.secondClient());
						if (requestKey == null || !requestKey.isValid() || requestKey.attachment() == null) {
							 SelectionKey senderKey = server.loginMap.get(d.firstClient());
							 Frame error = errorFrame(StandardOperation.PRIVATE_CONNEXION);
							 if (senderKey == null || !senderKey.isValid() || senderKey.attachment() == null) {
								 return error;
							 }
							 Context context = (Context) senderKey.attachment();
							 context.queueMessage(error);
							 return error;
						}
						Context context = (Context) requestKey.attachment();
						var data = Data.createDataPrivateConnectionReponse(StandardOperation.PRIVATE_CONNEXION, (byte)4, d.secondClient(), d.firstClient(), d.state());
						Frame frame = Frame.createFramePrivateConnectionReponse(data);
						context.queueMessage(frame);
						return frame;}).
					
					when(Data.DataPrivateConnectionAccepted.class, d -> {
						SelectionKey requestKey = server.loginMap.get(d.secondClient());
						if (requestKey == null || !requestKey.isValid() || requestKey.attachment() == null) {
							 SelectionKey senderKey = server.loginMap.get(d.firstClient());
							 Frame error = errorFrame(StandardOperation.PRIVATE_CONNEXION);
							 if (senderKey == null || !senderKey.isValid() || senderKey.attachment() == null) {
								 return error;
							 }
							 Context context = (Context) senderKey.attachment();
							 context.queueMessage(error);
							 return error;
						}
						Context context = (Context) requestKey.attachment();
						
						var data = Data.createDataPrivateConnectionAccepted(StandardOperation.PRIVATE_CONNEXION, (byte)6, 
								d.secondClient(), d.firstClient(), d.port(), d.host(), d.token());
						
						Frame frame = Frame.createFramePrivateConnectionAccepted(data);
						context.queueMessage(frame);
						return frame;}).
					when(Data.DataDeconnexion.class, d -> {
						if (key.attachment() == null) {
							return null;
						}
						logger.info("Client deconnected: " + key);
						Context context = (Context)key.attachment();
						var data = Data.createDataAck(StandardOperation.ACK, StandardOperation.DECONNEXION);
						Frame frame = Frame.createFrameAck(data);
						context.queueMessage(frame);
						context.closed = true;
						return null;
					});
			return new Context(server, key, fv);
		}


		private static Frame errorFrame(StandardOperation requestCode) {
			return Frame.createFrameError(Data.createDataError(StandardOperation.ERROR, requestCode));
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
		 * Add a message to the message queue, tries to fill bbOut and updateInterestOps
		 *
		 * @param value
		 */
		private void queueMessage(Frame value) {
			queue.add(value.buffer());
			processOut();
			updateInterestOps();
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
		 * Update the interestOps of the key looking only at values of the boolean
		 * closed and of both ByteBuffers.
		 *
		 * The convention is that both buffers are in write-mode before the call to
		 * updateInterestOps and after the call. Also it is assumed that process has
		 * been be called just before updateInterestOps.
		 */
		private void updateInterestOps() {
			int ops = 0;
			if (!key.isValid()) {
				return;
			}
			if (bbin.hasRemaining() && !closed) {
				ops |= SelectionKey.OP_READ;
			}
			if (bbout.position() != 0) {
				ops |= SelectionKey.OP_WRITE;
			}
			if (ops == 0) {
				silentlyClose();
			} else {
				key.interestOps(ops);
			}
		}

		private void silentlyClose() {
			try {
				sc.close();
			} catch (IOException e) {
				// ignore exception
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
			if (sc.read(bbin) == -1) {
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
			sc.write(bbout);
			bbout.compact();
			processOut();
			updateInterestOps();
		}

		private void doConnect() throws IOException {
			if (!sc.finishConnect()) {
				return;
			}
			updateInterestOps();
		}
	}
	static private int BUFFER_SIZE = 1_024;
	static private Logger logger = Logger.getLogger(ServerChatHack.class.getName());
	private final SocketAddress serverPasswordAdress;
	private SelectionKey uniqueKey;
	private final ServerSocketChannel serverSocketChannel;
	private final Selector selector;
	private final Map<Long, DataMdp> requestMap = new HashMap<>();
	private final Map<String, SelectionKey> loginMap = new HashMap<>();
	private final SocketChannel scPassword;



	public ServerChatHack(int port, String host, int mdpPort) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverPasswordAdress = new InetSocketAddress(host, mdpPort);
		scPassword = SocketChannel.open();
	}


	public void launch() throws IOException {

		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		scPassword.configureBlocking(false);
		scPassword.connect(serverPasswordAdress);
		logger.info("Connnected to DataBase server:"+serverPasswordAdress);
		uniqueKey=scPassword.register(selector, SelectionKey.OP_CONNECT);
		uniqueKey.attach(Context.contextServerMdp(this, uniqueKey));

		while (!Thread.interrupted()) {
			printKeys(); // for debug
			System.out.println("Starting select");
			try {
				selector.select(this::treatKey);

			} catch (UncheckedIOException tunneled) {
				throw tunneled.getCause();
			}
			System.out.println("Select finished");
		}
	}
	
	private void treatKey(SelectionKey key) {
		printSelectedKey(key); // for debug
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

	private void doAccept(SelectionKey key) throws IOException {

		SocketChannel sc = serverSocketChannel.accept();
		if (sc != null) {
			sc.configureBlocking(false);
			SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ);
			clientKey.attach(Context.contextClient(this, clientKey));
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

	/**
	 * Add a message to all connected clients queue
	 *
	 * @param value
	 */
	private void broadcast(Frame value) {
		for (SelectionKey key : selector.keys()) {
			Context ctx = (Context) key.attachment();
			if (ctx == null || key.equals(uniqueKey)) {
				continue;
			}
			ctx.queueMessage(value);
		}
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		if (args.length != 3) {
			usage();
			return;
		}
		new ServerChatHack(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2])).launch();
	}

	private static void usage() {
		System.out.println("Usage : ServerSumBetter port");
	}

	/***
	 * Theses methods are here to help understanding the behavior of the selector
	 ***/

	private String interestOpsToString(SelectionKey key) {
		if (!key.isValid()) {
			return "CANCELLED";
		}
		int interestOps = key.interestOps();
		ArrayList<String> list = new ArrayList<>();
		if ((interestOps & SelectionKey.OP_ACCEPT) != 0)
			list.add("OP_ACCEPT");
		if ((interestOps & SelectionKey.OP_READ) != 0)
			list.add("OP_READ");
		if ((interestOps & SelectionKey.OP_WRITE) != 0)
			list.add("OP_WRITE");
		return String.join("|", list);
	}

	public void printKeys() {
		Set<SelectionKey> selectionKeySet = selector.keys();
		if (selectionKeySet.isEmpty()) {
			System.out.println("The selector contains no key : this should not happen!");
			return;
		}
		System.out.println("The selector contains:");
		for (SelectionKey key : selectionKeySet) {
			SelectableChannel channel = key.channel();
			if (channel instanceof ServerSocketChannel) {
				System.out.println("\tKey for ServerSocketChannel : " + interestOpsToString(key));
			} else {
				SocketChannel sc = (SocketChannel) channel;
				System.out.println("\tKey for Client " + remoteAddressToString(sc) + " : " + interestOpsToString(key));
			}
		}
	}

	private String remoteAddressToString(SocketChannel sc) {
		try {
			return sc.getRemoteAddress().toString();
		} catch (IOException e) {
			return "???";
		}
	}

	public void printSelectedKey(SelectionKey key) {
		SelectableChannel channel = key.channel();
		if (channel instanceof ServerSocketChannel) {
			System.out.println("\tServerSocketChannel can perform : " + possibleActionsToString(key));
		} else {
			SocketChannel sc = (SocketChannel) channel;
			System.out.println(
					"\tClient " + remoteAddressToString(sc) + " can perform : " + possibleActionsToString(key));
		}
	}

	private String possibleActionsToString(SelectionKey key) {
		if (!key.isValid()) {
			return "CANCELLED";
		}
		ArrayList<String> list = new ArrayList<>();
		if (key.isAcceptable())
			list.add("ACCEPT");
		if (key.isReadable())
			list.add("READ");
		if (key.isWritable())
			list.add("WRITE");
		return String.join(" and ", list);
	}


}
