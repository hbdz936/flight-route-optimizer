package algorithms;

import graph.FlightGraph;
import graph.Route;

import java.util.*;

public class FloydWarshall {

    private FlightGraph graph;
    private double[][] dist;
    private int[][] next;
    private List<String> codes;

    public FloydWarshall(FlightGraph graph) {
        this.graph = graph;
    }

    public void compute() {
        codes    = new ArrayList<>(graph.getAllCodes());
        int V    = codes.size();
        dist     = new double[V][V];
        next     = new int[V][V];

        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < V; i++) index.put(codes.get(i), i);

        // Step 1: Initialize matrix
        for (double[] row : dist) Arrays.fill(row, Double.MAX_VALUE / 2);
        for (int[] row : next)   Arrays.fill(row, -1);
        for (int i = 0; i < V; i++) dist[i][i] = 0;

        // Step 2: Fill in direct edges
        for (String src : codes) {
            int u = index.get(src);
            for (Route r : graph.getNeighbors(src)) {
                int v = index.getOrDefault(r.getDestCode(), -1);
                if (v == -1) continue;
                dist[u][v] = r.getCost();
                next[u][v] = v;
            }
        }

        // Step 3: Relax through every intermediate node k
        for (int k = 0; k < V; k++)
            for (int i = 0; i < V; i++)
                for (int j = 0; j < V; j++)
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
    }

    public List<String> getPath(String src, String dest) {
        List<String> allCodes = new ArrayList<>(graph.getAllCodes());
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < allCodes.size(); i++) index.put(allCodes.get(i), i);

        int u = index.getOrDefault(src, -1);
        int v = index.getOrDefault(dest, -1);
        if (u == -1 || v == -1 || next[u][v] == -1) return Collections.emptyList();

        List<String> path = new ArrayList<>();
        path.add(codes.get(u));
        while (u != v) {
            u = next[u][v];
            path.add(codes.get(u));
        }
        return path;
    }
}