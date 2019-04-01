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
                expandCluster(point, neighbors, points);
                clusterGlobalID++;
            } else {
                // noise point temporarily, may become border point later
                point.clusterIndex = Point.NOISE;
            }
        }
    }

    public void incrementalUpdate(final Point newPoint,
                                  final List<Point> points) {
        List<Point> candidates = new ArrayList<>();
        final List<Point> neighbors = getNeighbors(newPoint, points);
        for (Point nbr : neighbors) {
            if (nbr.epsNbrNum == (minPts - 1)) {
                candidates.add(nbr);
            }
        }

        List<Point> updateSeed = new ArrayList<>();
        for (Point q_Prime : candidates) {
            List<Point> q_Prime_Neighbors = getNeighbors(q_Prime, points);
            for (Point q : q_Prime_Neighbors) {
                int numEpsNbrs = q.epsNbrNum;
                if (q.euclidDist(newPoint) <= eps) {
                    numEpsNbrs++;
                }
                if (numEpsNbrs >= minPts) {
                    updateSeed.add(q);
                }
            }
        }

        // Different cases based on the updateSeed
        if (updateSeed.isEmpty()) {

        }
    }

    /**
     * Expands the cluster to include all density-reachable points.
     *
     * @param point     starting core point
     * @param neighbors point's neighbors
     * @param points    all point set
     */
    private void expandCluster(final Point point, final List<Point> neighbors,
                               final List<Point> points) {
        List<Point> seeds = new ArrayList<>(neighbors);
        int index = 0;
        while (index < seeds.size()) {
            final Point current = seeds.get(index);
            // only check non-visited points
            if (!current.visited) {
                current.visited = true;
                current.clusterIndex = clusterGlobalID;
                final List<Point> currentNeighbors = getNeighbors(current, points);

                // current point is a density-connected core point
                if (currentNeighbors.size() >= minPts) {
                    for (Point currentNbr : currentNeighbors) {
                        if (!currentNbr.visited) {
                            seeds.add(currentNbr);
                        }
                    }
                }
            }

            // assign cluster ID to boarder point
            if (current.clusterIndex == Point.NOISE) {
                current.visited = true;
                current.clusterIndex = clusterGlobalID;
            }

            index++;
        }
    }


    /**
     * Return a list of density-reachable neighbors of a {@code point}
     *
     * @param point  the point to look for
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
        // add number of eps-neighbors for each point
        point.epsNbrNum = neighbors.size();

        return neighbors;
    }

}
