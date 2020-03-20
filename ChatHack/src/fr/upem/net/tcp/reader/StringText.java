package fr.upem.net.tcp.reader;

import java.nio.ByteBuffer;

public class StringReader {
	private enum State {
		DONE, WAITING_SIZE, WAITING_MSG, ERROR
	};

	private ByteBuffer bb;
	private State state = State.WAITING_SIZE;
	private int size;
	private String text;
}
