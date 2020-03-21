package fr.upem.net.tcp.frame.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
	
	@Test
	void TestSameData() {
		assertAll(
			() -> TestSameDataText(),
			() -> TestSameDataGlobal(),
			() -> TestSameDataError()
		);
	}
}
