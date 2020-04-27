package fr.upem.net.tcp.reader;

/**
 * <p>
 * The Reader will be use to process and return a content.
 * </p>
 * @author LOPES MENDES Ailton
 * @author LAMBERT--DELAVAQUERIE Fabien
 */
public interface Reader<E> {
	/**
	 * <p>
	 * The ProcessStatus has 3 different types DONE, REFILL and ERROR.
	 * </p>
	 * @author LOPES MENDES Ailton
	 * @author LAMBERT--DELAVAQUERIE Fabien
	 */
	public static enum ProcessStatus {
		DONE, REFILL, ERROR;
	}
	
	/**
	 * Will process our reader.
	 * If the processing is over returns DONE.
	 * REFILL if it's not done.
	 * ERROR if there's an error while processing.
	 * @return {@link ProcessStatus}.
	 */
	ProcessStatus process();
	
	/**
	 * Will return the content process.
	 * @return an <code>E</code>.
	 */
	E get();
	
	/**
	 * Reset our process.
	 */
	void reset();
}
