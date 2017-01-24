package com.example.tangcan0823.chart_test;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Handler;

import static android.R.attr.name;
import static android.os.Build.VERSION_CODES.M;


/**
 * Created by tangcan0823 on 2016/11/16.
 */
public class TomoFragment extends Fragment {
    String name = MainActivity.name;
    String interval = "5";
    private String sdPath = Environment.getExternalStorageDirectory().getPath();

    //現在日時を取得する
    Calendar ccc = Calendar.getInstance();
    //フォーマットパターンを指定して表示する
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日");
    String time = sdf.format(ccc.getTime());
    String nowtime = time.substring(0,4)+time.substring(5,7)+time.substring(8,10);

    File MTIdir = new File("/sdcard/MTI/");
    File namedir = new File("/sdcard/MTI/"+name);
    File datedir = new File("/sdcard/MTI/"+name+"/"+nowtime);

    private Button button;
    private ListView listview;
    private Toolbar mToolbar;

    private String CSV_read = name+".csv";
    private String CSV_read1 = name+"_acc.csv";
    private File file=new File(sdPath +"/"+ CSV_read);
    private File file1=new File(sdPath +"/"+ CSV_read1);

    Message message=null;
    android.os.Handler  handler=new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1==1){
                // Toast.makeText(getActivity().getApplicationContext(),"执行完了",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    };


    //********************
    Thread thread;
    //*****************
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tomo, null);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
       // button = (Button) view.findViewById(R.id.button1);
        listview = (ListView) view.findViewById(R.id.listview_data);


        System.out.println("-------------------------->"+MTIdir+"+++++++++++++++++++++++++++++");
        if(!(MTIdir.exists())){
            MTIdir.mkdir();

        }
        System.out.println("-------------------------->"+namedir+"+++++++++++++++++++++++++++++");
        if(!(namedir.exists())){
            namedir.mkdir();

        }
        System.out.println("-------------------------->"+datedir+"+++++++++++++++++++++++++++++");
        if(!(datedir.exists())){
            datedir.mkdir();
        }
        file.renameTo(new File(sdPath +"/MTI/"+ name +"/"+ nowtime + "/"+ CSV_read));
        file1.renameTo(new File(sdPath +"/MTI/"+ name +"/"+ nowtime + "/"+ CSV_read1));



        File[] files;
        ArrayList<String> csvlist = new ArrayList<String>();
        files = new File(sdPath + "/MTI/" + name).listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    csvlist.add(files[i].getName());
                }
            }
        }
        Collections.sort(csvlist);
        Setlist(listview, csvlist);

        return view;
    }

//******************
ProgressDialog progressDialog;
//************
    private void Setlist(ListView listview, final ArrayList<String> csvlist) {


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, csvlist);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                nowtime = (String) listView.getItemAtPosition(position);

                for (int i = 0; i < csvlist.size(); i++) {
                    if (i == position) {
                        view.setBackgroundColor(Color.parseColor("#7fbfff"));
                    } else {
                        listView.getChildAt(i).setBackgroundColor(Color.parseColor("#00BEBEBE"));
                    }
                }
                //********************

               showProgressDialogWithTitle();

                //*******************

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        DataProcess();
                        message=new Message();
                        message.arg1=1;
                        handler.sendMessage(message);
                    }
                }).start();


            }
        });
    }
    //***************
    private void showProgressDialogWithTitle() {
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("処理中...");
        progressDialog.show();
    }
        //********************************************
    private void DataProcess() {

        //input processing time interval is one second
        int TimeGap=Integer.parseInt(interval)*60;
        //input represents time interval in seconds
        int ShowGap=Integer.parseInt(interval)*60;//always synchronized with the step in PiechartFragment
        /**
         * Gets the current time
         */
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");//Representation in  ("yyyy/MM/dd HH:mm:ss")
        String hehe = dateFormat.format( now );
        int NowTime=Integer.parseInt(hehe);


        //Turns the current time into seconds
        int NowTime_Second = NowTime/10000*3600+(NowTime%10000)/100*60+NowTime%100;



        /**
         * Import ble Data
         */
        List<String> list_TIME0 = new ArrayList<String>();
        List<String> list_ID0 = new ArrayList<String>();

        try {

            FileReader fr = new FileReader(new File(sdPath + "/MTI/" + name + "/" +nowtime+"/" + CSV_read));
            BufferedReader br = new BufferedReader(fr);

            String line;
            StringTokenizer token;
            while ((line = br.readLine()) != null) {
                token = new StringTokenizer(line, ",");
                while (token.hasMoreTokens()) {
                    list_ID0.add(token.nextToken());
                    list_TIME0.add(token.nextToken());
                }
            }
            br.close();

        } catch (IOException ex) {

            ex.printStackTrace();
        }
        /**
         * Read the acceleration data, get the last acceleration time as the current time
         */
        String lastTime = "";
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(sdPath  + "/MTI/" + name+ "/" + nowtime + "/" + CSV_read1), "r");
            long len = raf.length();
            String stringspilt[];
            String lastLine = "";

            if (len != 0L) {
                long pos = len - 1;
                while (pos > 0) {
                    pos--;
                    raf.seek(pos);
                    if (raf.readByte() == '\n') {
                        lastLine = raf.readLine();
                        stringspilt=lastLine.split(",");
                        lastTime=stringspilt[3];
                        break;
                    }
                }
            }
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int TrueTimeDetla=Integer.parseInt(lastTime)-Integer.parseInt(list_TIME0.get(list_TIME0.size()-1)) ;

        if(TrueTimeDetla<=0){


            /**
             * When the original data is empty when given 3 blank table to prevent an error
             */
            if(list_ID0.size()==0){
                EmptyFile();
            }else{
                /**
                 * Set up friends list,
                 */
                List<String> list_friend = new ArrayList<String>();
                try {

                    FileReader fr = new FileReader(new File(sdPath+"/friendlist.csv"));
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    StringTokenizer token;
                    while ((line = br.readLine()) != null) {
                        token = new StringTokenizer(line, ",");
                        while (token.hasMoreTokens()) {
                            list_friend.add(token.nextToken());
                        }
                    }
                    br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                /**
                 * Take the current time as the end time, subtract the last time of the acquisition time to get the Delta
                 * The original time each time, respectively, with the difference so that the last time just equal to the current time
                 * At this point the first time is no longer 0:00 0 seconds
                 * So the length of the array not change  but the time the starting point has changed
                 */
                int LastTimeACC=Integer.parseInt(lastTime);

                int FirstTimeACC=NowTime_Second-LastTimeACC+1;

                int LastTimeBLE=FirstTimeACC+Integer.parseInt(list_TIME0.get(list_TIME0.size()-1));


                int Delta=NowTime_Second-LastTimeACC;


                /**
                 *
                 * Eliminate all people who are not friends, but this will cause the array length shorter lead to other problems
                 * So all the people who are not friends agree to change to a specific ID ID, changed as long as the ID is a specific value, set directly to the standard red
                 * Because of want to modify a specific location also turned back into an array after the first
                 */


                String[][]  f = new String[list_ID0.size()][2];

                for(int i=0;i<list_ID0.size();i++){
                    f[i][0]=list_ID0.get(i);
                }
                for(int i=0;i<list_TIME0.size();i++){
                    f[i][1]=list_TIME0.get(i);
                }

                int ids=f.length;
                for (int i=0; i<ids; i++) {
                    if(!list_friend.contains(f[i][0])){
                        f[i][0]="badboy";
                    }
                }



                /**
                 * Processing starts
                 * According to the setted processing time, all of the time data for the time of the small side points,
                 * calculate the number of data for each segment
                 *
                 */


                // in the part of the above things
                // Remove people who are not friends
                List<String> f_nobadboy_id = new ArrayList<String>();
                List<String> f_nobadboy_time = new ArrayList<String>();

                for (int i=0; i<f.length; i++) {
                    if(list_friend.contains(f[i][0])){
                        f_nobadboy_id.add(f[i][0]);
                        f_nobadboy_time.add(f[i][1]);
                    }
                }



                //only ID  to unique, in order to see all the data in a total of several friends ID is recorded,
                // array ALL which the relationship array between the two-dimensional  has a few columns.
                // String_ID.size () is the number of columns
                List<String> String_ID = new ArrayList<String>();//Copy for uniquely
                //String_ID.add(f_nobadboy_id.get(0));
                for (int i=0; i<f_nobadboy_id.size(); i++) {
                    if(!String_ID.contains(f_nobadboy_id.get(i))){
                        String_ID.add(f_nobadboy_id.get(i));
                    }
                }
                if(String_ID.size()==0){
                    InvalidData();
                }else{

                //Arbitrary time segmentation,
                // all data of the time marked as the time of the small end point,
                // rewrited time is not written in the original table,
                // but written in the new table list_int_TimeSection
                    List<Integer> int_TIME = new ArrayList<Integer>();
                    for(int i=0;i<f_nobadboy_time.size();i++){
                        int_TIME.add(((Integer.parseInt((f_nobadboy_time.get(i)))/TimeGap)*TimeGap)) ;
                    }





                //Calculate the total number of data in each TimeGap minute have data
                List<Integer> NUM = new ArrayList<Integer>();
                int w=0;
                int n=0;

                for(int i = 0; i < int_TIME.size(); ){
                    while((int_TIME.get(i)).equals(0+TimeGap*n)){
                        w++;
                        i++;
                        if(i==int_TIME.size()){break;}
                    }

                    NUM.add(w);
                    w=0;
                    n++;

                }
                //The first few terms of the series and, that is, the number of array in the after
                List<Integer> NUM_SUM = new ArrayList<Integer>();
                NUM_SUM.add(0);//Add a bit for the next (n+1)  in ALL
                int sum=0;
                for(int i=0;i<NUM.size();i++){
                    sum+=NUM.get(i);
                    NUM_SUM.add(sum);
                }



                int RR;//ALL.size
                //To prevent int_TIME0.size()==0, the array get index is equal to -1
                if(int_TIME.size()==0){
                    RR=0;
                }
                else{
                    RR=(int_TIME.get(int_TIME.size()-1))/TimeGap+1;//Read the last time, and then segmented it, in paragraph above, there are many lines
                }

                    int LL=(NowTime_Second-FirstTimeACC)/TimeGap+1;

                int m=0;
                int[][]  ALL = new int[LL][String_ID.size()];
                //put data in ALL
                    for (int y = 0; y < RR; y++) {
                        {
                            if(NUM_SUM.size()>=2){
                                for (int q = NUM_SUM.get(m); q < NUM_SUM.get(m+1); q++)
                                {
                                    for (int x = 0; x < String_ID.size(); x++)
                                    {
                                        if(int_TIME.get(q).equals(0+TimeGap*m)&&String_ID.get(x).equals(f_nobadboy_id.get(q))){
                                            ALL[y][x] = 1;
                                        }
                                    }
                                }
                                m++;
                            }else{
                                for(int p=0;p<NUM_SUM.get(0);p++){
                                    for (int x = 0; x < String_ID.size(); x++)
                                    {
                                        if(int_TIME.get(p).equals(0+TimeGap*m)&&String_ID.get(x).equals(f_nobadboy_id.get(p))){
                                            ALL[y][x] = 1;
                                        }
                                    }}
                            }
                        }
                    }




                int[][]  TIME_add = new int[ALL.length][2];
                //Write the first column
                    for(int i=0,nn=0;i<TIME_add.length;i++){
                        TIME_add[i][0]=Delta+TimeGap*(nn+1);
                        nn++;
                    }

                //Write the 2 column
                    for(int i=0,ss=0;i<TIME_add.length;i++){
                        for(int j=0;j<String_ID.size();j++){
                            ss+=ALL[i][j];

                        }
                        TIME_add[i][1]=ss;
                        ss=0;
                    }



                //In order to output in accordance with the standard time format, so to copy a data standardization
                    String[][]  bb4_sum = new String[TIME_add.length][2];
                //Write the first column

                    for(int i=0,nn=0,s=0;i<TIME_add.length;i++){
                        String HH;
                        String MM;

                        s=TIME_add[i][0];

                        if(s<60){
                            bb4_sum[i][0]="0";
                        }
                        if((s>=60)&&(s<3600)){
                            MM=Integer.toString(s/60);
                            bb4_sum[i][0]="0:"+MM;
                        }
                        if(s%3600==0){
                            HH=String.valueOf(s/3600);
                            bb4_sum[i][0]=HH+":"+00;
                        }
                        if(s>3600){
                            HH=String.valueOf(s/3600);
                            MM=String.valueOf(s%3600/60);
                            bb4_sum[i][0]=HH+":"+MM;
                        }

                        nn++;
                    }
                //Write the 2nd column
                    for(int i1=0,ss=0;i1<TIME_add.length;i1++){
                        bb4_sum[i1][1]=String.valueOf(TIME_add[i1][1]);
                    }

                /**
                 * Creates two single lists of the same length used to record the start points and end points of each 0 range of time_add
                 */
                    List<Integer> alone_up = new ArrayList<Integer>();
                    List<Integer> alone_down = new ArrayList<Integer>();
                    int timeup=0;
                    int timedown=0;
                    for(int i1=0;i1<TIME_add.length;i1++){
                        int count=0;
                        if(i1!=TIME_add.length-1){
                            if(TIME_add[i1][1]==0&&TIME_add[i1+1][1]!=0){////Only one data is 0 case
                                if(i1==0){}
                                else
                                if(i1>0){
                                    timeup=TIME_add[i1-1][0];
                                    timedown=TIME_add[i1][0];
                                    alone_up.add(timeup);
                                    alone_down.add(timedown);}
                            }
                            else
                            if(TIME_add[i1][1]==0&&TIME_add[i1+1][1]==0){//A series of 0 cases
                            timeup=TIME_add[i1][0];
                            int j=i1;
                            if(j>TIME_add.length-2){
                                timedown=TIME_add[j-1][0];
                            }
                            else{
                                while(TIME_add[j][1]==0){
                                    count++;
                                    if(j==TIME_add.length-1){
                                        break;
                                    }
                                    j++;
                                }
                                timedown=TIME_add[j][0];
                            }
                            alone_up.add(timeup);
                            alone_down.add(timedown);
                        }
                    }
                    i1=i1+count;
                }
                for(int i=0;i<alone_up.size();i++){

                }
                int HourDelta=0;
                int MinDelta=0;
                List<Integer> list_RED= new ArrayList<Integer>();
                List<Integer> list_RED_min= new ArrayList<Integer>();


                for(int i1=0;i1<alone_up.size();i1++){

                    HourDelta=alone_down.get(i1)/3600-alone_up.get(i1)/3600+1;
                    MinDelta=alone_down.get(i1)/60-alone_up.get(i1)/60+1;
                    //hour
                    ////System.out.println("HourDelta"+HourDelta);
                    for(int j=0;j<HourDelta;j++){
                        list_RED.add(alone_up.get(i1)/3600+j);
                    }

                    //min
                    ////System.out.println("MinDelta"+MinDelta);
                    for(int j=0;j<MinDelta;j++){
                        list_RED_min.add(alone_up.get(i1)/60+j);
                    }


                }



                //hour marked red
                int [] array_HourMark = new int[24];
                for(int i1=0;i1<array_HourMark.length;i1++){//24 hours to fill 1
                    array_HourMark[i1]=1;
                }

                for(int i1=0;i1<list_RED.size();i1++){//Get the third column of the table where the value is written to 0
                    array_HourMark[list_RED.get(i1)]=0;
                }


                //Minute marked red
                int [] array_MinMark = new int[24*60];
                for(int i=0;i<array_MinMark.length;i++){//fill 1440 min in 1
                    array_MinMark[i]=1;
                }

                for(int i=0;i<list_RED_min.size();i++){
                    array_MinMark[list_RED_min.get(i)]=0;//Get the list_RED_min where the value is written to 0
                }



                //Start here because list_RED_min is a one minute limit. 1440 lattice,
                // in order to express any minute span any minute span add up,
                int [] MinMark_show = new int[(24*3600)/ShowGap];//288


                for(int i=0 , t=0,nn=0;i<MinMark_show.length;i++){
                    for(int j=0 ;j<ShowGap/60;j++)
                    {
                        t=t+array_MinMark[nn+j];
                    }
                    nn=nn+(ShowGap/60);
                    MinMark_show[i]=t;
                    t=0;
                }





                /**
                 * The new array ALONE format is equal to alonerange
                 */

                int[][]  Alone = new int[list_RED.size()][3];
                int c=0;
                int tt=0;
                for(int i=0;i<alone_up.size();i++){
                    HourDelta=alone_down.get(i)/3600-alone_up.get(i)/3600+1;


                    for (int jj=0; jj<HourDelta; jj++){
                        tt=c++;
                        Alone[tt][0]=alone_up.get(i);
                        Alone[tt][1]=alone_down.get(i);
                        Alone[tt][2]=list_RED.get(tt);

                    }
                }



                //To print the array alone to give the first two columns into the array of standard time format, the new string array to solve
                String[][]  Alone2 = new String[Alone.length][3];
                //The first two columns themselves are in the form of seconds, each of which corresponds to the standard time form
                //The first column
                for(int i=0;i<Alone2.length;i++){
                    if(((Alone[i][0]%3600/60)>=0)&&((Alone[i][0]%3600/60)<10)){
                        Alone2[i][0]=String.valueOf(Alone[i][0]/3600)+":0"+String.valueOf(Alone[i][0]%3600/60);
                    }
                    else
                        Alone2[i][0]=String.valueOf(Alone[i][0]/3600)+":"+String.valueOf(Alone[i][0]%3600/60);
                }
                //The 2nd column
                for(int i=0;i<Alone2.length;i++){
                    if(((Alone[i][1]%3600/60)>=0)&&((Alone[i][1]%3600/60)<10)){
                        Alone2[i][1]=String.valueOf(Alone[i][1]/3600)+":0"+String.valueOf(Alone[i][1]%3600/60);
                    }
                    else
                        Alone2[i][1]=String.valueOf(Alone[i][1]/3600)+":"+String.valueOf(Alone[i][1]%3600/60);
                }
                //The 3rd column
                for(int i=0;i<Alone2.length;i++){
                    Alone2[i][2]=String.valueOf(Alone[i][2]);
                }



                int[][]  TIME_change = new int[RR][2];
                //The first column
                    for(int i=0,pp=0;i<RR;i++){
                        TIME_change[i][0]=TIME_add[i][0];
                    }
                //The 2nd column
                TIME_change[0][1]=0;
                for(int i1=1;i1<RR;i1++){
                    for(int j=0;j<String_ID.size();j++){
                        TIME_change[i1][1]=Math.abs(ALL[i1-1][j]-ALL[i1][j])+TIME_change[i1][1];
                    }
                }

//In order to output in accordance with the standard time format, so to copy a data standardization
                String[][]  bb4_change = new String[TIME_change.length][2];
                //The first column

                for(int i=0,nn=0,s=0;i<TIME_change.length;i++){
                    String HH;
                    String MM;

                    s=TIME_change[i][0];

                    if(s<60){
                        bb4_change[i][0]="0";
                    }
                    if((s>=60)&&(s<3600)){
                        MM=Integer.toString(s/60);
                        bb4_change[i][0]="0:"+MM;
                    }
                    if(s>3600){
                        HH=String.valueOf(s/3600);
                        MM=String.valueOf(s%3600/60);
                        bb4_change[i][0]=HH+":"+MM;
                    }
                    if(s%3600==0){
                        HH=String.valueOf(s/3600);
                        bb4_change[i][0]=HH+":"+00;
                    }
                    nn++;
                }
                //The 2nd column
                for(int i1=0,ss=0;i1<TIME_change.length;i1++){
                    bb4_change[i1][1]=String.valueOf(TIME_change[i1][1]);
                }


                //Export bb4_sum.csv

            try {
                FileWriter writer = new FileWriter(new File(sdPath + "/MTI/" + name + "/" + nowtime, name+"_sum.csv"));
                for (int i1 = 0; i1 < TIME_add.length; ) {
                    for (int j = 0; j < TIME_add[0].length; j++) {
                        writer.append(String.valueOf(bb4_sum[i1][j]));
                        writer.append(',');
                    }
                    writer.append('\n');
                    i1++;
                    writer.flush();
                }

                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            ////Export bb4_change.csv
            try {
                FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_change.csv"));

                for (int i1 = 0; i1 < bb4_change.length; ) {
                    for (int j = 0; j < bb4_change[0].length; j++) {
                        writer.append(String.valueOf(bb4_change[i1][j]));
                        writer.append(',');
                    }
                    writer.append('\n');
                    i1++;
                    writer.flush();
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ////Export bb4_hourmark.csv in 24 row
            try {
                FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_hourmark.csv"));

                for (int i9 = 0; i9 < array_HourMark.length; ) {

                    writer.append(String.valueOf(array_HourMark[i9]));
                    writer.append('\n');
                    i9++;
                    writer.flush();
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

                //Export bb4_minmark.csv
            try {
                FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_minmark.csv"));

                for (int i = 0; i < MinMark_show.length; ) {

                    writer.append(String.valueOf(MinMark_show[i]));
                    writer.append('\n');
                    i++;
                    writer.flush();
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ////Export AloneRange.csv
            try {
                FileWriter writer = new FileWriter(new File(sdPath + "/MTI/" + name + "/" + nowtime, name+"_alonerange.csv"));

                for (int i1 = 0; i1 < Alone2.length; ) {
                    for (int j = 0; j < Alone2[0].length; j++) {
                        writer.append(String.valueOf(Alone2[i1][j]));
                        writer.append(',');

                    }
                    writer.append('\n');
                    i1++;
                    writer.flush();
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
                }


        }
        }

/**
 * / / Acceleration final time is bigger than the final time of the Bluetooth case the final time with the acceleration of the final time
 */
        if(TrueTimeDetla>0){
/**
 *
 * When the original data is empty when given 3 blank table to prevent an error
 *
 */
            if(list_ID0.size()==0)
            {
                EmptyFile();
            }else{

/**
 * Set up friends list,
 */
                List<String> list_friend = new ArrayList<String>();
                try {

                    FileReader fr = new FileReader(new File(sdPath+"/friendlist.csv"));
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    StringTokenizer token;
                    while ((line = br.readLine()) != null) {
                        token = new StringTokenizer(line, ",");
                        while (token.hasMoreTokens()) {
                            list_friend.add(token.nextToken());
                        }
                    }
                    br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                /**
                 * Take the current time as the end time, subtract the last time of the acquisition time to get the Delta
                 * The original time each time, respectively, with the difference so that the last time just equal to the current time
                 * At this point the first time is no longer 0:00 0 seconds
                 * So the length of the array not change  but the time the starting point has changed
                 */
                int LastTimeACC=Integer.parseInt(lastTime);

                int FirstTimeACC=NowTime_Second-LastTimeACC+1;

                int LastTimeBLE=FirstTimeACC+Integer.parseInt(list_TIME0.get(list_TIME0.size()-1));

                int Delta=NowTime_Second-LastTimeACC;

                /**
                 *
                 * Eliminate all people who are not friends, but this will cause the array length shorter lead to other problems
                 * So all the people who are not friends agree to change to a specific ID ID, changed as long as the ID is a specific value, set directly to the standard red
                 * Because of want to modify a specific location also turned back into an array after the first
                 */

                String[][]  f = new String[list_ID0.size()][2];
                for(int i=0;i<list_ID0.size();i++){
                    f[i][0]=list_ID0.get(i);
                }
                for(int i=0;i<list_TIME0.size();i++){
                    f[i][1]=list_TIME0.get(i);
                }


                int ids=f.length;
                for (int i=0; i<ids; i++) {
                    if(!list_friend.contains(f[i][0])){
                        f[i][0]="badboy";
                    }
                }



                /**
                 * Processing starts
                 * According to the setted processing time, all of the time data for the time of the small side points,
                 * calculate the number of data for each segment
                 *
                 */


                // in the part of the above things
                // Remove people who are not friends
                List<String> f_nobadboy_id = new ArrayList<String>();
                List<String> f_nobadboy_time = new ArrayList<String>();


                for (int i=0; i<f.length; i++) {
                    if(list_friend.contains(f[i][0])){
                        f_nobadboy_id.add(f[i][0]);
                        f_nobadboy_time.add(f[i][1]);
                    }
                }


                //ID alone is unique, in order to see all the data in a total of several friends ID is recorded,
                // array ALL which the relationship array between the two-dimensional  has a few columns.
                // String_ID.size () is the number of columns
                List<String> String_ID = new ArrayList<String>();//Copy for uniquely
//                String_ID.add(f_nobadboy_id.get(0));

                for (int i=0; i<f_nobadboy_id.size(); i++) {
                    if(!String_ID.contains(f_nobadboy_id.get(i))){
                        String_ID.add(f_nobadboy_id.get(i));
                    }
                }
                if(String_ID.size()==0){
                    InvalidData();
                }else{

                //Arbitrary time segmentation,
                // all data of the time marked as the time of the small end point,
                // rewrited time is not written in the original table,
                // but written in the new table list_int_TimeSection
                    List<Integer> int_TIME = new ArrayList<Integer>();
                    for(int i=0;i<f_nobadboy_time.size();i++){
                        int_TIME.add(((Integer.parseInt((f_nobadboy_time.get(i)))/TimeGap)*TimeGap)) ;
                    }

                    List<Integer> NUM = new ArrayList<Integer>();//Calculate the total number of data in each TimeGap minute have data
                    int w=0;
                    int n=0;

                    for(int i = 0; i < int_TIME.size(); ){
                        while((int_TIME.get(i)).equals(0+TimeGap*n)){
                            w++;
                            i++;
                            if(i==int_TIME.size()){break;}
                        }

                        NUM.add(w);
                        w=0;
                        n++;
                    }

                List<Integer> NUM_SUM = new ArrayList<Integer>();//The first few terms of the series and, that is, the number of array in the after
                NUM_SUM.add(0);//Add a bit for the next (n+1)  in ALL
                int sum=0;
                for(int i=0;i<NUM.size();i++){
                    sum+=NUM.get(i);
                    NUM_SUM.add(sum);
                }



                int RR;//ALL.size
                //To prevent int_TIME0.size()==0, the array get index is equal to -1
                if(int_TIME.size()==0){
                    RR=0;
                }
                else{
                    RR=(int_TIME.get(int_TIME.size()-1)-int_TIME.get(0))/TimeGap+1;//Read the last time, and then segmented it, in paragraph above, there are many lines
                }
                    int LL=(NowTime_Second-FirstTimeACC)/TimeGap+1;//True ble data start and end time difference
                    int m=0;
                    int[][]  ALL = new int[LL][String_ID.size()];
                //put data in ALL
                    for (int y = 0; y < RR; y++) {
                        {
                            if(NUM_SUM.size()>=2){
                                for (int q = NUM_SUM.get(m); q < NUM_SUM.get(m+1); q++)
                                {
                                    for (int x = 0; x < String_ID.size(); x++)
                                    {
                                        if(int_TIME.get(q).equals(0+TimeGap*m)&&String_ID.get(x).equals(f_nobadboy_id.get(q))){
                                            ALL[y][x] = 1;
                                        }
                                    }
                                }
                                m++;
                            }else{
                                for(int p=0;p<NUM_SUM.get(0);p++){
                                    for (int x = 0; x < String_ID.size(); x++)
                                    {
                                        if(int_TIME.get(p).equals(0+TimeGap*m)&&String_ID.get(x).equals(f_nobadboy_id.get(p))){
                                            ALL[y][x] = 1;
                                        }
                                    }}
                            }
                        }
                    }




                    int[][]  TIME_add = new int[LL][2];
                //Write the first column
                    for(int i=0,nn=0;i<TIME_add.length;i++){
                        TIME_add[i][0]=Delta+TimeGap*(nn+1);
                        nn++;
                    }

                //Write the 2 column
                    for(int i=0,ss=0;i<TIME_add.length;i++){
                        for(int j=0;j<String_ID.size();j++){
                            ss+=ALL[i][j];

                        }
                        TIME_add[i][1]=ss;
                        ss=0;
                    }


                //In order to output in accordance with the standard time format, so to copy a data standardization
                String[][]  bb4_sum = new String[TIME_add.length][2];
                //Write the first column

                    for(int i=0,nn=0,s=0;i<TIME_add.length;i++){
                        String HH;
                        String MM;

                        s=TIME_add[i][0];

                        if(s<60){
                            bb4_sum[i][0]="0";
                        }
                        if((s>=60)&&(s<3600)){
                            MM=Integer.toString(s/60);
                            bb4_sum[i][0]="0:"+MM;
                        }
                        if(s%3600==0){
                            HH=String.valueOf(s/3600);
                            bb4_sum[i][0]=HH+":"+00;
                        }
                        if(s>3600){
                            HH=String.valueOf(s/3600);
                            MM=String.valueOf(s%3600/60);
                            bb4_sum[i][0]=HH+":"+MM;
                        }

                        nn++;
                    }
                //Write the 2nd column
                    for(int i1=0,ss=0;i1<TIME_add.length;i1++){
                        bb4_sum[i1][1]=String.valueOf(TIME_add[i1][1]);
                    }

                /**
                 * Creates two single lists of the same length used to record the start points and end points of each 0 range of time_add
                 */
                    List<Integer> alone_up = new ArrayList<Integer>();
                    List<Integer> alone_down = new ArrayList<Integer>();
                    int timeup=0;
                    int timedown=0;
                    for(int i1=0;i1<TIME_add.length;i1++){
                        int count=0;
                        if(i1!=TIME_add.length-1){
                            if(TIME_add[i1][1]==0&&TIME_add[i1+1][1]!=0){///Only one data is 0 case
                                if(i1==0){}
                                else
                                if(i1>0){
                                    timeup=TIME_add[i1-1][0];
                                    timedown=TIME_add[i1][0];
                                    alone_up.add(timeup);
                                    alone_down.add(timedown);}
                            }
                        else
                        if(TIME_add[i1][1]==0&&TIME_add[i1+1][1]==0){//A series of 0 cases
                            timeup=TIME_add[i1][0];
                            int j=i1;
                            if(j>TIME_add.length-2){
                                timedown=TIME_add[j-1][0];
                            }
                            else{
                                while(TIME_add[j][1]==0){
                                    count++;
                                    if(j==TIME_add.length-1){
                                        break;
                                    }
                                    j++;
                                }
                                timedown=TIME_add[j][0];
                            }
                            alone_up.add(timeup);
                            alone_down.add(timedown);
                        }
                    }
                    i1=i1+count;
                }
                for(int i=0;i<alone_up.size();i++){

                }
                int HourDelta=0;
                int MinDelta=0;
                List<Integer> list_RED= new ArrayList<Integer>();
                List<Integer> list_RED_min= new ArrayList<Integer>();


                for(int i1=0;i1<alone_up.size();i1++){

                    HourDelta=alone_down.get(i1)/3600-alone_up.get(i1)/3600+1;
                    MinDelta=alone_down.get(i1)/60-alone_up.get(i1)/60+1;
                    //hour
                    ////System.out.println("HourDelta"+HourDelta);
                    for(int j=0;j<HourDelta;j++){
                        list_RED.add(alone_up.get(i1)/3600+j);
                    }

                    //min
                    ////System.out.println("MinDelta"+MinDelta);
                    for(int j=0;j<MinDelta;j++){
                        list_RED_min.add(alone_up.get(i1)/60+j);
                    }


                }



                //hour marked red
                int [] array_HourMark = new int[24];
                for(int i1=0;i1<array_HourMark.length;i1++){//24 hours to fill 1
                    array_HourMark[i1]=1;
                }

                for(int i1=0;i1<list_RED.size();i1++){//Get the third column of the table where the value is written to 0
                    array_HourMark[list_RED.get(i1)]=0;
                }

                //Minute marked red
                int [] array_MinMark = new int[24*60];
                for(int i=0;i<array_MinMark.length;i++){//fill 1440 min in 1
                    array_MinMark[i]=1;
                }


                for(int i=0;i<list_RED_min.size();i++){
                    array_MinMark[list_RED_min.get(i)]=0;//Get the list_RED_min where the value is written to 0
                }




                //Start here because list_RED_min is a one minute limit. 1440 lattice,
                // in order to express any minute span any minute span add up,
                int [] MinMark_show = new int[(24*3600)/ShowGap];//288


                for(int i=0 , t=0,nn=0;i<MinMark_show.length;i++){
                    for(int j=0 ;j<ShowGap/60;j++)
                    {
                        t=t+array_MinMark[nn+j];
                    }
                    nn=nn+(ShowGap/60);
                    MinMark_show[i]=t;
                    t=0;
                }



                /**
                 * The new array ALONE format is equal to alonerange
                 */
                int[][]  Alone = new int[list_RED.size()][3];

                for(int i=0,c=0,tt=0;i<alone_up.size();i++){
                    HourDelta=alone_down.get(i)/3600-alone_up.get(i)/3600+1;


                    for (int jj=0; jj<HourDelta; jj++){
                        tt=c++;
                        Alone[tt][0]=alone_up.get(i);
                        Alone[tt][1]=alone_down.get(i);
                        Alone[tt][2]=list_RED.get(tt);
                    }
                }


                //To print the array alone to give the first two columns into the array of standard time format, the new string array to solve
                String[][]  Alone2 = new String[Alone.length][3];
                //The first two columns themselves are in the form of seconds, each of which corresponds to the standard time form
                //The first column
                for(int i=0;i<Alone2.length;i++){
                    if(((Alone[i][0]%3600/60)>=0)&&((Alone[i][0]%3600/60)<10)){
                        Alone2[i][0]=String.valueOf(Alone[i][0]/3600)+":0"+String.valueOf(Alone[i][0]%3600/60);
                    }
                    else
                        Alone2[i][0]=String.valueOf(Alone[i][0]/3600)+":"+String.valueOf(Alone[i][0]%3600/60);
                }
                //The 2nd column
                for(int i=0;i<Alone2.length;i++){
                    if(((Alone[i][1]%3600/60)>=0)&&((Alone[i][1]%3600/60)<10)){
                        Alone2[i][1]=String.valueOf(Alone[i][1]/3600)+":0"+String.valueOf(Alone[i][1]%3600/60);
                    }
                    else
                        Alone2[i][1]=String.valueOf(Alone[i][1]/3600)+":"+String.valueOf(Alone[i][1]%3600/60);
                }
                //The 3rd column
                for(int i=0;i<Alone2.length;i++){
                    Alone2[i][2]=String.valueOf(Alone[i][2]);
                }






                int[][]  TIME_change = new int[TIME_add.length][2];
                //The first column

                for(int i1=0,pp=0;i1<RR;i1++){
                    TIME_change[i1][0]=TIME_add[i1][0];
                }
                //The 2nd column
                TIME_change[0][1]=0;
                for(int i1=1;i1<RR;i1++){
                    for(int j=0;j<String_ID.size();j++){
                        TIME_change[i1][1]=Math.abs(ALL[i1-1][j]-ALL[i1][j])+TIME_change[i1][1];
                    }
                }

                //In order to output in accordance with the standard time format, so to copy a data standardization
                String[][]  bb4_change = new String[TIME_change.length][2];
                //The first column

                for(int i=0,nn=0,s=0;i<TIME_change.length;i++){
                    String HH;
                    String MM;

                    s=TIME_change[i][0];

                    if(s<60){
                        bb4_change[i][0]="0";
                    }
                    if((s>=60)&&(s<3600)){
                        MM=Integer.toString(s/60);
                        bb4_change[i][0]="0:"+MM;
                    }
                    if(s>3600){
                        HH=String.valueOf(s/3600);
                        MM=String.valueOf(s%3600/60);
                        bb4_change[i][0]=HH+":"+MM;
                    }
                    if(s%3600==0){
                        HH=String.valueOf(s/3600);
                        bb4_change[i][0]=HH+":"+00;
                    }
                    nn++;
                }
                //The 2nd column
                for(int i1=0,ss=0;i1<TIME_change.length;i1++){
                    bb4_change[i1][1]=String.valueOf(TIME_change[i1][1]);
                }


                //Export bb4_sum.csv

                try {
                    FileWriter writer = new FileWriter(new File(sdPath + "/MTI/" + name + "/" + nowtime, name+"_sum.csv"));
                    for (int i1 = 0; i1 < TIME_add.length; ) {
                        for (int j = 0; j < TIME_add[0].length; j++) {
                            writer.append(String.valueOf(bb4_sum[i1][j]));
                            writer.append(',');
                        }
                        writer.append('\n');
                        i1++;
                        writer.flush();
                    }

                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                ////Export bb4_change.csv
                try {
                    FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_change.csv"));

                    for (int i1 = 0; i1 < bb4_change.length; ) {
                        for (int j = 0; j < bb4_change[0].length; j++) {
                            writer.append(String.valueOf(bb4_change[i1][j]));
                            writer.append(',');
                        }
                        writer.append('\n');
                        i1++;
                        writer.flush();
                    }
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ////Export bb4_hourmark.csv in 24 row
                try {
                    FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_hourmark.csv"));

                    for (int i9 = 0; i9 < array_HourMark.length; ) {

                        writer.append(String.valueOf(array_HourMark[i9]));
                        writer.append('\n');
                        i9++;
                        writer.flush();
                    }
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Export bb4_minmark.csv
                try {
                    FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_minmark.csv"));

                    for (int i = 0; i < MinMark_show.length; ) {

                        writer.append(String.valueOf(MinMark_show[i]));
                        writer.append('\n');
                        i++;
                        writer.flush();
                    }
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ////Export AloneRange.csv
                try {
                    FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_alonerange.csv"));

                    for (int i1 = 0; i1 < Alone2.length; ) {
                        for (int j = 0; j < Alone2[0].length; j++) {
                            writer.append(String.valueOf(Alone2[i1][j]));
                            writer.append(',');

                        }
                        writer.append('\n');
                        i1++;
                        writer.flush();
                    }
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                }

            }


        }


    }
    /**
     * Set static methods for outputting 3 empty tables to prevent errors
      * The output case includes
      * The file exists but it is empty
     *
     */
    static void EmptyFile() {
        String name = MainActivity.name;
        String sdPath = Environment.getExternalStorageDirectory().getPath();

        //現在日時を取得する
        Calendar c = Calendar.getInstance();
        //フォーマットパターンを指定して表示する
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日");
        String time = sdf.format(c.getTime());
        String nowtime = time.substring(0,4)+time.substring(5,7)+time.substring(8,10);



        try
        {
            FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_hourmark.csv"));
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_minmark.csv"));
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_alonerange.csv"));
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * Set the static method to output 3 empty tables to prevent errors
           * The output case includes
           * Data has no friends all day
           * Direct output of 3 a day marked red data
     *
     */
    static void InvalidData(){
        String name = MainActivity.name;
        String sdPath = Environment.getExternalStorageDirectory().getPath();

        //現在日時を取得する
        Calendar c = Calendar.getInstance();
        //フォーマットパターンを指定して表示する
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日");
        String time = sdf.format(c.getTime());
        String nowtime = time.substring(0,4)+time.substring(5,7)+time.substring(8,10);



        int[] array_HourMark = new int[24];
        for(int i=0;i<array_HourMark.length;i++){
            array_HourMark[i]=0;
        }

        try {
            FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_hourmark.csv"));

            for (int i9 = 0; i9 < array_HourMark.length; ) {

                writer.append(String.valueOf(array_HourMark[i9]));
                writer.append('\n');
                i9++;
                writer.flush();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



        int[] array_MinMark = new int[24*60*60/300];
        for(int i=0;i<array_MinMark.length;i++){
            array_MinMark[i]=0;
        }

        try {
            FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_minmark.csv"));

            for (int i9 = 0; i9 < array_MinMark.length; ) {

                writer.append(String.valueOf(array_MinMark[i9]));
                writer.append('\n');
                i9++;
                writer.flush();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //alonerange
        String [][] alonerange = new String[24][3];
        for(int i=0;i<alonerange.length;i++){
            alonerange[i][0]="0:00";
        }
        for(int i=0;i<alonerange.length;i++){
            alonerange[i][1]="23:59";
        }
        for(int i=0;i<alonerange.length;i++){
            alonerange[i][1]=String.valueOf(i+1);
        }


        try {
            FileWriter writer = new FileWriter(new File(sdPath  + "/MTI/" + name+ "/" + nowtime, name+"_alonerange.csv"));

            for (int i1 = 0; i1 < alonerange.length; ) {
                for (int j = 0; j < alonerange[0].length; j++) {
                    writer.append(String.valueOf(alonerange[i1][j]));
                    writer.append(',');

                }
                writer.append('\n');
                i1++;
                writer.flush();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}






