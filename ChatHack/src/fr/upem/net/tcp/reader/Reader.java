package fr.upem.net.tcp.reader;

import fr.upem.net.tcp.frame.FrameVisitor;

public interface Reader<E> {
	public static enum ProcessStatus {
		DONE, REFILL, ERROR;
	}
	
	ProcessStatus process();
	
	E get();
	
	void reset();
}
