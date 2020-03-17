package fr.upem.net.tcp.frame;

import java.util.HashMap;
import java.util.function.Function;

public class FrameVisitor<U, R> {
	private final HashMap<Class<? extends U>, Function<U, ? extends R>> map = new HashMap<>();

	/**
	 * @param <T>
	 * @param type
	 * @param fun
	 * @return 
	 */
	public <T extends U> FrameVisitor<U, R> when(Class<? extends T> type, Function<? super T, ? extends R> fun) {
		map.put(type, fun.compose(type::cast));
		return this;
	}
	
	/**
	 * @param receiver
	 * @return
	 */
	public R call(U receiver) {
		return map.getOrDefault(receiver.getClass(),
				obj -> { throw new IllegalArgumentException("invalid " + obj); })
				.apply(receiver);
	}
}
