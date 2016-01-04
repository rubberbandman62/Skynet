package skynet;

/**
 * A Loose Exception is thrown by letTheAgentMoveOn when the agent
 * has reached a gateway node.
 * 
 * @author hto
 *
 */
public class LooseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1159122397482863402L;

	/**
	 * Creates a Loose Exception with a default message.
	 */
	public LooseException() {
		super("The world is lost! The agent reached the next subnet!");
	}
	
	/**
	 * Creates a Loose Exception with the given message.
	 * @param message the message this exception is create with
	 */
	public LooseException(String message) {
		super(message);
	}
}
