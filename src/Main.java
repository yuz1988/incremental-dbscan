import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        double eps = 6.0d;
        int minPts = 3;

        String inFn = "./src/power.txt";
        String outFn = "./src/time.txt";

        List<Point> points = readData(inFn);
        DBSCANCluster dbsCluster = new DBSCANCluster(eps, minPts);

        long start = System.nanoTime();
        dbsCluster.cluster(points);
        long end = System.nanoTime();
        double time = (end - start) / 1e9d;
        System.out.println("Time elapsed: " + time);

        // data point stream
        String streamFn = "./src/stream.txt";
        int numPoints = 5001;
        Scanner sc = new Scanner(new File(streamFn));
        FileWriter fw = new FileWriter(new File(outFn));
        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] strs = line.split(",");
            double[] pos = new double[7];
            for (int i=0; i<7; i++) {
                pos[i] = Double.parseDouble(strs[i]);
            }
            Point p = new Point(pos, numPoints);
            numPoints++;

            // batch clustering
            points.add(p);
            for (Point p1 : points) {
                p1.clusterIndex = Point.NOISE;
                p1.visited = false;
            }
            start = System.nanoTime();
            dbsCluster.cluster(points);
            end = System.nanoTime();
            time = (end - start) / 1e9d;
            fw.write(time + "\n");
            fw.flush();
        }
        sc.close();
        fw.close();

        // find how many clusters in total
        countNumClusters(dbsCluster.clusterMapping);
    }

    private static List<Point> readData(String inFileName) {
        List<Point> points = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File(inFileName));
            int numPoints = 0;
            while(sc.hasNextLine() && numPoints<5000) {
                String line = sc.nextLine();
                String[] strs = line.split(",");
                double[] pos = new double[7];
                for (int i=0; i<7; i++) {
                    pos[i] = Double.parseDouble(strs[i]);
                }
                Point p = new Point(pos, numPoints);
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
        System.out.println("cluters in total: " + set.size());
    }
}
