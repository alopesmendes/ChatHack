package fr.upem.net.tcp.reader.frames;

import java.nio.ByteBuffer;
import java.util.Optional;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;
import fr.upem.net.tcp.reader.Reader;
import fr.upem.net.tcp.reader.basics.ByteReader;
import fr.upem.net.tcp.reader.basics.StringReader;


public class FramePublicConnectReader implements Reader<Data> {

	private enum State {
		DONE, WAITING_OP_CODE,WAITING_CONNEXION_TYPE,WAITING_LOGIN, WAITING_PASSWORD, ERROR
	};	
	
	private State state = State.WAITING_CONNEXION_TYPE;
	private final StringReader stringReader;
	private final ByteReader byteReader;
	
	//private Byte op_code;
	private Byte connexionType;
	private String login;
	private Data data;
	
	/**
	 * @param bb
	 */
	public FramePublicConnectReader(ByteBuffer bb) {
		this.stringReader = new StringReader(bb);
		this.byteReader = new ByteReader(bb);
	}

	
	@Override
	public ProcessStatus process() {
		if (state==State.DONE || state==State.ERROR) {
			throw new IllegalStateException();
		}
		switch (state) {
/*
		case WAITING_OP_CODE:
			ProcessStatus opStatus = byteReader.process();
			if (opStatus != ProcessStatus.DONE) {
				return opStatus;
			}
			op_code = byteReader.get();
			if (op_code != StandardOperation.CONNEXION.opcode()) {
				return ProcessStatus.ERROR;
			}
			byteReader.reset();
			state = State.WAITING_CONNEXION_TYPE;
*/			
		case WAITING_CONNEXION_TYPE:
			ProcessStatus typeStatus = byteReader.process();
			if (typeStatus != ProcessStatus.DONE) {
				return typeStatus;
			}
			connexionType = byteReader.get();
			if (connexionType != 0 && connexionType != 1) {
				return ProcessStatus.ERROR;
			}
			byteReader.reset();
			state = State.WAITING_LOGIN;
			
		case WAITING_LOGIN:
			ProcessStatus loginStatus = stringReader.process();
			if (loginStatus != ProcessStatus.DONE) {
				return loginStatus;
			}
			login = stringReader.get();
			stringReader.reset();
			if (connexionType == 1) {
				state = State.DONE;
				data = Data.createDataConnectionClient(StandardOperation.CONNEXION, connexionType, login, Optional.empty());
				return ProcessStatus.DONE;
			} 
			state = State.WAITING_PASSWORD;
		case WAITING_PASSWORD:
			ProcessStatus passwordStatus = stringReader.process();
			System.out.println("---------- ICI ----------");
			if (passwordStatus != ProcessStatus.DONE) {
				return passwordStatus;
			}
			String password = stringReader.get();
			stringReader.reset();
			state = State.DONE;
			data = Data.createDataConnectionClient(StandardOperation.CONNEXION, connexionType, login, Optional.of(password));
			System.out.println("---------- OK ----------");
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
		//return new PublicConnexionFrame(op_code,connexionType,login);
		return data;
	}

	@Override
	public void reset() {
		state = State.WAITING_CONNEXION_TYPE;
		stringReader.reset();
		byteReader.reset();
	}

}
