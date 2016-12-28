import java.util.HashSet;
import java.util.Random;

/**
 * Created by rotem on 28/12/16.
 */
public class WAVLTreeTester {
    public static void main(String[] args) {
        for (int i = 1; i <= 10 ; i++) {
            runTest(i);
        }
    }
    public static void runTest(int i) {
        int numElements = i * 10000;
        Random rand = new Random();
        WAVLTree tree = new WAVLTree();
        int sum = 0;
        int maxRebalance = 0;
        while (tree.size() < numElements) {
            int element = rand.nextInt();
            String str = Integer.toString(element);
            int rebalance_count = tree.insert(element, str);
            if(rebalance_count != -1) {
                maxRebalance = Math.max(maxRebalance, rebalance_count);
                sum += rebalance_count;
            }
        }
        double insert_average = ((double) sum) / numElements;
        int insert_max_rebalance = maxRebalance;

        sum = 0;
        maxRebalance = 0;
        int j = 0;
        while(!tree.empty()) {
            j++;

            int rebalance_count = tree.delete(Integer.parseInt(tree.min()));

            sum += rebalance_count;
            if (rebalance_count != -1) {
                maxRebalance = Math.max(maxRebalance, rebalance_count);
            }


        }

        double delete_average = ((double) sum) / numElements;
        int delete_max_rebalance = maxRebalance;
        System.out.println(insert_average + " " + insert_max_rebalance + " " + delete_average + " " + delete_max_rebalance);
    }
}
