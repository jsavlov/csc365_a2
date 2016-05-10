package com.jasonsavlov;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WebPage implements Serializable
{
    private final String pageURL;
    private transient String pageBodyText;
    private transient List<WebPage> listOfLinks;
    private long lastModifiedTime = 0L;
    private transient long newModifiedTime = 0L;
    private transient JSBTree mainTree;


    public WebPage(String pageURL)
    {
        this.pageURL = pageURL;
        this.mainTree = new JSBTree();
    }

    public WebPage(String pageURL, JSBTree tree)
    {
        this.pageURL = pageURL;
        this.mainTree = tree;
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

    public List<WebPage> getListOfLinks()
    {
        return listOfLinks;
    }

    public void setListOfLinks(List<WebPage> listOfLinks)
    {
        this.listOfLinks = listOfLinks;
    }

    public JSBTree getMainTree()
    {
        return mainTree;
    }

    @Override
    public String toString()
    {
        return this.pageURL;
    }
}
