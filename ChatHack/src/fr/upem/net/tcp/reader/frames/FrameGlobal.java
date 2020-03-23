package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;
import java.util.HashMap;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;

public class FrameGlobal implements Reader<Data> {
	
	private enum State {
		DONE, WAITING_BYTE, WAITING_READER, ERROR;
	}
	
	private State state = State.WAITING_BYTE;
	private final HashMap<Byte, Reader<Data>> map;
	private ByteReader byteReader;
	private Reader<Data> reader;
	private Data data;
	
	private FrameGlobal(ByteReader byteReader, HashMap<Byte, Reader<Data>> map) {
		this.map = map;
		this.byteReader = byteReader;
	}
	
	public static FrameGlobal create(ByteBuffer bb) {
		HashMap<Byte, Reader<Data>> map = new HashMap<>();
		map.put((byte)1, new FrameGlobalSendingReader(bb, (byte)1));
		map.put((byte)2, new FrameGlobalReceivingReader(bb, (byte)2));
		//map.put((byte) 1, new FrameGlobalReceivingReader(bb));
		//map.put((byte)2, new FrameGlobalSendingReader(bb, "Ailton"));
		return new FrameGlobal(new ByteReader(bb), map);
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalArgumentException();
		}
		ProcessStatus ps;
		switch (state) {
			case WAITING_BYTE:
				ps = byteReader.process();
				if (ps != ProcessStatus.DONE) {
					return ps;
				}
				Byte b = byteReader.get();
				reader = map.get(b);
				state = State.WAITING_READER;
				byteReader.reset();
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

	@Override
	public Data get() {
		if (state != State.DONE) {
			throw new AssertionError();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_BYTE;
		byteReader.reset();
		reader.reset();
	}

}
