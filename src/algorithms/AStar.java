package algorithms;

import graph.Airport;
import graph.FlightGraph;
import graph.Route;

import java.util.*;

public class AStar {

    private FlightGraph graph;

    public AStar(FlightGraph graph) {
        this.graph = graph;
    }

    // Heuristic: straight-line distance cost estimate to destination
    private double heuristic(String code, String dest) {
        Airport a = graph.getAirport(code);
        Airport b = graph.getAirport(dest);
        if (a == null || b == null) return 0;
        return graph.haversine(a, b) * 0.12; // cost estimate: $0.12 per km
    }

    public List<String> findPath(String src, String dest) {
        Map<String, Double> gScore = new HashMap<>(); // cost from src to node
        Map<String, Double> fScore = new HashMap<>(); // gScore + heuristic
        Map<String, String> prev   = new HashMap<>();
        Set<String> visited        = new HashSet<>();

        // Step 1: Initialize
        for (String code : graph.getAllCodes()) {
            gScore.put(code, Double.MAX_VALUE);
            fScore.put(code, Double.MAX_VALUE);
        }
        gScore.put(src, 0.0);
        fScore.put(src, heuristic(src, dest));

        PriorityQueue<String> open = new PriorityQueue<>(
            Comparator.comparingDouble(fScore::get)
        );
        open.add(src);

        // Step 2: Explore
        while (!open.isEmpty()) {
            String u = open.poll();

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u.equals(dest)) break;

            for (Route r : graph.getNeighbors(u)) {
                String v         = r.getDestCode();
                double tentative = gScore.get(u) + r.getCost();

                if (tentative < gScore.get(v)) {
                    gScore.put(v, tentative);
                    fScore.put(v, tentative + heuristic(v, dest));
                    prev.put(v, u);
                    open.add(v);
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