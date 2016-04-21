package com.jasonsavlov;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RunnableFuture;

public class PageDownloader implements Runnable
{
    private final WebPage mainPage;

    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    @Override
    public void run()
    {
        Document document;
        Connection connection;
        try {
            connection = Jsoup.connect(mainPage.getPageURL());

            try {
                document = connection.get();
            } catch (org.jsoup.HttpStatusException ex) {
                System.out.println("HttpStatusException. Page: " + mainPage.toString());
                //ex.printStackTrace();
                return;
            } catch (org.jsoup.UnsupportedMimeTypeException ex) {
                System.out.println("UnsupportedMimeTypeException. Page: " + mainPage.toString());
                //ex.printStackTrace();
                return;
            }

            String lastModifiedStr = connection.response().header("Last-Modified");

            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
            Date lastModifiedDate;

            try {
                lastModifiedDate = format.parse(lastModifiedStr);
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Setting lastModifiedDate to an empty Date object");
                lastModifiedDate = new Date();
            }



            Elements links = document.select("a");
            List<WebPage> linkList = new ArrayList<WebPage>();
            for (Element e : links) {
                String linkURL = e.attr("abs:href");
                if (!linkURL.startsWith("http")) {
                    continue;
                }
                linkList.add(new WebPage(linkURL, mainPage.getMainTree()));
            }

            // get the words from the body
            String parsedBody = document.body().text();

            // split up the body
            String[] words = parsedBody.split("\\s+");

            // Get only alphanumeric words from the body, make all lowercase
            for (int i = 0; i < words.length; i++) {
                words[i] = words[i].replaceAll("[^a-zA-Z]", "").toLowerCase();
            }

            JSBTree tree = mainPage.getMainTree();

            for (String s : words) {
                tree.add(s);
            }


            mainPage.setPageBodyText(parsedBody);
            mainPage.setLastModifiedTime(lastModifiedDate.getTime());
            mainPage.setListOfLinks(linkList);

            for (WebPage wp : linkList) {
                Main.mainDownloadPool.submit(new PageDownloader(wp));
            }

            System.out.println("Page " + mainPage.getPageURL() + ": " + Long.toString(mainPage.getLastModifiedTime()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PageDownloader(WebPage page)
    {
        this.mainPage = page;
    }

}
