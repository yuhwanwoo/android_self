package org.techtown.recognitiontest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Context cThis;
    String LogTT="[STT]";

    //음성 인식용
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    //음성 출력용
    TextToSpeech tts;

    //화면 처리용
    Button btnSttStart; // 버튼 설정
    EditText txtInMsg; // 입력 박스 설정
    EditText txtSystem; // 시스템 메시지
    ImageView imgViewLight; // 전등 이지

    //서버 전송용
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

        cThis=this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechAsyncTast=new SpeechAsyncTast();
        speechAsyncTast.execute();

        SttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        mRecognizer=SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

        andId="noo";

        //음성 출력 생성, 리스너 초기화
        tts=new TextToSpeech(cThis, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!= TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        btnSttStart=findViewById(R.id.btn_stt_start);
        btnSttStart.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("----------------------음성인식 시작!");
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);

                }else{
                    //권한을 허용한 경우
                    try {
                        mRecognizer.startListening(SttIntent);
                    }catch (SecurityException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        //입력 박스 설정
        txtInMsg=findViewById(R.id.txtInMsg);
        txtSystem=findViewById(R.id.txtSystem);

        imgViewLight=findViewById(R.id.imgViewLight);
        imgViewLight.setImageAlpha(50);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                txtSystem.setText("어플 실행됨~~ 자동 실행:::::::::::"+"\r\n"+txtSystem.getText());
                btnSttStart.performClick();
            }
        },1000);

        //stt가 일정 시간이 되면 죽기 때문에 일정 시간에 한번씩 계속 실행 처리
        // 테스트를 위한거라 실제에선 절대 쓰면 안된다네 왜지..?
       final Handler handler=new Handler(){
            public void handleMessage(Message msg){
                btnSttStart.performClick();
            }
        };

        Timer timer=new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg=handler.obtainMessage();
                Log.i(LogTT,msg+"::::::::::msg에요");
                handler.sendMessage(msg);
            }

            @Override
            public boolean cancel() {
                return super.cancel();
            }
        };
        timer.schedule(timerTask,0,5000); // 5초에 한번씩 버튼 클릭하기...

    }

    private RecognitionListener listener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            txtSystem.setText("onReadyForSpeech::::::::::::::::"+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onBeginningOfSpeech() {
            txtSystem.setText("지금부터 말하세요:::::::::"+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            txtSystem.setText("onBufferReceived::::::::::::::::"+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onEndOfSpeech() {
            txtSystem.setText("onEndOfSpeech:::: 종료됨::::::::::"+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onError(int error) {
            Log.i(LogTT,"ERROR : 천천히 다시 말해주세요1::::::::::::");
            txtSystem.setText("천천히 다시 말해주세요:::::::::::"+"\r\n"+txtSystem.getText());

        }

        @Override
        public void onResults(Bundle results) {
            String key="";
            key=SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult =results.getStringArrayList(key);
            String[] rs=new String[mResult.size()];
            mResult.toArray(rs);

            Log.i(LogTT,"입력값 : "+rs[0]);
            txtInMsg.setText(rs[0]+"\r\n"+txtInMsg.getText());
            Log.i(LogTT,"입력값getText : "+txtInMsg.getText());

            FunVoiceOrderCheck(rs[0]);

            mRecognizer.startListening(SttIntent); // 음성인식이 계속되는 구문이니 필요에 맞게 쓰길
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.i(LogTT,"onPartialResults:::::::::::");
            txtSystem.setText("onPartialResults:::::::::::"+"\r\n"+txtSystem.getText());
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.i(LogTT,"onEvent::::::::::::");
            txtSystem.setText("onEvent:::::::::::::::"+"\r\n"+txtSystem.getText());
        }
    };
    //입력한 음성 메시지 확인 후 동작 처리.
    private void FunVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length()<1) return;

        VoiceMsg=VoiceMsg.replace(" ", ""); //공백 제거

        if(VoiceMsg.indexOf("미리야")>-1){
            tts.setSpeechRate(0.2f);
            FunVoiceOut("네");
            tts.setSpeechRate(1.0f);
        }

        if(VoiceMsg.indexOf("전등켜")>-1||VoiceMsg.indexOf("불켜")>-1){
            Log.i(LogTT,"메시지 확인 : 전등 ON");
            imgViewLight.setImageAlpha(255);
            FunVoiceOut("전등을 켰습니다.");
        }
        if(VoiceMsg.indexOf("전등꺼")>-1|| VoiceMsg.indexOf("불꺼")>-1){
            Log.i(LogTT,"메시지 확인 : 전등 OFF");
            imgViewLight.setImageAlpha(50);
            FunVoiceOut("전등을 끕니다.");
        }
    }

    private void FunVoiceOut(String OutMsg){
        if(OutMsg.length()<1){
            return;
        }

        tts.setPitch(1.5f); //1.5톤 올려서
        tts.setSpeechRate(1.0f); //1배속으로  읽기
        tts.speak(OutMsg,TextToSpeech.QUEUE_FLUSH,null);
    }

    //어플이 종료될떄때 완전 제거

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){
            tts.stop();
            tts.shutdown();
            tts=null;
        }
        if(mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
    }

    class SpeechAsyncTast extends AsyncTask<Integer,String,String>{

        @Override
        protected String doInBackground(Integer... integers) {
            try {
                socket=new Socket("70.12.227.93",12345);
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
