package fr.upem.net.tcp.reader;

import java.nio.ByteBuffer;
import java.util.HashMap;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;
import fr.upem.net.tcp.reader.frames.FrameAckReader;
import fr.upem.net.tcp.reader.frames.FrameErrorReader;
import fr.upem.net.tcp.reader.frames.FrameGlobal;
import fr.upem.net.tcp.reader.frames.FramePrivateConnectionReader;
import fr.upem.net.tcp.reader.frames.FramePrivateMessageReader;
import fr.upem.net.tcp.reader.frames.FramePublicConnectReader;

public class SelectReaderOpcode implements Reader<Data> {

	private enum State {
		DONE, WAITING_OPCODE, WAITING_READER, ERROR;
	}
	
	private final HashMap<Byte, Reader<Data>> map;
	private State state = State.WAITING_OPCODE;
	private ByteReader byteReader;
	private Reader<Data> reader;
	private Data data;
	
	
	private SelectReaderOpcode(ByteBuffer bb, HashMap<Byte, Reader<Data>> map) {
		this.byteReader = new ByteReader(bb);
		this.map = map;
	}
	
	public static Reader<Data> create(ByteBuffer bb) {
		HashMap<Byte, Reader<Data>> map = new HashMap<>();
		map.put(StandardOperation.GLOBAL_MESSAGE.opcode(), FrameGlobal.create(bb));
		map.put(StandardOperation.CONNEXION.opcode(), new FramePublicConnectReader(bb));
		map.put(StandardOperation.PRIVATE_CONNEXION.opcode(), FramePrivateConnectionReader.create(bb));
		map.put(StandardOperation.ERROR.opcode(), new FrameErrorReader(bb));
		map.put((byte)1, new FrameAckReader(bb, (byte)1));
		map.put((byte)0, new FrameAckReader(bb, (byte)0));
		map.put(StandardOperation.PRIVATE_MESSAGE.opcode(), new FramePrivateMessageReader(bb));
		return new SelectReaderOpcode(bb, map);
	}
	
	private Reader<Data> exception() {
		throw new IllegalArgumentException();
	}
	
	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalArgumentException();
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
				reader = map.computeIfAbsent(b, by -> exception());
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
		state = State.WAITING_OPCODE;
		byteReader.reset();
		reader.reset();
		
	}

}
