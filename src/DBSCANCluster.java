import java.util.*;

public class DBSCANCluster {

    private final double eps;  // Maximum radius of the neighborhood to be considered

    private final int minPts;  // Minimum number of points needed for a cluster

    private int clusterGlobalID; // cluster unique ID, start from 0

    public DBSCANCluster(final double eps, final int minPts) {
        if (eps < 0.0d || minPts < 0) {
            throw new IllegalArgumentException("DBSCAN param cannot be negative");
        }

        this.eps = eps;
        this.minPts = minPts;
        clusterGlobalID = 0;
    }

    public void cluster(final List<Point> points) {
        for (final Point point : points) {
            if (point.visited) {
                continue;
            }
            final List<Point> neighbors = getNeighbors(point, points);
            if (neighbors.size() >= minPts) {
                expandCluster(point, neighbors, points);
                clusterGlobalID++;
            } else {
                point.visited = true;   // noise point at current time, may become border point later
            }
        }

    }

    private void expandCluster(final Point point, final List<Point> neighbors, final List<Point> points) {
        point.clusterIndex = clusterGlobalID;
        point.visited = true;

        // push all neighbors (seeds) to stack
        Deque<Point> seeds = new ArrayDeque<>();
        for (Point p : neighbors) {
            seeds.push(p);
        }

        while (!seeds.isEmpty()) {
            final Point current = seeds.pop();
            // only check non-visited points
            if (!current.visited) {
                final List<Point> currentNeighbors = getNeighbors(current, points);
                // density-connected core point
                if (currentNeighbors.size() >= minPts) {
                    for (Point currentNbr : currentNeighbors) {
                        if (!currentNbr.visited) {
                            seeds.push(currentNbr);
                        }
                    }
                }
            }

            if (current.clusterIndex == Point.NOISE) {
                current.clusterIndex = clusterGlobalID;
            }
        }
    }


    /**
     * Return a list of density-reachable neighbors of a {@code point}
     *
     * @param point the point to look for
     * @param points all points
     * @return neighbors not including point itself
     */
    private List<Point> getNeighbors(final Point point, final List<Point> points) {
        final List<Point> neighbors = new ArrayList<>();
        for (final Point neighbor : points) {
            if (point != neighbor && point.euclidDist(neighbor) <= eps) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

}
