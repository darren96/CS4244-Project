package stage2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// To generate a Random CNF file
public class RandomCNFGenerator {

    private static final String BASE_PATH = "input" + File.separator + "generated" + File.separator;

    public static void main(String[] args) {
        System.out.print("Please key in the value of k, n and r (in the form of <k> <n> <r>): ");
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        int k = 0;
        int n = 0;
        double r = 0;
        try {
            String temp = bufferedReader.readLine();
            String[] list = temp.split(" ");
            k = Integer.parseInt(list[0]);
            n = Integer.parseInt(list[1]);
            r = Double.parseDouble(list[2]);
            List<String> cnf = generateCNF(k, n, r);
            writeCNFToFile(cnf, k, n, r);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This is used to generate the CNF randomly with the given value k, n and r
     * @param k Number of literals / k-clause
     * @param n Number of variables
     * @param r Determinant of number of clauses generated
     * @return a list of clauses in dimacs format
     */
    public static List<String> generateCNF(int k, int n, double r) {
        Random random = new Random();
        int noOfClauses = (int) Math.ceil(r * n);
        int i = 0;
        Set<Integer> literalsSet = new HashSet<>();
        String line = "";
        Integer[] literals;
        List<String> cnf = new ArrayList<>();

        while (i < noOfClauses) {
            // while this clause has less than k literals
            while (literalsSet.size() < k) {
                int literal = random.nextInt(n) + 1;
                literal = random.nextBoolean() ? literal : -literal;

                // if this clause has the same literals (no matter positive or negative literal), skip
                if (literalsSet.contains(literal) || literalsSet.contains(-literal)) {
                    continue;
                }

                literalsSet.add(literal);
            }

            // Add the clause into the cnf
            literals = literalsSet.toArray(new Integer[0]);
            line = String.format("%d %d %d 0", literals[0], literals[1], literals[2]);
            cnf.add(line);
            literalsSet.clear();
            i++;
        }

        return cnf;
    }

    /**
     * This method is used to write cnf to a file when generating only one CNF for each k, n and r
     * @param cnf The generated list of clause in the Random CNF
     * @param k Number of literals / k-clause
     * @param n Number of variables
     * @param r Determinant of number of clauses generated
     * @throws IOException
     */
    public static void writeCNFToFile(List<String> cnf, int k, int n, double r) throws IOException {
        writeCNFToFile(cnf, k, n, r, 0);
    }

    /**
     * This method is used to write cnf to a file when generating only mulitple CNF for each k, n and r
     * @param cnf The generated list of clause in the Random CNF
     * @param k Number of literals / k-clause
     * @param n Number of variables
     * @param r Determinant of number of clauses generated
     * @param fileIndex The given index for the cnf
     * @throws IOException
     */
    public static void writeCNFToFile(List<String> cnf, int k, int n, double r, int fileIndex) throws IOException {
        String fileName = String.format("k%d_n%d_r%.1f", k, n, r);
        String directory = "";

        if (fileIndex == 0) {
            fileName += ".cnf";
        } else {
            directory = String.format("k%d" + File.separator, k);
            if (!Files.exists(Paths.get(BASE_PATH + directory))) {
                Files.createDirectory(Paths.get(BASE_PATH + directory));
            }
            fileName += String.format("_%d.cnf", fileIndex);
        }

        System.out.println("Generating for file: " + fileName);
        FileWriter fileWriter = new FileWriter(BASE_PATH + directory + fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        bufferedWriter.write(String.format("c This cnf file is generated with the following input: k = %d, n = %d, r "
                + "= %.1f" + System.lineSeparator(), k, n , r));
        bufferedWriter.write(String.format("p cnf %d %d" + System.lineSeparator(), n , cnf.size()));

        for (int i = 0; i < cnf.size(); i++) {
            bufferedWriter.write(cnf.get(i) + System.lineSeparator());
        }

        bufferedWriter.close();
    }
}
