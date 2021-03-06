package com.fist.cineyet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindPeopleActivity extends AppCompatActivity {

    Button searchPeople;
    EditText inputText;
    private DatabaseReference userRef;
    private DatabaseReference friendsRef;
    RecyclerView searchPeopleRecycler;
    String currentUserID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        FirebaseAuth myFirebaseAuth=FirebaseAuth.getInstance();
        currentUserID = myFirebaseAuth.getCurrentUser().getUid();

        setContentView(R.layout.activity_search_people);
        searchPeople=findViewById(R.id.search_people_activity_button);
        searchPeopleRecycler=findViewById(R.id.search_people_results);
        inputText=findViewById(R.id.search_people_bar);
        searchPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myinput=inputText.getText().toString();
                searchPeople(myinput);
            }
        });
    }
    private void searchPeople(String searchBarInput){
        FirebaseRecyclerOptions<FindPeople> options = new FirebaseRecyclerOptions.Builder<FindPeople>()
                .setQuery(userRef.orderByChild("name").startAt(searchBarInput.toLowerCase()).endAt(searchBarInput.toLowerCase()+"\uf8ff"), FindPeople.class)
                .build();

        FirebaseRecyclerAdapter<FindPeople,FindFriendsViewholder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<FindPeople, FindFriendsViewholder>(options){

            @NonNull
            @Override
            public FindFriendsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.people_search_result, parent,false);
                FindFriendsViewholder viewholder=new FindFriendsViewholder(view);
                return viewholder;
            }

            @Override
            protected void onBindViewHolder(@NonNull final FindFriendsViewholder holder, final int position, @NonNull final FindPeople model) {
                String[] name_split = model.getName().split(" ");
                String uppercase_name = name_split[0].substring(0, 1).toUpperCase() + name_split[0].substring(1).toLowerCase() + " "
                        +  name_split[1].substring(0, 1).toUpperCase() + name_split[1].substring(1).toLowerCase();
                holder.name.setText(uppercase_name);
                holder.interests.setText(model.getInterests());
                Picasso.get().load(model.getProfileimage())
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .into(holder.profileimage);
                holder.parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Fragment profFrag = new profile_page();
                        final Bundle mybund = new Bundle();
                        final String userKey = getRef(position).getKey();
                        if(userKey.equals(currentUserID)){
                            mybund.putString("isPersonalProfile","PERSONAL");
                            mybund.putString("UserID",currentUserID);
                            profFrag.setArguments(mybund);
                            getSupportFragmentManager().beginTransaction().replace(R.id.search_frag_container,profFrag).commit();

                        }
                        else{
                            friendsRef.child(currentUserID).child(userKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        mybund.putString("isPersonalProfile","FRIENDS");
                                    }
                                    else
                                        mybund.putString("isPersonalProfile","NOTFRIENDS"); //change later when you figure out friends lists
                                    mybund.putString("UserID",userKey);
                                    mybund.putString("fromHome","true");
                                    profFrag.setArguments(mybund);
                                    getSupportFragmentManager().beginTransaction().replace(R.id.search_frag_container, profFrag).commit();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                    }
                });
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        searchPeopleRecycler.setLayoutManager(layoutManager);
        searchPeopleRecycler.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    public static class FindFriendsViewholder extends RecyclerView.ViewHolder{
        TextView name;
        CircleImageView profileimage;
        TextView interests;
        ConstraintLayout parent;

        public FindFriendsViewholder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.search_person_name);
            profileimage = itemView.findViewById(R.id.search_person_image);
            interests = itemView.findViewById(R.id.search_person_interests);
            parent = itemView.findViewById(R.id.search_people_parent);
        }
    }
}
