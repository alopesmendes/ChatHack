package fr.upem.net.tcp.frame;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * The Data will be use to stock valuable information and use it.<br>
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public interface Data {
	/**
	 * 	<p>The DataText will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>text a {@link String}</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataText implements Data {
		final String text;

		/**
		 * Constructs a DataText with a text.
		 * @param text a {@link String}.
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
	
	/**
	 * 	<p>The DataGlobalClient will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}</li>
	 * 		<li>login a {@link DataText}</li>
	 * 		<li>message a {@link DataText}</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataGlobalClient implements Data {
		final StandardOperation opcode;
		final DataText login;
		final DataText message;
		
		/**
		 * Constructs a DataGlobalClient with it's opcode, step and message.
		 * @param opcode a {@link StandardOperation}.
		 * @param login a {@link DataText}.
		 * @param message a {@link DataText}.
		 */
		private DataGlobalClient(StandardOperation opcode, DataText login, DataText message) {
			this.opcode = opcode;
			this.login = login;
			this.message = message;
		}
		
		@Override
		public int hashCode() {
			return opcode.hashCode() ^ login.hashCode() ^ message.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataGlobalClient)) {
				return false;
			}
			DataGlobalClient d = (DataGlobalClient)obj;
			return opcode==d.opcode && d.login.equals(login) && d.message.equals(message);
		}
		
		/**
		 * @return get login.
		 */
		public String login() {
			return login.text;
		}
		
		/**
		 * @return get message.
		 */
		public String message() {
			return message.text;
		}
		
	}
	
	/**
	 * 	<p>The DataError will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}</li>
	 * 		<li>requestCode a {@link StandardOperation}</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataError implements Data {
		final StandardOperation opcode;
		final StandardOperation requestCode;
		
		/**
		 * Constructs a DataError with it's opcode and requestCode.
		 * @param opcode a {@link StandardOperation}.
		 * @param requestCode a {@link StandardOperation}.
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
		
		/**
		 * @return get requestCode.
		 */
		public StandardOperation requestCode() {
			return requestCode;
		}
	}
	
	/**
	 * 	<p>The DataPrivateConnectionBase will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}</li>
	 * 		<li>step a byte</li>
	 * 		<li>firstClient a {@link DataText}</li>
	 * 		<li>secondClient a {@link DataText}</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataPrivateConnectionBase implements Data {
		final StandardOperation opcode;
		final byte step;
		final DataText firstClient;
		final DataText secondClient;
		
		/**
		 * Constructs a DataPrivateConnectionBase with it's opcode, step, firstClient and secondClient.
		 * @param opcode a {@link StandardOperation}.
		 * @param step a byte.
		 * @param firstClient a {@link DataText}.
		 * @param secondClient a {@link DataText}.
		 */
		private DataPrivateConnectionBase(StandardOperation opcode, byte step, DataText firstClient, DataText secondClient) {
			this.opcode = opcode;
			this.step = step;
			this.firstClient = firstClient;
			this.secondClient = secondClient;
		}
		
		@Override
		public int hashCode() {
			return opcode.hashCode() ^ Byte.hashCode(step) ^ secondClient.hashCode() ^ firstClient.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateConnectionBase)) {
				return false;
			}
			DataPrivateConnectionBase d = (DataPrivateConnectionBase)obj;
			return 	d.opcode==opcode && d.step==step 
					&& d.firstClient.equals(firstClient) && d.secondClient.equals(secondClient);
		}
		
		/**
		 * @return get step.
		 */
		public byte step() {
			return step;
		}
		
		/**
		 * @return get opcode.
		 */
		public StandardOperation opcode() {
			return opcode;
		}
		
		/**
		 * @return get secondClient.
		 */
		public String secondClient() {
			return secondClient.text;
		}
		
		/**
		 * @return get firstClient.
		 */
		public String firstClient() {
			return firstClient.text;
		}
	}
	
	/**
	 * 	<p>The DataPrivateConnectionRequested will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}</li>
	 * 		<li>step a byte</li>
	 * 		<li>firstClient a {@link DataText}</li>
	 * 		<li>secondClient a {@link DataText}</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataPrivateConnectionRequested extends DataPrivateConnectionBase {

		/**
		 * Constructs a DataPrivateConnectionRequested with it's opcode, step, firstClient and secondClient.
		 * @param opcode a {@link StandardOperation}.
		 * @param step a byte.
		 * @param firstClient a {@link DataText}.
		 * @param secondClient a {@link DataText}.
		 */
		private DataPrivateConnectionRequested(StandardOperation opcode, byte step, DataText firstClient, DataText secondClient) {
			super(opcode, step, firstClient, secondClient);
		}
		
	}
	
	/**
	 * 	<p>The DataPrivateConnectionReponse will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}.</li>
	 * 		<li>step a byte.</li>
	 * 		<li>firstClient a {@link DataText}.</li>
	 * 		<li>secondClient a {@link DataText}.</li>
	 * 		<li>state a byte.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataPrivateConnectionReponse extends DataPrivateConnectionBase {
		final byte state;
		
		/**
		 * Constructs a with it's opcode, step, firstClient, secondClient and state.
		 * @param opcode a {@link StandardOperation}.
		 * @param step a byte.
		 * @param firstClient a {@link DataText}.
		 * @param secondClient a {@link DataText}.
		 * @param state a byte.
		 */
		public DataPrivateConnectionReponse(StandardOperation opcode, byte step, DataText firstClient, DataText secondClient, byte state) {
			super(opcode, step, firstClient, secondClient);
			this.state = state;
		}
		
		/**
		 * @return get state.
		 */
		public byte state() {
			return state;
		}
		
	}
	
	/**
	 * 	<p>The DataPrivateConnectionAccepted will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode {@link StandardOperation}.</li>
	 * 		<li>step a byte.</li>
	 * 		<li>firstClient a {@link DataText}.</li>
	 * 		<li>secondClient a {@link DataText}.</li>
	 * 		<li>port a int.</li>
	 * 		<li>host a {@link DataText}.</li>
	 * 		<li>token a long.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataPrivateConnectionAccepted extends DataPrivateConnectionBase {
		final int port;
		final DataText host;
		final long token;
		
		/**
		 * Constructs a with it's opcode, step, firstClient, secondClient, port, host and token.
		 * @param opcode {@link StandardOperation}.
		 * @param step a byte.
		 * @param firstClient a {@link DataText}.
		 * @param secondClient a {@link DataText}.
		 * @param port a int.
		 * @param host a {@link DataText}.
		 * @param token a long.
		 */
		private DataPrivateConnectionAccepted(StandardOperation opcode, byte step,  DataText firstClient, DataText secondClient, int port, DataText host,
				long token) {
			super(opcode, step, firstClient, secondClient);
			this.host = host;
			this.token = token;
			this.port = port;
		}
		
		
		/**
		 * @return get port.
		 */
		public int port() {
			return port;
		}
		
		/**
		 * @return get host.
		 */
		public String host() {
			return host.text;
		}
		
		/**
		 * @return get token.
		 */
		public long token() {
			return token;
		}
	}
	
	/**
	 * 	<p>The DataConnectionClient will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}.</li>
	 * 		<li>connexion a byte.</li>
	 * 		<li>login a {@link DataText}.</li>
	 * 		<li>password a {@link Optional} of {@link DataText}.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataConnectionClient implements Data {
		final StandardOperation opcode;
		final byte connexion;
		final DataText login;
		final Optional<DataText> password;
		/**
		 * Constructs a DataConnectionClient with it's opcode, connexion, login and password.
		 * @param opcode a {@link StandardOperation}.
		 * @param connexion a byte.
		 * @param login a {@link DataText}.
		 * @param password a {@link Optional} of {@link DataText}.
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
		
		/**
		 * @return get login.
		 */
		public String login() {
			return login.text;
		}
	}
	
	/**
	 * 	<p>The DataConnectionServerMdp will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>typeConnexion a byte.</li>
	 * 		<li>id a long.</li>
	 * 		<li>login a {@link DataText}.</li>
	 * 		<li>password a {@link Optional} of {@link DataText}.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataConnectionServerMdp implements Data {
		final byte typeConnexion;
		final long id;
		final DataText login;
		final Optional<DataText> password;
		
		/**
		 * Constructs a DataConnectionServerMdp woth ot's typeConnexion, id, login and password.
		 * @param typeConnexion a byte.
		 * @param id a long.
		 * @param login a {@link DataText}.
		 * @param password a {@link Optional} of {@link DataText}.
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
		
		/**
		 * @return get typeConnexion.
		 */
		public byte connexion() {
			return typeConnexion;
		}
		
		/**
		 * @return get id.
		 */
		public long getId() {
			return id;
		}
		
	}
	
	/**
	 * 	<p>The DataConnectionServerMdpReponse will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a byte</li>
	 * 		<li>id a long</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
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
		
		/**
		 * @return get opcode.
		 */
		public byte getOpcode() {
			return opcode;
		}
		
		/**
		 * @return get id.
		 */
		public long getId() {
			return id;
		}
		
	}
	
	/**
	 * 	<p>The DataPrivateMessage will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}.</li>
	 * 		<li>login a {@link DataText}.</li>
	 * 		<li>message a {@link DataText}.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataPrivateMessage implements Data {
		final StandardOperation opcode;
		final DataText login;
		final DataText message;
		
		/**
		 * Constructs a DataPrivateMessage with it's opcode and message.
		 * @param opcode a {@link StandardOperation}.
		 * @param login a {@link DataText}.
		 * @param message a {@link DataText}.
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
		
		/**
		 * @return get message.
		 */
		public String message() {
			return message.text;
		}
		
		/**
		 * @return get login.
		 */
		public String login() {
			return login.text;
		}
		
	}
	
	/**
	 * 	<p>The DataPrivateFile will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}.</li>
	 * 		<li>login a {@link DataText}.</li>
	 * 		<li>fileName a {@link DataText}.</li>
	 * 		<li>buff a {@link ByteBuffer}.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataPrivateFile implements Data {
		final StandardOperation opcode;
		final DataText login;
		final DataText fileName;
		final ByteBuffer buff;
		
		/**
		 * Constructs a DataPrivateFile with it's opcode, fileName, size and buff.
		 * @param opcode a {@link StandardOperation}.
		 * @param login a {@link DataText}.
		 * @param fileName a {@link DataText}.
		 * @param buff a {@link ByteBuffer}.
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
		
		/**
		 * @return get login.q
		 */
		public String login() {
			return login.text;
		}
		
		/**
		 * @return get fileName.
		 */
		public String fileName() {
			return fileName.text;
		}
		
		/**
		 * @return get buff.
		 */
		public ByteBuffer buffer() {
			return buff;
		}
		
		
	}
	
	/**
	 * 	<p>The DataAck will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}.</li>
	 * 		<li>requestCode a {@link StandardOperation}.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataAck implements Data {
		final StandardOperation opcode;
		final StandardOperation requestCode;
		
		/**
		 * Constructs a DataAck with is's opcode and requestCode.
		 * @param opcode a {@link StandardOperation}.
		 * @param requestCode a {@link StandardOperation}.
		 */
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
		
		/**
		 * @return get requestCode.
		 */
		public StandardOperation request() {
			return requestCode;
		}
	}
	
	/**
	 * 	<p>The DataPrivateAck will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}.</li>
	 *		<li>requestCode a {@link StandardOperation}.</li>
	 * 		<li>login a {@link DataText}.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataPrivateAck extends DataAck {
		final DataText login;
		
		/**
		 * Constructs a DataPrivateAck with it's opcode, requestCode and login.
		 * @param opcode a {@link StandardOperation}.
		 * @param requestCode a {@link StandardOperation}.
		 * @param login a {@link DataText}.
		 */
		private DataPrivateAck(StandardOperation opcode, StandardOperation requestCode, DataText login) {
			super(opcode, requestCode);
			this.login = login;
		}
		
		@Override
		public int hashCode() {
			return super.hashCode() ^ login.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataPrivateAck)) {
				return false;
			}
			DataPrivateAck d = (DataPrivateAck)obj;
			return super.equals(obj) && d.login.equals(login);
		}
		
		/**
		 * @return get login.
		 */
		public String login() {
			return login.text;
		}
	}
	
	/**
	 * 	<p>The DataDeconnexion will be use to stock the follow information :</p>
	 * 	<ul>
	 * 		<li>opcode a {@link StandardOperation}.</li>
	 * 		<li>login a {@link DataText}.</li>
	 * 	</ul>
	 * 	@author LOPES MENDES Ailton
	 * 	@author LAMBERT--DELAVAQUERIE Fabien
	 */
	static class DataLogout implements Data {
		final StandardOperation opcode;
		final DataText login;
		
		/**
		 * Constructs a DataDeconnexion with it's opcode and login.
		 * @param opcode a {@link StandardOperation}.
		 * @param login a {@link DataText}.
		 */
		private DataLogout(StandardOperation opcode, DataText login) {
			this.opcode = opcode;
			this.login = login;
		}
		
		@Override
		public int hashCode() {	
			return opcode.hashCode() ^ login.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataLogout)) {
				return false;
			}
			DataLogout d = (DataLogout)obj;
			return d.opcode==opcode && d.login.equals(login);
		}
		
		/**
		 * @return get login.
		 */
		public String login() {
			return login.text;
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
	 * Creates a DataGlobalClient.
	 * @param opcode a {@link StandardOperation}.
	 * @param login a {@link String}.
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
	 * @param requestCode a {@link StandardOperation}.
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
	 * @param password a {@link Optional} of {@link String}.
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
	 * @param opcode a byte.
	 * @param id a long.
	 * @return DataConnectionServerMdpReponse.
	 */
	static DataConnectionServerMdpReponse createDataConnectionServerMdpReponse(byte opcode, long id) {
		return new DataConnectionServerMdpReponse(opcode, id);
		
	}

	/**
	 * Creates a DataPrivateConnectionRequested.
	 * @param opcode a {@link StandardOperation}.
	 * @param step a byte.
	 * @param firstClient a {@link String}.
	 * @param secondClient a {@link String}.
	 * @return DataPrivateConnectionRequested.
	 */
	static DataPrivateConnectionRequested createDataPrivateConnectionRequested(StandardOperation opcode, byte step, String firstClient, String secondClient) {
		Objects.requireNonNull(secondClient);
		Objects.requireNonNull(firstClient);
		DataText firstClientData = new DataText(firstClient);
		DataText secondClientData = new DataText(secondClient);
		return new DataPrivateConnectionRequested(opcode, step, firstClientData, secondClientData);
	}

	/**
	 * Creates a DataPrivateConnectionAccepted.
	 * @param opcode a {@link StandardOperation}.
	 * @param step a byte.
	 * @param firstClient a {@link String}.
	 * @param secondClient a {@link String}.
	 * @param port a int.
	 * @param socketAdress a {@link String}.
	 * @param token a long.
	 * @return DataPrivateConnectionAccepted.
	 */
	static DataPrivateConnectionAccepted createDataPrivateConnectionAccepted(StandardOperation opcode, byte step, String firstClient, String secondClient, int port, String socketAdress, long token) {
		Objects.requireNonNull(firstClient);
		Objects.requireNonNull(socketAdress);
		DataText firstClientData = new DataText(firstClient);
		DataText secondClientData = new DataText(secondClient);
		DataText socketData = new DataText(socketAdress);
		return new DataPrivateConnectionAccepted(opcode, step, firstClientData, secondClientData, port, socketData, token);
	}

	/**
	 * Creates a DataPrivateConnectionReponse.
	 * @param opcode a {@link StandardOperation}.
	 * @param step a byte.
	 * @param firstClient a {@link String}.
	 * @param secondClient a {@link String}.
	 * @param state a byte.
	 * @return DataPrivateConnectionReponse
	 */
	static DataPrivateConnectionReponse createDataPrivateConnectionReponse(StandardOperation opcode, byte step, String firstClient, String secondClient, byte state) {
		Objects.requireNonNull(secondClient);
		DataText firstClientData = new DataText(firstClient);
		DataText secondClientData = new DataText(secondClient);
		return new DataPrivateConnectionReponse(opcode, step, firstClientData, secondClientData, state);
	}

	/**
	 * Creates a DataPrivateConnectionClient.
	 * @param opcode a {@link StandardOperation}.
	 * @param login a {@link String}.
	 * @param message a {@link String}.
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
	
	/**
	 * Creates a DataAck.
	 * @param opcode a {@link StandardOperation}.
	 * @param requestCode a {@link StandardOperation}.
	 * @return DataAck.
	 */
	static DataAck createDataAck(StandardOperation opcode, StandardOperation requestCode) {
		return new DataAck(opcode, requestCode);
	}
	
	/**
	 * Creates a DataPrivateAck.
	 * @param opcode a {@link StandardOperation}.
	 * @param requestCode a {@link StandardOperation}.
	 * @param login a {@link String}.
	 * @return DataPrivateAck.
	 */
	static DataPrivateAck createDataPrivateAck(StandardOperation opcode, StandardOperation requestCode, String login) {
		Objects.requireNonNull(login);
		DataText loginData = new DataText(login);
		return new DataPrivateAck(opcode, requestCode, loginData);
	}
	
	/**
	 * Creates a DataLogout.
	 * @param opcode a {@link StandardOperation}.
	 * @param login a {@link String}.
	 * @return DataLogout.
	 */
	static DataLogout createDataLogout(StandardOperation opcode, String login) {
		Objects.requireNonNull(login);
		DataText loginData = new DataText(login);
		return new DataLogout(opcode, loginData);
	}
}
