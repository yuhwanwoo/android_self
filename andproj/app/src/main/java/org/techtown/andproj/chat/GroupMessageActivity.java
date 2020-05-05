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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.techtown.andproj.R;
import org.techtown.andproj.model.ChatModel;
import org.techtown.andproj.model.NotificationModel;
import org.techtown.andproj.model.Usermodel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupMessageActivity extends AppCompatActivity {

    Map<String, Usermodel> users=new HashMap<>();
    String destinationRoom;
    String uid;
    EditText editText;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private RecyclerView recyclerView;

    List<ChatModel.Comment> comments=new ArrayList<>();

    int peopleCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        //ChatFragment의 2명초과일시 단톡방 만드는 부분에서 destinationRoom 코드를 통해 받은 값
        destinationRoom=getIntent().getStringExtra("destinationRoom");
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText=findViewById(R.id.groupMessageActivity_editText);

        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item:dataSnapshot.getChildren()){
                    users.put(item.getKey(),item.getValue(Usermodel.class));
                }
                init();
                recyclerView=findViewById(R.id.groupMessageActivity_recyclerview);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    void init(){
        Button button=findViewById(R.id.groupMessageActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel.Comment comment=new ChatModel.Comment();
                comment.uid=uid;
                comment.message=editText.getText().toString();
                comment.timestamp= ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {//chatrooms의 users에 접근
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Map<String,Boolean> map= (Map<String, Boolean>) dataSnapshot.getValue();//chatrooms의 users의 아이디들 담은것

                                for(String item:map.keySet()){
                                    if(item.equals(uid)){
                                        continue;
                                    }
                                    sendGcm(users.get(item).pushToken);
                                }
                                editText.setText("");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });
    }
    void sendGcm(String pushToken){
        Gson gson= new Gson();

        String userName=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel=new NotificationModel();
        notificationModel.to=pushToken;
        notificationModel.notification.title=userName;
        notificationModel.notification.text=editText.getText().toString();
        //푸쉬를 보낼때 데이타 부분도 조립해서 보낸다.
        notificationModel.data.title=userName;
        notificationModel.data.text=editText.getText().toString();

        //푸쉬를 받을때 데이터를 파싱하는 부분

        RequestBody requestBody=RequestBody.create(MediaType.parse("application/json; chatset=utf-8"),gson.toJson(notificationModel));

        Request request=new Request.Builder().header("Content-Type","application/json")
                .addHeader("Authorization","key=AIzaSyA-n7dsLmvRFsKjEJJoWnTIkd1XhxaIgBg")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient=new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });

    }
    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public GroupMessageRecyclerViewAdapter(){
            getMessageList();
        }

        void getMessageList(){
            databaseReference=FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments");
            valueEventListener=databaseReference.addValueEventListener(new ValueEventListener() {
                //읽어 들인 데이터는 이쪽으로 와
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear(); //클리어 안넣어주면 채팅방에 들어갈떄마다 데이터 쌓여
                    Map<String,Object> readUsersMap=new HashMap<>();
                    for(DataSnapshot item:dataSnapshot.getChildren()){

                        String key=item.getKey();
                        ChatModel.Comment comment_origin=item.getValue(ChatModel.Comment.class); //comment나눈 이유는 timestamp의 무한루프 버그를 잡기 위해서
                        ChatModel.Comment comment_modify=item.getValue(ChatModel.Comment.class);
                        comment_modify.readUsers.put(uid,true);

                        //읽은 내용을 가지고 있음
                        readUsersMap.put(key,comment_modify);
                        comments.add(comment_origin);
                    }
                    if (comments.size()==0){
                        return;
                    }
                    if(!comments.get(comments.size()-1).readUsers.containsKey(uid)){
                        //서버가 읽은거 확인
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                //리스트를 새로 갱신 ,
                                notifyDataSetChanged();

                                //전송해도 맨 마지막화면으로
                                recyclerView.scrollToPosition(comments.size()-1);
                            }
                        });
                    }else{
                        notifyDataSetChanged();

                        //전송해도 맨 마지막화면으로
                        recyclerView.scrollToPosition(comments.size()-1);

                    }




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


            return new GroupMessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GroupMessageViewHolder messageViewHolder=((GroupMessageViewHolder)holder);


            if(comments.get(position).uid.equals(uid)){ //첫번쨰 uid는 comments안의 uid equals안의 uid는 내 uid

                //내가 보낸 메시지
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);

                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.textView_message.setTextSize(25);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position,messageViewHolder.textView_readCounter_left);

                //상대방이 보낸 메시지
            }else{//상대방일 경우

                Glide.with(holder.itemView.getContext()).load(users.get(comments.get(position).uid).profileImageUrl).apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textview_name.setText(users.get(comments.get(position).uid).userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(25);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                setReadCounter(position,messageViewHolder.textView_readCounter_right);
            }

            // 타임스태프가 기본적으로 1970년 1월 1일 이후 계산하여 나타내는데 이걸 처리하는 작업
            long unixTime=(long)comments.get(position).timestamp;
            Date date=new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time=simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);

        }

        void setReadCounter(final int position, final TextView textView){
            if(peopleCount==0){

                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String,Boolean> users= (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount=users.size();
                        int count=peopleCount-comments.get(position).readUsers.size();
                        if (count > 0) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        }else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }else{
                int count=peopleCount-comments.get(position).readUsers.size();
                if (count > 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                }else{
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class GroupMessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textview_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public GroupMessageViewHolder(View view) {
                super(view);
                textView_message=itemView.findViewById(R.id.messageItem_textView_message);
                textview_name=itemView.findViewById(R.id.messageItem_textView_name);
                imageView_profile=itemView.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination=itemView.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main=itemView.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp=itemView.findViewById(R.id.messageItem_textView_timestamp);
                textView_readCounter_left=itemView.findViewById(R.id.messageItem_textView_readCount_left);
                textView_readCounter_right=itemView.findViewById(R.id.messageItem_textView_readCount_right);
            }
        }
    }
}
