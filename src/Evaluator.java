import java.util.*;

/**
 * Clustering quality evaluator.
 */
public class Evaluator {
    public double SSQ(final List<Point> points) {
        // TODO
        return 0.0;
    }

    /**
     * Compute the purity of clustering result.
     * See section 6.1 in DenStream paper.
     *
     * @param points
     * @return
     */
    public double purity(final List<Point> points) {
        // Group points by true labels.
        // key: true label, value: points
        HashMap<Integer, List<Point>> map = new HashMap<>();
        for (Point p : points) {
            if (!map.containsKey(p.label)) {
                map.put(p.label, new ArrayList<Point>());
            }
            map.get(p.label).add(p);
        }

        double sum = 0.0;
        for (int label : map.keySet()) {
            // Find dominant class in the list.
            List<Point> members = map.get(label);
            int maxClass = findDominant(members);
            sum += ((double) maxClass) / members.size();
        }

        // Return average purity of clusters.
        return sum / map.size();
    }

    /**
     * Compute the number of points with dominant class label.
     * @param members
     * @return
     */
    private int findDominant(List<Point> members) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (Point p : members) {
            map.put(p.clusterIndex, map.getOrDefault(p.clusterIndex, 0) + 1);
        }
        int max = 0;
        for (int value : map.values()) {
            max = Math.max(max, value);
        }
        return max;
    }
}
