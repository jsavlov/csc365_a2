package com.jasonsavlov;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main
{

    static final ForkJoinPool mainDownloadPool = new ForkJoinPool();

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
            mainDownloadPool.awaitTermination(60L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        File cacheFile = new File(cacheFilePath);

        System.out.println("Program complete");
    }
}
