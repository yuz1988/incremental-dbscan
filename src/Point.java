
public class Point {

    final double[] position;

    int pointIndex;

    int clusterIndex;

    int epsNbrNum;

    boolean visited;

    final static int NOISE = -1;

    public Point(final double[] position, int pointIndex) {
        this.visited = false;
        this.pointIndex = pointIndex;   // point index
        this.position = position;
        this.clusterIndex = NOISE;  // initially a noise point
        this.epsNbrNum = -1;  // number of eps-neighbors around me
    }

    public double euclidDist(Point p1) {
        double sumDistSq = 0;
        int d = position.length;
        for (int i=0; i<d; i++) {
            sumDistSq += (position[i] - p1.position[i]) * (position[i] - p1.position[i]);
        }
        return Math.sqrt(sumDistSq);
    }
}
