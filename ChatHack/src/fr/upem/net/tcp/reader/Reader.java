package fr.upem.net.tcp.reader;

import fr.upem.net.tcp.frame.Frame;

public interface Reader {
	public static enum ProcessStatus {
		DONE, REFILL, ERROR;
	}
	
	ProcessStatus process();
	
	Frame get();
	
	void reset();
}
