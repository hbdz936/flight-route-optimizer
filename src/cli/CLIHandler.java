package cli;

import algorithms.AStar;
import algorithms.BellmanFord;
import algorithms.Dijkstra;
import algorithms.FloydWarshall;
import graph.FlightGraph;

import java.util.List;
import java.util.Scanner;

public class CLIHandler {

    private FlightGraph graph;
    private Scanner scanner;
    private FloydWarshall fw;
    private boolean fwComputed = false;

    public CLIHandler(FlightGraph graph) {
        this.graph   = graph;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   Flight Route Optimization System   ║");
        System.out.println("╚══════════════════════════════════════╝");

        while (true) {
            printMenu();
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> runDijkstra();
                case 2 -> runBellmanFord();
                case 3 -> runFloydWarshall();
                case 4 -> runAStar();
                case 5 -> runBenchmark();
                case 6 -> manageDisruption();
                case 0 -> {
                    System.out.println("Exiting. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n┌─────────────────────────────┐");
        System.out.println("│  1. Dijkstra (Cost/Time)    │");
        System.out.println("│  2. Bellman-Ford            │");
        System.out.println("│  3. Floyd-Warshall          │");
        System.out.println("│  4. A* Search               │");
        System.out.println("│  5. Benchmark All           │");
        System.out.println("│  6. Simulate Disruption     │");
        System.out.println("│  0. Exit                    │");
        System.out.println("└─────────────────────────────┘");
    }

    // ── Algorithm Runners ──────────────────────────────────

    private void runDijkstra() {
        String src  = readCode("Source airport code: ");
        String dest = readCode("Destination airport code: ");

        if (!validate(src, dest)) return;

        System.out.println("\n[Dijkstra] Finding cheapest route...");
        long start = System.currentTimeMillis();

        Dijkstra dijkstra = new Dijkstra(graph);
        List<String> path = dijkstra.findShortestPath(src, dest);

        long time = System.currentTimeMillis() - start;
        printResult("Dijkstra", path, time);
    }

    private void runBellmanFord() {
        String src  = readCode("Source airport code: ");
        String dest = readCode("Destination airport code: ");

        if (!validate(src, dest)) return;

        System.out.println("\n[Bellman-Ford] Finding route (supports discounts)...");
        long start = System.currentTimeMillis();

        BellmanFord bf    = new BellmanFord(graph);
        List<String> path = bf.findShortestPath(src, dest);

        long time = System.currentTimeMillis() - start;
        printResult("Bellman-Ford", path, time);
    }

    private void runFloydWarshall() {
        if (!fwComputed) {
            System.out.println("\n[Floyd-Warshall] Pre-computing all pairs... (this may take a moment)");
            fw = new FloydWarshall(graph);
            fw.compute();
            fwComputed = true;
        }

        String src  = readCode("Source airport code: ");
        String dest = readCode("Destination airport code: ");

        if (!validate(src, dest)) return;

        long start        = System.currentTimeMillis();
        List<String> path = fw.getPath(src, dest);
        long time         = System.currentTimeMillis() - start;

        printResult("Floyd-Warshall", path, time);
    }

    private void runAStar() {
        String src  = readCode("Source airport code: ");
        String dest = readCode("Destination airport code: ");

        if (!validate(src, dest)) return;

        System.out.println("\n[A*] Finding optimal route with heuristic...");
        long start = System.currentTimeMillis();

        AStar astar       = new AStar(graph);
        List<String> path = astar.findPath(src, dest);

        long time = System.currentTimeMillis() - start;
        printResult("A*", path, time);
    }

    // ── Benchmark ──────────────────────────────────────────

    private void runBenchmark() {
        String src  = readCode("Source airport code: ");
        String dest = readCode("Destination airport code: ");

        if (!validate(src, dest)) return;

        System.out.println("\n════════════ BENCHMARK RESULTS ════════════");
        System.out.printf("%-20s %-10s %-10s%n", "Algorithm", "Time(ms)", "Path Found");
        System.out.println("───────────────────────────────────────────");

        // Dijkstra
        long t1       = System.currentTimeMillis();
        List<String> d = new Dijkstra(graph).findShortestPath(src, dest);
        System.out.printf("%-20s %-10d %-10s%n",
            "Dijkstra", System.currentTimeMillis() - t1, d.isEmpty() ? "No" : "Yes");

        // Bellman-Ford
        long t2       = System.currentTimeMillis();
        List<String> b = new BellmanFord(graph).findShortestPath(src, dest);
        System.out.printf("%-20s %-10d %-10s%n",
            "Bellman-Ford", System.currentTimeMillis() - t2, b.isEmpty() ? "No" : "Yes");

        // A*
        long t3       = System.currentTimeMillis();
        List<String> a = new AStar(graph).findPath(src, dest);
        System.out.printf("%-20s %-10d %-10s%n",
            "A*", System.currentTimeMillis() - t3, a.isEmpty() ? "No" : "Yes");

        // Floyd-Warshall (pre-compute once)
        if (!fwComputed) {
            fw = new FloydWarshall(graph);
            fw.compute();
            fwComputed = true;
        }
        long t4       = System.currentTimeMillis();
        List<String> f = fw.getPath(src, dest);
        System.out.printf("%-20s %-10d %-10s%n",
            "Floyd-Warshall", System.currentTimeMillis() - t4, f.isEmpty() ? "No" : "Yes");

        System.out.println("═══════════════════════════════════════════");
    }

    // ── Disruption Simulation ──────────────────────────────

    private void manageDisruption() {
        System.out.println("\n1. Disable a route");
        System.out.println("2. Re-enable a route");
        int choice = readInt("Choice: ");

        String src  = readCode("Route source code: ");
        String dest = readCode("Route destination code: ");

        if (choice == 1) graph.disableRoute(src, dest);
        else             graph.enableRoute(src, dest);
    }

    // ── Helpers ────────────────────────────────────────────

    private void printResult(String algo, List<String> path, long timeMs) {
        System.out.println("\n── " + algo + " Result ──");
        if (path.isEmpty()) {
            System.out.println("No path found.");
        } else {
            System.out.println("Route : " + String.join(" → ", path));
            System.out.println("Hops  : " + (path.size() - 1));
        }
        System.out.println("Time  : " + timeMs + " ms");
    }

    private boolean validate(String src, String dest) {
        if (!graph.hasAirport(src)) {
            System.out.println("Airport not found: " + src);
            return false;
        }
        if (!graph.hasAirport(dest)) {
            System.out.println("Airport not found: " + dest);
            return false;
        }
        return true;
    }

    private String readCode(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim().toUpperCase();
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (Exception e) { return -1; }
    }
}