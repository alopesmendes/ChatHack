package fr.upem.net.tcp.reader;

import fr.upem.net.tcp.frame.FrameVisitor;

public interface Reader<E> {
	public static enum ProcessStatus {
		DONE, REFILL, ERROR;
	}
	
	ProcessStatus process(FrameVisitor fv);
	
	E get();
	
	void reset();
}
