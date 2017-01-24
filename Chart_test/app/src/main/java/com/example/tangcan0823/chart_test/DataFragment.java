package com.example.tangcan0823.chart_test;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created by tangcan0823 on 2016/11/16.
 */
public class DataFragment extends Fragment implements View.OnClickListener,Runnable {

    String name = "bb4";
    //現在日時を取得する
    Calendar c = Calendar.getInstance();
    //フォーマットパターンを指定して表示する
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日");
    String time = sdf.format(c.getTime());
    String nowtime = time.substring(0,4)+time.substring(5,7)+time.substring(8,10);
    //String nowtime = "20170112";
    File namedir = new File("/sdcard/MTI/"+name);
    File datedir = new File("/sdcard/MTI/"+name+"/"+nowtime);

    String aboutsum = name+"_hourmark.csv";
    String sum = name+"_sum.csv";
    String change = name+"_change.csv";
    String nofriend = name+"_alonerange.csv";
    String minmark = name+"_minmark.csv";
    String filename ;
    String interval = "5";

    Thread thread;


    public View view;
    public Button btnsend, btnget;
    public int counter=0;
    private String maxtime;
    private String sdPath = Environment.getExternalStorageDirectory().getPath();
    private String CSV = name + "_acc.csv";
    private File file = new File(sdPath +"/"+ CSV);
    private Integer flag = 1;
    private Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_data, container, false);

        mToolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);
        btnsend = (Button) view.findViewById(R.id.send_data);
        btnsend.setOnClickListener(this);

       try{
           maxtime = tail(file).split(",",0)[3];
       }catch (Exception e)
       {
           e.printStackTrace();
       }


        //   Toast.makeText(getActivity().getApplicationContext(),maxtime,Toast.LENGTH_LONG).show();


        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    executeRemoteCommand("sit-user-16", "2016mti", "130.158.80.37", 22);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);
        toast("サーバーと接続しました");

        return view;
    }

    public static Session session;

    public String executeRemoteCommand(String username, String password, String hostname, int port)
            throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

// Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.connect();
        return "0";
    }

    ProgressDialog progressDialog;

    @Override
    public void onClick(View v) {
        if(flag ==1){
        switch (v.getId()) {
            case R.id.send_data : {
                btnsend.setBackgroundResource(R.drawable.frame_style_pressed);
                showProgressDialogWithTitle();
                thread = new Thread(this);
                thread.start();

                try {
                    sendData();
                } catch (JSchException e) {
                    e.printStackTrace();
                } catch (SftpException e) {
                    e.printStackTrace();
                }

                try {
                    runSsh();
                } catch (JSchException e) {
                    e.printStackTrace();
                }

                try {
//1000ミリ秒Sleepする
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }

                try {
                    getData();
                    mToolbar.setTitle(R.string.navigation_piechart);
                    flag = 0;

                } catch (JSchException e) {
                    e.printStackTrace();
                }



                break;
            }

            default:{
                break;
            }

        }
        }
    }

    public void sendData() throws JSchException, SftpException {
// SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);


// Execute command
        channelssh.setCommand("mkdir /home/sit-user-16/makefiles/data/"+nowtime);
        channelssh.connect();
        channelssh.disconnect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        sftpChannel.put("/sdcard/"+name+".csv", "/home/sit-user-16/makefiles/data/"+nowtime);

//sftpChannel.put("/sdcard/"+nameacc+".csv", "/home/sit-user-16/makefiles/data/"+nowtime);

        sftpChannel.disconnect();
    }



    public void getData() throws JSchException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        toast("データを取得しています");
        if(!(namedir.exists())){
            namedir.mkdir();
        }
        if(!(datedir.exists())){
            datedir.mkdir();
        }
        file.renameTo(new File(sdPath +"/MTI/"+ name +"/"+ nowtime + "/"+ CSV));


        for(int count=0;count<5;count++){
            if(count == 0){
                filename = aboutsum;
            }else if (count == 1){
                filename = sum;
            }else if (count == 2){
                filename = change;
            }else if (count == 3){
                filename = nofriend;
            }else{
                filename = minmark;
            }
            try (FileOutputStream out = new FileOutputStream("/sdcard/MTI/" + name + "/" + nowtime + "/" + filename)) {
                try (InputStream in = sftpChannel.get("/home/sit-user-16/makefiles/result/" + name + "/" + nowtime + "/" + filename)) {
// read from in, write to out
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sftpChannel.exit();
// session.disconnect();
        toast("データを取得しました");


    }

    public void runSsh() throws JSchException {
// SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

// Execute command

        channelssh.setCommand("sh /home/sit-user-16/makefiles/shell.sh " + name + " " + interval + " " + maxtime);
        channelssh.connect();
        channelssh.disconnect();
    }

    public void toast(String message){
        Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();
    }

    public void exec_post() throws IOException {
        HttpURLConnection con = null;
        URL url = new URL("http://130.158.80.37/phpinfo.php");
        con = (HttpURLConnection) url.openConnection();
        con.connect();
    }


    private void showProgressDialogWithTitle() {
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("通信中...");
        progressDialog.show();

// To Dismiss progress dialog
//progressDialog.dismiss();
//progressDialog.hide();
    }


    @Override
    public void run() {
        try {
            thread.sleep(8000);
        } catch (InterruptedException e) { }
        progressDialog.dismiss();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new PiechartFragment()).commit();

    }

    public String tail( File file ) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile( file, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                int readByte = fileHandler.readByte();

                if( readByte == 0xA ) {
                    if( filePointer == fileLength ) {
                        continue;
                    }
                    break;

                } else if( readByte == 0xD ) {
                    if( filePointer == fileLength - 1 ) {
                        continue;
                    }
                    break;
                }

                sb.append( ( char ) readByte );
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            return null;
        } catch( java.io.IOException e ) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null )
                try {
                    fileHandler.close();
                } catch (IOException e) {
                /* ignore */
                }
        }
    }

}