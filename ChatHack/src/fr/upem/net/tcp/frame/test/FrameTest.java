package fr.upem.net.tcp.frame.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.frame.StandardOperation;

class FrameTest {

	@Test
	void TestFrameGlobalClient() {
		byte opcode = StandardOperation.GLOBAL_MESSAGE.opcode();
		byte step = (byte)1;
		String message = "Test Global";
		ByteBuffer bb = Frame.createFrameGlobal(Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, step, message)).buffer();
		int fLimit = bb.limit();
		var op = bb.get();
		var st= bb.get();
		int size = bb.getInt();
		bb.limit(bb.position()+size);
		String m = StandardCharsets.UTF_8.decode(bb).toString();
		bb.limit(fLimit);
		assertAll(
				() -> assertEquals(opcode, op),
				() -> assertEquals(step, st),
				() -> assertEquals(message, m)
				);
	}

	@Test
	void TestFrameGlobalServer() {
		byte opcode = StandardOperation.GLOBAL_MESSAGE.opcode();
		byte step = (byte)1;
		String login = "Alice";
		String message = "Test Global";
		ByteBuffer bb = Frame.createFrameGlobal(Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, step, login, message)).buffer();
		int fLimit = bb.limit();
		var op = bb.get();
		var st= bb.get();
		int size = bb.getInt();
		bb.limit(bb.position()+size);
		String l = StandardCharsets.UTF_8.decode(bb).toString();
		bb.limit(fLimit);
		size = bb.getInt();
		bb.limit(bb.position()+size);
		String m = StandardCharsets.UTF_8.decode(bb).toString();
		bb.limit(fLimit);
		assertAll(
				() -> assertEquals(opcode, op),
				() -> assertEquals(step, st),
				() -> assertEquals(login, l),
				() -> assertEquals(message, m)
				);
	}
	
	@Test
	void TestFrameConnect() {
		byte opcode = StandardOperation.CONNEXION.opcode();
		byte connexion = (byte)2;
		String login = "Alice";
		Optional<String> password = Optional.of("bob");
		ByteBuffer bb = Frame.createFrameConnection(Data.createDataConnectionClient(StandardOperation.CONNEXION, connexion, login, password)).buffer();
		int fLimit = bb.limit();
		byte op = bb.get();
		byte co = bb.get();
		int size = bb.getInt();
		bb.limit(bb.position()+size);
		String l = StandardCharsets.UTF_8.decode(bb).toString();
		bb.limit(fLimit);
		size = bb.getInt();
		bb.limit(bb.position()+size);
		String p = StandardCharsets.UTF_8.decode(bb).toString();
		bb.limit(fLimit);
		assertAll(
				() -> assertEquals(opcode, op),
				() -> assertEquals(connexion, co),
				() -> assertEquals(login, l),
				() -> assertEquals(password.get(), p)
				);
	}
	
	@Test
	void TestFrameConnectionClient() {
		byte connexion = (byte)1;
		String login = "Alice";
		String password= "Bob";
		ByteBuffer bb = Frame.createFrameConnectionMdp(
		Data.createDataConnectionServerMdp(
		Data.createDataConnectionClient(StandardOperation.CONNEXION, connexion, login, Optional.of(password)))).buffer();
		int fLimit = bb.limit();
		byte co = bb.get();
		bb.getLong();
		int size = bb.getInt();
		bb.limit(bb.position()+size);
		String l = StandardCharsets.UTF_8.decode(bb).toString();
		bb.limit(fLimit);
		size = bb.getInt();
		bb.limit(bb.position()+size);
		String p = StandardCharsets.UTF_8.decode(bb).toString();
		bb.limit(fLimit);
		assertAll(
				() -> assertEquals((byte)2, co),
				() -> assertEquals(login, l),
				() -> assertEquals(password, p)
				);
	}

}
