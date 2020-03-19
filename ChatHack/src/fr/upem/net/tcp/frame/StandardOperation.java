package fr.upem.net.tcp.frame;

public enum StandardOperation {
	CONNEXION(0), GLOBAL_MESSAGE(1), PRIVATE_CONNEXION(2), PRIVATE_MESSAGE(3),
	PRIVATE_FILE(4), DECONNEXION(5), ACK(6), ERROR(7);
	
	private final int opcode;

	/**
	 * @param opcode
	 */
	private StandardOperation(int opcode) {
		this.opcode = opcode;
	}
	
	public byte opcode() {
		return (byte)opcode;
	}
	
	
}
