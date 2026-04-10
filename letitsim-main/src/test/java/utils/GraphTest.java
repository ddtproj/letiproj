package utils;

import org.junit.Before;

import java.util.Set;

import static org.junit.Assert.*;

public class GraphTest {
    Graph gr=new Graph();




    @org.junit.Test
    public void getIncomingEdges() {

        Set<Graph.Edge> edge=gr.getIncomingEdges(3);
        Graph.Edge ed=edge.stream().findAny().get();
        assertEquals("E2",ed.getName());
    }

    @org.junit.Test
    public void getOutgoingEdges() {
        Set<Graph.Edge> edge=gr.getOutgoingEdges(2);
        Graph.Edge ed=edge.stream().findAny().get();
        assertEquals("E2",ed.getName());
    }


    @org.junit.Test
    public void getSourceNodes() {

        Set<Integer> setNds=gr.getSourceNodes();
        Integer vert=setNds.stream().findAny().get();
        assertEquals(1,vert.intValue());






    }

    @org.junit.Test
    public void isSplit() {

        assertFalse(gr.isSplit(1));

    }

    @org.junit.Test
    public void isJoin() {
        assertTrue(gr.isJoin(2));
    }

    @Before
    public void setUp() throws Exception {


        gr.addVertex("One");
        gr.addVertex("Two");
        gr.addVertex("Three");
        gr.addVertex("Four");
        gr.addVertex("Five");
        gr.addEdge(1,2,"E1");
        gr.addEdge(2,3,"E2");
        gr.addEdge(4,2,"E3");
        gr.addEdge(3,4,"E4");

        Set<Graph.Edge> edge=gr.getIncomingEdges(2);


    }
}