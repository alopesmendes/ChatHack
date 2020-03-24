package fr.upem.net.tcp.reader.data;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;


public class DataGlobalSendingReader implements Reader<Data> {

	private enum State {
		DONE, WAITING_TEXT, ERROR
	};	
	
	private State state = State.WAITING_TEXT;
	private final ByteReader byteReader;
	private final StringReader stringReader;
	private byte step;
	private String text;
	private Data data;
	
	/**
	 * @param bb
	 */
	public DataGlobalSendingReader(ByteBuffer bb, byte step) {
		this.stringReader = new StringReader(bb);
		this.byteReader = new ByteReader(bb);
		this.step = step;
	}
	
	
	@Override
	public ProcessStatus process() {
		switch (state) {
		case WAITING_TEXT:
			ProcessStatus textStatus = stringReader.process();
			if (textStatus != ProcessStatus.DONE) {
				return textStatus;
			}
			text = stringReader.get();
			stringReader.reset();
			state = State.DONE;
			data = Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, step, text);
			return ProcessStatus.DONE;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Data get() {
		if (state != State.DONE) {
			throw new IllegalStateException();
		}
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_TEXT;
		stringReader.reset();
		byteReader.reset();
		
	}

}
