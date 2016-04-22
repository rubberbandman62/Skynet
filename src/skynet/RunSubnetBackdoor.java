package skynet;

/**
 * Example class with just a main method to show how to use SubnetBackdoor 
 * Interface:
 * 
 * <pre>
 * <code>
	SubnetBackdoor backdoor = SkynetSubnet.createBackdoorToExistingSubnet(SkynetSubnet.ALPHA);

	int links[][] = backdoor.getNodeLinks();
	for (int[] link : links) {
		System.out.println("" + link[0] + " " + link[1]);
	}

	int[] gatewayNodes = backdoor.getGatewayNodes();
	for (int id : gatewayNodes) {
		System.out.println(id);
	}

	int pos = backdoor.getAgentPosition();
	System.out.println("The agent starts at position: " + pos);

	while (backdoor.isAgentStillMoving()) {
		pos = backdoor.getAgentPosition();
		System.out.println("Agent's position: " + pos);
		// the nodes 99 and 999 do not exist, hence cannot be disconnected.
		// Nevertheless the agent moves on towards the next gateway.
		backdoor.disconnectNodesBeforeAgentMovesOn(99, 999);
	}
	if (backdoor.isAgentOnAGateway()) {
		System.out.println("Agent has reached gateway " + backdoor.getAgentPosition());
	} else {
		System.out.println("You saved the world!");
	}
 * </code>
 * </pre>
 * 
 * @author hto
 *
 */
public class RunSubnetBackdoor {

	/**
	 * main just lets the agent run through SubnetAlpha
	 * 
	 * @param args
	 *            is not used in this example
	 */
	public static void main(String[] args) {
		SubnetBackdoor backdoor = SkynetSubnet.createBackdoorToExistingSubnet(SkynetSubnet.ALPHA);
		// SubnetBackdoor backdoor = SkynetSubnet.createRandomSubnet(50);
		// System.out.println(backdoor);
		int[] gatewayNodes = backdoor.getGatewayNodes();
		int pos = backdoor.getAgentPosition();
		int links[][] = backdoor.getNodeLinks();
		System.out.println(links.length);
		System.out.println(gatewayNodes.length);
		for (int[] link : links) {
			System.out.println("" + link[0] + " " + link[1]);
		}
		for (int id : gatewayNodes) {
			System.out.println(id);
		}
		System.out.println(pos);
		
		System.out.println();
		System.out.println();
		System.out.println("The agent starts at position: " + pos);

		while (backdoor.isAgentStillMoving()) {
			pos = backdoor.getAgentPosition();
			System.out.println("Agent's position: " + pos);
			// the nodes 99 and 999 do not exist, hence cannot be disconnected.
			// Nevertheless the agent moves on towards the next gateway.
			backdoor.disconnectNodesBeforeAgentMovesOn(99, 999);
		}
		if (backdoor.isAgentOnAGateway()) {
			System.out.println("Agent has reached gateway " + backdoor.getAgentPosition());
		} else {
			System.out.println("You saved the world!");
		}
	}
}
