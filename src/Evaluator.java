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

    /**
     * Compute f1 score and purity.
     * Refer "6.4 External Measures 1: Matching-Based Measures" in "Cluster
     * Analysis in Data Mining" by Jiawei Han.
     * Purity score as Section 6.1 in Denstream paper.
     *
     * @param points
     * @return
     */
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

    public double nmi(List<Point> points) {
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

        int n = points.size();
        double entropyClustering = 0.0;  // H(C)
        double entropyTruth = 0.0;    // H(T)
        HashMap<Integer, Double> entropyClusteringMap = new HashMap<>();
        HashMap<Integer, Double> entropyTruthMap = new HashMap<>();

        // Compute entropy of clustering C and partitioning T.
        for (Map.Entry<Integer, List<Point>> entry :
                clusterIndexMap.entrySet()) {
            double prob = ((double) entry.getValue().size()) / n;
            double entropy = computeEntropy(prob);
            entropyClusteringMap.put(entry.getKey(), entropy);
            entropyClustering += entropy;
        }
        for (Map.Entry<Integer, List<Point>> entry : classLabelMap.entrySet()) {
            double prob = ((double) entry.getValue().size()) / n;
            double entropy = computeEntropy(prob);
            entropyTruthMap.put(entry.getKey(), entropy);
            entropyTruth += entropy;
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
                double prob = ((double)num) / n;

            }
        }

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
