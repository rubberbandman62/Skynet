package skynet;

/**
 * Interface that enables you to read a skynet subnet and to manipulate it by 
 * disconnecting nodes.
 * 
 * @author hto
 *
 */
public interface SubnetBackdoor {

	/**
	 * This method return all the links in the subnetwork. Example for printing
	 * all the links in the network:
	 * 
	 * <pre>
	 * <code>
	 *	int links[][] = backdoor.getNodeLinks();
	 *	for (int[] link : links) {
	 *		System.out.println("" + link[0] + " " + link[1]);
	 *	}
	 * </code>
	 * </pre>
	 * 
	 * @return all the links in an integer array with n rows and 2 columns
	 */
	public int[][] getNodeLinks();

	/**
	 * Disconnects a link between two nodes and than makes the agent move on.
	 * 
	 * @param a
	 *            first node of the link
	 * @param b
	 *            second node of the link
	 * @return true, if there was a link between the two nodes and it was
	 *         successfully disconnected, otherwise false. In any case the
	 *         agent moved towards the next gateway if possible.
	 */
	public boolean disconnectNodesBeforeAgentMovesOn(int a, int b);

	/**
	 * Returns the current position of the agent.
	 * 
	 * @return integer value of the current agent position
	 */
	public int getAgentPosition();

	/**
	 * There is hope but the agent is still moving on. Disconnect another
	 * link to save the world.
	 * 
	 * @return true if the agent has not yet reached a gateway
	 *         node but still has the chance to, otherwise false.
	 */
	public boolean isAgentStillMoving();

	/**
	 * Return if the battle is lost
	 * 
	 * @return true if the agent has reached a gateway node, false if
	 *         the agent has not yet reached a gateway node.
	 */
	public boolean isAgentOnAGateway();

	/**
	 * @return all the gateway nodes in the subnetwork
	 */
	public int[] getGatewayNodes();

}
