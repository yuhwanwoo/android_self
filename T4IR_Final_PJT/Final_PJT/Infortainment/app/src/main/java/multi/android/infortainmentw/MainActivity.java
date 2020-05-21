package multi.android.infortainmentw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import multi.android.infortainmentw.control.Control;
import multi.android.infortainmentw.music.MusicActivity;
import multi.android.infortainmentw.music.MusicAdapter;
import multi.android.infortainmentw.music.MusicDTO;
import multi.android.infortainmentw.music.MusicFragment;
import multi.android.infortainmentw.music.PlayFragment;

public class MainActivity extends AppCompatActivity {
    MusicFragment musicFragment=new MusicFragment();
    PlayFragment playFragment =new PlayFragment();

    //**********************************************************************
    //음성인식 추가
    Context cThis;
    String LogTT="[STT]";

    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    TextToSpeech tts;

    Button btnSttStart;




    //**********************************************************************

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);



       FragmentManager fragmentManager;
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction;
        transaction = fragmentManager.beginTransaction();
        //transaction.replace(R.id.fragment_control,control);
        transaction.replace(R.id.fragment_music,musicFragment);

        transaction.commit();


        //음성인식부분


    }


}