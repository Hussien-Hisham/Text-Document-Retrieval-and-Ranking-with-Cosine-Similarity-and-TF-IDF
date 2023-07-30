import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CosineSimilarity {
    private static final String[] FILE_NAMES = {
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file0.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file1.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file2.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file3.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file4.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file5.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file6.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file7.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file8.txt",
            "F:\\College\\AI_year_4\\2nd_Term\\Information Retreval\\Assignments\\Ass_2\\Ass_2\\Docs\\file9.txt"
    };

    public static void main(String[] args) throws IOException {
        Map<String, Map<String, Integer>> invertedIndex = buildInvertedIndex(FILE_NAMES);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your query (separated by spaces):");
        String query = scanner.nextLine();
        System.out.println("Enter the number of top results (K):");
        int topK = scanner.nextInt();
        scanner.close();

        Map<String, Integer> queryTermFrequency = getTermFrequency(query);
        List<FileSimilarity> rankedFiles = rankFilesByCosineSimilarity(invertedIndex, queryTermFrequency);

        System.out.println("Top " + topK + " ranked files:");
        for (int i = 0; i < topK && i < rankedFiles.size(); i++) {
            FileSimilarity fileSimilarity = rankedFiles.get(i);
            System.out.printf("Matched File : %s Cos_similarity: %.4f%n", fileSimilarity.fileName, fileSimilarity.similarity);
        }


        TfIdfCalculator tfIdfCalculator = new TfIdfCalculator(invertedIndex, FILE_NAMES.length);
        Map<String, Map<String, Double>> tfIdfIndex = tfIdfCalculator.calculateTfIdf();
        System.out.printf("\n");
        System.out.println("TF-IDF");

        for (Map.Entry<String, Map<String, Double>> entry : tfIdfIndex.entrySet()) {
            String term = entry.getKey();
            Map<String, Double> postings = entry.getValue();
            for (Map.Entry<String, Double> posting : postings.entrySet()) {
                String fileName = posting.getKey();
                double tfIdf = posting.getValue();
                System.out.printf("Term: %s, Document: %s, TF-IDF: %.4f%n", term, fileName, tfIdf);
            }
        }

    }


    private static Map<String, Map<String, Integer>> buildInvertedIndex(String[] fileNames) throws IOException {
        Map<String, Map<String, Integer>> invertedIndex = new HashMap<>();

        for (String fileName : fileNames) {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            Map<String, Integer> termFrequency = getTermFrequency(content);

            for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
                String term = entry.getKey();
                int frequency = entry.getValue();

                if (!invertedIndex.containsKey(term)) {
                    invertedIndex.put(term, new HashMap<>());
                }
                invertedIndex.get(term).put(fileName, frequency);
            }
        }

        return invertedIndex;
    }

    private static Map<String, Integer> getTermFrequency(String content) {
        String[] words = content.toLowerCase().split("\\W+");
        Map<String, Integer> termFrequency = new HashMap<>();

        for (String word : words) {
            termFrequency.put(word, termFrequency.getOrDefault(word, 0) + 1);
        }

        return termFrequency;
    }

    private static List<FileSimilarity> rankFilesByCosineSimilarity(Map<String, Map<String, Integer>> invertedIndex, Map<String, Integer> queryTermFrequency) {
        Map<String, Double> fileSimilarities = new HashMap<>();

        for (Map.Entry<String, Integer> queryEntry : queryTermFrequency.entrySet()) {
            String term = queryEntry.getKey();
            int queryFrequency = queryEntry.getValue();

            if (invertedIndex.containsKey(term)) {
                for (Map.Entry<String, Integer> fileEntry : invertedIndex.get(term).entrySet()) {
                    String fileName = fileEntry.getKey();
                    int fileFrequency = fileEntry.getValue();

                    double similarity = queryFrequency * fileFrequency;
                    fileSimilarities.put(fileName, fileSimilarities.getOrDefault(fileName, 0.0) + similarity);
                }
            }
        }

        List<FileSimilarity> rankedFiles = new ArrayList<>();
        for (Map.Entry<String, Double> entry : fileSimilarities.entrySet()) {
            rankedFiles.add(new FileSimilarity(entry.getKey(), entry.getValue()));
        }

        rankedFiles.sort(Comparator.comparingDouble(FileSimilarity::getSimilarity).reversed());
        return rankedFiles;
    }

    private static class FileSimilarity {
        private final String fileName;
        private final double similarity;

        public FileSimilarity(String fileName, double similarity) {
            this.fileName = fileName;
            this.similarity = similarity;
        }

        public String getFileName() {
            return fileName;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}
