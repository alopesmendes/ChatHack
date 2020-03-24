package fr.upem.net.tcp.reader;

import java.nio.ByteBuffer;
import java.util.HashMap;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.data.DataAckReader;
import fr.upem.net.tcp.reader.data.DataErrorReader;
import fr.upem.net.tcp.reader.data.DataGlobal;
import fr.upem.net.tcp.reader.data.DataPrivateConnectionReader;
import fr.upem.net.tcp.reader.data.DataPublicConnectReader;

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
		map.put(StandardOperation.GLOBAL_MESSAGE.opcode(), DataGlobal.create(bb));
		map.put(StandardOperation.CONNEXION.opcode(), new DataPublicConnectReader(bb));
		map.put(StandardOperation.PRIVATE_CONNEXION.opcode(), DataPrivateConnectionReader.create(bb));
		map.put(StandardOperation.ERROR.opcode(), new DataErrorReader(bb));
		map.put((byte)1, new DataAckReader(bb, (byte)1));
		map.put((byte)0, new DataAckReader(bb, (byte)0));
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
