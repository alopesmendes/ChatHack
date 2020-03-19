package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Objects;

import fr.upem.net.tcp.frame.Data.DataError;
import fr.upem.net.tcp.frame.Data.DataGlobal;

public interface Frame {
	
	
	/**
	 * @return the buffer according to the frame.
	 */
	ByteBuffer buffer();
	
	
	/**
	 * Creates a Global Frame.
	 * @param dataGlobal a DataGlobal.
	 * @return FrameGlobal.
	 */
	static Frame createFrameGlobal(DataGlobal dataGlobal) {
		Objects.requireNonNull(dataGlobal);
		return new FrameGlobal(dataGlobal);
	}
	
	static Frame createFrameError(DataError dataError) {
		Objects.requireNonNull(dataError);
		return new FrameError(dataError);
	}
}
