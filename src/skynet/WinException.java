package skynet;

/**
 * A Win Exception is thrown by letTheAgentMoveOn when it is impossible for
 * the agent to reach a gateway node.
 * 
 * @author hto
 *
 */
public class WinException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7573954077667742729L;
	
	/**
	 * Creates a Win Exception with a default Message.
	 */
	public WinException() {
		super("Congratulations! You successfully prevented the agent from reaching another subnet.");
	}

}
