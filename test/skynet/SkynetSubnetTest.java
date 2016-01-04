package skynet;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SkynetSubnetTest {
	
	SubnetBackdoor backdoor = null;

	@Before
	public void setUp() throws Exception {
		this.backdoor = SkynetSubnet.createBackdoorToSubnetBETA();
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
		this.backdoor.disconnectNodes(3, 2);
		int[][] links = this.backdoor.getNodeLinks();
		assertEquals(0, links[0][0]);
		assertEquals(1, links[0][1]);
		assertEquals(0, links[1][0]);
		assertEquals(2, links[1][1]);
		assertEquals(1, links[2][0]);
		assertEquals(3, links[2][1]);
		assertEquals(3, links.length);
		
		assertFalse(this.backdoor.disconnectNodes(0, 2));
		
		try {
			this.backdoor.letTheAgentMoveOn();
		} catch (WinException | LooseException e) {
			fail("should not throw a win or loose exception in this situation");
		}

		assertFalse(this.backdoor.disconnectNodes(10, 12));
		assertFalse(this.backdoor.disconnectNodes(10, 3));
		assertFalse(this.backdoor.disconnectNodes(0, 12));
		assertTrue(this.backdoor.disconnectNodes(0, 3));

	}

	@Test
	public void testGetAgentPosition() {
		int pos = this.backdoor.getAgentPosition();
		assertEquals(0, pos);
		try {
			pos = this.backdoor.letTheAgentMoveOn();
			assertNotEquals(0, pos);
		} catch (WinException | LooseException e) {
			fail("should not throw a win or loose exception in this situation");
		}
		try {
			pos = this.backdoor.letTheAgentMoveOn();
		} catch (WinException e) {
			fail("should not throw a win exception in this situation");
		} catch (LooseException e) {
			// this is the expected event
		}
		pos = this.backdoor.getAgentPosition();
		assertEquals(3, pos);
	}

	@Test
	public void testLetTheAgentMoveOn() {
		int pos = 0;
		try {
			pos = this.backdoor.letTheAgentMoveOn();
			assertNotEquals(0, pos);
		} catch (WinException | LooseException e) {
			fail("should not throw a win or loose exception in this situation");
		}
		try {
			pos = this.backdoor.letTheAgentMoveOn();
		} catch (WinException e) {
			fail("should not throw a win exception in this situation");
		} catch (LooseException e) {
			// this is the expected event
		}
		pos = this.backdoor.getAgentPosition();
		assertEquals(3, pos);
	}

	@Test
	public void testGetGatewayNodes() {
		int[] gateways = this.backdoor.getGatewayNodes();
		
		assertEquals(3, gateways[0]);
		assertEquals(1, gateways.length);
	}

}
