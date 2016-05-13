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
    static final ExecutorService mainDownloadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS, new JSThreadFactory("mainDownload"));
    static JSHashTable urlHashTable = new JSHashTable();

    // Thread timeout in seconds
    static final long DOWNLOAD_THREAD_TIMEOUT = 30L;
    static final long CHECK_URL_THREAD_TIMEOUT = 60L;
    static final TimeUnit DOWNLOAD_THREAD_TIMEOUT_UNIT = TimeUnit.SECONDS;
    static final String URL_FILE_LIST_DIRECTORY = "url_list";

    public static void main(String[] args) {
        new MainWindow();
    }
}
