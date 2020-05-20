package org.techtown.recognitiontest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Locale;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        cThis=this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        mRecognizer=SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

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
                txtSystem.setText("어플 실행됨~~ 자동 실행");
            }
        },1000);

    }
}
