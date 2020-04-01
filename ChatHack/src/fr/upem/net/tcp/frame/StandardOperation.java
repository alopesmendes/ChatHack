package fr.upem.net.tcp.frame;

import java.util.Optional;

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
	
	public static Optional<StandardOperation> convert(byte code) {
		if (CONNEXION.opcode() == code) {
			return Optional.of(CONNEXION);
		} else if (GLOBAL_MESSAGE.opcode == code) {
			return Optional.of(GLOBAL_MESSAGE);
		} else if (PRIVATE_CONNEXION.opcode == code) {
			return Optional.of(PRIVATE_CONNEXION);
		} else if (PRIVATE_MESSAGE.opcode == code) {
			return Optional.of(PRIVATE_MESSAGE);
		} else if (PRIVATE_FILE.opcode == code) {
			return Optional.of(PRIVATE_FILE);
		} else if (DECONNEXION.opcode == code) {
			return Optional.of(DECONNEXION);
		} else if (ACK.opcode == code) {
			return Optional.of(ACK);
		} else if (ERROR.opcode == code) {
			return Optional.of(ERROR);
		} else {
			return Optional.empty();
		}
	}
	
}
