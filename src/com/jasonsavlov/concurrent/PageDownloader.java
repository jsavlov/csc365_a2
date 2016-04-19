package com.jasonsavlov.concurrent;


import com.jasonsavlov.node.WebPage;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PageDownloader implements Runnable
{
    private final WebPage mainPage;

    @Override
    public void run()
    {
        Document document;
        Connection connection;
        try {
            connection = Jsoup.connect(mainPage.getPageURL());
            document = connection.get();

            String lastModifiedStr = connection.response().header("Last-Modified");

            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            Date lastModifiedDate;

            try {
                lastModifiedDate = format.parse(lastModifiedStr);
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("Setting lastModifiedDate to an empty Date object");
                lastModifiedDate = new Date();
            }

            mainPage.setPageBodyText(document.body().text());
            mainPage.setLastModifiedTime(lastModifiedDate.getTime());

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
