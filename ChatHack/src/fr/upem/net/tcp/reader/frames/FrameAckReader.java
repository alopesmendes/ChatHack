package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;




public class FrameAckReader implements Reader<Data> {

	private enum State {
		DONE, WAITING_OP_REQUEST, WAITING_READER, ERROR
	};	
	
	private State state = State.WAITING_OP_REQUEST;
	private final ByteReader byteReader;
	private StringReader stringReader;
	private String login;
	private StandardOperation requestCode;
	private Data data;
	private byte op_request;
	
	/**
	 * @param bb
	 */
	public FrameAckReader(ByteBuffer bb) {
		byteReader = new ByteReader(bb);
		stringReader = new StringReader(bb);

	}
	
	@Override
	public ProcessStatus process() {
		switch(state) {
		case WAITING_OP_REQUEST:
			ProcessStatus requestStatus = byteReader.process();
			if (requestStatus != ProcessStatus.DONE) {
				return requestStatus;
			}
			op_request = byteReader.get();
			var op = StandardOperation.convert(op_request);
			if (op.isEmpty()) {
				return ProcessStatus.ERROR;
			}
			requestCode = op.get();
			byteReader.reset();
			state = State.WAITING_READER;
		case WAITING_READER:
			switch (requestCode) {
			case PRIVATE_MESSAGE: case PRIVATE_FILE:
				ProcessStatus processLogin = stringReader.process();
				if (processLogin != ProcessStatus.DONE) {
					return processLogin;
				}
				login = stringReader.get();
				stringReader.reset();
				data = Data.createDataPrivateAck(StandardOperation.ACK, requestCode, login);
				break;
			default:
				data = Data.createDataAck(StandardOperation.ACK, requestCode);
				break;
			}
			state = State.DONE;
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
		state = State.WAITING_OP_REQUEST;
		byteReader.reset();
		stringReader.reset();
		
	}

}
