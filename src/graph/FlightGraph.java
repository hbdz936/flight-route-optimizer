package graph;

import java.io.*;
import java.util.*;

public class FlightGraph {
    private Map<String, Airport> airports = new HashMap<>();
    private Map<String, List<Route>> adjList = new HashMap<>();
    private Set<String> disabledRoutes = new HashSet<>(); // for disruption simulation

    //Data Loading

    public void loadAirports(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (p.length < 8) continue;
            try {
                String code = clean(p[4]);
                if (code.isEmpty() || code.equals("\\N")) continue;
                String name    = clean(p[1]);
                String city    = clean(p[2]);
                String country = clean(p[3]);
                double lat     = Double.parseDouble(clean(p[6]));
                double lon     = Double.parseDouble(clean(p[7]));
                Airport a = new Airport(code, name, city, country, lat, lon);
                airports.put(code, a);
                adjList.put(code, new ArrayList<>());
            } catch (Exception ignored) {}
        }
        br.close();
        System.out.println("Loaded " + airports.size() + " airports.");
    }

    public void loadRoutes(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",", -1);
            if (p.length < 9) continue;
            try {
                String src  = clean(p[2]);
                String dest = clean(p[4]);
                int stops   = Integer.parseInt(clean(p[7]));
                if (!airports.containsKey(src) || !airports.containsKey(dest)) continue;

                // Estimate cost & duration from haversine distance
                double dist = haversine(airports.get(src), airports.get(dest));
                double cost = 50 + dist * 0.12;       // ~$0.12/km base
                double duration = 1.0 + dist / 800.0; // ~800 km/h cruise speed

                Route r = new Route(src, dest, cost, duration, stops);
                adjList.get(src).add(r);
                count++;
            } catch (Exception ignored) {}
        }
        br.close();
        System.out.println("Loaded " + count + " routes.");
    }

    //Graph Operations

    public List<Route> getNeighbors(String airportCode) {
        List<Route> routes = adjList.getOrDefault(airportCode, new ArrayList<>());
        List<Route> active = new ArrayList<>();
        for (Route r : routes) {
            if (!disabledRoutes.contains(routeKey(r.getSourceCode(), r.getDestCode())))
                active.add(r);
        }
        return active;
    }

    public Airport getAirport(String code) { return airports.get(code); }

    public Set<String> getAllCodes() { return airports.keySet(); }

    public boolean hasAirport(String code) { return airports.containsKey(code); }

    public void disableRoute(String src, String dest) {
        disabledRoutes.add(routeKey(src, dest));
        System.out.println("Route " + src + " → " + dest + " disabled.");
    }

    public void enableRoute(String src, String dest) {
        disabledRoutes.remove(routeKey(src, dest));
        System.out.println("Route " + src + " → " + dest + " re-enabled.");
    }

    public Map<String, List<Route>> getAdjList() { return adjList; }

    public Collection<Airport> getAllAirports() { return airports.values(); }

    //Helpers

    private String clean(String s) {
        return s.replaceAll("^\"|\"$", "").trim();
    }

    private String routeKey(String src, String dest) {
        return src + ":" + dest;
    }

    public double haversine(Airport a, Airport b) {
        final int R = 6371;
        double dLat = Math.toRadians(b.getLatitude()  - a.getLatitude());
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());
        double hav  = Math.sin(dLat/2) * Math.sin(dLat/2)
                    + Math.cos(Math.toRadians(a.getLatitude()))
                    * Math.cos(Math.toRadians(b.getLatitude()))
                    * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(hav), Math.sqrt(1 - hav));
    }
}