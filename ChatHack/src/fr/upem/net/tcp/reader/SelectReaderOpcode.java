package fr.upem.net.tcp.reader;

import java.nio.ByteBuffer;
import java.util.HashMap;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.frames.FrameAckReader;
import fr.upem.net.tcp.reader.frames.FrameLogoutReader;
import fr.upem.net.tcp.reader.frames.FrameErrorReader;
import fr.upem.net.tcp.reader.frames.FrameGlobal;
import fr.upem.net.tcp.reader.frames.FrameMdpReader;
import fr.upem.net.tcp.reader.frames.FramePrivateConnectionReader;
import fr.upem.net.tcp.reader.frames.FramePrivateFileReader;
import fr.upem.net.tcp.reader.frames.FramePrivateMessageReader;
import fr.upem.net.tcp.reader.frames.FramePublicConnectReader;

/**
 * <p>The SelectReaderOpcode will be our factory {@link Reader}.</p>
 * Will select the correct {@link Reader} according to the first byte it reads.
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class SelectReaderOpcode implements Reader<Data> {

	private enum State {
		DONE, WAITING_OPCODE, WAITING_READER, ERROR;
	}
	
	private final HashMap<Byte, Reader<Data>> map;
	private State state = State.WAITING_OPCODE;
	private ByteReader byteReader;
	private Reader<Data> reader;
	private Data data;
	
	
	/**
	 * Constructs a SelectReaderOpcode with it's {@link ByteBuffer} and {@link HashMap}.
	 * @param bb a {@link ByteBuffer}.
	 * @param map a {@link HashMap} of {@link Byte} as key and {@link Reader} of {@link Data} as value.
	 */
	private SelectReaderOpcode(ByteBuffer bb, HashMap<Byte, Reader<Data>> map) {
		this.byteReader = new ByteReader(bb);
		this.map = map;
	}
	
	/**
	 * Factory to create {@link Reader} of {@link Data}.
	 * Initiates our {@link HashMap}.
	 * @param bb a {@link ByteBuffer}.
	 * @return SelectReaderOpcode.
	 */
	public static Reader<Data> create(ByteBuffer bb) {
		HashMap<Byte, Reader<Data>> map = new HashMap<>();
		map.put(StandardOperation.GLOBAL_MESSAGE.opcode(), new FrameGlobal(bb));
		map.put(StandardOperation.CONNEXION.opcode(), new FramePublicConnectReader(bb));
		map.put(StandardOperation.PRIVATE_CONNEXION.opcode(), FramePrivateConnectionReader.create(bb));
		map.put(StandardOperation.ERROR.opcode(), new FrameErrorReader(bb));
		map.put(StandardOperation.PRIVATE_MESSAGE.opcode(), new FramePrivateMessageReader(bb));
		map.put(StandardOperation.PRIVATE_FILE.opcode(), new FramePrivateFileReader(bb));
		map.put(StandardOperation.ACK.opcode(), new FrameAckReader(bb));
		map.put(StandardOperation.LOGOUT.opcode(), new FrameLogoutReader(bb));
		map.put((byte)1, new FrameMdpReader(bb, (byte)1));
		map.put((byte)0, new FrameMdpReader(bb, (byte)0));
		return new SelectReaderOpcode(bb, map);
	}
	
	private Reader<Data> exception(byte b) {
		throw new IllegalArgumentException("Error not in map got "+b);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException("state = Done or Error");
		}
		ProcessStatus ps;
		switch(state) {
			case WAITING_OPCODE:
				ps = byteReader.process();
				if (ps != ProcessStatus.DONE) {
					return ps;
				}
				Byte b = byteReader.get();
				byteReader.reset();
				state = State.WAITING_READER;
				reader = map.computeIfAbsent(b, by -> exception(b));
			case WAITING_READER:
				ps = reader.process();
				if (ps != ProcessStatus.DONE) {
					return ps;
				}
				data = reader.get();
				reader.reset();
				state = State.DONE;
				return ProcessStatus.DONE;
			default:
				throw new AssertionError();
		}
	}

	/**
	 * @return {@link Data}.
	 */
	@Override
	public Data get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_OPCODE;
		byteReader.reset();
		reader.reset();
		
	}

}
