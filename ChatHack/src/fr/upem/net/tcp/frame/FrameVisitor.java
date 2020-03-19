package fr.upem.net.tcp.frame;

import java.util.HashMap;
import java.util.function.Function;

public class FrameVisitor {
	private final HashMap<Class<? extends Data>, Function<Data, ? extends Frame>> map = new HashMap<>();

	/**
	 * Add an function according to a class.
	 * @param <T>
	 * @param type
	 * @param fun
	 * @return this.
	 */
	public <T extends Data> FrameVisitor when(Class<? extends T> type, Function<? super T, ? extends Frame> fun) {
		map.put(type, fun.compose(type::cast));
		return this;
	}
	
	/**
	 * Apply the function saved to the receiver class and apply's it.
	 * @param receiver
	 * @return R
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
	
	
	/**
	 * Same keySet.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FrameVisitor)) {
			return false;
		}
		FrameVisitor fv = (FrameVisitor)obj;
		return map.keySet().equals(fv.map.keySet());
	}
}
