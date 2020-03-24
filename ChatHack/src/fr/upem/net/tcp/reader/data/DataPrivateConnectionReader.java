package fr.upem.net.tcp.reader.data;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;

public class DataPrivateConnectionReader implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_STEP, WAITING_READER, ERROR;
	}
	
	private State state = State.WAITING_STEP;
	private final ByteReader byteReader;
	private final Map<Byte, Reader<Data>> map;
	private byte step;
	private Reader<Data> reader;
	private Data data;
	
	private DataPrivateConnectionReader(ByteBuffer bb, Map<Byte, Reader<Data>> map) {
		byteReader = new ByteReader(bb);
		this.map = map;
	}
	
	public static DataPrivateConnectionReader create(ByteBuffer bb) {
		HashMap<Byte, Reader<Data>> map = new HashMap<>();
		map.put((byte)1, new DataPrivateConnectionRequestReader((byte)1, bb));
		map.put((byte)2, new DataPrivateConnectionRequestReader((byte)2, bb));
		map.put((byte)3, new DataPrivateConnectionReponseReader((byte)3, bb));
		map.put((byte)4, new DataPrivateConnectionReponseReader((byte)4, bb));
		map.put((byte)5, new DataPrivateConnectionAcceptedReader((byte)5, bb));
		map.put((byte)6, new DataPrivateConnectionAcceptedReader((byte)6, bb));
		return new DataPrivateConnectionReader(bb, map);
	}
	
	
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalArgumentException();
		}
		switch (state) {
			case WAITING_STEP:
				ProcessStatus processStep = byteReader.process();
				if (processStep != ProcessStatus.DONE) {
					return processStep;
				}
				step = byteReader.get();
				byteReader.reset();
				reader = map.computeIfAbsent(step, b -> { throw new AssertionError();});
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
				throw new IllegalArgumentException("Unexpected value: " + state);
		}
	}

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
