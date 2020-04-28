package org.techtown.andproj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class ResultActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseRecyclerAdapter<ChatMessage,MessageViewHolder> mFirebaseAdapter;

    public static final String MESSAGES_CHILD = "message";
    private DatabaseReference mFirebaseDatabaseReference;
    private EditText mMessageEditText;

    private TextView tv_result; // 닉네임 text
    private ImageView iv_profile; //이미지 뷰
    private FirebaseAuth auth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient googleApiClient;

    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView nameTextView;
        ImageView messageImageView;
        TextView messageTextView;
        CircleImageView photoImageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView=itemView.findViewById(R.id.nameTextView);
            messageImageView=itemView.findViewById(R.id.messageImageView);
            messageTextView=itemView.findViewById(R.id.messageTextView);
            photoImageView=itemView.findViewById(R.id.photoImageView);
        }
    }

    private RecyclerView mMessageRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient=new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API).build();

        auth=FirebaseAuth.getInstance();
        mFirebaseUser=auth.getCurrentUser();

        Intent intent=getIntent();
        final String nickName=intent.getStringExtra("nickName"); // MainActivity로 부터 닉네임 전달 받음
        final String photoUrl=intent.getStringExtra("photoUrl"); // MainActivity로 부터 프로필 사진 Url 전달받음

        tv_result=findViewById(R.id.tv_result);
        tv_result.setText(nickName); // 닉네임 text를 텍스트 뷰에 세팅

        iv_profile=findViewById(R.id.iv_profile);
        Glide.with(this).load(photoUrl).into(iv_profile); // 프로필 url를 이미지 뷰에 세팅


        mFirebaseDatabaseReference= FirebaseDatabase.getInstance().getReference();
        mMessageEditText=findViewById(R.id.message_edit);
        mMessageRecyclerView=findViewById(R.id.message_recycler_view);


        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage chatMessage=new ChatMessage(mMessageEditText.getText().toString(),nickName,photoUrl,null);  //여기서 오류 생길수 있어 파이어베이스활용4  5:38부분 보자
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chatMessage);
                mMessageEditText.setText("");
            }
        });

        Query query= mFirebaseDatabaseReference.child(MESSAGES_CHILD);
        FirebaseRecyclerOptions<ChatMessage> options= new FirebaseRecyclerOptions.Builder<ChatMessage>().setQuery(query,ChatMessage.class).build();



        mFirebaseAdapter=new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(MessageViewHolder holder, int i, ChatMessage chatMessage) {
                holder.messageTextView.setText(chatMessage.getText());
                holder.nameTextView.setText(chatMessage.getName());

                if(chatMessage.getPhotoUrl()==null){
                    holder.photoImageView.setImageDrawable(ContextCompat.getDrawable(ResultActivity.this,R.drawable.ic_account_circle_black_24dp));
                }else{
                    Glide.with(ResultActivity.this).load(chatMessage.getPhotoUrl()).into(holder.photoImageView);
                }

            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
                return new MessageViewHolder(view);
            }
        };

        mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

    }

    // FirebaseAdapter는 activity생명주기에 따라서 상태를 모니터링하고 멈추게 하고 그런 코드를 작성하게 되어있다.


    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }

    // res/menu의 main.xml를 삽입
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    //로그아웃 구현
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                //auth.signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient);

                startActivity(new Intent(this,SignInActivity.class));
                tv_result.setText("");
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
