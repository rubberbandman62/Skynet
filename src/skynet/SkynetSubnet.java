package skynet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The Skynet subnet which implements the public interface SubnetBackdoor
 * through which one can manipulate this subnet. There are factory methods which
 * allow access to a default subnet and the subnets Alpha, Beta and Gamma. If
 * one has a map of another subnet it is also possible to attack that subnet.
 * 
 * @author hto
 *
 */
public class SkynetSubnet implements SubnetBackdoor {

	private static final int ALPHA = 1;
	private static final int BETA = 2;
	private static final int GAMMA = 3;

	private int[][] linksAsIntegers;

	private Map<Integer, Node> nodes;
	private List<Node> gatewayNodes;
	private Node agentNode = null;

	/**
	 * Status of the network which means whether a call to disconnectNodes is
	 * allowed. The disconnection of two nodes is allowed after creation of the
	 * subnet and after a call to letTheAgentMoveOn
	 */
	private boolean disconnectPossible = false;

	/**
	 * If you somehow got to a map of a Skynet subnet you can create a back door
	 * to this subnet, which enables you the perform method to prevent the
	 * subnet agent to reach a gateway to another subnet.
	 * 
	 * @param pathToSubnetMap
	 *            the filename of the stolen map. The file must be found on the
	 *            class path and it should contain several integers:
	 *            <ul>
	 *            <li>l - first integer including the number of links in the
	 *            network</li>
	 *            <li>e - second integer including the number of gateways in the
	 *            network</li>
	 *            <li>links - l pairs of integers each representing a pair of
	 *            linked nodes</li>
	 *            <li>gateways - e integers representing the gateway nodes</li>
	 *            <li>agent position - last integer representing the node where
	 *            the agent is initially positioned</li>
	 *            </ul>
	 * @return an object that implements the SubnetnetBackdoor interface
	 */
	public static SubnetBackdoor createSubnetBackdoor(String pathToSubnetMap) {
		return new SkynetSubnet(pathToSubnetMap);
	}

	/**
	 * Creates a back door to Skynet subnet ALPHA
	 * 
	 * @return an object that implements the SubnetnetBackdoor interface
	 */
	public static SubnetBackdoor createBackdoorToSubnetAlpha() {
		return new SkynetSubnet(ALPHA);
	}

	/**
	 * Creates a back door to Skynet subnet BETA
	 * 
	 * @return an object that implements the SubnetnetBackdoor interface
	 */
	public static SubnetBackdoor createBackdoorToSubnetBETA() {
		return new SkynetSubnet(BETA);
	}

	/**
	 * Creates a back door to Skynet subnet GAMMA
	 * 
	 * @return an object that implements the SubnetnetBackdoor interface
	 */
	public static SubnetBackdoor createBackdoorToSubnetGAMMA() {
		return new SkynetSubnet(GAMMA);
	}

	/**
	 * Creates a back door to a Skynet subnet
	 * 
	 * @return an object that implements the SubnetnetBackdoor interface
	 */
	public static SubnetBackdoor createBackdoorToSubnet() {
		return new SkynetSubnet(666);
	}

	/**
	 * Initializes the Skynet subnet with a stolen map of the network
	 * 
	 * @param pathToSubnetMap
	 *            the position of the stolen map on the class path (see:
	 *            {@link #createSubnetBackdoor(String pathToSubnetMap})
	 */
	private SkynetSubnet(String pathToSubnetMap) {
		InputStream subnetMapInput = getClass().getResourceAsStream(pathToSubnetMap);
		Scanner in = new Scanner(subnetMapInput);

		this.initializeSubnet(in);

		in.close();
	}

	/**
	 * Create a new subnet with a predefined networks
	 * 
	 * @param subnetID
	 *            id of the predefined network may be 1, 2 or 3 all other
	 *            integers cause the network to be initialized with a default
	 *            network.
	 */
	private SkynetSubnet(int subnetID) {
		String pathToSubnetMap = getPathToSubnetMap(subnetID);
		InputStream subnetMapInput = getClass().getResourceAsStream(pathToSubnetMap);
		Scanner in = new Scanner(subnetMapInput);

		this.initializeSubnet(in);

		in.close();
	}

	private String getPathToSubnetMap(int subnetID) {
		String pathToSubnetMap = "";
		switch (subnetID) {
		case ALPHA:
			pathToSubnetMap = "/subnetAlpha.txt";
			break;

		case BETA:
			pathToSubnetMap = "/subnetBeta.txt";
			break;

		case GAMMA:
			pathToSubnetMap = "/subnetGamma.txt";
			break;

		default:
			pathToSubnetMap = "/defaultSubnet.txt";
			break;
		}
		return pathToSubnetMap;
	}

	/**
	 * Initializes all the fields in SkynetSubnet object.
	 * 
	 * @param map
	 *            is the file where the initialization is stored. (see
	 *            {@link #createSubnetBackdoor(String pathToSubnetMap} for the
	 *            file format)
	 */
	private void initializeSubnet(Scanner map) {
		int l = map.nextInt(); // the number of links
		int e = map.nextInt(); // the number of exit gateways

		// initialize nodes and links between nodes (stored in the nodes)
		this.linksAsIntegers = new int[l][2];
		this.nodes = new HashMap<Integer, Node>();
		for (int i = 0; i < l; i++) {
			int nodeIdA = map.nextInt(); // nodeIdA and nodeIdB defines a link
											// between these nodes
			int nodeIdB = map.nextInt();
			this.addLink(nodeIdA, nodeIdB);
			this.linksAsIntegers[i] = new int[] { nodeIdA, nodeIdB };
		}

		this.gatewayNodes = new ArrayList<Node>();
		for (int i = 0; i < e; i++) {
			int gatewayId = map.nextInt(); // the index of a gateway node
			this.addGateway(gatewayId);
		}

		int agentPosition = map.nextInt();
		this.setAgentNode(agentPosition);

		// initialize the shortest ways to the gateway
		this.recalculateAndSetStepsToNextGateway();

		this.disconnectPossible = true;
	}

	@Override
	public int[][] getNodeLinks() {
		return this.linksAsIntegers.clone();
	}

	@Override
	public boolean disconnectNodes(int a, int b) {
		if (this.disconnectPossible) {
			Node nodeA = this.nodes.get(new Integer(a));
			Node nodeB = this.nodes.get(new Integer(b));
			if (nodeA != null && nodeB != null) {
				nodeA.removeNeighbour(nodeB);
				nodeB.removeNeighbour(nodeA);
				this.removeFromLinksAsIntegers(a, b);

				// change status so if there is no call to getNewAgentPosition
				// all subsequent calls to disconnectNodes will return false.
				this.disconnectPossible = false;
				return true;
			}
		}
		return false;
	}

	@Override
	public int getAgentPosition() {
		return this.agentNode.getId();
	}

	/**
	 * To keep the redundant list of links as integers in synch with the
	 * representation of the network, a link must also be removed from this
	 * list, too.
	 * 
	 * @param a
	 *            starting node of the link to be severed
	 * @param b
	 *            end node of the link to be be severed
	 */
	private void removeFromLinksAsIntegers(int a, int b) {
		int[][] newLinksAsIntegers = new int[this.linksAsIntegers.length - 1][2];
		int index = 0;
		try {
			for (int[] link : this.linksAsIntegers) {
				if (!(link[0] == a && link[1] == b) && !(link[0] == b && link[1] == a)) {
					newLinksAsIntegers[index] = link;
					index += 1;
				}
			}
			this.linksAsIntegers = newLinksAsIntegers;
		} catch (ArrayIndexOutOfBoundsException e) {
			// this may happen if there is no link between a and b
		}
	}

	@Override
	public int letTheAgentMoveOn() throws WinException, LooseException {
		if (this.agentNode == null) {
			throw new RuntimeException("Network not initialized correctly: Agent node is null!");
		}

		Node newAgentNode = this.findNextNodeToTheNearestGatewayFrom(this.agentNode);

		if (newAgentNode == null || newAgentNode.stepsToNextGateway == Integer.MAX_VALUE) {
			throw new WinException();
		}

		this.setAgentNode(newAgentNode);

		if (this.agentNode.isGateway()) {
			throw new LooseException(
					"The world is lost! The agent reached the next subnet via gateway " + this.agentNode.getId() + "!");
		}

		// after setting the new agent node, it is necessary to recalculate the
		// the number of
		// steps to the next gateway node from each node
		this.recalculateAndSetStepsToNextGateway();

		// change status so one call to disconnectNodes may be successful.
		this.disconnectPossible = true;

		return this.agentNode.getId();
	}

	@Override
	public int[] getGatewayNodes() {
		int[] gatewayNodeIds = new int[this.gatewayNodes.size()];
		for (int i = 0; i < this.gatewayNodes.size(); i++) {
			gatewayNodeIds[i] = this.gatewayNodes.get(i).getId();
		}
		return gatewayNodeIds;
	}

	/**
	 * Adds a gateway node to the network. If the node is already in the
	 * network, it is just marked as a gateway node.
	 * 
	 * @param getwayId
	 *            id of the gateway node
	 */
	private void addGateway(int getwayId) {
		Node g = this.createOrGetNode(getwayId);
		g.setGateway(true);
		this.gatewayNodes.add(g);
	}

	/**
	 * The node where the agent is located is added to the network. If the node
	 * is already in the network, it is just marked as the agent node. Notice
	 * that there is only one agent node in the network.
	 * 
	 * @param agentNodeId
	 *            id of the initial agent Node
	 */
	private void setAgentNode(int agentNodeId) {
		Node newAgentNode = this.createOrGetNode(agentNodeId);
		this.setAgentNode(newAgentNode);
	}

	/**
	 * Set the new agent node and thereby mark it as the agent node and remove
	 * the mark from the old agent node.S
	 * 
	 * @param agentNode
	 *            the new agent node
	 */
	private void setAgentNode(Node agentNode) {
		if (this.agentNode != null) {
			// Reset the old agent node to be no longer the agent node
			this.agentNode.setAgentNode(false);
		}
		// set the new agent node and mark is as the agent node
		this.agentNode = agentNode;
		this.agentNode.setAgentNode(true);
	}

	/**
	 * Adds a link from one node to another to the network.
	 * 
	 * @param nodeIDa
	 *            one end of the link
	 * @param nodeIDb
	 *            other end of the link
	 */
	private void addLink(int nodeIDa, int nodeIDb) {
		Node a = this.createOrGetNode(nodeIDa);
		Node b = this.createOrGetNode(nodeIDb);
		a.addNeighbour(b);
		b.addNeighbour(a);
	}

	/**
	 * Initially or after the agent has moved the steps from the agent node to
	 * the gateways must bee recalculated. This solution of the network depends
	 * on storing the number of steps to the next gateway node in each node.
	 */
	private void recalculateAndSetStepsToNextGateway() {
		if (this.getAgentNode() == null) {
			throw new RuntimeException("No agent node found! Network is not completely initialized!");
		}
		if (this.gatewayNodes.isEmpty()) {
			throw new RuntimeException("No gateway nodes found! Network is not completely initialized!");
		}
		this.resetAllSteps();

		for (Node gateway : this.gatewayNodes) {
			gateway.goBackToAgentNode(0);
		}
	}

	/**
	 * Just returns the agent node.
	 * 
	 * @return the agent node
	 */
	private Node getAgentNode() {
		return this.agentNode;
	}

	/**
	 * From current node the next step to the nearest gateway is the neighbour
	 * with the lowest count of steps to a gateway.
	 * 
	 * @param current
	 *            the node from which the next step must be calculated.
	 * @return the node to which to go next
	 */
	private Node findNextNodeToTheNearestGatewayFrom(Node current) {
		if (current == null) {
			throw new RuntimeException("Current node is null!");
		}
		Node neighbour = current.findNeighbourWithMinimumStepsToNextGateway();
		// because a link could have been disconnected the count of steps to the
		// next gateway from the current node may be wrong. It must be corrected
		// for preventing endless loops.
		if (neighbour != null) {
			current.stepsToNextGateway = neighbour.stepsToNextGateway + 1;
		}
		return neighbour;
	}

	/**
	 * Before recalculating the ways through the network the count in al nodes
	 * must be reset.
	 */
	private void resetAllSteps() {
		for (Node n : nodes.values()) {
			n.resetStepsToNextGateway();
		}
	}

	/**
	 * Find the node with id id. if no such node exists, create a new node and
	 * return this
	 * 
	 * @param id
	 *            id of the node requested
	 * @return the unique node with the id id.
	 */
	private Node createOrGetNode(int id) {
		Node node = this.nodes.get(new Integer(id));
		if (node == null) {
			Node newNode = new Node(id);
			this.nodes.put(new Integer(id), newNode);
			return newNode;
		}
		return node;
	}

	/**
	 * The class node represents a node in the network.
	 * 
	 * @author hto
	 *
	 */
	private class Node {
		private Integer id;
		private HashMap<Integer, Node> neighbours;
		private boolean gateway;
		private boolean agentNode;

		private int stepsToNextGateway;

		/**
		 * Creates a new node with the id id.
		 * 
		 * @param id
		 *            id of the new node
		 */
		private Node(int id) {
			this.id = new Integer(id);
			this.neighbours = new HashMap<Integer, Node>();
			this.stepsToNextGateway = Integer.MAX_VALUE;
			this.gateway = false;
			this.agentNode = false;
			this.stepsToNextGateway = Integer.MAX_VALUE;
		}

		/**
		 * From all the nodes which can be reached from this node return the one
		 * from which the way to any gateway is the shortest.
		 * 
		 * @return the neighbour from which the way to a gateway is th shortest.
		 */
		private Node findNeighbourWithMinimumStepsToNextGateway() {
			int minDistance = Integer.MAX_VALUE;
			Node minNeighbour = null;
			for (Node neighbour : this.getNeighbours().values()) {
				if (neighbour.getStepsToNextGateway() <= minDistance) {
					minNeighbour = neighbour;
					minDistance = neighbour.getStepsToNextGateway();
				}
			}
			return minNeighbour;
		}

		/**
		 * Recursive method to find the shortest way bay to the agent node. The
		 * minimal number of steps in each node is recorded. If this method is
		 * called for all gateways the agent node is at last be initialized with
		 * the number of steps the agent at least needs to a gateway.
		 * 
		 * @param steps
		 *            the number of steps back from a gateway to this node.
		 */
		private void goBackToAgentNode(int steps) {
			if (this.getStepsToNextGateway() > steps) {
				this.stepsToNextGateway = steps;
			} else {
				return;
			}
			if (this.isAgentNode()) {
				return;
			}

			for (Node neighbour : this.neighbours.values()) {
				if (!neighbour.isGateway()) {
					neighbour.goBackToAgentNode(steps + 1);
				}
			}
		}

		/**
		 * Removes a neighbour node from the list of neighbours. This method
		 * must be called on both nodes of a link that shall be severed.
		 * 
		 * @param neighbour
		 *            the node to be disconnected
		 * @return the neighbour disconnected.
		 */
		private Node removeNeighbour(Node neighbour) {
			return this.neighbours.remove(neighbour.id);
		}

		private int getId() {
			return this.id;
		}

		private Map<Integer, Node> getNeighbours() {
			return neighbours;
		}

		/**
		 * Add a neighbour to the list of neighbours. Conciders that a node
		 * cannot be his own neighbour and one node can only be once the
		 * neighbour of another node.
		 * 
		 * @param newNeighbour
		 *            node to be added to my neighbours
		 * @return true if the neighbour was successfully added
		 */
		private boolean addNeighbour(Node newNeighbour) {
			if (this.id.equals(newNeighbour.id)) {
				// I'm not my own neighbour
				return false;
			}
			if (this.neighbours.containsKey(newNeighbour.id)) {
				// one neighbour cannot be twice my neighbour
				return false;
			}
			this.neighbours.put(newNeighbour.id, newNeighbour);
			return true;
		}

		private int getStepsToNextGateway() {
			return this.stepsToNextGateway;
		}

		private boolean isGateway() {
			return gateway;
		}

		private void setGateway(boolean gateway) {
			this.gateway = gateway;
		}

		private void resetStepsToNextGateway() {
			this.stepsToNextGateway = Integer.MAX_VALUE;
		}

		private boolean isAgentNode() {
			return this.agentNode;
		}

		private void setAgentNode(boolean agentNode) {
			this.agentNode = agentNode;
		}
	}

}
