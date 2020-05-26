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
    SpeechAsyncTask speechAsyncTask;
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    OutputStream os;
    PrintWriter pw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechAsyncTask=new SpeechAsyncTask();
        speechAsyncTask.execute();
    }


    class SpeechAsyncTask extends AsyncTask<Integer,String,String> {

        @Override
        protected String doInBackground(Integer... integers) {
            try {
                socket=new Socket("70.12.116.63",12345);
                Log.d("확인","socket다음");
                if (socket!=null){
                    speechWork();
                }
                Thread t1=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            String msg;
                            try{
                                msg=br.readLine();
                                Log.d("확인","서버로 부터 수신된 메시지>>"+msg);
                            }catch (IOException e){
                                try{
                                    is.close();
                                    isr.close();
                                    br.close();
                                    os.close();
                                    pw.close();
                                    socket.close();
                                }catch (IOException e1){
                                    e1.printStackTrace();
                                }
                                break;//반복문 빠져나가도록 설정
                            }
                        }
                    }
                });
                t1.start();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

            try {
                if(socket!=null) {
                    socket.close();
                }
                } catch (IOException e) {
                e.printStackTrace();

        }
    }
}
