package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

import fr.upem.net.tcp.frame.Data.DataAck;
import fr.upem.net.tcp.frame.Data.DataConnectionClient;
import fr.upem.net.tcp.frame.Data.DataConnectionServerMdp;
import fr.upem.net.tcp.frame.Data.DataConnectionServerMdpReponse;
import fr.upem.net.tcp.frame.Data.DataLogout;
import fr.upem.net.tcp.frame.Data.DataError;
import fr.upem.net.tcp.frame.Data.DataGlobalClient;
import fr.upem.net.tcp.frame.Data.DataPrivateAck;
import fr.upem.net.tcp.frame.Data.DataPrivateConnectionAccepted;
import fr.upem.net.tcp.frame.Data.DataPrivateConnectionReponse;
import fr.upem.net.tcp.frame.Data.DataPrivateConnectionRequested;
import fr.upem.net.tcp.frame.Data.DataPrivateFile;
import fr.upem.net.tcp.frame.Data.DataPrivateMessage;

/**
 * <p>
 * The Frame will be use to create a Frame which will create a ByteByffer with the data.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public interface Frame {


	/**
	 * <p>Will create a {@link ByteBuffer} from the current Frame.</p>
	 * @return the {@link ByteBuffer} according to the frame.
	 */
	ByteBuffer buffer();

	/**
	 * <p>Creates a Frame Global with it's {@link DataGlobalClient}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|login    |message  |
	 * |------|---------|---------|
	 * |byte  |int+bytes|int+bytes|
	 * </pre>
	 * @param data a {@link DataGlobalClient}.
	 * @return Frame.
	 */
	static Frame createFrameGlobal(DataGlobalClient data) {
		Objects.requireNonNull(data);
		ByteBuffer message = new FrameText(data.message).buffer();
		ByteBuffer login = new FrameText(data.login).buffer();
		return () -> { 
			ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + login.remaining() + message.remaining());
			bb.put(data.opcode.opcode());
			bb.put(login).put(message);
			login.flip();
			message.flip();
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Error Frame with it's {@link DataError}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|requestCode    |
	 * |------|---------------|
	 * |byte  |byte           |
	 * </pre>
	 * @param data a {@link DataError}.
	 * @return Frame.
	 */
	static Frame createFrameError(DataError data) {
		Objects.requireNonNull(data);
		return () -> { 
			ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES);
			bb.put(data.opcode.opcode()).put(data.requestCode.opcode());
			return bb.flip(); 
		};
	}

	/**
	 * <p>Creates a Connection Frame with it's {@link DataConnectionClient}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|connexion|login    |
	 * |------|---------|---------|
	 * |byte  |byte     |int+bytes|
	 * </pre>
	 * Or
	 * <pre>
	 * |opcode|connexion|login    |password |
	 * |------|---------|---------|---------|
	 * |byte  |byte     |int+bytes|int+bytes|
	 * </pre>
	 * @param data a {@link DataConnectionClient}.
	 * @return Frame.
	 */
	static Frame createFrameConnection(DataConnectionClient data) {
		Objects.requireNonNull(data);
		ByteBuffer login = new FrameText(data.login).buffer();
		Optional<ByteBuffer> password = Optional.empty();
		int size = 2*Byte.BYTES+login.remaining();
		if (data.password.isPresent()) {
			password = Optional.of(new FrameText(data.password.get()).buffer());
			size += password.get().remaining();
		}
		final Optional<ByteBuffer> pass = password;
		final int s = size;
		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(s);
			bb.put(data.opcode.opcode()).put(data.connexion);
			bb.put(login);
			if (pass.isPresent()) {
				bb.put(pass.get());
				pass.get().flip();
			}
			login.flip();
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame Connection Mdp with it's {@link DataConnectionServerMdp}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|id  |login    |
	 * |------|----|---------|
	 * |byte  |long|int+bytes|
	 * </pre>
	 * Or
	 * <pre>
	 * |opcode|id  |login    |password |
	 * |------|----|---------|---------|
	 * |byte  |long|int+bytes|int+bytes|
	 * </pre>
	 * @param data a {@link DataConnectionServerMdp}.
	 * @return Frame.
	 */
	static Frame createFrameConnectionMdp(DataConnectionServerMdp data) {
		Objects.requireNonNull(data);
		ByteBuffer login = new FrameText(data.login).buffer();
		Optional<ByteBuffer> password = data.password.isPresent() ? 
				Optional.of(new FrameText(data.password.get()).buffer()) : Optional.empty();
				int size = Byte.BYTES+Long.BYTES+login.remaining();
				if (password.isPresent()) {
					size += password.get().remaining();
				}
				if (size > 1_024) {
					return createFrameError(Data.createDataError(StandardOperation.ERROR, StandardOperation.CONNEXION));
				}
				return () -> {
					ByteBuffer bb = ByteBuffer.allocate(1_024);

					bb.put(data.typeConnexion);
					bb.putLong(data.id);
					bb.put(login);
					if (password.isPresent()) {
						bb.put(password.get());
						password.get().flip();
					}
					login.flip();
					return bb.flip();
				};
	}

	/**
	 * <p>Creates a Frame Connect Mdp Server with it's {@link DataConnectionServerMdpReponse}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|id  |
	 * |------|----|
	 * |byte  |long|
	 * </pre>
	 * @param data a {@link DataConnectionServerMdpReponse}.
	 * @return Frame.
	 */
	static Frame createFrameConnectMdpServer(DataConnectionServerMdpReponse data) {
		Objects.requireNonNull(data);

		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES+Long.BYTES);
			bb.put(data.opcode);
			bb.putLong(data.id);
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame Private Connection Requested with it's {@link DataPrivateConnectionRequested}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|step|firstClient|secondClient|
	 * |------|----|-----------|------------|
	 * |byte  |byte|int+bytes  |int+bytes   |
	 * </pre>
	 * @param data a {@link DataPrivateConnectionRequested}.
	 * @return Frame.
	 */
	static Frame createFramePrivateConnectionRequested(DataPrivateConnectionRequested data) {
		Objects.requireNonNull(data);
		ByteBuffer firstClient = new FrameText(data.firstClient).buffer();
		ByteBuffer secondClient = new FrameText(data.secondClient).buffer();
		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES + secondClient.remaining() +  firstClient.remaining());
			bb.put(data.opcode.opcode()).put(data.step);
			bb.put(firstClient).put(secondClient);
			firstClient.flip();
			secondClient.flip();
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame Private Connection Reponse with it's {@link DataPrivateConnectionReponse}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|step|firstClient|secondClient|state|
	 * |------|----|-----------|------------|-----|
	 * |byte  |byte|int+bytes  |int+bytes   |byte |
	 * </pre>
	 * @param data a {@link DataPrivateConnectionReponse}.
	 * @return Frame.
	 */
	static Frame createFramePrivateConnectionReponse(DataPrivateConnectionReponse data) {
		Objects.requireNonNull(data);
		ByteBuffer firstClient = new FrameText(data.firstClient).buffer();
		ByteBuffer secondClient = new FrameText(data.secondClient).buffer();
		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(3*Byte.BYTES + firstClient.remaining() + secondClient.remaining());
			bb.put(data.opcode.opcode()).put(data.step);
			bb.put(firstClient).put(secondClient);
			bb.put(data.state);
			firstClient.flip();
			secondClient.flip();
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame Private Connection Accepted with it's {@link DataPrivateConnectionAccepted}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|step|firstClient|secondClient|port|socket address|token|
	 * |------|----|-----------|------------|----|--------------|-----|
	 * |byte  |byte|int+bytes  |int+bytes   |int |int+bytes     |long |
	 * </pre>
	 * @param data a {@link DataPrivateConnectionAccepted}.
	 * @return Frame.
	 */
	static Frame createFramePrivateConnectionAccepted(DataPrivateConnectionAccepted data) {
		Objects.requireNonNull(data);
		ByteBuffer firstClient = new FrameText(data.firstClient).buffer();
		ByteBuffer secondClient = new FrameText(data.secondClient).buffer();
		ByteBuffer encodeSocket = new FrameText(data.host).buffer();
		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + 2*Byte.BYTES + Long.BYTES 
					+ firstClient.remaining() + secondClient.remaining() + encodeSocket.remaining());
			bb.put(data.opcode.opcode());
			bb.put(data.step);
			bb.put(firstClient).put(secondClient);
			bb.putInt(data.port);
			bb.put(encodeSocket);
			bb.putLong(data.token);
			firstClient.flip();
			secondClient.flip();
			encodeSocket.flip();
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame Private Message with it's {@link DataPrivateMessage}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|login    |message  |
	 * |------|---------|---------|
	 * |byte  |int+bytes|int+bytes|
	 * </pre>
	 * @param data a {@link DataPrivateMessage}.
	 * @return Frame.
	 */
	static Frame createFramePrivateMessage(DataPrivateMessage data) {
		Objects.requireNonNull(data);
		ByteBuffer messageEncode = new FrameText(data.message).buffer();
		ByteBuffer loginEncode = new FrameText(data.login).buffer();
		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES+messageEncode.remaining()+loginEncode.remaining());
			bb.put(data.opcode.opcode());
			bb.put(loginEncode).put(messageEncode);
			loginEncode.flip();
			messageEncode.flip();
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame Private File with it's {@link DataPrivateFile}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|login    |file name|file data|
	 * |------|---------|---------|---------|
	 * |byte  |int+bytes|int+bytes|int+bytes|
	 * </pre>
	 * @param data a {@link DataPrivateFile}.
	 * @return Frame.
	 */
	static Frame createFramePrivateFile(DataPrivateFile data) {
		Objects.requireNonNull(data);
		ByteBuffer fileName = new FrameText(data.fileName).buffer();
		ByteBuffer login = new FrameText(data.login).buffer();

		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + fileName.remaining() + login.remaining()
			+ Integer.BYTES + data.buff.remaining());

			bb.put(data.opcode.opcode());
			bb.put(login).put(fileName);
			bb.putInt(data.buff.remaining());
			bb.put(data.buff);
			data.buff.flip();
			login.flip();
			fileName.flip();
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame Ack with it's {@link DataAck}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|requestCode|
	 * |------|-----------|
	 * |byte  |byte       |
	 * </pre>
	 * @param data a {@link DataAck}.
	 * @return Frame.
	 */
	static Frame createFrameAck(DataAck data) {
		Objects.requireNonNull(data);
		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(2 * Byte.BYTES);
			bb.put(data.opcode.opcode()).put(data.requestCode.opcode());
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame with it's {@link DataPrivateAck}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|requestCode|login    |
	 * |------|-----------|---------|
	 * |byte  |byte       |int+bytes|
	 * </pre>
	 * @param data a {@link DataPrivateAck}.
	 * @return Frame
	 */
	static Frame createFramePrivateAck(DataPrivateAck data) {
		Objects.requireNonNull(data);
		ByteBuffer login = new FrameText(data.login).buffer();
		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(2*Byte.BYTES + login.remaining());
			bb.put(data.opcode.opcode()).put(data.requestCode.opcode());
			bb.put(login);
			login.flip();
			return bb.flip();
		};
	}

	/**
	 * <p>Creates a Frame Logout with it's {@link DataLogout}.</p>
	 * Representation of our {@link ByteBuffer}.
	 * <pre>
	 * |opcode|login    |
	 * |------|---------|
	 * |byte  |int+bytes|
	 * </pre>
	 * @param data a {@link DataLogout}.
	 * @return Frame.
	 */
	static Frame createFrameLogout(DataLogout data) {
		Objects.requireNonNull(data);
		ByteBuffer login = new FrameText(data.login).buffer();

		return () -> {
			ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + login.remaining());
			bb.put(data.opcode.opcode());
			bb.put(login);
			login.flip();
			return bb.flip();
		};
	}



}
