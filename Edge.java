public class Edge implements Comparable<Edge>{
    String source;
    String destination;
    int distance;
    boolean visited;
    int weight;

    Edge(String v1, String v2, int d) {
        source = v1;
        destination = v2;
        distance = d;
        visited = false;
    }

    @Override //to be used in a min heap during dijkstra's
    public int compareTo(Edge e) {
        return this.weight - e.weight;
    }
}