package skynet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.math3.random.RandomDataGenerator;

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

	/**
	 * ID of the predefined default subnet
	 */
	public static final int DEFAULT = 4;

	/**
	 * ID of the predefined subnet ALPHA
	 */
	public static final int ALPHA = 1;

	/**
	 * ID of the predefined subnet BETA
	 */
	public static final int BETA = 2;

	/**
	 * ID of the predefined subnet GAMMA
	 */
	public static final int GAMMA = 3;

	private int[][] linksAsIntegers;

	private Map<Integer, Node> nodes = new HashMap<>();
	private List<Node> gatewayNodes = new ArrayList<>();
	private Node agentNode = null;

	// status notifier
	private boolean agentMoving = true;
	private boolean agentOnAGateway = true;

	/**
	 * If you somehow got a map of a Skynet subnet you can create a back door to
	 * this subnet, which enables you the perform method to prevent the subnet
	 * agent to reach a gateway to another subnet.
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
	public static SubnetBackdoor createSubnet(String pathToSubnetMap) {
		return new SkynetSubnet(pathToSubnetMap);
	}

	/**
	 * Creates a randomly generated network with the specified number of nodes.
	 * The algorithm declares between 1 and numberOfNodes/2 nodes to be gateways,
	 * connects some nodes to the gateways (a none gateway node is connected at
	 * most to one gateway node) and generates links between the none gateway
	 * nodes. Finally a none gateway node is selected to be the agent node.
	 * 
	 * @param numberOfNodes the number of the nodes in the network &gt; 4
	 * @return an object implementing the SubnetBackdor interface
	 */
	public static SubnetBackdoor createRandomSubnet(int numberOfNodes) {
		return new SkynetSubnet(numberOfNodes);
	}

	/**
	 * Creates a back door to a existing Skynet subnet. The available subnet
	 * ID's are:
	 * <ul>
	 * <li>ALPHA (1)</li>
	 * <li>BETA (2)</li>
	 * <li>GAMMA (3)</li>
	 * </ul>
	 * 
	 * You can use the public constants of the class.
	 * <p>
	 * There is also a default subnet which is returned in none of theses
	 * strings match.
	 * 
	 * @param subnetId
	 *            integer id of an subnet
	 * @return an object that implements the SubnetnetBackdoor interface. If the
	 *         ID is not 1, 2 or 3 a default subnet is returned.
	 */
	public static SubnetBackdoor createBackdoorToExistingSubnet(int subnetId) {
		if (subnetId == ALPHA) {
			return new SkynetSubnet(ALPHA);
		}
		if (subnetId == BETA) {
			return new SkynetSubnet(BETA);
		}
		if (subnetId == GAMMA) {
			return new SkynetSubnet(GAMMA);
		}
		return new SkynetSubnet(DEFAULT);
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

		this.initializeSubnetFromFile(in);

		in.close();
	}

	/**
	 * Create a new subnet with a predefined or a random network
	 * 
	 * @param selector
	 *            id of the predefined network may be 1, 2, 3 or 4 all other
	 *            integers cause the network to be initialized with a random
	 *            network.
	 */
	private SkynetSubnet(int selector) {

		if (selector < 5 && selector > 0) {
			String pathToSubnetMap = getPathToSubnetMap(selector);
			InputStream subnetMapInput = getClass().getResourceAsStream(pathToSubnetMap);
			Scanner in = new Scanner(subnetMapInput);

			this.initializeSubnetFromFile(in);

			in.close();
		} else if (selector > 4) {
			this.initializeRandomSubnet(selector);
		} else {
			throw new RuntimeException(
					"Illegal subnet selector " + selector + ". the selctor must be greater than zero.");
		}
	}

	/**
	 * Helper to select one on the predefined subnets.
	 * @param subnetID the id of the subnet.
	 * @return the path to the subnet as a string
	 */
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
	 * Initializes a Skynet subnet from a file.
	 * 
	 * @param map
	 *            is the scanner to a file where the initialization is stored.
	 *            (see {@link #createSubnetBackdoor(String pathToSubnetMap} for
	 *            the file format)
	 */
	private void initializeSubnetFromFile(Scanner map) {
		int l = map.nextInt(); // the number of links
		int e = map.nextInt(); // the number of exit gateways

		// initialize nodes and links between nodes (stored in the nodes)
		int[][] links = new int[l][2];
		for (int i = 0; i < l; i++) {
			int nodeIdA = map.nextInt();
			int nodeIdB = map.nextInt();
			links[i] = new int[] { nodeIdA, nodeIdB };
		}

		this.gatewayNodes = new ArrayList<Node>();
		int[] gateways = new int[e];
		for (int i = 0; i < e; i++) {
			int gatewayId = map.nextInt();
			gateways[i] = gatewayId;
		}

		int agentPosition = map.nextInt();

		this.initializeSubnet(links, gateways, agentPosition);
	}

	/**
	 * initializes the fields of subnet
	 * 
	 * @param links
	 *            nx2 matrix of n links
	 * @param gateways
	 *            array of gateways
	 * @param agentPosition
	 *            position of the agentnode
	 */
	private void initializeSubnet(int[][] links, int[] gateways, int agentPosition) {
		this.linksAsIntegers = links;

		// initialize nodes an links between nodes
		this.nodes = new HashMap<Integer, Node>();
		for (int i = 0; i < links.length; i++) {
			this.addLink(links[i][0], links[i][1]);
		}

		// initialize gateway nodes
		this.gatewayNodes = new ArrayList<Node>();
		for (int i = 0; i < gateways.length; i++) {
			this.addGateway(gateways[i]);
		}

		this.setAgentNode(agentPosition);

		// initialize the shortest ways to the gateway
		this.recalculateAndSetStepsToNextGateway();

		this.calculateStatus();
	}

	/**
	 * Randomly creates gateways from the integers 0 to numberOfNodes-1 and
	 * links between all nodes considering that each node may at most be linked
	 * to one gateway.
	 * 
	 * @param numberOfNodes
	 *            of nodes in the network
	 */
	private void initializeRandomSubnet(int numberOfNodes) {
		// gateways - Integers for all gateway nodes from nodeIds
		// noneGateways - nodeIds - gatewayIds
		// nodesLinkedToGateways - Map including integers of nodes linked to
		// each gateway
		// nodesLinkedToNodes - Map including integers of none gateway nodes
		// linked to each none gateway node
		// agentNode - the agent position among none gateway nodes.
		Set<Integer> nodes = new LinkedHashSet<Integer>(numberOfNodes);
		for (int i = 0; i < numberOfNodes; i++) {
			nodes.add(Integer.valueOf(i));
		}
		RandomDataGenerator rdg = new RandomDataGenerator();
		// rdg.reSeed(1234567890);

		// at most half of all nodes can be gateways
		int numberOfGateways = rdg.nextInt(1, numberOfNodes / 2);

		// select numberOfGateways integers from 0 to numberOfNodes - 1
		int[] gatewayInts = rdg.nextPermutation(numberOfNodes, numberOfGateways);
		Set<Integer> gatewayNodes = new LinkedHashSet<Integer>(numberOfGateways);
		for (int i = 0; i < numberOfGateways; i++) {
			gatewayNodes.add(Integer.valueOf(gatewayInts[i]));
		}

		// now identify the none gateway nodes
		Set<Integer> noneGatewayIds = new LinkedHashSet<Integer>(nodes);
		noneGatewayIds.removeAll(gatewayNodes);
		int numberOfNoneGateways = noneGatewayIds.size();

		// now create numberOfGateways disjunct subsets of the integers
		int m = numberOfNoneGateways / numberOfGateways;
		Set<Integer> restIds = new LinkedHashSet<Integer>(noneGatewayIds);
		HashMap<Integer, Collection<Integer>> nodesLinkedToGateways = new HashMap<>();
		for (Integer id : gatewayNodes) {
			int ni = rdg.nextInt(1, m);
			Object[] objs = rdg.nextSample(restIds, ni);
			ArrayList<Integer> nodesLinkedToGateway = new ArrayList<>(ni);
			for (Object obj : objs) {
				nodesLinkedToGateway.add((Integer) obj);
			}
			nodesLinkedToGateways.put(id, nodesLinkedToGateway);
			restIds.removeAll(nodesLinkedToGateway);
		}

		// now randomly create links between all none gateway nodes
		HashMap<Integer, Collection<Integer>> nodesLinkedToNodes = new HashMap<>();
		Set<Integer> otherNodeIds = new LinkedHashSet<>(noneGatewayIds);
		int possibleLinks = numberOfNoneGateways;
		for (Integer id : noneGatewayIds) {
			otherNodeIds.remove(id);
			possibleLinks -= 1;

			if (possibleLinks > 1) {
				int ni = rdg.nextInt(1, possibleLinks);
				Object[] objs = rdg.nextSample(otherNodeIds, ni);
				ArrayList<Integer> nodesLinkedToNode = new ArrayList<>(ni);
				for (Object obj : objs) {
					nodesLinkedToNode.add((Integer) obj);
				}
				nodesLinkedToNodes.put(id, nodesLinkedToNode);
			} else {
				nodesLinkedToNodes.put(id, new ArrayList<Integer>());
			}
		}

		// select an agent node
		Object[] objs = rdg.nextSample(noneGatewayIds, 1);
		Integer agentNode = (Integer) objs[0];

		// finally prepare for initializing the network
		int numberOfLinks = 0;
		for (Collection<Integer> links : nodesLinkedToGateways.values()) {
			numberOfLinks += links.size();
		}
		for (Collection<Integer> links : nodesLinkedToNodes.values()) {
			numberOfLinks += links.size();
		}

		int[][] links = new int[numberOfLinks][2];
		int index = 0;
		for (Integer id : nodes) {
			if (nodesLinkedToNodes.containsKey(id)) {
				for (Integer nodeId : nodesLinkedToNodes.get(id)) {
					links[index][0] = id;
					links[index][1] = nodeId;
					index++;
				}
			}
			if (nodesLinkedToGateways.containsKey(id)) {
				for (Integer nodeId : nodesLinkedToGateways.get(id)) {
					links[index][0] = id;
					links[index][1] = nodeId;
					index++;
				}
			}
		}

		int[] gateways = new int[numberOfGateways];
		index = 0;
		for (Integer id : gatewayNodes) {
			gateways[index] = id;
			index++;
		}

		initializeSubnet(links, gateways, agentNode.intValue());
	}

	@Override
	public int[][] getNodeLinks() {
		return this.linksAsIntegers.clone();
	}

	@Override
	public boolean disconnectNodesBeforeAgentMovesOn(int a, int b) {
		boolean success = false;
		Node nodeA = this.nodes.get(new Integer(a));
		Node nodeB = this.nodes.get(new Integer(b));
		if (nodeA != null && nodeB != null) {
			nodeA.removeNeighbour(nodeB);
			nodeB.removeNeighbour(nodeA);
			this.removeFromLinksAsIntegers(a, b);
			success = true;
		}

		// Anyway the agent moves one step forward to the next gateway.
		this.letTheAgentMoveOn();

		this.recalculateAndSetStepsToNextGateway();
		this.calculateStatus();

		return success;
	}

	@Override
	public int getAgentPosition() {
		return this.agentNode.getId();
	}

	@Override
	public boolean isAgentStillMoving() {
		return agentMoving;
	}

	@Override
	public boolean isAgentOnAGateway() {
		return agentOnAGateway;
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

	private Node letTheAgentMoveOn() {
		if (this.agentNode == null) {
			throw new RuntimeException("Network not initialized correctly: Agent node is null!");
		}

		Node newAgentNode = this.findNextNodeToTheNearestGatewayFrom(this.agentNode);

		if (newAgentNode != null) {
			this.setAgentNode(newAgentNode);
		}

		return this.agentNode;
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
	 * The status of the game depend on whether the agent has reached a gateway
	 * node or not and if it is still possible to reach a gateway node.
	 * <ul>
	 * <li>isAgentStruggling() == true means that it is still possible for the
	 * agent to reach a gateway node</li>
	 * <li>isAgentHasNotYetReachedAGateway() == true means that the agent has
	 * not not yet reached a gateway node</li>
	 */
	private void calculateStatus() {
		if (this.agentNode.getStepsToNextGateway() == Integer.MAX_VALUE) {
			this.agentMoving = false;
			this.agentOnAGateway = false;
		} else if (this.agentNode.isGateway()) {
			this.agentMoving = false;
			this.agentOnAGateway = true;
		} else {
			this.agentMoving = true;
			this.agentOnAGateway = false;
		}
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
	 * @return the node to which to go next. If current is a gateway node return
	 *         current.
	 */
	private Node findNextNodeToTheNearestGatewayFrom(Node current) {
		if (current == null) {
			throw new RuntimeException("Current node is null!");
		}

		if (current.isGateway()) {
			return current;
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

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.linksAsIntegers.length).append("\r\n");
		str.append(this.getGatewayNodes().length).append("\r\n");
		for (int[] link : this.getNodeLinks()) {
			str.append(link[0]).append(" ").append(link[1]).append("\r\n");
		}
		for (int id : this.getGatewayNodes()) {
			str.append(id).append("\r\n");
		}
		str.append(this.getAgentPosition()).append("\r\n");
		return str.toString();
	}

}
