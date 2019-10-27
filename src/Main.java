import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        double eps = 35.0;
        int minPts = 8;

        String inFn = "./src/covtype_train";
        String batchOut = "./src/batch-time.txt";
        String incOut = "./src/inc-time.txt";
        int numPointsToRead = 100000;
        List<Point> points = readData(inFn, numPointsToRead);
        System.out.println("Reading data complete");

//        // Batch for each new point.
//        FileWriter batchWriter = new FileWriter(new File(batchOut));
//        DBSCANCluster batchCluster = new DBSCANCluster(eps, minPts);
//        List<Point> list = new ArrayList<>();
//        int iter = 0;
//        long start = System.nanoTime();
//        for (Point p : points) {
//            list.add(p);
//            batchCluster.cluster(list);
//            long end = System.nanoTime();
//            double time = (end - start) / 1e9d;
//            if (iter % 100 == 0) {
//                System.out.println("Number of iter: " + iter);
//                batchWriter.write(time + "\n");
//            }
//            iter++;
//        }
//
//        batchWriter.close();
//        // find how many clusters in total
//        countNumClusters(batchCluster.clusterMapping);

        // Incremental DBSCAN.
        FileWriter incWriter = new FileWriter(new File(incOut));
        IncDBSCANCluster incCluster = new IncDBSCANCluster(eps, minPts);
        int iter = 0;
        long start = System.nanoTime();
        for (Point p : points) {
            incCluster.incrementalUpdate(p);
            long end = System.nanoTime();
            double time = (end - start) / 1e9d;
            if (iter % 100 == 0) {
                System.out.println("Processed points: " + iter);
                incWriter.write(time + "\n");
            }
            iter++;
        }

        incWriter.close();
        countNumClusters(incCluster.clusterMapping);
    }

    private static List<Point> readData(String inFileName,
                                        int numPointsToRead) {
        List<Point> points = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File(inFileName));
            int numPoints = 0;
            while(sc.hasNextLine() && numPoints<numPointsToRead) {
                String line = sc.nextLine();
                String[] strs = line.split(",");
                int d = strs.length;
                double[] pos = new double[d];
                for (int i=0; i<d; i++) {
                    pos[i] = Double.parseDouble(strs[i]);
                }
                Point p = new Point(pos, numPoints, -1);
                points.add(p);
                numPoints++;
            }
            sc.close();
        } catch (Exception e) {
            System.out.println("read input exception");
        }
        return points;
    }


    private static void countNumClusters(HashMap<Integer, Integer> map) {
        HashSet<Integer> set = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getKey() == entry.getValue()) {
                set.add(entry.getKey());
            }
        }
        System.out.println(set.size());
    }
}
