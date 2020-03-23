package fr.upem.net.tcp.frame;

public enum StandardOperation {
	CONNEXION(3), GLOBAL_MESSAGE(4), PRIVATE_CONNEXION(5), PRIVATE_MESSAGE(6),
	PRIVATE_FILE(7), DECONNEXION(8), ACK(9), ERROR(10);
	
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
