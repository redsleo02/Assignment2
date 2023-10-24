package com.example.searchengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;


@RestController
public class SearchEngine {

	public final String indexFileName = "./src/main/resources/index.csv";
	public final String flippedIndexFileName = "./src/main/resources/index_flipped.csv";
	public final String startUrl = "https://api.interactions.ics.unisg.ch/hypermedia-environment/cc2247b79ac48af0";

	@Autowired
	Searcher searcher;
	@Autowired
	IndexFlipper indexFlipper;
	@Autowired
	SearchEngineProperties properties;
	Crawler crawler;
	@Autowired
	private ResourceLoader resourceLoader; // Autowire the ResourceLoader

	@PostConstruct
	public void initialize() {
		if (properties.getCrawler().equals("multithread")) {
			this.crawler = new MultithreadCrawler(indexFileName);
		} else {
			this.crawler = new SimpleCrawler(indexFileName);
		}
		if (properties.getCrawl()) {
			crawler.crawl(startUrl);
			indexFlipper.flipIndex(indexFileName, flippedIndexFileName);
		}
	}

	@GetMapping("/")
	public ResponseEntity<String> getMainPage() {
		try {
			// opening/loading the index.html file
			Resource resource = resourceLoader.getResource("classpath:static/index.html");
			String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
			return new ResponseEntity<>(content, HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>("Error loading index.html", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	//search function
	@GetMapping("/search")
	public ResponseEntity<?> search(@RequestParam(required = false) String q) {
		if (q == null) {
			return new ResponseEntity<>("Missing query string parameter.", HttpStatus.BAD_REQUEST);
		}
		return checkSearch(q, false);
	}

	//
	@GetMapping("/lucky")
	public ResponseEntity<?> getLucky(@RequestParam(required = false) String q) {
		if (q == null) {
			return new ResponseEntity<>("Missing query string parameter.", HttpStatus.BAD_REQUEST);
		}
		return checkSearch(q, true);
	}

	private ResponseEntity<?> checkSearch(String q, boolean isLucky) {
		try {
			List<String> results = searcher.search(q, flippedIndexFileName); //return as HTML

			StringBuilder resultsUrl = new StringBuilder();

			//generating an hyperlink
			if (!results.isEmpty()) {
				for (String url : results) {
					resultsUrl.append("<a href=\"").append(url).append("\">").append(url).append("</a><br>");
				}
			}
			if (results.isEmpty()) {
				return new ResponseEntity<>("No results found", isLucky ? HttpStatus.NOT_FOUND : HttpStatus.OK);
			}
			if (isLucky) {
				String luckyUrl = "<a href=\"" + results.get(0) + "\">" + results.get(0) + "</a>";
				return new ResponseEntity<>(luckyUrl, HttpStatus.OK);
			}
			return new ResponseEntity<>(resultsUrl.toString(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
