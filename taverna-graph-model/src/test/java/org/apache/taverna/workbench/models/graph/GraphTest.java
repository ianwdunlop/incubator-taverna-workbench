package org.apache.taverna.workbench.models.graph;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.taverna.workbench.models.graph.GraphController;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.apache.taverna.workbench.models.graph.GraphEdge;
import org.apache.taverna.workbench.models.graph.Graph;
import org.apache.taverna.workbench.models.graph.GraphNode;
import org.apache.taverna.workbench.models.graph.Graph.Alignment;

import org.junit.Before;
import org.junit.Test;

public class GraphTest {

	private Graph graph;

	private Alignment alignment;

	private GraphController graphController;

	@Before
	public void setUp() throws Exception {
		alignment = Alignment.VERTICAL;
		graph = new Graph(graphController);
	}

	@Test
	public void testGraph() {
		assertNotNull(new Graph(graphController));
	}

	@Test
	public void testAddEdge() {
		GraphEdge newEdge = new GraphEdge(graphController);
		graph.addEdge(newEdge);
		assertEquals(1, graph.getEdges().size());
		assertTrue(graph.getEdges().contains(newEdge));
	}

	@Test
	public void testAddNode() {
		GraphNode newNode = new GraphNode(graphController);
		graph.addNode(newNode);
		assertEquals(1, graph.getNodes().size());
		assertTrue(graph.getNodes().contains(newNode));
		assertEquals(graph, newNode.getParent());
	}

	@Test
	public void testAddSubgraph() {
		Graph newGraph = new Graph(graphController);
		graph.addSubgraph(newGraph);
		assertEquals(1, graph.getSubgraphs().size());
		assertTrue(graph.getSubgraphs().contains(newGraph));
		assertEquals(graph, newGraph.getParent());
	}

	@Test
	public void testGetAlignment() {
		assertEquals(alignment, graph.getAlignment());
	}

	@Test
	public void testGetEdges() {
		assertNotNull(graph.getNodes());
		assertEquals(0, graph.getNodes().size());
	}

	@Test
	public void testGetNodes() {
		assertNotNull(graph.getEdges());
		assertEquals(0, graph.getEdges().size());
	}

	@Test
	public void testGetSubgraphs() {
		assertNotNull(graph.getSubgraphs());
		assertEquals(0, graph.getSubgraphs().size());
	}

	@Test
	public void testRemoveEdge() {
		GraphEdge newEdge = new GraphEdge(graphController);
		assertFalse(graph.removeEdge(newEdge));
		graph.addEdge(newEdge);
		assertTrue(graph.removeEdge(newEdge));
		assertFalse(graph.getNodes().contains(newEdge));
	}

	@Test
	public void testRemoveNode() {
		GraphNode newNode = new GraphNode(graphController);
		assertFalse(graph.removeNode(newNode));
		graph.addNode(newNode);
		assertTrue(graph.removeNode(newNode));
		assertFalse(graph.getNodes().contains(newNode));
	}

	@Test
	public void testRemoveSubgraph() {
		Graph newGraph = new Graph(graphController);
		assertFalse(graph.removeSubgraph(newGraph));
		graph.addSubgraph(newGraph);
		assertTrue(graph.removeSubgraph(newGraph));
		assertFalse(graph.getSubgraphs().contains(newGraph));
	}

	@Test
	public void testSetAlignment() {
		graph.setAlignment(Alignment.VERTICAL);
		assertEquals(Alignment.VERTICAL, graph.getAlignment());
		graph.setAlignment(Alignment.HORIZONTAL);
		assertEquals(Alignment.HORIZONTAL, graph.getAlignment());
	}

}
