import graph.FlightGraph;
import cli.CLIHandler;
import ui.FlightMapUI;
import javafx.application.Application;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        // ── Load Data ──
        FlightGraph graph = new FlightGraph();
        System.out.println("Loading airport and route data...");
        graph.loadAirports("data/airports.dat");
        graph.loadRoutes("data/routes.dat");

        // ── Choose Mode ──
        Scanner sc = new Scanner(System.in);
        System.out.println("\nLaunch mode:");
        System.out.println("  1. CLI");
        System.out.println("  2. UI (JavaFX)");
        System.out.print("Choice: ");
        String choice = sc.nextLine().trim();

        if (choice.equals("2")) {
            FlightMapUI.setGraph(graph);
            Application.launch(FlightMapUI.class, args);
        } else {
            new CLIHandler(graph).start();
        }
    }
}