package com.example.searchengine;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
@Component
public class IndexFlipper {
    public void flipIndex(String indexFileName, String flippedIndexFileName) {

        // Use try-with-resources to ensure proper resource management
        try (
                CSVReader csvReader = new CSVReader(new FileReader(indexFileName));
                CSVWriter csvWriter = new CSVWriter(new FileWriter(flippedIndexFileName))
        ) {
            List<String[]> csvLines = csvReader.readAll();
            Map<String, Set<String>> flippedIndex = createFlippedIndex(csvLines);

            // Write the flipped index to the CSV file
            for (Map.Entry<String, Set<String>> entry : flippedIndex.entrySet()) {
                String word = entry.getKey();
                Set<String> references = entry.getValue();
                String[] outputLine = new String[references.size() + 1];
                outputLine[0] = word;
                // Use array copying for safer and slightly optimized data insertion
                System.arraycopy(references.toArray(), 0, outputLine, 1, references.size());
                csvWriter.writeNext(outputLine);
            }

        } catch (IOException | CsvException e) {
            throw new RuntimeException("Error flipping index", e);
        }
    }

    private Map<String, Set<String>> createFlippedIndex(List<String[]> csvLines) {
        Map<String, Set<String>> flippedIndex = new HashMap<>();

        for (String[] line : csvLines) {
            String url = line[0].trim();

            for (int i = 1; i < line.length; i++) {
                String word = line[i].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                flippedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(url);
            }
        }

        return flippedIndex;
    }
}
