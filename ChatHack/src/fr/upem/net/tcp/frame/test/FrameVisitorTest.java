package fr.upem.net.tcp.frame.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.upem.net.tcp.frame.Data;
import fr.upem.net.tcp.frame.Frame;
import fr.upem.net.tcp.frame.FrameVisitor;

public class FrameVisitorTest {
	
	
	@Test
	void testSameFrameVisitor() {
		FrameVisitor fv1 = new FrameVisitor();
		FrameVisitor fv2 = new FrameVisitor();
		FrameVisitor fv3 = new FrameVisitor();
		
		fv1.when(Data.DataGlobal.class, d -> Frame.createFrameGlobal(d));
		fv2.when(Data.DataGlobal.class, d -> Frame.createFrameGlobal(d));
		fv3.when(Data.DataGlobal.class, d -> Frame.createFrameGlobal(d));
		
		
		fv1.when(Data.DataError.class, d -> Frame.createFrameError(d));
		fv2.when(Data.DataError.class, d -> Frame.createFrameError(d));
		fv3.when(Data.DataError.class, d -> Frame.createFrameError(d));
		assertAll(
			() -> assertEquals(fv1, fv1),
			() -> assertEquals(fv1, fv2),
			() -> assertEquals(fv2, fv3),
			() -> assertEquals(fv1, fv3)
		);
		
	}
	
	
}
