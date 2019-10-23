import java.util.*;

/**
 * Match-based Clustering result.
 */
class MatchBasedStatRes {
    public double f1Score, purity;
}

class PairwiseMetrics {
    public double jaccard, randStat, fowlkesMallow;
}

/**
 * Clustering quality evaluator.
 * Refer the "Cluster Analysis in Data Mining" by Jiawei Han.
 */
public class Evaluator {

    List<Point> points;

    // Group points by true class labels.
    // key: true class label, value: points
    HashMap<Integer, List<Point>> classLabelMap;

    // Group points by cluster index.
    // key: cluster index, value: points
    HashMap<Integer, List<Point>> clusterIndexMap;

    public Evaluator(final List<Point> points) {
        this.points = points;
        classLabelMap = new HashMap<>();
        clusterIndexMap = new HashMap<>();
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
    }

    /**
     * Compute f1 score and purity.
     * Refer "6.4 External Measures 1: Matching-Based Measures" in "Cluster
     * Analysis in Data Mining" by Jiawei Han.
     * Purity score as Section 6.1 in Denstream paper.
     *
     * @return
     */
    public MatchBasedStatRes f1Score() {
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
            purity += dominantLabelNum / points.size();
            f1 += (2.0 * dominantLabelNum) / (members.size() +
                    dominantClusterMembers.size());
        }

        MatchBasedStatRes res = new MatchBasedStatRes();
        res.f1Score = f1 / clusterIndexMap.size();
        res.purity = purity;
        return res;
    }

    /**
     * Compute normalized mutual information.
     * Refer "6.5 External Measures 2: Entropy-Based Measures" in "Cluster
     * Analysis in Data Mining" by Jiawei Han.
     *
     * @return
     */
    public double NMI() {
        int N = points.size();
        double clusteringEntropy = 0.0;      // H(C)
        double partitioningEntropy = 0.0;    // H(T)
        HashMap<Integer, Double> clusteringEntropyMap = new HashMap<>();
        HashMap<Integer, Double> partitioningEntropyMap = new HashMap<>();

        // Compute entropy of clustering C and partitioning T.
        for (Map.Entry<Integer, List<Point>> entry :
                clusterIndexMap.entrySet()) {
            double prob = ((double) entry.getValue().size()) / N;
            double entropy = computeEntropy(prob);
            clusteringEntropyMap.put(entry.getKey(), entropy);
            clusteringEntropy += entropy;
        }
        for (Map.Entry<Integer, List<Point>> entry : classLabelMap.entrySet()) {
            double prob = ((double) entry.getValue().size()) / N;
            double entropy = computeEntropy(prob);
            partitioningEntropyMap.put(entry.getKey(), entropy);
            partitioningEntropy += entropy;
        }

        // Compute the shared information between clustering C and
        // partitioning T.
        double mutualInfo = 0.0;
        for (Map.Entry<Integer, List<Point>> entry :
                clusterIndexMap.entrySet()) {
            int clusterIndex = entry.getKey();
            List<Point> cluster = entry.getValue();
            HashMap<Integer, Integer> map = new HashMap<>();
            for (Point p : cluster) {
                map.put(p.label, map.getOrDefault(p.label, 0) + 1);
            }

            for (Map.Entry<Integer, Integer> subEntry : map.entrySet()) {
                int label = subEntry.getKey();
                int num = subEntry.getValue();
                double prob = ((double) num) / N;
                mutualInfo += prob * Math.log(prob / (clusteringEntropyMap.
                        get(clusterIndex) * partitioningEntropyMap.get(label)));
            }
        }

        return mutualInfo / Math.sqrt(clusteringEntropy * partitioningEntropy);
    }

    /**
     * Compute pairwise measures.
     * Refer "6.6 External Measures 2: Entropy-Based Measures" in
     * "Cluster Analysis in Data Mining" by Jiawei Han.
     *
     * @return
     */
    public PairwiseMetrics pairwiseMetrics() {
        int tp = 0, fn = 0, fp = 0, tn = 0;
        for (List<Point> cluster : clusterIndexMap.values()) {
            HashMap<Integer, Integer> map = new HashMap<>();
            for (Point p : cluster) {
                map.put(p.label, map.getOrDefault(p.label, 0) + 1);
            }
            for (int nij : map.values()) {
                tp += nij * (nij - 1) / 2;
            }
            fp += cluster.size() * (cluster.size() - 1) / 2;
        }
        fp -= tp;

        for (List<Point> partition : classLabelMap.values()) {
            fn += partition.size() * (partition.size() - 1) / 2;
        }
        fn -= tp;
        tn = points.size() - (tp + fp + fn);

        PairwiseMetrics pairwise = new PairwiseMetrics();
        pairwise.jaccard = ((double) tp) / (tp + fn + fp);
        pairwise.randStat = ((double) tp + tn) / points.size();
        pairwise.fowlkesMallow =
                ((double) tp) / Math.sqrt((tp + fn) * (tp + fp));
        return pairwise;
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

    /**
     * Compute entropy of probability.
     *
     * @param prob
     * @return
     */
    private double computeEntropy(double prob) {
        return -1.0 * prob * Math.log(prob);
    }
}
