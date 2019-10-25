import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Batch DBSCAN clusterer.
 */
public class DBSCANCluster {

    private final double eps;  // Maximum radius of the neighborhood to be
    // considered

    private final int minPts;  // Minimum number of points needed for a cluster

    private int clusterGlobalID; // cluster unique ID, start from 0

    HashMap<Integer, Integer> clusterMapping;  // cluster parent tree

    public DBSCANCluster(final double eps, final int minPts) {
        if (eps < 0.0 || minPts < 1) {
            throw new IllegalArgumentException("DBSCAN param cannot be " +
                    "negative");
        }

        this.eps = eps;
        this.minPts = minPts;
        this.clusterMapping = new HashMap<>();
        clusterGlobalID = 0;
    }

    /**
     * Batch DBSCAN clustering algorithm.
     * The clustering result is each point is labelled
     * with either a cluster index or noise.
     *
     * @param points all point set
     */
    public void cluster(final List<Point> points) {
        for (final Point point : points) {
            if (point.visited) {
                continue;
            }
            point.visited = true;
            final List<Point> neighbors = getNeighbors(point, points);

            if (neighbors.size() >= minPts) {
                point.clusterIndex = clusterGlobalID;
                expandCluster(point, neighbors, points, clusterGlobalID);
                clusterMapping.put(clusterGlobalID, clusterGlobalID);
                clusterGlobalID++;
            } else {
                // noise point temporarily, may become border point later
                point.clusterIndex = Point.NOISE;
            }
        }
    }

    /**
     * Expands the cluster to include all density-reachable points.
     *
     * @param point     starting core point
     * @param neighbors point's neighbors
     * @param points    all point set
     * @param clusterId new cluster id
     */
    private void expandCluster(final Point point, final List<Point> neighbors
            , final List<Point> points, int clusterId) {
        List<Point> seeds = new ArrayList<>(neighbors);
        int index = 0;
        while (index < seeds.size()) {
            final Point current = seeds.get(index);
            // only check non-visited points
            if (!current.visited) {
                current.visited = true;
                current.clusterIndex = clusterId;
                final List<Point> currentNeighbors = getNeighbors(current,
                        points);

                // current point is a density-connected core point
                if (currentNeighbors.size() >= minPts) {
                    for (Point currentNbr : currentNeighbors) {
                        seeds.add(currentNbr);
                    }
                }
            }

            // assign cluster ID to boarder point
            if (current.clusterIndex == Point.NOISE) {
                current.visited = true;
                current.clusterIndex = clusterId;
            }

            index++;
        }
    }

    /**
     * Return a list of density-reachable neighbors of a {@code point}
     *
     * @param point  the point to look for
     * @param points all points
     * @return neighbors (including point itself)
     */
    private List<Point> getNeighbors(final Point point,
                                     final List<Point> points) {
        final List<Point> neighbors = new ArrayList<>();
        for (final Point p : points) {
            // include point itself
            if (point.euclidDist(p) <= eps) {
                neighbors.add(p);
            }
        }
        // add number of eps-neighbors for each point
        point.epsNbrNum = neighbors.size();

        return neighbors;
    }
}
