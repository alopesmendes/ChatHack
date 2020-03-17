package fr.upem.net.tcp.reader;

import fr.upem.net.tcp.frame.Frame;

public class MessageReader implements Reader {
	

	@Override
	public ProcessStatus process() {
		return ProcessStatus.DONE;
	}

	@Override
	public Frame get() {
		return null;
	}

	@Override
	public void reset() {
		
	}

}
