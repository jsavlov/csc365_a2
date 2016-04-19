package com.jasonsavlov.node;

import java.io.Serializable;

public class WebPage implements Serializable
{
    private final String pageURL;
    private String pageBodyText;
    private long lastModifiedTime = 0L;


    public WebPage(String pageURL)
    {
        this.pageURL = pageURL;
    }

    public long getLastModifiedTime()
    {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime)
    {
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getPageURL()
    {
        return pageURL;
    }

    public String getPageBodyText()
    {
        return pageBodyText;
    }

    public void setPageBodyText(String pageBodyText)
    {
        this.pageBodyText = pageBodyText;
    }
}
