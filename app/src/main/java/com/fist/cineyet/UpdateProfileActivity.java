package com.fist.cineyet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class UpdateProfileActivity extends AppCompatActivity {
    EditText name, username, interests, email;
    ImageView profile_pic;
    Button submit, update_profile_pic;
    FirebaseAuth mAuth;
    String currentUserID;
    final static int gallery_pick=1;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        /* Firebase */
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        
        /* Buttons */
        name = (EditText) findViewById(R.id.edit_name);
        username = (EditText) findViewById(R.id.edit_username);
        interests = (EditText) findViewById(R.id.edit_interests);
        email = (EditText)findViewById(R.id.edit_email);
        profile_pic = findViewById(R.id.edit_profile_pic);
        update_profile_pic = findViewById(R.id.edit_profile_pic_button);

        update_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent= new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallery_pick);

            }
        });

        /* Display profile image */
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.with(UpdateProfileActivity.this).load(image).placeholder(R.drawable.roundprofilepic).into(profile_pic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        submit = findViewById(R.id.edit_submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name_str=name.getText().toString();
                String username_str=username.getText().toString();
                String interests_str=interests.getText().toString();
                String email_str=interests.getText().toString();
                Intent intent = new Intent(UpdateProfileActivity.this,HomeActivity.class);

                intent.putExtra("name",name_str);
                intent.putExtra("username",username_str);
                intent.putExtra("interests",interests_str);
                intent.putExtra("email",email_str);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_pick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

            final StorageReference filePath = UserProfileImageRef.child(currentUserID+".jpg");

            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                     if(task.isSuccessful()) {
                         Toast.makeText(UpdateProfileActivity.this, "Profile Image Successfully Uploaded to storage in Firebase", Toast.LENGTH_SHORT).show();
                         final String downloadUrl = filePath.getDownloadUrl().toString();

                         UserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 if(task.isSuccessful()){
                                     Toast.makeText(UpdateProfileActivity.this, "Profile Image Successfully Stored in database", Toast.LENGTH_SHORT).show();
                                 }
                                 else{

                                     String message =task.getException().getMessage();
                                     Toast.makeText(UpdateProfileActivity.this, "Error Occurred"+message, Toast.LENGTH_SHORT).show();

                                 }

                             }
                         });
                     }
                }
            });
        }
        else{
            Toast.makeText(this,"Error Occured", Toast.LENGTH_SHORT).show();

        }
    }
}
