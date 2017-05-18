/**
 * Infinite state machines have a ninterface much like this for code.
 * @author not-tim
 *
 */
public interface StateListener {
	void callback(State state,Event event);
}
