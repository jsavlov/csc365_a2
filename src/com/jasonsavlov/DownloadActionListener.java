package com.jasonsavlov;

/**
 * Created by jason on 4/14/16.
 */
public interface DownloadActionListener
{
    void finishedDownloadingContent(WebPage page);
    void finishedCalculatingSimilarity(CosineSimilarityCalculation.CosineSimilarityResult result);
}
