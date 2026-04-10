package utils;



import java.util.*;
import java.util.stream.Collectors;


/**
 * Model for control-flow perspective of business process
 */
public class Graph {
    //inner counter of added vertexes
    private int currentVertexCount = 0;

    //inner storage of vertexes
    private Map<String, Integer> nodeIdMap = new HashMap();

    //vertexe's (Integer) outcoming edges (HashSet)
    private Map<Integer, Set<Graph.Edge>> sourceEdgeMap = new HashMap();

    //all incoming edges for specified vertex (Integer)
    private Map<Integer, Set<Graph.Edge>> incomingEdgeMap = new HashMap();


    public Graph() {
    }

    //inner class for modeling graph's edge
    public class Edge {
        private Integer source;
        private Integer target;
        private String name;

        Edge(Integer source, Integer target, String name) {
            this.source = source;
            this.target = target;
            this.name = name;
        }

        public Integer getSource() {
            return this.source;
        }

        public Integer getTarget() {
            return this.target;
        }

        public String getName() {
            return this.name;
        }
    }




    /**
     * adding neuer vertex to graph
     * @param name - name of
     * @return index of added vertex
     */
    public Integer addVertex(String name) {
        Integer newIndex = ++this.currentVertexCount;
        this.nodeIdMap.put(name, newIndex);
        return newIndex;
    }


    /**
     * adding edge to graph
     * @param source
     * @param target
     * @param edgeName
     */
    public void addEdge(Integer source, Integer target, String edgeName) {
        Set<Edge> sourceEdges = sourceEdgeMap.get(source);
        //check if vertex has outcoming  edges
        if (sourceEdges == null) {
            sourceEdges = new HashSet<Edge>();

            sourceEdgeMap.put(source, sourceEdges);
        }

        // make the same for incoming edges
        Set<Edge> incomingList = incomingEdgeMap.get(target);
        if (incomingList == null) {
            incomingList = new HashSet<Edge>();

            incomingEdgeMap.put(target, incomingList);
        }

        Edge e = new Edge(source, target, edgeName);
        sourceEdges.add(e);
        incomingList.add(e);
    }


    /**
     * Check
     * @param source
     * @param target
     * @return
     */


    /**
     * check if there is edge with source and destination
     * @param source
     * @param target
     * @return true if there is
     */
    public boolean containsEdge(Integer source, Integer target) {
        Set<Edge> destinations = this.sourceEdgeMap.get(source);

        if (destinations != null && !destinations.isEmpty()) {
            return destinations.stream().filter(e -> e.target == target).findAny().isPresent();
        }
        return false;
    }




    public int getVerticeCount() {
        return this.currentVertexCount;
    }

    public Set<Edge> getIncomingEdges(Integer vertexIndex) {
        Set<Edge> edges = this.incomingEdgeMap.get(vertexIndex);
        if (edges == null) {
            edges = new HashSet<>();
        }
        return edges;
    }

    public Set<Graph.Edge> getOutgoingEdges(Integer vertexIndex) {
        Set<Edge> edges = this.sourceEdgeMap.get(vertexIndex);
        if (edges == null) {
            edges = new HashSet<>();
        }
        return edges;
    }


    /**
     * Return nodes without incoming edges
     * @return
     */
    public Set<Integer> getSourceNodes() {
       return  nodeIdMap.values().stream().filter(i->incomingEdgeMap.get(i)==null).collect(Collectors.toSet());
    }



    public boolean isSplit(Integer vertexIndex) {
        Set<Edge> edges = this.sourceEdgeMap.get(vertexIndex);
        return edges != null && edges.size() > 1;
    }

    public boolean isJoin(Integer vertexIndex) {
        Set<Edge> edges = this.incomingEdgeMap.get(vertexIndex);
        return edges != null && edges.size() > 1;
    }

}