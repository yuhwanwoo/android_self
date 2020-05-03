package org.techtown.andproj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import org.techtown.andproj.model.Usermodel;

public class SignupActivity extends AppCompatActivity {

    /*
    * 아직 안한것 사진 등록 안했을때 기본 이미지 설정할 수 있도록 해야해
    *
    * */

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private String splash_background;
    private ImageView profile;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseRemoteConfig mFirebaseRemoteConfig=FirebaseRemoteConfig.getInstance();
        splash_background=mFirebaseRemoteConfig.getString(getString(R.string.rc_color)); // 원격으로 할떄는 이걸로 색 바꾸면될듯
        //mFirebaseRemoteConfig.getString("splash_color"));와 같은것
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }
        
        profile=findViewById(R.id.signupActivity_imageview_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,PICK_FROM_ALBUM); //
            }
        });
        
        email=findViewById(R.id.signupActivity_edittext_email);
        name=findViewById(R.id.signupActivity_edittext_name);
        password=findViewById(R.id.signupActivity_edittext_password);
        signup=findViewById(R.id.signupActivity_button_signup);


        signup.setBackgroundColor(Color.parseColor(splash_background));

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(email.getText().toString()==null || name.getText().toString()==null || password.getText().toString()==null||imageUri==null){

                    return;
                }

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                final String uid=task.getResult().getUser().getUid();

                                //회원가입할떄의 자신의 이름을 알람에 넣는것
                                UserProfileChangeRequest userProfileChangeRequest=new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();
                                task.getResult().getUser().updateProfile(userProfileChangeRequest);


                                //Log.d("abcd",""+FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri));
                                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

//                                        String imageUrl=task.getResult().getStorage().getDownloadUrl().toString();
                                        Task<Uri> imageUrl=task.getResult().getStorage().getDownloadUrl();
                                        while (!imageUrl.isComplete());


                                        Usermodel userModel= new Usermodel();
                                        userModel.userName=name.getText().toString();
                                        userModel.profileImageUrl=imageUrl.getResult().toString();
                                        userModel.uid=FirebaseAuth.getInstance().getCurrentUser().getUid();


                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                SignupActivity.this.finish();
                                            }
                                        });


                                    }
                                });



                            }
                        });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            profile.setImageURI(data.getData()); // 가운데 뷰를 바꿈
            imageUri=data.getData(); //이미지 경로 원본
        }
    }
}
