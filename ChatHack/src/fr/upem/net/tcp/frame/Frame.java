package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Objects;

import fr.upem.net.tcp.frame.Data.DataConnectionClient;
import fr.upem.net.tcp.frame.Data.DataError;
import fr.upem.net.tcp.frame.Data.DataGlobalClient;
import fr.upem.net.tcp.frame.Data.DataGlobalServer;

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
	static Frame createFrameGlobal(DataGlobalServer dataGlobal) {
		Objects.requireNonNull(dataGlobal);
		return new FrameGlobalServer(dataGlobal);
	}
	
	static Frame createFrameGlobal(DataGlobalClient dataGlobal) {
		Objects.requireNonNull(dataGlobal);
		return new FrameGlobalClient(dataGlobal);
	}
	
	/**
	 * Creates a Error Frame.
	 * @param dataError a DataError.
	 * @return FrameError.
	 */
	static Frame createFrameError(DataError dataError) {
		Objects.requireNonNull(dataError);
		return new FrameError(dataError);
	}
	
	/**
	 * Creates a Connection Frame.
	 * @param dataConnection a DataConnection.
	 * @return FrameConnection.
	 */
	static Frame createFrameConnection(DataConnectionClient dataConnection) {
		Objects.requireNonNull(dataConnection);
		return new FrameConnectionClient(dataConnection);
	}
}
