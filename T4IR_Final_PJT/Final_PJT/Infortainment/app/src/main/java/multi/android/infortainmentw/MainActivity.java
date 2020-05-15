package multi.android.infortainmentw;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import multi.android.infortainmentw.control.Control;
import multi.android.infortainmentw.music.MusicActivity;
import multi.android.infortainmentw.music.MusicAdapter;
import multi.android.infortainmentw.music.MusicDTO;
import multi.android.infortainmentw.music.MusicFragment;

public class MainActivity extends AppCompatActivity {
    MusicFragment musicFragment=new MusicFragment();
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


       FragmentManager fragmentManager;
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction;
        transaction = fragmentManager.beginTransaction();
        //transaction.replace(R.id.fragment_control,control);
        transaction.replace(R.id.fragment_music,musicFragment);
        transaction.commit();

    }

}