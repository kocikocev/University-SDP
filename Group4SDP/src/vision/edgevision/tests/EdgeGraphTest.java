package vision.edgevision.tests;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;

import org.junit.Test;

import vision.edgevision.Edge;
import vision.edgevision.EdgeGraph;

/**
 * JUnit 4 test for EdgeGraph
 * 
 * @author Alex Adams (s1046358)
 */
public class EdgeGraphTest {
	private static final int TEST_COUNT = 500;
	
	/**
	 * Generates an Edge object based on the seed value
	 * 
	 * @param seed
	 * @return a new Edge object
	 */
	private static Edge generateTestNode(int seed) {
		return new Edge(new Point(seed, seed), new Point(-seed, -seed));
	}

	/**
	 * Generates an EdgeGraph object with the specified number of nodes
	 * 
	 * @param nodes
	 * @return a new EdgeGraph with the specified number of nodes
	 */
	private static EdgeGraph generateTestGraph(int nodes) {
		assert (nodes >= 0);

		EdgeGraph test = new EdgeGraph();

		for (int i = 0; i < nodes; ++i) {
			test.addNode(generateTestNode(i));
		}

		return test;
	}

	/**
	 * Generates an EdgeGraph object with the specified number of nodes and arcs
	 * between all nodes
	 * 
	 * @param nodes
	 * @return
	 */
	private static EdgeGraph generateTestGraphWithArcs(int nodes) {
		EdgeGraph test = generateTestGraph(nodes);

		for (int i = 0; i < nodes; ++i) {
			for (int j = 0; j < i; ++j)
				test.addArc(i, j);
			for (int j = i + 1; j < nodes; ++j)
				test.addArc(j, i);
		}

		return test;
	}

	/**
	 * Tests an empty EdgeGraph
	 */
	@Test
	public void empty() {
		EdgeGraph test = new EdgeGraph();

		assertTrue(test.isEmpty());
		assertTrue(test.nodeCount() == 0);
		for (int i = -10; i < 10; ++i)
			assertFalse(test.isValidIndex(i));
	}

	/**
	 * Tests EdgeGraph.addNode and EdgeGraph.getNode
	 */
	@Test
	public void addGetNode() {
		EdgeGraph test = new EdgeGraph();

		for (int i = 0; i < TEST_COUNT; ++i) {
			Edge e1 = generateTestNode(i);

			test.addNode(e1);
			assertTrue(!test.isEmpty());
			assertEquals(test.nodeCount(), i + 1);
			assertTrue(test.isValidIndex(i));
			assertFalse(test.isValidIndex(i + 1));

			try {
				assertEquals(e1, test.getNode(i));
			} catch (IndexOutOfBoundsException e) {
				fail("IndexOutOfBoundsException thrown by test.getNode(" + i
						+ ")");
			}

			// Test duplicate rejection
			test.addNode(e1);
			assertTrue(!test.isEmpty());
			assertEquals(test.nodeCount(), i + 1);
			assertTrue(test.isValidIndex(i));
			assertFalse(test.isValidIndex(i + 1));
		}
	}

	/**
	 * Tests EdgeGraph.addArc and EdgeGraph.hasArc
	 */
	@Test
	public void addHasArc() {
		EdgeGraph test = generateTestGraph(TEST_COUNT);

		// Test hasArc on a graph with no arcs
		assertEquals(test.nodeCount(), TEST_COUNT);
		for (int i = 0; i < TEST_COUNT; ++i) {
			for (int j = 0; j < TEST_COUNT; ++j) {
				try {
					assertFalse(test.hasArc(i, j));
				} catch (IndexOutOfBoundsException e) {
					fail("IndexOutOfBoundsException thrown by test.hasArc(" + i
							+ "," + j + ")");
				}
				try {
					assertFalse(test.hasArc(j, i));
				} catch (IndexOutOfBoundsException e) {
					fail("IndexOutOfBoundsException thrown by test.hasArc(" + j
							+ "," + i + ")");
				}
			}
		}

		// Test addArc and hasArc on a graph with arcs
		for (int i = 0; i < TEST_COUNT; ++i) {
			for (int j = i + 1; j < TEST_COUNT; ++j) {
				try {
					test.addArc(i, j);
				} catch (IndexOutOfBoundsException e) {
					fail("IndexOutOfBoundsException thrown by test.addArc(" + i
							+ "," + j + ")");
				}

				try {
					assertTrue(test.hasArc(i, j));
				} catch (IndexOutOfBoundsException e) {
					fail("IndexOutOfBoundsException thrown by test.hasArc(" + i
							+ "," + j + ")");
				}
				try {
					assertTrue(test.hasArc(j, i));
				} catch (IndexOutOfBoundsException e) {
					fail("IndexOutOfBoundsException thrown by test.hasArc(" + j
							+ "," + i + ")");
				}
			}
		}
	}

	/**
	 * Tests EdgeGraph.removeNode
	 */
	@Test
	public void removeNode() {
		EdgeGraph test = generateTestGraph(TEST_COUNT);

		assertEquals(test.nodeCount(), TEST_COUNT);

		// Test removing last node
		try {
			test.removeNode(TEST_COUNT - 1);
		} catch (IndexOutOfBoundsException e) {
			fail("IndexOutOfBoundsException thrown by test.removeNode(99)");
		}
		assertEquals(test.nodeCount(), TEST_COUNT - 1);
		for (int i = 0; i < TEST_COUNT - 1; ++i) {
			assertEquals(test.getNode(i), generateTestNode(i));
		}

		// Test removing an arbitrary node with an index in the middle
		try {
			test.removeNode(42);
		} catch (IndexOutOfBoundsException e) {
			fail("IndexOutOfBoundsException thrown by test.removeNode(42)");
		}

		assertEquals(test.nodeCount(), TEST_COUNT - 2);
		for (int i = 0; i < 41; ++i) {
			assertEquals(test.getNode(i), generateTestNode(i));
		}
		for (int i = 42; i < TEST_COUNT - 2; ++i) {
			assertEquals(test.getNode(i), generateTestNode(i + 1));
		}

		// Test removing all nodes one at a time
		for (int i = 0; i < TEST_COUNT - 2; ++i) {
			try {
				test.removeNode(TEST_COUNT - 3 - i);
			} catch (IndexOutOfBoundsException e) {
				fail("IndexOutOfBoundsException thrown by test.removeNode(" + i
						+ ")");
			}
			if (i < TEST_COUNT - 3) {
				assertFalse(test.isEmpty());
				assertEquals(test.nodeCount(), TEST_COUNT - 3 - i);
			} else {
				assertTrue(test.isEmpty());
				assertEquals(test.nodeCount(), 0);
			}
		}

		// Test adding works after removing
		for (int i = 0; i < TEST_COUNT; ++i) {
			test.addNode(generateTestNode(TEST_COUNT + i));
			assertEquals(test.getNode(i), generateTestNode(TEST_COUNT + i));
		}
	}

	/**
	 * Tests EdgeGraph.removeArc and EdgeGraph.removeAllArcs
	 */
	@Test
	public void removeArc() {
		EdgeGraph test = generateTestGraphWithArcs(TEST_COUNT);
		
		// Test generateTestGraphWithArcs
		for (int i = 0; i < TEST_COUNT; ++i) {
			for (int j = 0; j < i; ++j) {
				assertTrue(test.hasArc(i, j));
				assertTrue(test.hasArc(j, i));
			}
			assertFalse(test.hasArc(i, i));
			for (int j = i + 1; j < TEST_COUNT; ++j) {
				assertTrue(test.hasArc(i, j));
				assertTrue(test.hasArc(j, i));
			}
		}
		
		// Test removeArc
		for (int i = 0; i < TEST_COUNT; ++i) {
			for (int j = 0; j < i; ++j) {
				test.removeArc(i, j);
				assertFalse(test.hasArc(i, j));
				assertFalse(test.hasArc(j, i));
			}
			for (int j = i + 1; j < TEST_COUNT; ++j) {
				test.removeArc(i, j);
				assertFalse(test.hasArc(i, j));
				assertFalse(test.hasArc(j, i));
			}
		}
		
		// Test removeAllArcs
		test = generateTestGraphWithArcs(TEST_COUNT);
		for (int i = 0; i < TEST_COUNT; ++i) {
			test.removeAllArcs(i);
			for (int j = 0; j < TEST_COUNT; ++j)
				assertFalse(test.hasArc(i, j));
		}
	}
	
	/**
	 * Tests EdgeGraph.getNeighbours
	 */
	@Test
	public void getNeighbours() {
		EdgeGraph test = generateTestGraphWithArcs(TEST_COUNT);
		
		for (int i = 0; i < TEST_COUNT; ++i) {
			ArrayList<Integer> neighbours = test.getNeighbours(i);
			for (int j : neighbours) {
				assertTrue(test.hasArc(i, j));
				assertTrue(test.hasArc(j, i));
			}
			assertFalse(neighbours.contains(i));
		}
	}
}
