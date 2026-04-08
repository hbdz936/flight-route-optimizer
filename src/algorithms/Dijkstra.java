package algorithms;

import graph.FlightGraph;
import graph.Route;

import java.util.*;

public class Dijkstra {

    private FlightGraph graph;

    public Dijkstra(FlightGraph graph) {
        this.graph = graph;
    }

    public List<String> findShortestPath(String src, String dest) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited      = new HashSet<>();

        // Initialize all distances to infinity
        for (String code : graph.getAllCodes())
            dist.put(code, Double.MAX_VALUE);
        dist.put(src, 0.0);

        // Min-heap priority queue: {airport code, distance}
        PriorityQueue<String> pq = new PriorityQueue<>(
            Comparator.comparingDouble(dist::get)
        );
        pq.add(src);

        while (!pq.isEmpty()) {
            String u = pq.poll(); // pick node with minimum distance

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u.equals(dest)) break;

            for (Route r : graph.getNeighbors(u)) {
                String v    = r.getDestCode();
                double newDist = dist.get(u) + r.getCost();

                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        return buildPath(src, dest, prev);
    }

    private List<String> buildPath(String src, String dest, Map<String, String> prev) {
        List<String> path = new ArrayList<>();
        String curr = dest;

        while (curr != null) {
            path.add(0, curr);
            curr = prev.get(curr);
        }

        if (!path.get(0).equals(src)) return Collections.emptyList();
        return path;
    }
}