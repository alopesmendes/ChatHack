package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Objects;

import fr.upem.net.tcp.frame.Data.DataConnectionClient;
import fr.upem.net.tcp.frame.Data.DataConnectionServerMdp;
import fr.upem.net.tcp.frame.Data.DataConnectionServerMdpReponse;
import fr.upem.net.tcp.frame.Data.DataError;
import fr.upem.net.tcp.frame.Data.DataGlobalClient;
import fr.upem.net.tcp.frame.Data.DataGlobalServer;
import fr.upem.net.tcp.frame.Data.DataPrivateConnectionAccepted;
import fr.upem.net.tcp.frame.Data.DataPrivateConnectionReponse;
import fr.upem.net.tcp.frame.Data.DataPrivateConnectionRequested;

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
	
	/**
	 * Creates a FrameConnectionMdp.
	 * @param data a DataConnectionServerMdp.
	 * @return FrameConnectMdp.
	 */
	static Frame createFrameConnectionMdp(DataConnectionServerMdp data) {
		Objects.requireNonNull(data);
		return new FrameConnectMdp(data);
	}
	 
	/**
	 * Creates a FrameConnectMdpServer.
	 * @param data a DataConnectionServerMdpReponse.
	 * @return FrameConnectMdpServer.
	 */
	static Frame createFrameConnectMdpServer(DataConnectionServerMdpReponse data) {
		Objects.requireNonNull(data);
		return new FrameConnectMdpServer(data);
	}
	
	/**
	 * Creates a FramePrivateConnectionRequested.
	 * @param data a DataPrivateConnectionRequested.
	 * @return FramePrivateConnectionRequested.
	 */
	static Frame createFramePrivateConnectionRequested(DataPrivateConnectionRequested data) {
		Objects.requireNonNull(data);
		return new FramePrivateConnectionRequested(data);
	}
	
	/**
	 * Creates a FramePrivateConnectionReponse.
	 * @param data a DataPrivateConnectionReponse.
	 * @return FramePrivateConnectionReponse.
	 */
	static Frame createFramePrivateConnectionReponse(DataPrivateConnectionReponse data) {
		Objects.requireNonNull(data);
		return new FramePrivateConnectionReponse(data);
	}
	
	/**
	 * Creates a FramePrivateConnectionAccepted.
	 * @param data a DataPrivateConnectionAccepted.
	 * @return FramePrivateConnectionAccepted.
	 */
	static Frame createFramePrivateConnectionAccepted(DataPrivateConnectionAccepted data) {
		Objects.requireNonNull(data);
		return new FramePrivateConnectionAccepted(data);
	}
}
