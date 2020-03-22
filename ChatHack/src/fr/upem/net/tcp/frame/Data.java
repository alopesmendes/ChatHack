package fr.upem.net.tcp.frame;

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
	}
	
	static class DataGlobalClient implements Data {
		final StandardOperation opcode;
		final byte step;
		final DataText message;
		
		/**
		 * Constructs a DataGlobalClient with it's opcode, step and message.
		 * @param opcode a StandardOperation
		 * @param step a byte
		 * @param message a DataText
		 */
		private DataGlobalClient(StandardOperation opcode, byte step, DataText message) {
			this.opcode = opcode;
			this.step = step;
			this.message = message;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode())^Byte.hashCode(step)^message.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataGlobalClient)) {
				return false;
			}
			DataGlobalClient d = (DataGlobalClient)obj;
			return step==d.step && opcode==d.opcode && d.message.equals(message);
		}
		
		public DataGlobalServer transformTo(String pseudo) {
			Objects.requireNonNull(pseudo);
			DataText pseudoData = new DataText(pseudo);
			return new DataGlobalServer(opcode, (byte)(step+1), pseudoData, message);
		}
	}
	
	static class DataError implements Data {
		final StandardOperation opcode;
		final byte requestCode;
		
		/**
		 * Constructs a DataError with it's opcode and requestCode.
		 * @param opcode
		 * @param requestCode
		 */
		private DataError(StandardOperation opcode, byte requestCode) {
			this.opcode = opcode;
			this.requestCode = requestCode;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ Byte.hashCode(requestCode);
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
		final DataText loginRequest;
		
		/**
		 * Constructs a DataPrivateConnectionRequested with it's opcode and loginRequest.
		 * @param opcode a StandardOperation
		 * @param loginRequest a DataText
		 */
		private DataPrivateConnectionRequested(StandardOperation opcode, DataText loginRequest) {
			this.opcode = opcode;
			this.loginRequest = Objects.requireNonNull(loginRequest);
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode()) ^ loginRequest.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionRequested)) {
				return false;
			}
			DataPrivateConnectionRequested d = (DataPrivateConnectionRequested)obj;
			return d.opcode==opcode && loginRequest.equals(d.loginRequest);
		}
	}
	
	static class DataPrivateConnectionAccepted implements Data {
		final StandardOperation opcode;
		final DataText loginReceiver;
		final DataText socketAdress;
		final long token;
		
		/**
		 * Constructs a DataPrivateConnectionAccepted with it's opcode, loginReceiver, socketAdress and token.
		 * @param opcode a StandardOperation.
		 * @param loginReceiver a DataText.
		 * @param socketAdress a DataText.
		 * @param token a long.
		 */
		private DataPrivateConnectionAccepted(StandardOperation opcode, DataText loginReceiver, DataText socketAdress,
				long token) {
			this.opcode = opcode;
			this.loginReceiver = loginReceiver;
			this.socketAdress = socketAdress;
			this.token = token;
		}
		
		@Override
		public int hashCode() {
			return 	Byte.hashCode(opcode.opcode())^loginReceiver.hashCode()^socketAdress.hashCode()
					^Long.hashCode(token);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionAccepted)) {
				return false;
			}
			DataPrivateConnectionAccepted d = (DataPrivateConnectionAccepted)obj;
			return 	d.opcode==opcode && token==d.token 
					&& loginReceiver.equals(d.loginReceiver) && socketAdress.equals(d.socketAdress);
		}	
	}

	static class DataPrivateConnectionRejected implements Data {
		final StandardOperation opcode;
		final DataText loginReceiver;
		/**
		 * Constructs a DataPrivateConnectionRejected with it's opcode and loginReceiver.
		 * @param opcode a StandardOperation 
		 * @param loginReceiver a DataText
		 */
		private DataPrivateConnectionRejected(StandardOperation opcode, DataText loginReceiver) {
			this.opcode = opcode;
			this.loginReceiver = Objects.requireNonNull(loginReceiver);
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode.opcode())^loginReceiver.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionRequested)) {
				return false;
			}
			DataPrivateConnectionRejected d = (DataPrivateConnectionRejected) obj;
			return opcode==d.opcode && loginReceiver.equals(d.loginReceiver);
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
		
	}
	
	/**
	 * Creates a DataText.
	 * @param text a string
	 * @return DataText.
	 */
	static Data createDataText(String text) {
		Objects.requireNonNull(text);
		return new DataText(text);
	}
	
	/**
	 * Creates a DataGlobal.
	 * @param ack a byte
	 * @param step a byte
	 * @param pseudo a string
	 * @param message a string
	 * @return DataGlobalServer.
	 */
	static Data createDataGlobalServer(StandardOperation opcode, byte step, String pseudo, String message) {
		Objects.requireNonNull(pseudo);
		Objects.requireNonNull(message);
		DataText pseudoData = new DataText(pseudo);
		DataText messageData = new DataText(message);
		return new DataGlobalServer(opcode, step, pseudoData, messageData);
	}
	
	/**
	 * Creates a DataGlobalClient.
	 * @param opcode a StandardOperation
	 * @param step a byte
	 * @param message a string
	 * @return DataGlobalClient.
	 */
	static Data createDataGlobalClient(StandardOperation opcode, byte step, String message) {
		Objects.requireNonNull(message);
		DataText messageData = new DataText(message);
		return new DataGlobalClient(opcode, step, messageData);
	}
	/**
	 * Creates a DataError.
	 * @param opcode
	 * @param requestCode
	 * @return DataError.
	 */
	static Data createDataError(StandardOperation opcode, byte requestCode) {
		return new DataError(opcode, requestCode);
	}
	
	
	/**
	 * Creates a DataPrivateConnectionRequested. 
	 * @param opcode a StandardOperation.
	 * @param loginRequest a String.
	 * @return DataPrivateConnectionRequested
	 */
	static Data createDataPrivateConnectionRequested(StandardOperation opcode, String loginRequest) {
		Objects.requireNonNull(loginRequest);
		DataText dataLogin = new DataText(loginRequest);
		return new DataPrivateConnectionRequested(opcode, dataLogin);
	}
	
	/**
	 * Creates a DataPrivateConnectionAccepted.
	 * @param opcode a StandardOperation.
	 * @param loginReceiver a String.
	 * @param socketAdress a String.
	 * @param token a long.
	 * @return DataPrivateConnectionAccepted.
	 */
	static Data createDataPrivateConnectionAccepted(StandardOperation opcode, String loginReceiver, String socketAdress, long token) {
		Objects.requireNonNull(loginReceiver);
		Objects.requireNonNull(socketAdress);
		DataText dLogin = new DataText(loginReceiver);
		DataText dSocket = new DataText(socketAdress);
		return new DataPrivateConnectionAccepted(opcode, dLogin, dSocket, token);
	}
	
	/**
	 * Creates a DataPrivateConnectionRejected.
	 * @param opcode a StandardOperation.
	 * @param loginReceived a String.
	 * @return DataPrivateConnectionRejected.
	 */
	static Data createDataPrivateConnectionRejected(StandardOperation opcode, String loginReceived) {
		Objects.requireNonNull(loginReceived);
		DataText dataLogin = new DataText(loginReceived);
		return new DataPrivateConnectionRejected(opcode, dataLogin);
	}
	
	static Data createDataConnectionClient(StandardOperation opcode, byte connexion, String login, Optional<String> password) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(password);
		DataText dataLogin = new DataText(login);
		Optional<DataText> dataPasssword = password.isEmpty() ? Optional.empty() : Optional.of(new DataText(password.get()));
		return new DataConnectionClient(opcode, connexion, dataLogin, dataPasssword);
	}
}
