package com.jasonsavlov.concurrent;


import com.jasonsavlov.node.WebPage;
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
            document = connection.get();

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
            List<String> linkStrList = new ArrayList<String>();
            for (Element e : links) {
                String linkURL = e.attr("abs:href");
                if (!linkURL.startsWith("http")) {
                    continue;
                }
                linkStrList.add(linkURL);
            }

            mainPage.setPageBodyText(document.body().text());
            mainPage.setLastModifiedTime(lastModifiedDate.getTime());
            mainPage.setListOfLinks(linkStrList);

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
