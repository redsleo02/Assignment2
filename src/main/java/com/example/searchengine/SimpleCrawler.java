package com.example.searchengine;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SimpleCrawler extends Crawler {

    private static final String BASE_URL = "https://api.interactions.ics.unisg.ch/hypermedia-environment/";

    protected SimpleCrawler(String indexFileName) {
        super(indexFileName);
    }

    public void crawl(String startURL) {
        long startTime = System.currentTimeMillis();

        Set<String[]> lines = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Set<String[]> fileLines = explore(startURL, lines, visited);

        super.writeFile(fileLines);

        long endTime = System.currentTimeMillis();
        super.checkTime(startTime, endTime);
    }


    public Set<String[]> explore(String startURL, Set<String[]> lines, Set<String> visited) {
        Queue<String> queue = new LinkedList<>();
        queue.add(startURL);

        while (!queue.isEmpty()) {
            String currentUrl = queue.poll();

            if (visited.contains(currentUrl)) continue;
            visited.add(currentUrl);

            Document doc = super.getHTML(currentUrl);
            if (doc == null) continue;

            lines.add(extracting(doc, currentUrl));

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absLink = link.attr("abs:href");
                if (absLink.startsWith(BASE_URL) && !visited.contains(absLink))
                    queue.add(absLink);
            }
        }

        return lines;
    }
}
