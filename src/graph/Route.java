package graph;

public class Route {
    private String sourceCode;
    private String destCode;
    private double cost;        
    private double duration;    
    private int stops;          

    public Route(String sourceCode, String destCode, double cost, double duration, int stops) {
        this.sourceCode = sourceCode;
        this.destCode   = destCode;
        this.cost       = cost;
        this.duration   = duration;
        this.stops      = stops;
    }

    public String getSourceCode() { return sourceCode; }
    public String getDestCode()   { return destCode; }
    public double getCost()       { return cost; }
    public double getDuration()   { return duration; }
    public int    getStops()      { return stops; }

    // Multi-criteria weight: 60% cost (normalized) + 40% time
    public double getWeight(double maxCost, double maxDuration) {
        double normCost = (maxCost > 0) ? cost / maxCost : 0;
        double normTime = (maxDuration > 0) ? duration / maxDuration : 0;
        return 0.6 * normCost + 0.4 * normTime;
    }

    @Override
    public String toString() {
        return sourceCode + " → " + destCode +
               " | $" + cost + " | " + duration + "h | stops: " + stops;
    }
}