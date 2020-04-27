package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.FileReader;
import fr.upem.net.tcp.reader.basics.IntReader;
import fr.upem.net.tcp.reader.basics.StringReader;

/**
 * <p>
 * The FramePrivateFileReader will be use to read all the data.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class FramePrivateFileReader implements Reader<Data> {

	private enum State {
		DONE, WAITING_LOGIN, WAITING_FILE_NAME, WAITING_FILE, ERROR;
	}

	private State state = State.WAITING_LOGIN;
	private final StringReader stringReader;
	private final IntReader intReader;
	private final FileReader fileReader;

	private String fileName;
	private String login;
	private ByteBuffer fileBuffer;
	private Data data;

	/**
	 * Constructs a FramePrivateFileReader with it's {@link ByteBuffer}.
	 * @param bb a {@link ByteBuffer}.
	 */
	public FramePrivateFileReader(ByteBuffer bb) {
		stringReader = new StringReader(bb);
		intReader = new IntReader(bb);
		fileReader = new FileReader(bb);
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
				state = State.WAITING_FILE;
			case WAITING_FILE:
				ProcessStatus processFile = fileReader.process();
				if (processFile != ProcessStatus.DONE) {
					return processFile;
				}
				fileBuffer = fileReader.get();
				fileReader.reset();
				state = State.DONE;
				data = Data.createDataPrivateFile(StandardOperation.PRIVATE_FILE, login, fileName, fileBuffer);
				return ProcessStatus.DONE;
	
			default:
				throw new AssertionError();
		}
	}

	/**
	 * @return {@link Data}
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
		state = State.WAITING_LOGIN;
		stringReader.reset();
		intReader.reset();
		fileReader.reset();

	}

}
