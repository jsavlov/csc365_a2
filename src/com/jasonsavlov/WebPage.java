package com.jasonsavlov;

import java.io.Serializable;
import java.util.List;

public class WebPage implements Serializable
{
    private final String pageURL;
    private transient String pageBodyText;
    private transient List<String> listOfLinks;
    private long lastModifiedTime = 0L;
    private JSBTree mainTree;


    public WebPage(String pageURL)
    {
        this.pageURL = pageURL;
        this.mainTree = new JSBTree();
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

    public List<String> getListOfLinks()
    {
        return listOfLinks;
    }

    public void setListOfLinks(List<String> listOfLinks)
    {
        this.listOfLinks = listOfLinks;
    }

    public JSBTree getMainTree()
    {
        return mainTree;
    }
}
