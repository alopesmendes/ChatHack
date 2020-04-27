package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

import fr.upem.net.tcp.frame.Data.DataAck;
import fr.upem.net.tcp.frame.Data.DataConnectionClient;
import fr.upem.net.tcp.frame.Data.DataConnectionServerMdp;
import fr.upem.net.tcp.frame.Data.DataConnectionServerMdpReponse;
import fr.upem.net.tcp.frame.Data.DataDeconnexion;
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
	 * Creates a Frame Global with it's {@link DataGlobalClient}.
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
	 * Creates a Error Frame with it's {@link DataError}.
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
	 * Creates a Connection Frame with it's {@link DataConnectionClient}.
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
	 * Creates a Frame Connection Mdp with it's {@link DataConnectionServerMdp}.
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
			throw new IllegalArgumentException("Cannot be superior to 1_024 bytes");
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
	 * Creates a Frame Connect Mdp Server with it's {@link DataConnectionServerMdpReponse}.
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
	 * Creates a Frame Private Connection Requested with it's {@link DataPrivateConnectionRequested}.
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
	 * Creates a Frame Private Connection Reponse with it's {@link DataPrivateConnectionReponse}.
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
	 * Creates a Frame Private Connection Accepted with it's {@link DataPrivateConnectionAccepted}.
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
	 * Creates a Frame Private Message with it's {@link DataPrivateMessage}.
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
	 * Creates a Frame Private File with it's {@link DataPrivateFile}.
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
	 * Creates a Frame Ack with it's {@link DataAck}.
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
	 * Creates a Frame with it's {@link DataPrivateAck}.
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
	 * Creates a Frame Deconnexion with it's {@link DataDeconnexion}.
	 * @param data a {@link DataDeconnexion}.
	 * @return Frame.
	 */
	static Frame createFrameDeconnexion(DataDeconnexion data) {
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
