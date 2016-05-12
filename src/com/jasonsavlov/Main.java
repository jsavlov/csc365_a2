package com.jasonsavlov;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Main
{
    public static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    static final ExecutorService mainDownloadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    static JSHashTable urlHashTable = new JSHashTable();

    // Thread timeout in seconds
    private static final long DOWNLOAD_THREAD_TIMEOUT = 30L;
    private static final TimeUnit DOWNLOAD_THREAD_TIMEOUT_UNIT = TimeUnit.SECONDS;

    public static void main(String[] args) {
        // Load in the web pages

        String urlListFilePath = null;
        String cacheFilePath = "url_cache.csc365";
        String btreeFilePath = "btree_a2.csc365";

        File cacheFile = new File(cacheFilePath);
        File btreeFile = new File(btreeFilePath);

        ExecutorService checkPagesThreadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        List<WebPage> rootUrlList = new ArrayList<WebPage>();
        List<PageDownloader> downloaderThreads = new ArrayList<PageDownloader>();

        final boolean[] page_modifications = {false};

        Map<String, JSBTree> savedTrees;

        if (btreeFile.exists()) {
            try {
                System.out.println("Generating trees from file...");
                savedTrees = JSBTree.generateTreesFromFile(btreeFile);
                System.out.println("Trees generated!");
            } catch (EOFException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("btree file not found");
            }
        }

        if (!cacheFile.exists()) {
            System.out.println("No cache file found.");
        } else {
            System.out.println("Cache file found. Will load URLs from cache file.");

            try (
                    InputStream file = new FileInputStream(cacheFile);
                    InputStream buffer = new BufferedInputStream(file);
                    ObjectInput input = new ObjectInputStream(buffer);
            ) {
                urlHashTable = (JSHashTable) input.readObject();
                List<Future> futures = new ArrayList<>();

                for (WebPage workingPage : urlHashTable.getTableAsList())
                {
                    futures.add(checkPagesThreadPool.submit((Runnable) () -> {
                        try {
                            URL url = new URL(workingPage.getPageURL());
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            long last_modified = con.getLastModified();
                            if (workingPage.getLastModifiedTime() != last_modified) {
                                page_modifications[0] = true;
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }));
                }

                for (Future f : futures) {
                    f.get();
                }

                if (page_modifications[0]) {
                    // Pages have been modified, so reload them
                    for (WebPage page : urlHashTable.getTableAsList())
                    {
                        downloaderThreads.add(new PageDownloader(page));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }



        if (args.length != 0 && !cacheFile.exists()) {
            urlListFilePath = args[0];
            File urlListFile = new File(urlListFilePath);
            BufferedReader urlReader;
            InputStream urlInputStream;
            InputStreamReader urlISReader;


            try {
                urlInputStream = new FileInputStream(urlListFile);
                urlISReader = new InputStreamReader(urlInputStream, Charset.forName("UTF-8"));
                urlReader = new BufferedReader(urlISReader);

                String line;

            while ((line = urlReader.readLine()) != null) {
                WebPage page = new WebPage(line);
                rootUrlList.add(page);
                downloaderThreads.add(new PageDownloader(page));
            }

            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        } else if (cacheFile.exists()) {

        } else {
            System.out.println("Please provide a file name for the list of URLs to use as the first command-line argument.");
            System.exit(1);
        }

        if (downloaderThreads.size() > 0) {
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
        } else {
            System.out.println("Nothing to download!");
        }


        Thread finalizeBTreeThread = new Thread(() -> {
            System.out.println("finalizeBTreeThread started");
            try {
                OutputStream file = null;
                try {
                    file = new FileOutputStream(btreeFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                OutputStream buffer = new BufferedOutputStream(file);
                final ByteArrayOutputStream btOutputStream = new ByteArrayOutputStream();
                JSBTree.serializeBTrees(rootUrlList, btreeFile);
                //btOutputStream.writeTo(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread finalizeHashTableThread = new Thread(() -> {
            System.out.println("finalizeHashTableThread started");
            try (
                    OutputStream file = new FileOutputStream(cacheFile);
                    OutputStream buffer = new BufferedOutputStream(file);
                    ObjectOutput output = new ObjectOutputStream(buffer);
            ) {
                output.writeObject(urlHashTable);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        });

        finalizeBTreeThread.start();
        finalizeHashTableThread.start();

        try {
            finalizeHashTableThread.join();
            System.out.println("finalizeHashTableThread complete");
            finalizeBTreeThread.join();
            System.out.println("finalizeBTreeThread complete");
            checkPagesThreadPool.shutdownNow();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }




        System.out.println("Program complete");
    }
}
