package com.jasonsavlov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by jason on 4/14/16.
 */
public class CosineSimilarityCalculatorEngine implements Runnable
{

    private final List<WebPage> listOfPages;
    private final WebPage queryPage;
    private final DownloadActionListener listener;

    @Override
    public void run()
    {
        FutureTask<CosineSimilarityCalculation.CosineSimilarityResult>[] futures = new FutureTask[listOfPages.size()];
        List<CosineSimilarityCalculation.CosineSimilarityResult> listOfResults = new ArrayList<>();

        for (int i = 0; i < futures.length; i++)
        {
            futures[i] = new FutureTask<>(new CosineSimilarityCalculation(listOfPages.get(i), queryPage));
            new Thread((futures[i])).start();
        }

        for (int i = 0; i < futures.length; i++)
        {
            try {
                listOfResults.add(futures[i].get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(listOfResults);

        listener.finishedCalculatingSimilarity(listOfResults.get(0));
    }

    public CosineSimilarityCalculatorEngine(
            List<WebPage> listOfPages, WebPage queryPage, DownloadActionListener downloadListener)
    {
        this.listOfPages = listOfPages;
        this.queryPage = queryPage;
        this.listener = downloadListener;
    }

}
