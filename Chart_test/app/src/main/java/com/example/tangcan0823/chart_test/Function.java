package com.example.tangcan0823.chart_test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by tangcan0823 on 2016/11/16.
 */

public class Function {


    //ユニーク関数
    public static void removeDuplicateWithOrder(ArrayList arlList)
    {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = arlList.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        arlList.clear();
        arlList.addAll(newList);
    }

    //リスト最大値を取る
    public double ArrayListMax(ArrayList sampleList)
    {
        try
        {
            double maxDevation = 0.0;
            int totalCount = sampleList.size();
            if (totalCount >= 1)
            {

                double max = Double.parseDouble(sampleList.get(0).toString());
                for (int i = 0; i < totalCount; i++)
                {
                    double temp = Double.parseDouble(sampleList.get(i).toString());
                    if (temp > max)
                    {
                        max = temp;
                    }
                } maxDevation = max;
            }
            return maxDevation;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    //リスト最小値を取る
    public double ArrayListMin(ArrayList sampleList)
    {
        try
        {
            double mixDevation = 0.0;
            int totalCount = sampleList.size();
            if (totalCount >= 1)
            {
                double min = Double.parseDouble(sampleList.get(0).toString());
                for (int i = 0; i < totalCount; i++)
                {
                    double temp = Double.parseDouble(sampleList.get(i).toString());
                    if (min > temp)
                    {
                        min = temp;
                    }
                } mixDevation = min;
            }
            return mixDevation;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }


}
