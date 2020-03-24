package fr.upem.net.tcp.frame.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.StandardOperation;

class DataTest {
	@Test
	void TestCreationParametersShouldNotBeNull() {
		assertAll(
				() -> assertThrows(NullPointerException.class, () -> Data.createDataText(null)),
				() -> assertThrows(NullPointerException.class, () -> Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, (byte)0, null, null)),
				() -> assertThrows(NullPointerException.class, () -> Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, (byte)0, null, "")),
				() -> assertThrows(NullPointerException.class, () -> Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, (byte)0, "", null))
				);
	}

	private void TestSameDataText() {
		Data d1 = Data.createDataText("bonjour");
		Data d2 = Data.createDataText("bonjour");
		Data d3 = Data.createDataText("bonjour");
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}

	private void TestSameDataGlobal() {
		Data d1 = Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, (byte)2, "Will", "bonjour");
		Data d2 = Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, (byte)2, "Will", "bonjour");
		Data d3 = Data.createDataGlobalServer(StandardOperation.GLOBAL_MESSAGE, (byte)2, "Will", "bonjour");
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}

	private void TestSameDataError() {
		Data d1 = Data.createDataError(StandardOperation.ERROR, (byte)1);
		Data d2 = Data.createDataError(StandardOperation.ERROR, (byte)1);
		Data d3 = Data.createDataError(StandardOperation.ERROR, (byte)1);
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}

	private void TestSameDataGlobalClient() {
		Data d1 = Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, (byte)2, "Ici");
		Data d2 = Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, (byte)2, "Ici");
		Data d3 = Data.createDataGlobalClient(StandardOperation.GLOBAL_MESSAGE, (byte)2, "Ici");
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}

	private void TestSameDataConnectionServerMdp() {
		Data d1 = Data.createDataConnectionServerMdp(Data.createDataConnectionClient(StandardOperation.CONNEXION, (byte)1, "Will", Optional.of("lol")));
		Data d2 = Data.createDataConnectionServerMdp(Data.createDataConnectionClient(StandardOperation.CONNEXION, (byte)1, "Will", Optional.of("lol")));
		Data d3 = Data.createDataConnectionServerMdp(Data.createDataConnectionClient(StandardOperation.CONNEXION, (byte)1, "Will", Optional.of("lol")));
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}

	private void TestSameDataConnectionClient() {
		Data d1 = Data.createDataConnectionClient(StandardOperation.CONNEXION, (byte)1, "Bob", Optional.of("man"));
		Data d2 = Data.createDataConnectionClient(StandardOperation.CONNEXION, (byte)1, "Bob", Optional.of("man"));
		Data d3 = Data.createDataConnectionClient(StandardOperation.CONNEXION, (byte)1, "Bob", Optional.of("man"));
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}
	
	private void TestSameDataConnectionServerMdpReponse() {
		Data d1 = Data.createDataConnectionServerMdpReponse((byte)0, 1);
		Data d2 = Data.createDataConnectionServerMdpReponse((byte)0, 1);
		Data d3 = Data.createDataConnectionServerMdpReponse((byte)0, 1);
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}
	
	private void TestSameDataPrivateConnectionAccepted() {
		Data d1 = Data.createDataPrivateConnectionAccepted(StandardOperation.PRIVATE_CONNEXION, (byte)5, "Will", 7777, "localhost", 1);
		Data d2 = Data.createDataPrivateConnectionAccepted(StandardOperation.PRIVATE_CONNEXION, (byte)5, "Will", 7777, "localhost", 1);
		Data d3 = Data.createDataPrivateConnectionAccepted(StandardOperation.PRIVATE_CONNEXION, (byte)5, "Will", 7777, "localhost", 1);
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}
	
	private void TestSameDataPrivateConnectionReponse() {
		Data d1 = Data.createDataPrivateConnectionReponse(StandardOperation.PRIVATE_CONNEXION, (byte)4, "Bob", (byte)0);
		Data d2 = Data.createDataPrivateConnectionReponse(StandardOperation.PRIVATE_CONNEXION, (byte)4, "Bob", (byte)0);
		Data d3 = Data.createDataPrivateConnectionReponse(StandardOperation.PRIVATE_CONNEXION, (byte)4, "Bob", (byte)0);
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}
	
	private void TestSameDataPrivateConnectionRequested() {
		Data d1 = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, (byte)1, "alice");
		Data d2 = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, (byte)1, "alice");
		Data d3 = Data.createDataPrivateConnectionRequested(StandardOperation.PRIVATE_CONNEXION, (byte)1, "alice");
		assertAll(
				() -> assertEquals(d1, d1),
				() -> assertEquals(d1, d2),
				() -> assertEquals(d2, d3),
				() -> assertEquals(d1, d3)
				);
	}

	@Test
	void TestSameData() {
		assertAll(
				() -> TestSameDataText(),
				() -> TestSameDataGlobal(),
				() -> TestSameDataError(),
				() -> TestSameDataGlobalClient(),
				() -> TestSameDataConnectionServerMdp(),
				() -> TestSameDataConnectionClient(),
				() -> TestSameDataConnectionServerMdpReponse(),
				() -> TestSameDataPrivateConnectionAccepted(),
				() -> TestSameDataPrivateConnectionReponse(),
				() -> TestSameDataPrivateConnectionRequested()
				);
	}
}
