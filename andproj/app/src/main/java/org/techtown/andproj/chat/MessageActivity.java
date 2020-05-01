package org.techtown.andproj.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.techtown.andproj.R;
import org.techtown.andproj.model.ChatModel;
import org.techtown.andproj.model.Usermodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy.MM.dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();  // 채팅을 요구하는 아이디 즉 단말기에 로그인된 UID

        destinationUid=getIntent().getStringExtra("destinationUid"); // 채팅을 당하는 아이디
        button=findViewById(R.id.messageActivity_button);
        editText=findViewById(R.id.messageActivity_editText);

        recyclerView=findViewById(R.id.messageActivity_recyclerview);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chatModel=new ChatModel();
                chatModel.users.put(uid,true);
                chatModel.users.put(destinationUid,true);

                if(chatRoomUid==null){

                    //서버 연결 되기 전까지 메시지 못보내 (채팅방 중복 방지)
                    button.setEnabled(false);

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() { //여기서 push는 일종의 primary키(이거 없으면 채팅방 이름이 없어)
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();
                        }
                    });
                    //checkChatRoom();
                }else{
                    ChatModel.Comment comment=new ChatModel.Comment();
                    comment.uid=uid;
                    comment.message=editText.getText().toString();

                    comment.timestamp= ServerValue.TIMESTAMP; // ServerValue => firebase에서 제공하는 메소드

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //전송 보내면 초기화
                            editText.setText("");
                        }
                    });
                }


            }
        });
        checkChatRoom();
    }

    //채팅방 중복을 체크해주는 코드
    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item:dataSnapshot.getChildren()){
                    ChatModel chatModel=item.getValue(ChatModel.class);
                    if(chatModel.users.containsKey(destinationUid)){
                        chatRoomUid=item.getKey();

                        //서버에서 채팅방 찾았을때 버튼 살림림
                       button.setEnabled(true);

                       recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                       recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;
        Usermodel usermodel;
        public RecyclerViewAdapter(){

            comments=new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    usermodel=dataSnapshot.getValue(Usermodel.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        void getMessageList(){
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                //읽어 들인 데이터는 이쪽으로 와
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear(); //클리어 안넣어주면 채팅방에 들어갈떄마다 데이터 쌓여

                    for(DataSnapshot item:dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }


                    //리스트를 새로 갱신 ,
                    notifyDataSetChanged();

                    //전송해도 맨 마지막화면으로
                    recyclerView.scrollToPosition(comments.size()-1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder=((MessageViewHolder)holder);


            if(comments.get(position).uid.equals(uid)){ //첫번쨰 uid는 comments안의 uid equals안의 uid는 내 uid

                //내가 보낸 메시지
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);

                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.textView_message.setTextSize(25);
               messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);                                                              //오류부분

                //상대방이 보낸 메시지
            }else{//상대방일 경우

                Glide.with(holder.itemView.getContext()).load(usermodel.profileImageUrl).apply(new RequestOptions().circleCrop())
                                                                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textview_name.setText(usermodel.userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(25);
              messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);                                                              //오류부분 //13이야
            }

            // 타임스태프가 기본적으로 1970년 1월 1일 이후 계산하여 나타내는데 이걸 처리하는 작업
            long unixTime=(long)comments.get(position).timestamp;
            Date date=new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time=simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textview_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                textView_message=itemView.findViewById(R.id.messageItem_textView_message);

                textview_name=itemView.findViewById(R.id.messageItem_textView_name);
                imageView_profile=itemView.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination=itemView.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main=itemView.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp=itemView.findViewById(R.id.messageItem_textView_timestamp);

            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fromleft,R.anim.toright);
    }
}
