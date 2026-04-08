package ui;

import algorithms.AStar;
import algorithms.Dijkstra;
import graph.Airport;
import graph.FlightGraph;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

public class FlightMapUI extends Application {

    private static FlightGraph graph;
    private Canvas canvas;
    private Label statusLabel;

    // Call this before launch()
    public static void setGraph(FlightGraph g) {
        graph = g;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Flight Route Optimization System");

        // ── Controls ──
        TextField srcField  = new TextField();
        srcField.setPromptText("Source (e.g. DEL)");

        TextField destField = new TextField();
        destField.setPromptText("Destination (e.g. LHR)");

        ComboBox<String> algoBox = new ComboBox<>();
        algoBox.getItems().addAll("Dijkstra", "A*");
        algoBox.setValue("Dijkstra");

        Button findBtn = new Button("Find Route");
        findBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                         "-fx-font-weight: bold; -fx-padding: 6 16;");

        statusLabel = new Label("Enter airports and click Find Route.");
        statusLabel.setStyle("-fx-font-size: 13px;");

        HBox controls = new HBox(10, srcField, destField, algoBox, findBtn);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(12));

        // ── Canvas ──
        canvas = new Canvas(1100, 550);
        drawWorldMap();

        // ── Layout ──
        VBox root = new VBox(controls, canvas, statusLabel);
        VBox.setMargin(statusLabel, new Insets(6, 0, 6, 16));
        root.setStyle("-fx-background-color: #0f172a;");

        // ── Button Action ──
        findBtn.setOnAction(e -> {
            String src  = srcField.getText().trim().toUpperCase();
            String dest = destField.getText().trim().toUpperCase();
            String algo = algoBox.getValue();

            if (!graph.hasAirport(src) || !graph.hasAirport(dest)) {
                statusLabel.setText("Invalid airport code(s). Try IATA codes like DEL, LHR, JFK.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            List<String> path = algo.equals("A*")
                ? new AStar(graph).findPath(src, dest)
                : new Dijkstra(graph).findShortestPath(src, dest);

            drawWorldMap();
            if (path.isEmpty()) {
                statusLabel.setText("No route found between " + src + " and " + dest + ".");
                statusLabel.setStyle("-fx-text-fill: orange;");
            } else {
                drawPath(path);
                statusLabel.setText(algo + " route: " + String.join(" → ", path) +
                                    "  |  Hops: " + (path.size() - 1));
                statusLabel.setStyle("-fx-text-fill: #4ade80;");
            }
        });

        stage.setScene(new Scene(root));
        stage.show();
    }

    // ── Drawing Helpers ────────────────────────────────────

    private void drawWorldMap() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw all airports as faint dots
        gc.setFill(Color.web("#334155", 0.6));
        for (Airport a : graph.getAllAirports()) {
            double[] xy = project(a.getLatitude(), a.getLongitude());
            gc.fillOval(xy[0] - 1, xy[1] - 1, 2, 2);
        }
    }

    private void drawPath(List<String> path) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw route edges
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(2.0);

        for (int i = 0; i < path.size() - 1; i++) {
            Airport a = graph.getAirport(path.get(i));
            Airport b = graph.getAirport(path.get(i + 1));
            if (a == null || b == null) continue;

            double[] from = project(a.getLatitude(), a.getLongitude());
            double[] to   = project(b.getLatitude(), b.getLongitude());
            gc.strokeLine(from[0], from[1], to[0], to[1]);
        }

        // Draw airport nodes on path
        for (String code : path) {
            Airport a = graph.getAirport(code);
            if (a == null) continue;
            double[] xy = project(a.getLatitude(), a.getLongitude());

            gc.setFill(Color.web("#ef4444"));
            gc.fillOval(xy[0] - 5, xy[1] - 5, 10, 10);

            gc.setFill(Color.WHITE);
            gc.fillText(code, xy[0] + 7, xy[1] + 4);
        }
    }

    // Equirectangular projection: lat/lon → canvas x/y
    private double[] project(double lat, double lon) {
        double x = (lon + 180) / 360.0 * canvas.getWidth();
        double y = (90 - lat)  / 180.0 * canvas.getHeight();
        return new double[]{x, y};
    }
}