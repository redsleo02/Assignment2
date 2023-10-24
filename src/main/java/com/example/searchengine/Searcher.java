package com.example.searchengine;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Searcher {
    public List<String> search(String keyword, String flippedIndexFileName) {
        long startTime = System.currentTimeMillis();
        List<String> urls = new ArrayList<>();

        try {
            BufferedReader CSV = new BufferedReader(new FileReader(flippedIndexFileName));
            String line = " ";
            keyword = keyword.toLowerCase();

            while ((line = CSV.readLine()) != null) {
                String[] parts = line.split(","); // Split the word and URLs
                String word = parts[0];
                String word1 = word.substring(1, word.length() - 1);
                if (word1.equals(keyword)) {
                    String[] urlArray = new String[parts.length - 1]; // Create an array for URLs
                    // Copy URLs into the new array, skipping the first element (index 0)
                    System.arraycopy(parts, 1, urlArray, 0, parts.length - 1);
                    for (String url : urlArray) {
                        String URL = url.substring(1, url.length() - 1);
                        urls.add("https://api.interactions.ics.unisg.ch/hypermedia-environment" + URL);
                    }
                }
            }

            CSV.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("duration searcher flipped: " + duration);
        return urls;
    }

    public static void main(String[] args) {
        Searcher searcher = new Searcher();
        String keyword ="cancel";
        List<String> urls = searcher.search(keyword, "index_flipped.csv");

        if (!urls.isEmpty()) {
            System.out.println("Web pages containing the keyword '" + keyword + "':");
            StringBuilder result = new StringBuilder();

            for (String url : urls) {
                result.append(url).append("\n");
            }

            System.out.print(result.toString());
        } else {
            System.out.println("Keyword not found in any web pages.");
        }
    }
}
