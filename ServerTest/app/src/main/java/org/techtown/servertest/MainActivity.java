package org.techtown.servertest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    Socket socket;
    String andId;
    SpeechAsyncTast speechAsyncTast;
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    OutputStream os;
    PrintWriter pw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechAsyncTast=new SpeechAsyncTast();
        speechAsyncTast.execute(10,20);
    }


    class SpeechAsyncTast extends AsyncTask<Integer,String,String> {

        @Override
        protected String doInBackground(Integer... integers) {
            try {
                socket=new Socket("121.131.215.143",12345);
                Log.d("확인","socket다음");
                if (socket!=null){
                    speechWork();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }

        void speechWork(){
            try {
                is=socket.getInputStream();
                isr=new InputStreamReader(is);
                br=new BufferedReader(isr);

                os=socket.getOutputStream();
                pw=new PrintWriter(os,true);
                pw.println("phone/"+andId);
                pw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
