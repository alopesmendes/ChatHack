package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.IntReader;
import fr.upem.net.tcp.reader.basics.StringReader;

public class FramePrivateFileReader implements Reader<Data> {

	private enum State {
		DONE, WAITING_LOGIN, WAITING_FILE_NAME, WAITING_FILE_SIZE, WAITING_FILE, ERROR;
	}

	private State state = State.WAITING_LOGIN;
	private final StringReader stringReader;
	private final IntReader intReader;
	private final ByteBuffer bb;

	private String fileName;
	private String login;
	private int size;
	private ByteBuffer fileBuffer;
	private Data data;

	public FramePrivateFileReader(ByteBuffer bb) {
		this.bb = bb;
		stringReader = new StringReader(bb);
		intReader = new IntReader(bb);
	}


	@Override
	public ProcessStatus process() {
		if (state == State.DONE || state == State.ERROR) {
			throw new IllegalArgumentException();
		}
		switch (state) {
			case WAITING_LOGIN:
				ProcessStatus processLogin = stringReader.process();
				if (processLogin != ProcessStatus.DONE) {
					return processLogin;
				}
				login = stringReader.get();
				stringReader.reset();
				state = State.WAITING_FILE_NAME;
			case WAITING_FILE_NAME:
				ProcessStatus processFileName = stringReader.process();
				if (processFileName != ProcessStatus.DONE) {
					return processFileName;
				}
				fileName = stringReader.get();
				stringReader.reset();
				state = State.WAITING_FILE_SIZE;
			case WAITING_FILE_SIZE:
				ProcessStatus processFileSize = intReader.process();
				if (processFileSize != ProcessStatus.DONE) {
					return processFileSize;
				}
				size = intReader.get();
				intReader.reset();
				state = State.WAITING_FILE;
				fileBuffer = ByteBuffer.allocate(size);
			case WAITING_FILE:
				if (!bb.hasRemaining()) {
					return ProcessStatus.ERROR;
				}
				if (bb.remaining() < size) {
					return ProcessStatus.REFILL;
				}
				bb.limit(bb.position()+size);
				fileBuffer.put(bb).flip();
				data = Data.createDataPrivateFile(StandardOperation.PRIVATE_FILE, login, fileName, fileBuffer);
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
		state = State.WAITING_LOGIN;
		stringReader.reset();
		intReader.reset();

	}

}
