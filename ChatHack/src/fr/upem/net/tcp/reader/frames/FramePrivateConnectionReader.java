package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;

/**
 * <p>
 * The FramePrivateConnectionAcceptedReader will be use to read all the data.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class FramePrivateConnectionReader implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_STEP, WAITING_READER, ERROR;
	}
	
	private State state = State.WAITING_STEP;
	private final ByteReader byteReader;
	private final Map<Byte, Reader<Data>> map;
	private byte step;
	private Reader<Data> reader;
	private Data data;
	
	/**
	 * Constructs a FramePrivateConnectionReader with it's {@link ByteBuffer} and {@link Map}.
	 * @param bb a {@link ByteBuffer}.
	 * @param map a {@link Map}.
	 */
	private FramePrivateConnectionReader(ByteBuffer bb, Map<Byte, Reader<Data>> map) {
		byteReader = new ByteReader(bb);
		this.map = map;
	}
	
	/**
	 * A Factory Method which will create our FramePrivateConnectionReader.
	 * It will initiate the map.
	 * @param bb a {@link ByteBuffer}.
	 * @return a FramePrivateConnectionReader.
	 */
	public static FramePrivateConnectionReader create(ByteBuffer bb) {
		HashMap<Byte, Reader<Data>> map = new HashMap<>();
		map.put((byte)1, new FramePrivateConnectionRequestReader((byte)1, bb));
		map.put((byte)2, new FramePrivateConnectionRequestReader((byte)2, bb));
		map.put((byte)3, new FramePrivateConnectionReponseReader((byte)3, bb));
		map.put((byte)4, new FramePrivateConnectionReponseReader((byte)4, bb));
		map.put((byte)5, new FramePrivateConnectionAcceptedReader((byte)5, bb));
		map.put((byte)6, new FramePrivateConnectionAcceptedReader((byte)6, bb));
		return new FramePrivateConnectionReader(bb, map);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalStateException();
		}
		switch (state) {
			case WAITING_STEP:
				ProcessStatus processStep = byteReader.process();
				if (processStep != ProcessStatus.DONE) {
					return processStep;
				}
				step = byteReader.get();
				byteReader.reset();
				reader = map.computeIfAbsent(step, b -> { throw new IllegalArgumentException();});
				state = State.WAITING_READER;
			case WAITING_READER:
				ProcessStatus processLogin = reader.process();
				if (processLogin != ProcessStatus.DONE) {
					return processLogin;
				}
				data = reader.get();
				reader.reset();
				state = State.DONE;
				return ProcessStatus.DONE;
	
			default:
				throw new AssertionError("Unexpected value: " + state);
		}
	}

	/**
	 * @return {@link Data}
	 */
	@Override
	public Data get() {
		if (state != State.DONE) {
			throw new IllegalArgumentException();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_STEP;
		byteReader.reset();
		reader.reset();
		
	}

}
