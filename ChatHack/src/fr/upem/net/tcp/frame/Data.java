package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

public interface Data {
	static class DataText implements Data {
		final String text;

		/**
		 * Constructs a DataText with a text.
		 * @param text a String
		 */
		private DataText(String text) {
			this.text = Objects.requireNonNull(text);
		}
		
		@Override
		public int hashCode() {
			return text.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataText)) {
				return false;
			}
			DataText d = (DataText)obj; 
			return text.equals(d.text);
		}
		
	}
	
	static class DataGlobalServer implements Data {
		final StandardOperation opcode;
		final byte step;
		final DataText pseudo;
		final DataText message;
		
		/**
		 * Constructs a DataGlobal with it's ack, step, pseudo and message.
		 * @param opcode a byte.
		 * @param step a byte.
		 * @param pseudo a DataText.
		 * @param message a DataText.
		 */
		private DataGlobalServer(StandardOperation opcode, byte step, DataText pseudo, DataText message) {
			this.opcode = opcode;
			this.step = step;
			this.pseudo = pseudo;
			this.message = message;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ Byte.hashCode(step) ^ pseudo.hashCode() ^ message.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataGlobalServer)) {
				return false;
			}
			DataGlobalServer d = (DataGlobalServer)obj;
			return 	opcode==d.opcode && step==d.step 
					&& pseudo.equals(d.pseudo) && message.equals(d.message);
		}
		
		public String login() {
			return pseudo.text;
		}
		
		public String message() {
			return message.text;
		}
	}
	
	static class DataGlobalClient implements Data {
		final StandardOperation opcode;
		final DataText login;
		final DataText message;
		
		/**
		 * Constructs a DataGlobalClient with it's opcode, step and message.
		 * @param opcode a StandardOperation
		 * @param step a byte
		 * @param message a DataText
		 */
		private DataGlobalClient(StandardOperation opcode, DataText login, DataText message) {
			this.opcode = opcode;
			this.login = login;
			this.message = message;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ login.hashCode() ^ message.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataGlobalClient)) {
				return false;
			}
			DataGlobalClient d = (DataGlobalClient)obj;
			return opcode==d.opcode && d.login.equals(login) && d.message.equals(message);
		}
		
		public String login() {
			return login.text;
		}
		
		public String message() {
			return message.text;
		}
		
	}
	
	static class DataError implements Data {
		final StandardOperation opcode;
		final StandardOperation requestCode;
		
		/**
		 * Constructs a DataError with it's opcode and requestCode.
		 * @param opcode
		 * @param requestCode
		 */
		private DataError(StandardOperation opcode, StandardOperation requestCode) {
			this.opcode = opcode;
			this.requestCode = requestCode;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ Byte.hashCode(requestCode.opcode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataError)) {
				return false;
			}
			DataError d = (DataError)obj;
			return d.opcode==opcode && d.requestCode==requestCode;
		}
	}
		
	static class DataPrivateConnectionRequested implements Data {
		final StandardOperation opcode;
		final byte step;
		final DataText loginRequest;
		
		/**
		 * Constructs a DataPrivateConnectionRequested with it's opcode and loginRequest.
		 * @param opcode a StandardOperation
		 * @param loginRequest a DataText
		 */
		private DataPrivateConnectionRequested(StandardOperation opcode, byte step, DataText loginRequest) {
			this.opcode = opcode;
			this.step = step;
			this.loginRequest = Objects.requireNonNull(loginRequest);
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ Byte.hashCode(step) ^ loginRequest.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionRequested)) {
				return false;
			}
			DataPrivateConnectionRequested d = (DataPrivateConnectionRequested)obj;
			return d.opcode==opcode && d.step==step && loginRequest.equals(d.loginRequest);
		}
		
		public byte step() {
			return step;
		}
		
		public StandardOperation opcode() {
			return opcode;
		}
		
		public String login() {
			return loginRequest.text;
		}
	}
	
	static class DataPrivateConnectionReponse implements Data {
		final DataPrivateConnectionRequested dataPrivateConnectionRequested;
		final byte state;
		
		/**
		 * Constructs a DataPrivateConnectionReponse.
		 * @param dataPrivateConnectionRequested a DataPrivateConnectionReponse.
		 * @param state a byte.
		 */
		public DataPrivateConnectionReponse(DataPrivateConnectionRequested dataPrivateConnectionRequested, byte state) {
			this.dataPrivateConnectionRequested = dataPrivateConnectionRequested;
			this.state = state;
		}
		
		@Override
		public int hashCode() {
			return dataPrivateConnectionRequested.hashCode() ^ Byte.hashCode(state);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionReponse)) {
				return false;
			}
			DataPrivateConnectionReponse d = (DataPrivateConnectionReponse)obj;
			return d.state==state && d.dataPrivateConnectionRequested.equals(dataPrivateConnectionRequested);
		}
		
		public String login() {
			return dataPrivateConnectionRequested.loginRequest.text;
		}
		
		public byte state() {
			return state;
		}
		
	}
	
	static class DataPrivateConnectionAccepted implements Data {
		final StandardOperation opcode;
		final byte step;
		final DataText loginReceiver;
		final int port;
		final DataText host;
		final long token;
		
		/**
		 * Constructs a DataPrivateConnectionAccepted with it's opcode, loginReceiver, socketAdress and token.
		 * @param opcode a StandardOperation.
		 * @param loginReceiver a DataText.
		 * @param socketAdress a DataText.
		 * @param token a long.
		 */
		private DataPrivateConnectionAccepted(StandardOperation opcode, byte step,  DataText loginReceiver, int port, DataText host,
				long token) {
			this.opcode = opcode;
			this.step = step;
			this.loginReceiver = loginReceiver;
			this.host = host;
			this.token = token;
			this.port = port;
		}
		
		@Override
		public int hashCode() {
			return 	Byte.hashCode(opcode.opcode()) ^ Byte.hashCode(opcode.opcode())  ^ loginReceiver.hashCode() 
					^ host.hashCode() ^ Long.hashCode(token) ^ port;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionAccepted)) {
				return false;
			}
			DataPrivateConnectionAccepted d = (DataPrivateConnectionAccepted)obj;
			return 	d.opcode==opcode && d.step==step && token==d.token && d.port==port
					&& loginReceiver.equals(d.loginReceiver) && host.equals(d.host);
		}	
		
		public String login() {
			return loginReceiver.text;
		}
		
		public int port() {
			return port;
		}
		
		public String host() {
			return host.text;
		}
		
		public long token() {
			return token;
		}
	}

	static class DataPrivateConnectionConnect implements Data {
		final StandardOperation opcode;
		final byte step;
		final DataText login;
		final long token;
		
		/**
		 * Creates a DataPrivateConnectionConnect with it's opcode, step, login and token.
		 * @param opcode a StandardOperation.
		 * @param step a byte.
		 * @param login a DataText.
		 * @param token a long.
		 */
		private DataPrivateConnectionConnect(StandardOperation opcode, byte step, DataText login, long token) {
			this.opcode = opcode;
			this.step = step;
			this.login = login;
			this.token = token;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(step) ^ Long.hashCode(token) ^ opcode.hashCode() ^ login.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionConnect)) {
				return false;
			}
			DataPrivateConnectionConnect d = (DataPrivateConnectionConnect)obj;
			return d.opcode==opcode && d.step==step && token==d.token && login.equals(d.login);
		}
		
		public String login() {
			return login.text;
		}
		
		public long token() {
			return token;
		}
		
		
	}
	
	static class DataPrivateConnectionRejected implements Data {
		final StandardOperation opcode;
		final byte requestCode;
		final byte step;
		
		
		
		/**
		 * Constructs a DataPrivateConnectionRejected with it's opcode, requestCode and step.
		 * @param opcode
		 * @param requestCode
		 * @param step
		 */
		public DataPrivateConnectionRejected(StandardOperation opcode, byte requestCode, byte step) {
			this.opcode = opcode;
			this.requestCode = requestCode;
			this.step = step;
		}

		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ Byte.hashCode(requestCode) ^ Byte.hashCode(step);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionRequested)) {
				return false;
			}
			DataPrivateConnectionRejected d = (DataPrivateConnectionRejected) obj;
			return opcode==d.opcode && requestCode==d.requestCode && step==d.step;
		}
		
		
	}
	
	static class DataConnectionClient implements Data {
		final StandardOperation opcode;
		final byte connexion;
		final DataText login;
		final Optional<DataText> password;
		/**
		 * Constructs a DataConnectionClient with it's opcode, connexion, login and password.
		 * @param opcode a StandardOperation.
		 * @param connexion a byte.
		 * @param login a DataText.
		 * @param password a Optional<DataText>.
		 */
		private DataConnectionClient(StandardOperation opcode, byte connexion, DataText login, Optional<DataText> password) {
			this.opcode = opcode;
			this.connexion = connexion;
			this.login = login;
			this.password = password;
		}
		
		@Override
		public int hashCode() {
			return 	Byte.hashCode(opcode.opcode()) ^ Byte.hashCode(connexion)
					^ login.hashCode() ^ password.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataConnectionClient)) {
				return false;
			}
			DataConnectionClient d = (DataConnectionClient)obj;
			return 	d.connexion==connexion && d.opcode==opcode 
					&& d.login.equals(login) && password.equals(d.password);
		}
		
		public String login() {
			return login.text;
		}
	}
	
	static class DataConnectionServerMdp implements Data {
		final byte typeConnexion;
		final long id;
		final DataText login;
		final Optional<DataText> password;
		/**
		 * @param typeConnexion a byte.
		 * @param id a long.
		 * @param login a dataText.
		 * @param password a Optional of DataText.
		 */
		private DataConnectionServerMdp(byte typeConnexion, long id, DataText login, Optional<DataText> password) {
			this.typeConnexion = typeConnexion;
			this.id = id;
			this.login = login;
			this.password = password;
		}
		
		@Override
		public int hashCode() {
			return	Byte.hashCode(typeConnexion) ^ Long.hashCode(id)
					^ login.hashCode() ^ password.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataConnectionServerMdp)) {
				return false;
			}
			DataConnectionServerMdp d = (DataConnectionServerMdp)obj;
			return 	d.typeConnexion==typeConnexion && id==d.id && login.equals(d.login)
					&& password.equals(d.password);
		}
		
		public byte connexion() {
			return typeConnexion;
		}
		
		public long getId() {
			return id;
		}
		
	}
	
	static class DataConnectionServerMdpReponse implements Data {
		final byte opcode;
		final long id;
		/**
		 * Constructs a DataConnectionServerMdpReponse with it's byte and id.
		 * @param opcode a byte
		 * @param id a long
		 */
		private DataConnectionServerMdpReponse(byte opcode, long id) {
			this.opcode = opcode;
			this.id = id;
		}

		@Override
		public int hashCode() {
			return Byte.hashCode(opcode) ^ Long.hashCode(id);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataConnectionServerMdpReponse)) {
				return false;
			}
			DataConnectionServerMdpReponse d = (DataConnectionServerMdpReponse)obj;
			return opcode==d.opcode && id==d.id;
		}
		
		public byte getOpcode() {
			return opcode;
		}
		
		public long getId() {
			return id;
		}
		
	}

	static class DataPrivateMessage implements Data {
		final StandardOperation opcode;
		final DataText login;
		final DataText message;
		
		/**
		 * Constructs a DataPrivateMessage with it's opcode and message.
		 * @param login a DataText.
		 * @param opcode a StandardOperation.
		 * @param message a DataText.
		 */
		private DataPrivateMessage(StandardOperation opcode, DataText login, DataText message) {
			this.opcode = opcode;
			this.login = login;
			this.message = message;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ message.hashCode() ^ login.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateMessage)) {
				return false;
			}
			DataPrivateMessage d = (DataPrivateMessage)obj;
			return opcode==d.opcode && message.equals(d.message) && login.equals(d.login);
		}
		
		public String message() {
			return message.text;
		}
		
		public String login() {
			return login.text;
		}
		
	}
	
	static class DataPrivateFile implements Data {
		final StandardOperation opcode;
		final DataText login;
		final DataText fileName;
		final ByteBuffer buff;
		
		/**
		 * Constructs a DataPrivateFile with it's opcode, fileName, size and buff.
		 * @param opcode a StandardOperation.
		 * @param login a DataText.
		 * @param fileName a DataText.
		 * @param buff a ByteBuffer.
		 */
		private DataPrivateFile(StandardOperation opcode, DataText login, DataText fileName, ByteBuffer buff) {
			this.opcode = opcode;
			this.login = login;
			this.fileName = fileName;
			this.buff = buff;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ fileName.hashCode() ^ login.hashCode() ^ buff.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateFile)) {
				return false;
			}
			DataPrivateFile d = (DataPrivateFile)obj;
			return 	d.opcode == opcode && d.login.equals(login)
					&& d.fileName.equals(fileName) && d.buff.equals(buff);
		}
		
		public String login() {
			return login.text;
		}
		
		public String fileName() {
			return fileName.text;
		}
		
		public ByteBuffer buffer() {
			return buff;
		}
		
		
	}
	
	
	static class DataAck implements Data {
		final StandardOperation opcode;
		final StandardOperation requestCode;
		
		private DataAck(StandardOperation opcode, StandardOperation requestCode) {
			this.opcode = opcode;
			this.requestCode = requestCode;
		}
		
		@Override
		public int hashCode() {
			return opcode.hashCode() ^ requestCode.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataAck)) {
				return false;
			}
			DataAck d = (DataAck)obj;
			return 	d.opcode==opcode && d.requestCode==requestCode;
		}
		
		public StandardOperation request() {
			return requestCode;
		}
	}
	
	/**
	 * Creates a DataText.
	 * @param text a {@link String}.
	 * @return DataText.
	 */
	static DataText createDataText(String text) {
		Objects.requireNonNull(text);
		return new DataText(text);
	}
	
	/**
	 * Creates a DataGlobal.
	 * @param ack a {@link Byte}.
	 * @param step a {@link Byte}.
	 * @param pseudo a {@link String}.
	 * @param message a {@link String}.
	 * @return DataGlobalServer.
	 */
	static DataGlobalServer createDataGlobalServer(StandardOperation opcode, byte step, String pseudo, String message) {
		Objects.requireNonNull(pseudo);
		Objects.requireNonNull(message);
		DataText pseudoData = new DataText(pseudo);
		DataText messageData = new DataText(message);
		return new DataGlobalServer(opcode, step, pseudoData, messageData);
	}
	
	/**
	 * Creates a DataGlobalClient.
	 * @param opcode a {@link StandardOperation}.
	 * @param step a {@link Byte}.
	 * @param message a {@link String}.
	 * @return DataGlobalClient.
	 */
	static DataGlobalClient createDataGlobalClient(StandardOperation opcode, String login, String message) {
		Objects.requireNonNull(message);
		DataText loginData = new DataText(login);
		DataText messageData = new DataText(message);
		return new DataGlobalClient(opcode, loginData, messageData);
	}
	
	/**
	 * Creates a DataError.
	 * @param opcode a {@link StandardOperation}.
	 * @param requestCode a {@link Byte}.
	 * @return DataError.
	 */
	static DataError createDataError(StandardOperation opcode, StandardOperation requestCode) {
		return new DataError(opcode, requestCode);
	}
	
	/**
	 * Creates a DataConnectionClient.
	 * @param opcode a {@link StandardOperation}.
	 * @param connexion a {@link Byte}.
	 * @param login a {@link String}.
	 * @param password a {@link Optional}.
	 * @return DataConnectionClient.
	 */
	static DataConnectionClient createDataConnectionClient(StandardOperation opcode, byte connexion, String login, Optional<String> password) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(password);
		DataText dataLogin = new DataText(login);
		Optional<DataText> dataPasssword = password.isEmpty() ? Optional.empty() : Optional.of(new DataText(password.get()));
		return new DataConnectionClient(opcode, connexion, dataLogin, dataPasssword);
	}
	
	/**
	 * Creates a DataConnectionServerMDP.
	 * @param data a {@link DataConnectionClient}.
	 * @return DataConnectionServerMdp.
	 */
	static DataConnectionServerMdp createDataConnectionServerMdp(DataConnectionClient data) {
		Objects.requireNonNull(data);
		byte typeConnexion;
		if (data.connexion == 0) {
			typeConnexion = 1;
		} else {
			typeConnexion = 2;
		}
		return new DataConnectionServerMdp(typeConnexion, System.currentTimeMillis(), data.login, data.password);
	}
	
	/**
	 * Creates a DataConnectionServerMdpReponse.
	 * @param opcode a {@link Byte}.
	 * @param id a {@link Long}.
	 * @return DataConnectionServerMdpReponse.
	 */
	static DataConnectionServerMdpReponse createDataConnectionServerMdpReponse(byte opcode, long id) {
		return new DataConnectionServerMdpReponse(opcode, id);
		
	}

	/**
	 * Creates a DataPrivateConnectionRequested.
	 * @param opcode a {@link StandardOperation}.
	 * @param step a {@link Byte}.
	 * @param login a {@link String}.
	 * @return DataPrivateConnectionRequested.
	 */
	static DataPrivateConnectionRequested createDataPrivateConnectionRequested(StandardOperation opcode, byte step, String login) {
		Objects.requireNonNull(login);
		DataText loginRequest = new DataText(login);
		return new DataPrivateConnectionRequested(opcode, step, loginRequest);
	}

	/**
	 * Creates a DataPrivateConnectionAccepted.
	 * @param opcode a {@link StandardOperation}.
	 * @param step a {@link Byte}.
	 * @param login a {@link String}.
	 * @param socketAdress a {@link String}.
	 * @param token a {@link Long}.
	 * @return DataPrivateConnectionAccepted.
	 */
	static DataPrivateConnectionAccepted createDataPrivateConnectionAccepted(StandardOperation opcode, byte step, String login, int port, String socketAdress, long token) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(socketAdress);
		DataText loginData = new DataText(login);
		DataText socketData = new DataText(socketAdress);
		return new DataPrivateConnectionAccepted(opcode, step, loginData, port, socketData, token);
	}

	/**
	 * Creates a DataPrivateConnectionReponse.
	 * @param opcode a {@link StandardOperation}.
	 * @param step a {@link Byte}.
	 * @param login a {@link String}.
	 * @param state a {@link Byte}.
	 * @return DataPrivateConnectionReponse
	 */
	static DataPrivateConnectionReponse createDataPrivateConnectionReponse(StandardOperation opcode, byte step, String login, byte state) {
		Objects.requireNonNull(login);
		DataPrivateConnectionRequested d = createDataPrivateConnectionRequested(opcode, step, login);
		return new DataPrivateConnectionReponse(d, state);
	}

	/**
	 * Creates a DataPrivateConnectionConnect.
	 * @param opcode a {@link StandardOperation}.
	 * @param step a {@link Byte}.
	 * @param login a {@link String}.
	 * @param token a {@link Long}.
	 * @return DataPrivateConnectionConnect.
	 */
	static DataPrivateConnectionConnect createDataPrivateConnectionConnect(StandardOperation opcode, byte step, String login, long token) {
		Objects.requireNonNull(login);
		DataText loginData = new DataText(login);
		return new DataPrivateConnectionConnect(opcode, step, loginData, token);
	}

	/**
	 * Creates a DataPrivateConnectionClient.
	 * @param opcode a {@link StandardOperation}
	 * @param message a {@link String}
	 * @return DataPrivateMessage.
	 */
	static DataPrivateMessage createDataPrivateMessage(StandardOperation opcode,String login, String message) {
		Objects.requireNonNull(message);
		Objects.requireNonNull(login);
		DataText messageData = new DataText(message);
		DataText loginData = new DataText(login);
		return new DataPrivateMessage(opcode, loginData, messageData);
	}
	
	/**
	 * Creates a DataPrivateFile.
	 * @param opcode a {@link StandardOperation}.
	 * @param login a {@link String}.
	 * @param fileName a {@link String}.
	 * @param fileBuffer a {@link ByteBuffer}.
	 * @return DataPrivateFile.
	 */
	static DataPrivateFile createDataPrivateFile(StandardOperation opcode, String login, String fileName, ByteBuffer fileBuffer) {
		Objects.requireNonNull(fileName);
		Objects.requireNonNull(fileBuffer);
		DataText loginData = new DataText(login);
		DataText fileNameData = new DataText(fileName);
		return new DataPrivateFile(opcode, loginData, fileNameData, fileBuffer);
	}
	
	static DataAck createDataAck(StandardOperation opcode, StandardOperation requestCode) {
		return new DataAck(opcode, requestCode);
	}
}
