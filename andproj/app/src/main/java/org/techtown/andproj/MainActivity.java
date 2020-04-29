package org.techtown.andproj;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.techtown.andproj.fragment.PeopleFragment;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout,new PeopleFragment()).commit(); // 만약 fragment오류 뜨면 v4.fragment로 사용해야한대


    }
}
