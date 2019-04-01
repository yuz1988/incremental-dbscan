import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

public class Main {

    public static void main(String[] args) {
        double eps = 0.2;
        int minPts = 3;

        DBSCANClusterer<DoublePoint> dbcluster = new DBSCANClusterer<DoublePoint>(eps, minPts);

    }
}
