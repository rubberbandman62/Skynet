package skynet;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SkynetSubnetTest {

	SubnetBackdoor backdoor = null;

	@Before
	public void setUp() throws Exception {
		this.backdoor = SkynetSubnet.createBackdoorToExistingSubnet(SkynetSubnet.BETA);
	}

	@After
	public void tearDown() throws Exception {
		this.backdoor = null;
	}

	@Test
	public void testGetNodeLinks() {
		int[][] links = this.backdoor.getNodeLinks();
		assertEquals(0, links[0][0]);
		assertEquals(1, links[0][1]);
		assertEquals(0, links[1][0]);
		assertEquals(2, links[1][1]);
		assertEquals(1, links[2][0]);
		assertEquals(3, links[2][1]);
		assertEquals(2, links[3][0]);
		assertEquals(3, links[3][1]);
		assertEquals(4, links.length);
	}

	@Test
	public void testDisconnectNodes() {
		this.backdoor.disconnectNodesBeforeAgentMovesOn(3, 2);
		int[][] links = this.backdoor.getNodeLinks();
		assertEquals(0, links[0][0]);
		assertEquals(1, links[0][1]);
		assertEquals(0, links[1][0]);
		assertEquals(2, links[1][1]);
		assertEquals(1, links[2][0]);
		assertEquals(3, links[2][1]);
		assertEquals(3, links.length);

		assertTrue(this.backdoor.disconnectNodesBeforeAgentMovesOn(0, 2));

		assertFalse(this.backdoor.disconnectNodesBeforeAgentMovesOn(10, 12));
		assertFalse(this.backdoor.disconnectNodesBeforeAgentMovesOn(10, 3));
		assertFalse(this.backdoor.disconnectNodesBeforeAgentMovesOn(0, 12));
		assertTrue(this.backdoor.disconnectNodesBeforeAgentMovesOn(0, 3));

	}

	@Test
	public void testGetAgentPosition() {
		int pos = this.backdoor.getAgentPosition();
		assertEquals(0, pos);

		this.backdoor.disconnectNodesBeforeAgentMovesOn(99, 999);
		pos = this.backdoor.getAgentPosition();
		assertNotEquals(0, pos);
		assertTrue(this.backdoor.isAgentStillMoving());
		assertFalse(this.backdoor.isAgentOnAGateway());

		this.backdoor.disconnectNodesBeforeAgentMovesOn(99, 999);
		pos = this.backdoor.getAgentPosition();
		assertEquals(3, pos);
		assertFalse(this.backdoor.isAgentStillMoving());
		assertTrue(this.backdoor.isAgentOnAGateway());
	}

	@Test
	public void testGetGatewayNodes() {
		int[] gateways = this.backdoor.getGatewayNodes();

		assertEquals(3, gateways[0]);
		assertEquals(1, gateways.length);
	}

}
