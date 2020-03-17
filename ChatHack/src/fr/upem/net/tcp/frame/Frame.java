package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;

public interface Frame {
	
	
	/**
	 * @return the buffer according to the frame.
	 */
	ByteBuffer buffer();
}
