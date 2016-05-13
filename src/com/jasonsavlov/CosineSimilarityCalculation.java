package com.jasonsavlov;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by jason on 4/14/16.
 */
public class CosineSimilarityCalculation implements Callable<CosineSimilarityCalculation.CosineSimilarityResult>
{

    private final WebPage sourcePage, queryPage;


    @Override
    public CosineSimilarityResult call() throws Exception
    {
        List<Integer> listA = new ArrayList<>(), listB = new ArrayList<>();
        List<WordNode> wordsFromSource = sourcePage.getMainTree().treeToList();

        int pos = 0;
        for (WordNode wn : wordsFromSource)
        {
            try {
                int srcWordFreq = wn.frequency;
                int qWordFreq = 0;

                WordNode qn = queryPage.getMainTree().get(wn.value);
                if (qn != null) {
                    qWordFreq = qn.frequency;
                }
                listA.add(srcWordFreq);
                listB.add(qWordFreq);
                pos++;
            } catch (NullPointerException ex) {
                continue;
            }
        }

        int dotProduct = calculateDotProduct(listA, listB);
        double listANorm = calculateEuclidianNorm(listA);
        double listBNorm = calculateEuclidianNorm(listB);

        double result = (double) dotProduct / (listANorm * listBNorm);


        return new CosineSimilarityResult(this.sourcePage.getPageURL(), result);
    }



    public CosineSimilarityCalculation(WebPage sourcePage, WebPage queryPage)
    {
        this.sourcePage = sourcePage;
        this.queryPage = queryPage;
    }

    private int calculateDotProduct(List<Integer> listA, List<Integer> listB)
    {
        int resultToReturn = 0;

        int number = 0;

        if (listA.size() != listB.size()) return -1; // The lists arent the same size.. not good
        else number = listA.size();

        for (int i = 0; i < number; i++)
        {
            resultToReturn += listA.get(i) * listB.get(i);
        }

        return resultToReturn;
    }

    public double calculateEuclidianNorm(List<Integer> listOfFreqs)
    {
        double resultToReturn = 0;
        int sqSum = 0;

        for (Integer num : listOfFreqs)
        {
            sqSum += num * num;
        }

        resultToReturn = Math.sqrt((double) sqSum);
        return resultToReturn;
    }

    public static class CosineSimilarityResult implements Comparable<CosineSimilarityResult> {
        final String page;
        final double similarity;

        public CosineSimilarityResult(String page, double similarity)
        {
            this.page = page;
            this.similarity = similarity;
        }

        @Override
        public int compareTo(CosineSimilarityResult o)
        {
            if (o == this) return 0;

            return o.similarity > this.similarity ? 1 : o.similarity < this.similarity ? -1 : 0;
        }
    }
}
