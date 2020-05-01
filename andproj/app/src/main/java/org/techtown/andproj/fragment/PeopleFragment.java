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
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.andproj.R;
import org.techtown.andproj.chat.MessageActivity;
import org.techtown.andproj.model.Usermodel;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_people,container,false);
        RecyclerView recyclerView=view.findViewById(R.id.peoplefragment_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

        return view;
    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<Usermodel> usermodels;
        public PeopleFragmentRecyclerViewAdapter() {
            usermodels=new ArrayList<>();
            final String myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //누적데이터를 없애기 위해서 클리어 (지웠다가 확인해보면 정확히 알수있다.)
                    usermodels.clear();

                    //서버에서 온 데이터를 담는다.
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                        //내 아이디는 친구리스트에 안담는것
                        Usermodel usermodel=snapshot.getValue((Usermodel.class));
                        if(usermodel.uid.equals(myUid)){
                            continue;
                        }

                        usermodels.add(snapshot.getValue(Usermodel.class));
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
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);


            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            Glide.with(holder.itemView.getContext()).load(usermodels.get(position).profileImageUrl).apply(new RequestOptions().circleCrop()).into(((CustomViewHolder)holder).imageView);

            ((CustomViewHolder)holder).textView.setText(usermodels.get(position).userName);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(v.getContext(), MessageActivity.class);

                    // 위에 인텐트 선언했으므로 클릭한 상대방의 채팅방(MessageActivity)으로 옮긴다
                    intent.putExtra("destinationUid",usermodels.get(position).uid);

                    ActivityOptions activityOptions=null;
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
                        activityOptions=ActivityOptions.makeCustomAnimation(v.getContext(),R.anim.fromright,R.anim.toleft);
                        startActivity(intent,activityOptions.toBundle());
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return usermodels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;

            public CustomViewHolder(View view) {
                super(view);
                imageView=view.findViewById(R.id.frienditem_imageview);
                textView=view.findViewById(R.id.frienditem_textview);
            }
        }
    }
}
