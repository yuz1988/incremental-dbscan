import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBSCANCluster {

    private final double eps;  // Maximum radius of the neighborhood to be considered

    private final int minPts;  // Minimum number of points needed for a cluster

    private int clusterID; // cluster unique ID, start from 0

    public DBSCANCluster(final double eps, final int minPts) {
        if (eps < 0.0d || minPts < 0) {
            throw new IllegalArgumentException("DBSCAN param cannot be negative");
        }

        this.eps = eps;
        this.minPts = minPts;
        clusterID = 0;
    }

    public void cluster(final List<Point> points) {
        for (final Point point : points) {
            if (!point.visited) {
                continue;
            }
            final List<Point> neighbors = getNeighbors(point, points);
            if (neighbors.size() >= minPts) {
                expandCluster(point, neighbors, points);
                clusterID++;
            } else {
                point.visited = true;
            }
        }

    }

    private void expandCluster(final Point point, final List<Point> neighbors, final List<Point> points) {
        point.clusterIndex = clusterID;
        point.visited = true;

        List<Point> seeds = new ArrayList<Point>(neighbors);
        int index = 0;
        while (index < seeds.size()) {
            final Point current = seeds.get(index);
            // only check non-visited points
            if (!current.visited) {
                final List<Point> currentNeighbors = getNeighbors(current, points);
                // density-connected core point
                if (currentNeighbors.size() >= minPts) {
                    seeds = merge(seeds, currentNeighbors);
                }
            }

            if (current.clusterIndex == Point.NOISE) {
                current.clusterIndex = clusterID;
            }

            index++;
        }
    }

    private List<Point> getNeighbors(final Point point, final List<Point> points) {
        final List<Point> neighbors = new ArrayList<Point>();
        for (final Point neighbor : points) {
            if (point != neighbor && point.euclidDist(neighbor) <= eps) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    private List<Point> merge(final List<Point> one, final List<Point> two) {
        final Set<Point> oneSet = new HashSet<Point>(one);
        for (Point item : two) {
            if (!oneSet.contains(item)) {
                one.add(item);
            }
        }
        return one;
    }

}
