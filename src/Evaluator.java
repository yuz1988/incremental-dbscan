import java.util.*;

/**
 * Clustering result.
 */
class StatRes {
    public double f1Score, purity;
}

/**
 * Clustering quality evaluator.
 */
public class Evaluator {

    public StatRes f1Score(final List<Point> points) {
        // Group points by true class labels.
        // key: true class label, value: points
        HashMap<Integer, List<Point>> classLabelMap = new HashMap<>();

        // Group points by cluster index.
        // key: cluster index, value: points
        HashMap<Integer, List<Point>> clusterIndexMap = new HashMap<>();

        for (Point p : points) {
            if (!classLabelMap.containsKey(p.label)) {
                classLabelMap.put(p.label, new ArrayList<Point>());
            }
            classLabelMap.get(p.label).add(p);

            if (!clusterIndexMap.containsKey(p.clusterIndex)) {
                clusterIndexMap.put(p.clusterIndex, new ArrayList<>());
            }
            clusterIndexMap.get(p.clusterIndex).add(p);
        }

        // Compute F1-score and purity.
        double f1 = 0.0;
        double purity = 0.0;
        for (int clusterIndex : clusterIndexMap.keySet()) {
            // Find dominant class in the list.
            List<Point> members = clusterIndexMap.get(clusterIndex);
            int[] arr = findDominant(members);
            int dominantLabel = arr[0];
            int dominantLabelNum = arr[1];

            List<Point> dominantClusterMembers =
                    classLabelMap.get(dominantLabel);
            purity += dominantLabelNum / members.size();
            f1 += (2.0 * dominantLabelNum) / (members.size() + dominantClusterMembers.size());
        }

        StatRes res = new StatRes();
        res.f1Score = f1 / clusterIndexMap.size();
        res.purity = purity / clusterIndexMap.size();
        return res;
    }


    /**
     * Compute the number of points with dominant class label (ground truth).
     *
     * @param members
     * @return dominant class label and frequency.
     */
    private int[] findDominant(List<Point> members) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (Point p : members) {
            map.put(p.label, map.getOrDefault(p.label, 0) + 1);
        }

        int dominantLabel = -1;
        int maxFreq = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxFreq) {
                dominantLabel = entry.getKey();
                maxFreq = entry.getValue();
            }
        }
        return new int[]{dominantLabel, maxFreq};
    }
}
