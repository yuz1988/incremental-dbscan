import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        double eps = 1.0d;
        int minPts = 1;

        String inFn = "./src/accident_data.txt";

        List<Point> points = readData(inFn);
        DBSCANCluster dbsCluster = new DBSCANCluster(eps, minPts);

        long start = System.nanoTime();
        dbsCluster.cluster(points);
        long end = System.nanoTime();
        double time = (end - start) / 1e9d;
        System.out.println("Time elapsed: " + time);

        // Print out clustering result of each point.
        for (Point p : points) {
//            System.out.println("cluster index " + p.clusterIndex);
            System.out.println("number of neighbors " + p.epsNbrNum);
        }
    }

    private static List<Point> readData(String inFileName) {
        List<Point> points = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File(inFileName));
            int numPoints = 0;
            while(sc.hasNextLine() && numPoints<50000) {
                String line = sc.nextLine();
                String[] strs = line.split(",");
                double[] pos = new double[2];
                for (int i=0; i<2; i++) {
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
}
