package skynet;

/**
 * Example which just lets the agent run through SubnetAlpha.
 * 
 * @author hto
 *
 */
public class RunSubnetBackdoor {

	/**
	 * Example how to use the back door:
	 * <pre>
	 * <code>
		SubnetBackdoor backdoor = SkynetSubnet.createBackdoorToSubnetAlpha();
		int links[][] = backdoor.getNodeLinks();
		for (int[] link : links) {
			System.out.println("" + link[0] + " " + link[1]);
		}

		try {
			int pos = backdoor.getAgentPosition();
			System.out.println("The agent starts at position: " + pos);
			
			backdoor.disconnectNodes(links[0][0], links[0][1]);
			while (true) {
				pos = backdoor.letTheAgentMoveOn();
				System.out.println("Agent's position: " + pos);
			}
		} catch (WinException e) {
			System.out.println(e.getMessage());
		} catch (LooseException e) {
			System.out.println(e.getMessage());
		}
	 * </code>
	 * </pre>
	 * @param args is not used in this example
	 */
	public static void main(String[] args) {

		SubnetBackdoor backdoor = SkynetSubnet.createBackdoorToSubnetAlpha();
		int links[][] = backdoor.getNodeLinks();
		for (int[] link : links) {
			System.out.println("" + link[0] + " " + link[1]);
		}

		try {
			int pos = backdoor.getAgentPosition();
			System.out.println("The agent starts at position: " + pos);
			
			backdoor.disconnectNodes(links[0][0], links[0][1]);
			while (true) {
				pos = backdoor.letTheAgentMoveOn();
				System.out.println("Agent's position: " + pos);
			}
		} catch (WinException e) {
			System.out.println(e.getMessage());
		} catch (LooseException e) {
			System.out.println(e.getMessage());
		}
	}

}
