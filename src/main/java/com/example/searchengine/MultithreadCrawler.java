package com.example.searchengine;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

public class MultithreadCrawler extends Crawler {

    private ThreadPoolTaskExecutor executorService;
    private Set<String> visited;
    private Set<String[]> lines;
    private ObserveRunnable observeRunnable;
    private volatile boolean done = false;
    private Map<String, Document> pageCache;

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    public MultithreadCrawler(String indexFileName) {
        super(indexFileName);
        this.lines = new CopyOnWriteArraySet<>();
        this.pageCache = new ConcurrentHashMap<>();
        executorService = new ThreadPoolTaskExecutor();
        executorService.setCorePoolSize(NUM_THREADS); // You can adjust the number of threads as needed
        executorService.initialize();
    }

    public void crawl(String startUrl) {
        long startTime = System.currentTimeMillis();
        //lines = Collections.synchronizedSet(new HashSet<>());
        visited = Collections.synchronizedSet(new HashSet<>());

        CrawlerRunnable initialTask = new CrawlerRunnable(this, startUrl);
        executorService.submit(initialTask);

        observeRunnable = new ObserveRunnable(this);
        executorService.execute(observeRunnable);

        while (!done) {
            // Wait for crawling to finish
        }

        long endTime = System.currentTimeMillis();
        checkTime(startTime, endTime);
        executorService.shutdown();
    }


    class CrawlerRunnable implements Runnable {
        MultithreadCrawler crawler;
        String startUrl;
        private static final String BASE_URL = "https://api.interactions.ics.unisg.ch/hypermedia-environment/";
        private static final int MAX_RETRIES = 3;
        private static final int RETRY_DELAY_MS = 700;

        public CrawlerRunnable(MultithreadCrawler crawler, String startUrl) {
            this.crawler = crawler;
            this.startUrl = startUrl;
        }

        @Override
        public void run() {
            Set<String[]> localLines = explore(startUrl, BASE_URL);
            lines.addAll(localLines);

            // Create new jobs for hyperlinks found in the page
            for (String[] line : localLines) {
                String pageUrl = line[0]; // Assuming the URL is stored in the first element of the array
                if (pageUrl != null) {
                    CrawlerRunnable newJob = new CrawlerRunnable(crawler, pageUrl);
                    executorService.execute(newJob);
                }
            }

            done = true;
        }
        public Set<String[]> explore(String startURL, String BASE_URL) {
            Queue<String> queue = new ConcurrentLinkedQueue<>();
            queue.add(startURL);
            Set<String[]> localLines = new HashSet<>();

            while (!queue.isEmpty()) {
                String currentUrl = queue.poll();

                if (visited.contains(currentUrl)) continue;
                visited.add(currentUrl);

                Document doc = fetchDocument(currentUrl);
                if (doc == null) continue;

                localLines.add(extracting(doc, currentUrl));

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String absLink = link.attr("abs:href");
                    if (absLink.startsWith(BASE_URL) && !visited.contains(absLink)) {
                        queue.add(absLink); // Add URLs to the concurrent queue for parallel processing
                    }
                }
            }

            return localLines;
        }


        private Document fetchDocument(String url) {
            Document cachedDoc = pageCache.get(url);
            if (cachedDoc != null) {
                return cachedDoc;  // Return cached page if available
            }

            int retries = 0;
            Document doc = null;

            while (retries < MAX_RETRIES) {
                try {
                    Connection connection = Jsoup.connect(url)
                            .timeout(10000)  // Set a connection timeout (10 seconds)
                            .userAgent("Mozilla/5.0");  // Provide a user agent to mimic a web browser
                    doc = connection.get();
                    pageCache.put(url, doc);  // Cache the fetched page
                    break;
                } catch (Exception ex) {
                    System.out.println("Error while reading url: " + url);
                    retries++;
                    if (retries < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return doc;
        }
    }



    class ObserveRunnable implements Runnable {
        private MultithreadCrawler crawler;

        public ObserveRunnable(MultithreadCrawler crawler) {
            this.crawler = crawler;
        }

        @Override
        public void run() {
            try {
                while (!done) {
                    Thread.sleep(700);
                }
            } catch (InterruptedException e) {
                // Thread has been interrupted, exit gracefully
                System.out.println("ObserveRunnable interrupted. Exiting.");
                writeFile(lines);
            }
        }
    }
}
