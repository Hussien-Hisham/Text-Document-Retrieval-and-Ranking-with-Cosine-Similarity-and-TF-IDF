import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TfIdfCalculator {
    private final Map<String, Map<String, Integer>> invertedIndex;
    private final int numDocs;

    public TfIdfCalculator(Map<String, Map<String, Integer>> invertedIndex, int numDocs) {
        this.invertedIndex = invertedIndex;
        this.numDocs = numDocs;
    }

    public Map<String, Map<String, Double>> calculateTfIdf() {
        Map<String, Map<String, Double>> tfIdfIndex = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> entry : invertedIndex.entrySet()) {
            String term = entry.getKey();
            Map<String, Integer> postings = entry.getValue();
            Map<String, Double> tfIdfPostings = new HashMap<>();

            for (Map.Entry<String, Integer> posting : postings.entrySet()) {
                String fileName = posting.getKey();
                int termFreq = posting.getValue();
                double tf = (double) termFreq / getDocLength(fileName);
                double idf = Math.log((double) numDocs / postings.size());
                double tfIdf = tf * idf;
                tfIdfPostings.put(fileName, tfIdf);
            }

            tfIdfIndex.put(term, tfIdfPostings);
        }

        return tfIdfIndex;
    }

    private double getDocLength(String fileName) {
        double docLength = 0;
        Map<String, Integer> termFreqs = invertedIndex.entrySet().stream()
                .filter(e -> e.getValue().containsKey(fileName))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(fileName)));

        for (int freq : termFreqs.values()) {
            docLength += Math.pow(freq, 2);
        }

        return Math.sqrt(docLength);
    }
}
