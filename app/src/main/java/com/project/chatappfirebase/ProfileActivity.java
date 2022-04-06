package com.project.chatappfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private EditText profile_activity_age_et;
    private ImageView profile_activity_iv;
    private Uri selected;

    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_activity_age_et = findViewById(R.id.profile_activity_age_et);
        profile_activity_iv = findViewById(R.id.profile_activity_iv);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        getData();
    }

    private void getData() {
        DatabaseReference newReference = database.getReference("Profiles");
        newReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    HashMap<String,String> hashmap = (HashMap<String,String>)ds.getValue();
                    String username = hashmap.get("useremail");
                    if(username.matches(mAuth.getCurrentUser().getEmail())){
                        String userAge = hashmap.get("userage");
                        String userImage = hashmap.get("userimageurl");
                        if(userAge != null && userImage != null){
                            profile_activity_age_et.setText(userAge);
                            Picasso.get().load(userImage).into(profile_activity_iv);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void UploadBtnPressed(View view) {

        final UUID UuidImage = UUID.randomUUID();
        String imageName = "myfolder/" + UuidImage + ".jpg";
        StorageReference newReference = storage.getReference().child(imageName);
        newReference.putFile(selected).addOnSuccessListener(taskSnapshot -> {
            StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("myfolder/" + UuidImage + ".jpg");
            profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();

                final UUID uuid = UUID.randomUUID();
                String uuidString = uuid.toString();

                String userAge = profile_activity_age_et.getText().toString();

                FirebaseUser user = mAuth.getCurrentUser();
                String useremail = user.getEmail().toString();

                dbRef.child("Profiles").child(uuidString).child("userimageurl").setValue(downloadUrl);
                dbRef.child("Profiles").child(uuidString).child("userage").setValue(userAge);
                dbRef.child("Profiles").child(uuidString).child("useremail").setValue(useremail);
                Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                startActivity(i);
            });
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void selectPicture(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 2);
        }
    }

    //FOR requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //FOR Intent.ACTION_PICK
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            selected = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selected);
                profile_activity_iv.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}