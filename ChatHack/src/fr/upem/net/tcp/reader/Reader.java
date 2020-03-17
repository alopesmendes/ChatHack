package fr.upem.net.tcp.reader;


public interface Reader<E> {
	public static enum ProcessStatus {
		DONE, REFILL, ERROR;
	}
	
	ProcessStatus process();
	
	E get();
	
	void reset();
}
