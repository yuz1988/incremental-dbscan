
public class Point {

    final double[] position;

    final int pointIndex;

    final int label;

    int clusterIndex;

    int epsNbrNum;

    boolean visited;

    final static int NOISE = -1;

    public Point(final double[] position, int pointIndex, int label) {
        this.pointIndex = pointIndex;   // point index
        this.position = position;    // point position
        this.label = label;  // true cluster label of point

        this.visited = false;
        this.clusterIndex = NOISE;  // initially a noise point
        this.epsNbrNum = 1;  // number of eps-neighbors around me
    }

    public double euclidDist(Point p1) {
        double sumDistSq = 0.0;
        int d = position.length;
        for (int i = 0; i < d; i++) {
            sumDistSq += (position[i] - p1.position[i]) * (position[i] - p1.position[i]);
        }
        return Math.sqrt(sumDistSq);
    }
}
