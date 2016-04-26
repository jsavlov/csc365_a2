package com.jasonsavlov;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main
{
    private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    static final ExecutorService mainDownloadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    static final JSHashTable urlHashTable = new JSHashTable();

    // Thread timeout in seconds
    private static final long DOWNLOAD_THREAD_TIMEOUT = 60L;
    private static final TimeUnit DOWNLOAD_THREAD_TIMEOUT_UNIT = TimeUnit.SECONDS;

    public static void main(String[] args) {
        // Load in the web pages

        String urlListFilePath = null;
        String cacheFilePath = "url_cache.csc365";

        if (args.length != 0) {
            urlListFilePath = args[0];
        } else {
            System.out.println("Please provice a file name for the list of URLs to use as the first command-line argument.");
            System.exit(1);
        }

        File urlListFile = new File(urlListFilePath);
        BufferedReader urlReader;
        InputStream urlInputStream;
        InputStreamReader urlISReader;
        List<WebPage> urlList = new ArrayList<WebPage>();
        List<PageDownloader> downloaderThreads = new ArrayList<PageDownloader>();

        try {
            urlInputStream = new FileInputStream(urlListFile);
            urlISReader = new InputStreamReader(urlInputStream, Charset.forName("UTF-8"));
            urlReader = new BufferedReader(urlISReader);

            String line;

            while ((line = urlReader.readLine()) != null) {
                WebPage page = new WebPage(line);
                urlList.add(page);
                downloaderThreads.add(new PageDownloader(page));
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        for (PageDownloader pd : downloaderThreads) {
            mainDownloadPool.submit(pd);
        }

        try {
            if (!mainDownloadPool.awaitTermination(DOWNLOAD_THREAD_TIMEOUT, DOWNLOAD_THREAD_TIMEOUT_UNIT))
            {
                // if we reach this point, it timed out before completing the downloading/crawling
                // operation. handle it accordingly
                System.out.println("mainDownloadPool timed out. Timeout set to " + DOWNLOAD_THREAD_TIMEOUT + " " + DOWNLOAD_THREAD_TIMEOUT_UNIT);
                mainDownloadPool.shutdownNow();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Testing list to words...");
        List<WordNode> testListOfWords = urlList.get(0).getMainTree().treeToList();


        File cacheFile = new File(cacheFilePath);

        System.out.println("Program complete");
    }
}
