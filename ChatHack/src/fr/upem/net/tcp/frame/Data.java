package fr.upem.net.tcp.frame;

import java.util.Objects;

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
	
	static class DataGlobal implements Data {
		final byte ack;
		final byte step;
		final DataText pseudo;
		final DataText message;
		
		/**
		 * Constructs a DataGlobal with it's ack, step, pseudo and message.
		 * @param ack a byte.
		 * @param step a byte.
		 * @param pseudo a DataText.
		 * @param message a DataText.
		 */
		private DataGlobal(byte ack, byte step, DataText pseudo, DataText message) {
			this.ack = ack;
			this.step = step;
			this.pseudo = pseudo;
			this.message = message;
		}
		
		@Override
		public int hashCode() {
			
			return Byte.hashCode(ack) ^ Byte.hashCode(step) ^ pseudo.hashCode() ^ message.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataGlobal)) {
				return false;
			}
			DataGlobal d = (DataGlobal)obj;
			return 	ack==d.ack && step==d.step 
					&& pseudo.equals(d.pseudo) && message.equals(d.message);
		}
	}
	
	static class DataError implements Data {
		final byte opcode;
		final byte requestCode;
		/**
		 * Constructs a DataError with it's opcode and requestCode.
		 * @param opcode
		 * @param requestCode
		 */
		private DataError(byte opcode, byte requestCode) {
			this.opcode = opcode;
			this.requestCode = requestCode;
		}
		
		@Override
		public int hashCode() {
			return Byte.hashCode(opcode) ^ Byte.hashCode(requestCode);
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
	 * @return DataGlobal.
	 */
	static Data createDataGlobal(byte ack, byte step, String pseudo, String message) {
		Objects.requireNonNull(pseudo);
		Objects.requireNonNull(message);
		DataText pseudoData = new DataText(pseudo);
		DataText messageData = new DataText(message);
		return new DataGlobal(ack, step, pseudoData, messageData);
	}
	
	/**
	 * Creates a DataError.
	 * @param opcode
	 * @param requestCode
	 * @return DataError.
	 */
	static Data createDataError(byte opcode, byte requestCode) {
		return new DataError(opcode, requestCode);
	}
	
}
