package com.example.tangcan0823.chart_test;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by tangcan0823 on 2016/11/30.
 */

public class PiechartFragment extends Fragment {

    private int hour_tag = 12;
    private  int step = 5;
    private int step_num = 12*(60/step);

    private PieChart mPiechart,mPiechart_up;
    private int[] yData = new int[hour_tag];
    private int[] yDataup = new int[step_num];
    private String[] xData = new String[hour_tag];
    private String[] xDataup = new String[step_num];
    private int[] colors = new int[yData.length];
    private int[] colorsup = new int[yDataup.length];
    private ArrayList<String> start = new ArrayList<String>();
    private ArrayList<String> end = new ArrayList<String>();
    private ArrayList<String> tag = new ArrayList<String>();
    private List<String> RED = new ArrayList<String>();
    private List<String> RED_up = new ArrayList<String>();
    private int a = 0 , b = hour_tag , c = 0 , d = 0;
    private int A = 0 , B = step_num , D = 0;
    private TextView textview,textView_emp;
    private ListView listview;
    private String file;
    private Switch st;
    private String CSV_red, CSV_hitori,CSV_UP;
    private String ID = "bb4";
    private String sdPath = Environment.getExternalStorageDirectory().getPath() + "/MTI/" + ID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_piechart, null);


        CSV_UP = ID + "_minmark.csv";
        CSV_hitori = ID +"_alonerange.csv";
        CSV_red = ID +"_hourmark.csv";


        mPiechart = (PieChart) view.findViewById(R.id.pie_chart);
        mPiechart_up = (PieChart) view.findViewById(R.id.pie_chart_up);
        textview = (TextView) view.findViewById(R.id.item_tv_pie2);
        textView_emp = (TextView) view.findViewById(R.id.tv2_emp);
        listview = (ListView) view.findViewById(R.id.listview_pie);

        st = (Switch) view.findViewById(R.id.switch2);
        Initial();
        PieChartSetting(mPiechart);
        Up_PiechartSetting(mPiechart_up);
        return view;
    }




    private void PieChartSetting(PieChart mChart) {

         File[] files;
         ArrayList<String> csvlist = new ArrayList<String>();
         files = new File(sdPath).listFiles();

        if (files != null){
        for(int i = 0; i < files.length; i++){
            if(files[i].isDirectory()){
                csvlist.add(files[i].getName());
            }
        }
        }
        Collections.sort(csvlist);
        Setlist(listview,Changtodate(csvlist));


        String[] next;
        List<String[]> list = new ArrayList<String[]>();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(new File(sdPath + "/"+ file +"/"+ CSV_red))));
            while ((next = reader.readNext()) != null) {
                list.add(next);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < list.size(); i++) {
            RED.add(list.get(i)[0]);
        }

        list.clear();

        try {
            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(new File(sdPath + "/"+ file +"/"+ CSV_hitori))));
            while ((next = reader.readNext()) != null) {
                list.add(next);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < list.size(); i++) {
            start.add(list.get(i)[0]);
            end.add(list.get(i)[1]);
            tag.add(list.get(i)[2]);
        }


        mChart.setUsePercentValues(false);
        mChart.setDescription("");

        // enable hole and configure
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);
        mChart.setHoleRadius(7);
        mChart.setTransparentCircleRadius(10);
        mChart.setTouchEnabled(false);

        // enable rotation of the chart by touch
        mChart.setRotationAngle(270);
        mChart.setRotationEnabled(false);

        // set a chart value selected listener
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                // display msg when value selected
                if (e == null)
                    return;
                textview.setText("");
                for (int i = 0; i < tag.size(); i++) {
                    if(e.getXIndex() + c == Integer.parseInt(tag.get(i))) {
                        textview.append(start.get(i)+ "～"+ end.get(i)+ "\n" );
                    }
                }
                if (textview.length() == 0){
                    textview.setText("この時間帯は大丈夫だよ!");
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });

        // add tomo
        addData(mChart);

        // customize legends
        Legend l = mChart.getLegend();
        l.setEnabled(false);
    }






    private void addData(PieChart mChart) {

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < yData.length; i++)
            yVals1.add(new Entry(yData[i], i));

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < xData.length; i++)
            xVals.add(xData[i]);

        // create pie tomo set
        PieDataSet dataSet = new PieDataSet(yVals1, "Information");
        dataSet.setSliceSpace(0);
        dataSet.setSelectionShift(2);


        for (int i = a ; i < b ; i++) {

            if (RED.size() == 0){
                // do nothing
            }
            else if (Integer.parseInt(RED.get(i)) == 0) {
                colors[i-d] = Color.parseColor("#f1c40f");
            }
            else {
                colors[i-d] = Color.parseColor("#E8E8E8");
            }

        }


        dataSet.setColors(colors);

        // instantiate pie tomo object now
        PieData data = new PieData(xVals, dataSet);
        // tomo.setValueFormatter(new DefaultValueFormatter(0));
        data.setDrawValues(false);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        // update pie chart
        mChart.invalidate();
    }






    private void Initial() {
        for (int i = 0; i < yData.length; i++) {
            yData[i] = 1;
            xData[i] = String.valueOf(i);


            for (int j = 0; j <yDataup.length ; j++) {
              yDataup[j] = 1;
                xDataup[j] = String.valueOf(j);
            }
        }

        st.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                st.setText("午後");
                    a = hour_tag; b = hour_tag*2; c = hour_tag; d = hour_tag;
                    A = step_num; B = step_num*2; D = step_num;

                    listclear();
                    textview.setText("");
                    PieChartSetting(mPiechart);
                    mPiechart.setTouchEnabled(true);
                    Up_PiechartSetting(mPiechart_up);
                }
                else {
                    st.setText("午前");
                    a = 0; b = hour_tag; c = 0; d = 0;
                    A = 0; B = step_num;  D = 0;

                    listclear();
                    textview.setText("");
                    PieChartSetting(mPiechart);
                    mPiechart.setTouchEnabled(true);
                    Up_PiechartSetting(mPiechart_up);
                }
            }
        });
    }






    private void Setlist(ListView listview,ArrayList<String> csvlist){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_expandable_list_item_1,csvlist);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                file = (String) listView.getItemAtPosition(position);
                file = file.replaceAll("[\\u4e00-\\u9fa5]", "");
                listclear();
                textview.setText("");
                textView_emp.setText("");
                PieChartSetting(mPiechart);
                mPiechart.setTouchEnabled(true);
                Up_PiechartSetting(mPiechart_up);
            }
        });
    }





    private void listclear(){
        RED.clear();
        start.clear();
        end.clear();
        tag.clear();
        RED_up.clear();
    }






    private void Up_PiechartSetting(PieChart mPiechart_up) {
        String[] next;
        List<String[]> list = new ArrayList<String[]>();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(new File(sdPath + "/"+ file +"/"+ CSV_UP))));
            while ((next = reader.readNext()) != null) {
                list.add(next);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < list.size(); i++) {
            RED_up.add(list.get(i)[0]);
        }

        mPiechart_up.setUsePercentValues(false);
        mPiechart_up.setDescription("");

        // enable hole and configure
        mPiechart_up.setDrawHoleEnabled(true);
        mPiechart_up.setHoleColorTransparent(true);
        mPiechart_up.setHoleRadius(7);
        mPiechart_up.setTransparentCircleRadius(10);
        mPiechart_up.setTouchEnabled(false);

        // enable rotation of the chart by touch
        mPiechart_up.setRotationAngle(270);
        mPiechart_up.setRotationEnabled(false);

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < yDataup.length; i++)
            yVals1.add(new Entry(yDataup[i], i));

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < xDataup.length; i++)
            xVals.add(xDataup[i]);

        // create pie tomo set
        PieDataSet dataSet = new PieDataSet(yVals1, "Information");
        dataSet.setSliceSpace(0);
        dataSet.setSelectionShift(0);


        for (int i = A ; i < B ; i++) {

            if (RED_up.size() == 0){
                // do nothing
            }
            else if (Integer.parseInt(RED_up.get(i)) == 0) {

                colorsup[i-D] = Color.parseColor("#f79951");
            }
            else {
                colorsup[i-D] = Color.parseColor("#bbE8E8E8");

            }

        }


        dataSet.setColors(colorsup);

        // instantiate pie tomo object now
        PieData data = new PieData(xVals, dataSet);
        // tomo.setValueFormatter(new DefaultValueFormatter(0));
        data.setDrawValues(false);

        mPiechart_up.setData(data);

        // undo all highlights
        mPiechart_up.highlightValues(null);

        // update pie chart
        mPiechart_up.invalidate();


        // customize legends
        Legend l = mPiechart_up.getLegend();
        l.setEnabled(false);

    }

    private ArrayList<String> Changtodate(ArrayList<String> csvlist){
        ArrayList<String > list = new ArrayList<String>();
        Date date = null;
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy年MM月dd日");
        DateFormat format = new SimpleDateFormat("yyyyMMdd");

        for (int i = 0; i <csvlist.size() ; i++) {

            try {
                date = format.parse(csvlist.get(i));
                list.add(ft.format(date));
            }catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


}

