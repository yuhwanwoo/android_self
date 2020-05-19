package org.techtown.speech_recognitiontest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED
                        ||ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE},5);
            toast("권한이 없어요");
       }

        LinearLayout layout=new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        final TextView txt=new TextView(this);
        txt.setText("\n");
        txt.setTextSize(18);
        layout.addView(txt);

        Button input=new Button(this);

        input.setText("음성 입력");
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputVoice(txt);
            }
        });
        layout.addView(input);

        int pad=dip2px(10);
        layout.setPadding(pad,pad,pad,pad);

        ScrollView scroll =new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);
        scroll.addView(layout);
        setContentView(scroll);
        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
    }

    private void inputVoice(final TextView txt) {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-Kr");
            final SpeechRecognizer stt=SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    toast("음성 입력 시작!");
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {
                    toast("음성 입력 종료");
                }

                @Override
                public void onError(int error) {
                    toast("오류 발생 : "+error);
                    stt.destroy();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result= (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    txt.append("[나]"+result.get(0)+"\n");

                    //1초 딜레이를 주기 위함 나는 딜레이 주기 싫어서 그냥 빼서 씀
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            replyAnswer(result.get(0),txt);
                        }
                    },1000);*/
                    replyAnswer(result.get(0),txt);


                    stt.destroy();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            stt.startListening(intent);
        }catch (Exception e){
            toast(e.toString());
        }
    }

    private int dip2px(int dips){
        return (int) Math.ceil(dips*this.getResources().getDisplayMetrics().density);
    }

    private void replyAnswer(String input, TextView txt) {
        try {
            /*if (input.equals("안녕")) {
                txt.append("[미리] 누구세요\n");
                tts.speak("누구세요?", TextToSpeech.QUEUE_FLUSH, null);
            } else if (input.equals("너는 누구니")) {
                txt.append("[미리] 나는 미리라고 해.\n");
                tts.speak("나는 미리라고 해.", TextToSpeech.QUEUE_FLUSH, null);
            } else if (input.equals("종료")) {
                finish();
            } else {
                txt.append("[미리] 무슨 말인지 모르겠어요.\n");
                tts.speak("무슨 말인지 모르겠어요", TextToSpeech.QUEUE_FLUSH, null);
            }*/
            String cmd=input.split(" ")[0];
            String[] que={"안녕","너는 누구니","야"};
            String[] ans={"누구세요?","나는 미리라고 해.","왜 부르셨나요?"};
            if (input.equals("종료")){
                finish();
                return;
            }
            if(cmd.equals("검색")){
                String data=input.replace("검색 ", "");
                txt.append("[미리] "+data+"에 대한 검색 결과입니다.\n");
                tts.speak(data+"에 대한 검색 결과입니다.",TextToSpeech.QUEUE_FLUSH,null);
                Intent intent=new Intent(this,WebViewActivity.class);
                intent.putExtra("value",data);
                startActivity(intent);
                return;
            }
            if(input.equals("날씨")){
                txt.append("[미리] 전국 날씨 입니다.\n");
                tts.speak("전국 날씨입니다.",TextToSpeech.QUEUE_FLUSH,null);
                Intent intent=new Intent(this,WebViewActivity.class);
                intent.putExtra("value","전국 날씨");
                startActivity(intent);
                return;
            }
            if(input.equals("음악")){
                txt.append("[미리] 음악을 재생합니다.\n");
                tts.speak("음악을 재생합니다.",TextToSpeech.QUEUE_FLUSH,null);
                Intent intent=new Intent(this,MusicService.class);
                startService(intent);
                return;
            }
            if(input.equals("음악 정지")){
                txt.append("[미리] 음악을 정지합니다.\n");
                tts.speak("음악을 정지합니다.",TextToSpeech.QUEUE_FLUSH,null);
                Intent intent=new Intent(this,MusicService.class);
                stopService(intent);
                return;
            }

            for (int i=0;i<que.length;i++){
                if (input.equals(que[i])){
                    txt.append("[미리] "+ans[i]+"\n");
                    tts.speak(ans[i],TextToSpeech.QUEUE_FLUSH,null);
                    return;
                }
            }
            txt.append("[미리] 무슨 말인지 모르겠어요.\n");
            tts.speak("무슨 말인지 모르겠어요.",TextToSpeech.QUEUE_FLUSH,null);
        }catch (Exception e){
            toast(e.toString());
        }
    }

    private void toast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }
}
