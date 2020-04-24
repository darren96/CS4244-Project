package stage2;

import java.io.IOException;
import java.util.List;

// To generate all Random CNF for the experiment
public class AutomatedCNFGeneratorRunner {

    public static void main(String[] args) {
        int n = 150;
        List<String> cnf;

        try {
            for (int k = 3; k <= 5; k++) {
                for (double r = 0.2; r < 9.8; r += 0.2) {
                    for (int i = 0; i < 50; i++) {
                        cnf = RandomCNFGenerator.generateCNF(k, n, r);
                        RandomCNFGenerator.writeCNFToFile(cnf, k, n, r, i + 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
