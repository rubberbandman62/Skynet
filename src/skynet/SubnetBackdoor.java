package skynet;

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
	 * Disconnects a link between two nodes. This method is only successful  
	 * right after creation of the network or after a call to letTheAgentMoveOn.
	 * @param a first node of the link
	 * @param b second node of the link
	 * @return true, if there was a link between the two nodes and it was 
	 * successfully disconnected, otherwise false 
	 */
	public boolean disconnectNodes(int a, int b);
	
	/**
	 * Returns the current position of the agent.
	 * 
	 * @return integer value of the current agent position
	 */
	public int getAgentPosition();
	
	/**
	 * Moves the agent to a new Position and returns the new agent position 
	 * in the subnetwork. If you consecutively 
	 * call this method without calling disconnectNodes, your loose the game.
	 * @return the new position of the agent
	 * @throws WinException if there is no chance for the agent to reach a 
	 * gateway node.
	 * @throws LooseException if the agent reached a gateway node.
	 */
	public int letTheAgentMoveOn() throws WinException, LooseException;
	
	
	/**
	 * @return all the gateway nodes in the subnetwork
	 */
	public int[] getGatewayNodes();

}
