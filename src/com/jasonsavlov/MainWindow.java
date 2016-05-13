package com.jasonsavlov;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class MainWindow implements DownloadActionListener
{
    private JTextField urlTextField;
    private JButton beginButton;
    private JList urlListView;
    private JProgressBar progressBar;
    private JLabel statusTextLabel;
    private JPanel mFrame;

    private DefaultListModel<WebPage> pageListModel;

    private List<WebPage> webPageList;

    public MainWindow()
    {
        pageListModel = new DefaultListModel<>();

        beginButton.addActionListener((ActionEvent e) -> {
            processURL(urlTextField.getText());
        });
        new InitiateDataWorker().execute();

        init();
    }

    private void init() {

        JFrame frame = new JFrame("MainWindow");
        frame.setContentPane(mFrame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void setStatusText(String str)
    {
        synchronized (statusTextLabel) {
            this.statusTextLabel.setText(str);
        }
    }

    private void processURL(String url)
    {
        PageDownloader downloader = new PageDownloader(new WebPage(url), this);
        Thread urlDownloadThread = new Thread(downloader);
        urlDownloadThread.start();
        this.setStatusText("Downloading page source...");
    }

    public boolean setProgressBarState(boolean animate)
    {
        boolean prev = progressBar.isVisible();
        progressBar.setVisible(animate);
        return prev;
    }

    public void setWebPageList(List<WebPage> pageList)
    {
        synchronized (webPageList) {
            this.webPageList = pageList;

            for (WebPage page : this.webPageList)
            {
                this.pageListModel.addElement(page);
            }

            this.urlListView.setModel(this.pageListModel);
        }
    }

    @Override
    public void finishedCalculatingSimilarity(CosineSimilarityCalculation.CosineSimilarityResult result)
    {
        System.out.println("Most similar page: " + result.page);
        this.setStatusText("Closest match: " + result.page);
    }

    @Override
    public void finishedDownloadingContent(WebPage page)
    {
        System.out.println("finishedDownloadingContent(WebPage): " + page.getPageURL());

        CosineSimilarityCalculatorEngine engine = new CosineSimilarityCalculatorEngine(webPageList, page, this);
        new Thread(engine).start();
        this.setStatusText("Calculating similarity");
    }

    private class InitiateDataWorker extends SwingWorker<Void, String>
    {

        @Override
        protected Void doInBackground() throws Exception
        {
            // Load in the web pages

            String urlListFilePath;
            String cacheFilePath = "url_cache.csc365";
            String btreeFilePath = "btree_a2.csc365";

            File cacheFile = new File(cacheFilePath);
            File btreeFile = new File(btreeFilePath);

            ExecutorService checkPagesThreadPool = Executors.newFixedThreadPool(Main.NUMBER_OF_THREADS, new JSThreadFactory("checkPage"));

            List<WebPage> rootUrlList = new ArrayList<WebPage>();
            List<PageDownloader> downloaderThreads = new ArrayList<PageDownloader>();

            final boolean[] page_modifications = {false};

            Map<String, JSBTree> savedTrees = null;

            if (btreeFile.exists()) {
                try {
                    System.out.println("Generating trees from file...");
                    publish("Generating trees from file");
                    savedTrees = JSBTree.generateTreesFromFile(btreeFile);
                    System.out.println("Trees generated!");
                    publish("Trees generated!");
                } catch (EOFException ex) {
                    ex.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("btree file not found");
                    publish("B-reww d");
                }
            }

            if (!cacheFile.exists()) {
                System.out.println("No cache file found. Loading URLs from file");
                publish("No cache file found. Loading URLs from file");
            } else {
                System.out.println("Cache file found. Will load URLs from cache file.");
                publish("Cache file found. Loading URLs from cache");

                try (
                        InputStream file = new FileInputStream(cacheFile);
                        InputStream buffer = new BufferedInputStream(file);
                        ObjectInput input = new ObjectInputStream(buffer);
                ) {
                    Main.urlHashTable = (JSHashTable) input.readObject();
                    List<Future> futures = new ArrayList<>();

                    int threadNumber = 0;
                    for (WebPage workingPage : Main.urlHashTable.getTableAsList())
                    {
                        Thread t = new Thread( () -> {
                            try {
                                URL url = new URL(workingPage.getPageURL());
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");
                                con.setReadTimeout(30 * 1000);
                                con.connect();
                                long last_modified = con.getLastModified();
                                if (workingPage.getLastModifiedTime() != last_modified) {
                                    page_modifications[0] = true;
                                }
                                con.disconnect();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        });
                        t.setName("URL_check_" + (++threadNumber));
                        checkPagesThreadPool.execute(t);
                    }

                    if(!checkPagesThreadPool.awaitTermination(Main.CHECK_URL_THREAD_TIMEOUT, TimeUnit.SECONDS)) {
                        System.out.println("Check URL thread pool time out. Time out set to " + Main.CHECK_URL_THREAD_TIMEOUT + " " + TimeUnit.SECONDS.name());
                        checkPagesThreadPool.shutdownNow();
                    }

                /*
                for (Future f : futures) {
                    f.get();
                }
                */

                    if (page_modifications[0]) {
                        // Pages have been modified, so reload them
                        System.out.println("We have page modifications");
                        publish("Page modifications found -- Reloading pages");
                        Main.urlHashTable = new JSHashTable();
                        for (String s : savedTrees.keySet()) {
                            WebPage wp = new WebPage(s);
                            rootUrlList.add(wp);
                            downloaderThreads.add(new PageDownloader(wp));
                        }

                    } else {

                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }



            if (!cacheFile.exists()) {
                File urlListFile = new File(Main.URL_FILE_LIST_DIRECTORY);
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
                publish("Please include a url_list file in the executable directory and restart the program.");
                //System.exit(1);
                return null;
            }

            if (downloaderThreads.size() > 0) {
                for (PageDownloader pd : downloaderThreads) {
                    Main.mainDownloadPool.execute(pd);
                }

                try {
                    if (!Main.mainDownloadPool.awaitTermination(Main.DOWNLOAD_THREAD_TIMEOUT, Main.DOWNLOAD_THREAD_TIMEOUT_UNIT))
                    {
                        // if we reach this point, it timed out before completing the downloading/crawling
                        // operation. handle it accordingly
                        System.out.println("mainDownloadPool timed out. Timeout set to " + Main.DOWNLOAD_THREAD_TIMEOUT + " " + Main.DOWNLOAD_THREAD_TIMEOUT_UNIT);
                        Main.mainDownloadPool.shutdownNow();
                        checkPagesThreadPool.shutdownNow();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Nothing to download!");
            }

            MainWindow.this.webPageList = rootUrlList;
            publish("Finished loading URLs! Ready.");

            for (WebPage wp : rootUrlList) {
                MainWindow.this.pageListModel.addElement(wp);
            }

            MainWindow.this.urlListView.setModel(MainWindow.this.pageListModel);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown hook started");
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
                        output.writeObject(Main.urlHashTable);
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
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }));

            return null;
        }

        @Override
        protected void process(List<String> chunks)
        {
            String statusText = "Loading: ";
            for (String str : chunks) {
                statusText += str + ".. ";
            }
            MainWindow.this.setStatusText(statusText);
            super.process(chunks);
        }
    }


}
