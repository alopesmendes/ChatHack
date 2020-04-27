package fr.upem.net.tcp.frame;

import java.util.HashMap;
import java.util.function.Function;

/**
 * <p>The FrameVisitor will be our visitor.</p>
 * It will be a generic visitor so the user as to register how to treat a data according to it's class first.
 * Then call the {@link Data} Object. 
 * Every {@link Data} will return a {@link Frame}.
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public class FrameVisitor {
	private final HashMap<Class<? extends Data>, Function<Data, ? extends Frame>> map = new HashMap<>();

	/**
	 * Add an function according to a class.
	 * @param <T> a T
	 * @param type a {@link Class}.
	 * @param fun a {@link Function} of {@link Class} and {@link Frame}..
	 * @return this.
	 */
	public <T extends Data> FrameVisitor when(Class<? extends T> type, Function<? super T, ? extends Frame> fun) {
		map.put(type, fun.compose(type::cast));
		return this;
	}
	
	/**
	 * Apply the function saved to the receiver class and apply's it.
	 * @param receiver a {@link Data}.
	 * @return a {@link Frame}.
	 */
	public Frame call(Data receiver) {
		return map.getOrDefault(receiver.getClass(),
				obj -> { throw new IllegalArgumentException("invalid " + obj); })
				.apply(receiver);
	}
	
	
	@Override
	public int hashCode() {
		return map.hashCode();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FrameVisitor)) {
			return false;
		}
		FrameVisitor fv = (FrameVisitor)obj;
		return map.keySet().equals(fv.map.keySet());
	}
}
