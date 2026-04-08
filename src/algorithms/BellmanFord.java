package algorithms;

import graph.FlightGraph;
import graph.Route;

import java.util.*;

public class BellmanFord {

    private FlightGraph graph;

    public BellmanFord(FlightGraph graph) {
        this.graph = graph;
    }

    public List<String> findShortestPath(String src, String dest) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        List<String> allCodes    = new ArrayList<>(graph.getAllCodes());

        // Step 1: Initialize all distances to infinity
        for (String code : allCodes)
            dist.put(code, Double.MAX_VALUE);
        dist.put(src, 0.0);

        int V = allCodes.size();

        // Step 2: Relax all edges V-1 times
        for (int i = 0; i < V - 1; i++) {
            for (String u : allCodes) {
                if (dist.get(u) == Double.MAX_VALUE) continue;

                for (Route r : graph.getNeighbors(u)) {
                    String v       = r.getDestCode();
                    double newDist = dist.get(u) + r.getCost();

                    if (newDist < dist.get(v)) {
                        dist.put(v, newDist);
                        prev.put(v, u);
                    }
                }
            }
        }

        // Step 3: Check for negative weight cycles
        for (String u : allCodes) {
            if (dist.get(u) == Double.MAX_VALUE) continue;
            for (Route r : graph.getNeighbors(u)) {
                String v = r.getDestCode();
                if (dist.get(u) + r.getCost() < dist.get(v)) {
                    System.out.println("Negative weight cycle detected!");
                    return Collections.emptyList();
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

        if (path.isEmpty() || !path.get(0).equals(src))
            return Collections.emptyList();
        return path;
    }
}