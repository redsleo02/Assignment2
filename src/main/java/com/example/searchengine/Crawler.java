package com.example.searchengine;

import com.opencsv.CSVWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;


import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public abstract class Crawler {

    final String indexFileName;

    private String baseUrl = "https://api.interactions.ics.unisg.ch/hypermedia-environment/";

    /**
     *
     * @param indexFileName the name of the file that is used as index.
     */
    protected Crawler(String indexFileName) {
        this.indexFileName = indexFileName;
    }

    /**
     *
     * @param url the url where the crawling starts
     */
    public abstract void crawl(String url);

    void writeFile(Set<String[]> lines) {
        try (FileWriter fileWriter = new FileWriter(indexFileName);
             CSVWriter writer = new CSVWriter(fileWriter, ',', CSVWriter.NO_QUOTE_CHARACTER, ' ', "\r\n")) {
            for (String[] line : lines) {
                writer.writeNext(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void checkTime(long startTime, long endTime) {
        long duration = (endTime - startTime) / 1000; // Zeitdifferenz in Sekunden
        System.out.println("duration simple crawler: " + duration + "s");
    }


    Document getHTML(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (Exception ex) {
            System.out.println("Error while reading url: " + url);
            return null;
        }
    }

    String[] extracting(Document doc, String currentUrl) {
        Elements pTags = doc.select("p");
        String[] line = new String[pTags.size() + 1];
        line[0] = currentUrl.substring(currentUrl.lastIndexOf("/"));
        int index = 1;
        for (Element p : pTags) {
            line[index++] = p.text();
        }
        return line;
    }

    public  List<List<String>> getInfo(String urlString){
        List<String> keywords = new ArrayList<>();
        List<String> hyperlinks = new ArrayList<>();
        List<List<String>> returnList = new ArrayList<>();
        try {
            URL url = new URL(urlString);
            Elements elements; //TODO: initialize elements based on the webpage at the given url.
            //TODO: Use elements to put the keywords in the webpage in the list keywords.
            //TODO: Use elements to the hyperlinks to other pages in the environment in the list hyperlinks.
        } catch (Exception e){
            e.printStackTrace();
        }
        returnList.add(keywords);
        returnList.add(hyperlinks);
        return returnList;
    }
}
