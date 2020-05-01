package org.techtown.andproj.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.andproj.R;
import org.techtown.andproj.chat.MessageActivity;
import org.techtown.andproj.model.ChatModel;
import org.techtown.andproj.model.Usermodel;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatFragment extends Fragment {

    private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy.MM.dd hh:mm");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chat,container,false);

        RecyclerView recyclerView=view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels=new ArrayList<>();
        private String uid;
        private ArrayList<String> destinationUsers=new ArrayList<>();

        public ChatRecyclerViewAdapter() {
            uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

            //내가 소속된 방 들어감
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    for(DataSnapshot item:dataSnapshot.getChildren()){
                        chatModels.add(item.getValue(ChatModel.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            final CustomViewHolder customViewHolder=(CustomViewHolder)holder;
            String destinationUid=null;

            //일일 챗방에 있는 유저를 체크
            for(String user:chatModels.get(position).users.keySet()){
                if (!user.equals(uid)){
                    destinationUid=user;
                }
            }
            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Usermodel usermodel=dataSnapshot.getValue(Usermodel.class);
                    Glide.with(customViewHolder.itemView.getContext()).load(usermodel.profileImageUrl).apply(new RequestOptions().circleCrop()).into(customViewHolder.imageView);

                    //채팅방 이름을 상대방 이름으로
                    customViewHolder.textView_title.setText(usermodel.userName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //마지막 메시지 띄우기
            //메시지를 내림차순으로 정렬 후 마지막 메시지의 키값을 가져옴
            Map<String,ChatModel.Comment> commentMap=new TreeMap<>(Collections.<String>reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            String lastMessageKey= (String) commentMap.keySet().toArray()[0];
            customViewHolder.textView_last_message.setText(chatModels.get(position).comments.get(lastMessageKey).message);

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(v.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid",destinationUsers.get(position));


                    ActivityOptions activityOptions=null;
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions=ActivityOptions.makeCustomAnimation(v.getContext(),R.anim.fromright,R.anim.toleft);
                        startActivity(intent,activityOptions.toBundle());
                    }
                }
            });

            //채팅리스트의 타임스탬프
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            long unixTime=(long)chatModels.get(position).comments.get(lastMessageKey).timestamp;
            Date date =new Date(unixTime);
            customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));
        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_last_message;
            public TextView textView_timestamp;
            public CustomViewHolder(View view) {
                super(view);

                imageView=view.findViewById(R.id.chatitem_imageview);
                textView_title=view.findViewById(R.id.chatitem_textview_title);
                textView_last_message=view.findViewById(R.id.chatitem_textview_lastMessage);
                textView_timestamp=view.findViewById(R.id.chatitem_textview_timestamp);
            }
        }
    }


}
