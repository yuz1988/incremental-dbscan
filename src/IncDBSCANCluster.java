import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class IncDBSCANCluster {

    private List<Point> points;   // data warehouse storing all the points

    private final double eps;  // maximum radius of the neighborhood to be
    // considered

    private final int minPts;  // minimum number of points needed for a cluster

    private int clusterGlobalID; // cluster unique ID, start from 0

    private int cntOfNbrSearch;  // number of "getEpsNeighbors" operations
    // per incrementally update

    HashMap<Integer, Integer> clusterMapping;  // cluster parent tree

    public IncDBSCANCluster(final double eps, final int minPts) {
        if (eps < 0.0 || minPts < 1) {
            throw new IllegalArgumentException("DBSCAN param cannot be " +
                    "negative");
        }

        this.eps = eps;
        this.minPts = minPts;
        this.points = new ArrayList<>();
        this.clusterMapping = new HashMap<>();
        clusterGlobalID = 0;
        cntOfNbrSearch = 0;
    }

    /**
     * Incrementally update with a new point
     *
     * @param newPoint
     */
    public void incrementalUpdate(Point newPoint) {
        points.add(newPoint);
        cntOfNbrSearch = 0;

        // candidates contains q' points.
        List<Point> candidates = new ArrayList<>();
        List<Point> neighbors = getEpsNeighbors(newPoint);
        for (Point nbr : neighbors) {
            if (nbr == newPoint) {
                // add number of eps-neighbors for new point
                newPoint.epsNbrNum = neighbors.size();
                if (newPoint.epsNbrNum >= minPts) {
                    candidates.add(newPoint);
                }
            } else {
                // update number of neighbors.
                nbr.epsNbrNum++;
                // q' is core point in {D union p} but not in D.
                if (nbr.epsNbrNum == minPts) {
                    candidates.add(nbr);
                }
            }
        }

        // find UpdSeed_Ins, q is a core point in {D union p} and
        // q \in N_Eps(q')
        HashSet<Point> updateSeed = new HashSet<>();
        for (Point q_Prime : candidates) {
            List<Point> q_Prime_Neighbors = getEpsNeighbors(q_Prime);
            for (Point q : q_Prime_Neighbors) {
                if (q.epsNbrNum >= minPts) {
                    updateSeed.add(q);
                }
            }
        }

        for (Point p : updateSeed) {
            System.out.println("updateSeed " + p.toString());
        }

        // different cases based on the UpdSeed_Ins
        if (updateSeed.isEmpty()) {  // UpdSeed is empty, p is a noise point
            System.out.println("Upd is empty");
            newPoint.clusterIndex = Point.NOISE;
        } else {
            // set contains only non-noise cluster index.
            HashSet<Integer> clusterIdSet = new HashSet<>();
            for (Point seed : updateSeed) {
                if (seed.clusterIndex != Point.NOISE) {
                    int rootClusterID = findRootClusterID(seed.clusterIndex);
                    clusterIdSet.add(rootClusterID);
                }
            }

            if (clusterIdSet.isEmpty()) {
                System.out.println("All seeds are noise");
                // case 1: all seeds were noise before new point insertion,
                // a new cluster containing these noise objects as well as
                // new point is created.
                for (Point seed : updateSeed) {
                    expandCluster(seed, clusterGlobalID);
                    clusterMapping.put(clusterGlobalID, clusterGlobalID);
                }
                clusterGlobalID++;
            } else if (clusterIdSet.size() == 1) {
                System.out.println("All seeds are one cluster");
                // retrieve the unique cluster id.
                int uniqueClusterID = -1;
                for (int id : clusterIdSet) {
                    uniqueClusterID = id;
                }
                // case 2: seeds contain core points of exactly one cluster
                for (Point seed : updateSeed) {
                    expandCluster(seed, uniqueClusterID);
                }
            } else {
                System.out.println("All seeds are different clusters");
                // case 3: seeds contains several clusters, merge these clusters
                newPoint.clusterIndex = clusterGlobalID;
                for (int id : clusterIdSet) {
                    clusterMapping.put(id, clusterGlobalID);
                }
                clusterMapping.put(clusterGlobalID, clusterGlobalID);
                clusterGlobalID++;
            }
        }

        newPoint.visited = true;
    }

    /**
     * Get the number of neighbor search operations.
     *
     * @return
     */
    public int getCntOfNbrSearch() {
        return cntOfNbrSearch;
    }

    /**
     * Find root of the tree given a cluster index.
     *
     * @param id
     * @return
     */
    private int findRootClusterID(int id) {
        int root = id;
        if (!clusterMapping.containsKey(root)) {
            System.out.println("Error: " + root);
        }
        while (clusterMapping.get(root) != root) {
            root = clusterMapping.get(root);
            if (!clusterMapping.containsKey(root)) {
                System.out.println("Error: " + root);
            }
        }

        // path compression
        while (id != root) {
            int parent = clusterMapping.get(id);
            clusterMapping.put(id, root);
            id = parent;
        }
        return root;
    }

    /**
     * Expands the cluster to include all density-reachable points.
     * Mark all the density-reachable noise points with the cluster id.
     *
     * @param seed starting core point
     * @param clusterId new cluster id
     */
    private void expandCluster(Point seed, int clusterId) {
        List<Point> seeds = getEpsNeighbors(seed);
        int index = 0;
        while (index < seeds.size()) {
            Point current = seeds.get(index);
            // only check noise points
            if (current.clusterIndex == Point.NOISE) {
                current.clusterIndex = clusterId;
                List<Point> currentNeighbors = getEpsNeighbors(current);

                // add noisy density-connected points
                if (currentNeighbors.size() >= minPts) {
                    for (Point currentNbr : currentNeighbors) {
                        if (currentNbr.clusterIndex == Point.NOISE) {
                            seeds.add(currentNbr);
                        }
                    }
                }
            }
            index++;
        }
    }

    /**
     * Return a list of density-reachable neighbors of a {@code point}
     *
     * @param point the point to look for
     * @return neighbors (including point itself)
     */
    private List<Point> getEpsNeighbors(final Point point) {
        final List<Point> neighbors = new ArrayList<>();
        for (final Point p : points) {
            // include point itself
            if (point.euclidDist(p) <= eps) {
                neighbors.add(p);
            }
        }
        cntOfNbrSearch++;
        return neighbors;
    }
}
