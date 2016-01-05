/**
 * This example is taken from codingames.com @see <a href="https://www.codingame.com/games/puzzles">https://www.codingame.com/games/puzzles</a>. 
 * Look for "Skynet: The Virus".
 * <p>
 * Your virus has caused a backdoor to open on the Skynet network enabling you 
 * to send new instructions in real time.
 * <p>
 * You decide to take action by stopping Skynet from communicating on its own 
 * internal network.
 * <p>
 * Skynet's network is divided into several smaller networks, in each sub-network 
 * is a Skynet agent tasked with transferring information by moving from node to 
 * node along links and accessing gateways leading to other sub-networks.
 * <p>
 * Your mission is to reprogram the virus so it will sever links in such a way 
 * that the Skynet Agent is unable to access another sub-network thus preventing 
 * information concerning the presence of our virus to reach Skynet's central hub. 
 * This package gives you the necessary tools through the interface SubnetBackdoor.
 * See the documentation for class RunSubnetBckdoor for how to use the interface.
 * <p>
 * Following an example with four nodes including a gateway at node 3 (in red). 
 * The Agent (black marker) is initially positioned on node 0:
 * <p>
 * <img src="{@docRoot}/images/01 Initial Situation.gif" alt="Initial Situation">
 * <p>
 * The first action you might take is to disconnect node 1 from node 0:   
 * <p>
 * <img src="{@docRoot}/images/02 disconnect 0 1.gif" alt="Disconnect 1 from 0">
 * <p>
 * The agent than moves from node 0 to node 2:
 * <p>
 * <img src="{@docRoot}/images/03 agent move to 2.gif" alt="Agent moves to node 2">
 * <p>
 * Finally, to win the game you disconnect the gateway node 3 from node 2:
 * <p>
 * <img src="{@docRoot}/images/04 disconnect 2 3.gif" alt="Disconnect 3 from 2">
 */

package skynet;