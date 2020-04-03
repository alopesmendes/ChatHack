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
			() -> assertThrows(NullPointerException.class, () -> Data.createDataText(null))
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

	
	private void TestSameDataError() {
		Data d1 = Data.createDataError(StandardOperation.ERROR, StandardOperation.GLOBAL_MESSAGE);
		Data d2 = Data.createDataError(StandardOperation.ERROR, StandardOperation.GLOBAL_MESSAGE);
		Data d3 = Data.createDataError(StandardOperation.ERROR, StandardOperation.GLOBAL_MESSAGE);
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
			() -> TestSameDataError()
		);
	}
}
