package com.project.chatappfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private RecyclerView chat_activity_recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    private EditText chat_activity_message_et;
    private Button chat_activity_send_btn;

    private ArrayList<String> chatMessages = new ArrayList<>();

    FirebaseDatabase database;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_activity_recyclerView = findViewById(R.id.chat_activity_recyclerView);
        chat_activity_message_et = findViewById(R.id.chat_activity_message_et);
        chat_activity_send_btn = findViewById(R.id.chat_activity_send_btn);

        chat_activity_send_btn.setOnClickListener(this);

        recyclerViewAdapter = new RecyclerViewAdapter(chatMessages);

        RecyclerView.LayoutManager recyclerViewManager = new LinearLayoutManager(getApplicationContext());
        chat_activity_recyclerView.setLayoutManager(recyclerViewManager);
        chat_activity_recyclerView.setItemAnimator(new DefaultItemAnimator());
        chat_activity_recyclerView.setAdapter(recyclerViewAdapter);

        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();

        getData();

        String userId = OneSignal.getDeviceState().getUserId();

        DatabaseReference newRef = database.getReference("PlayerIDs");
        newRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> playerIdsFromServer = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    HashMap<String, String> hashmap = (HashMap<String, String>) ds.getValue();
                    String currentPlayerId = hashmap.get("playerID");
                    playerIdsFromServer.add(currentPlayerId);
                }

                if (!playerIdsFromServer.contains(userId)) {

                    UUID uuid = UUID.randomUUID();
                    String uuidString = uuid.toString();

                    dbRef.child("PlayerIDs").child(uuidString).child("playerID").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });



    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.chat_activity_send_btn) {
            sendBtnPressed();
        }
    }

    private void sendBtnPressed() {
        String messageToSend = chat_activity_message_et.getText().toString();

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        FirebaseUser user = mAuth.getCurrentUser();
        String userEmail = user.getEmail().toString();

        dbRef.child("Chats").child(uuidString).child("usermessages").setValue(messageToSend);
        dbRef.child("Chats").child(uuidString).child("usermail").setValue(userEmail);
        dbRef.child("Chats").child(uuidString).child("usermessagetime").setValue(ServerValue.TIMESTAMP);
        chat_activity_message_et.setText("");


        DatabaseReference newRef = database.getReference("PlayerIDs");
        newRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    HashMap<String, String> hashmap = (HashMap<String, String>) ds.getValue();
                    String playerId = hashmap.get("playerID");

                    try {
                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'"+messageToSend+"'}, 'include_player_ids': ['" + playerId + "']}"),
                                new OneSignal.PostNotificationResponseHandler() {
                                    @Override
                                    public void onSuccess(JSONObject response) {
                                        Log.i("OneSignalExample", "postNotification Success: " + response.toString());
                                    }

                                    @Override
                                    public void onFailure(JSONObject response) {
                                        Log.e("OneSignalExample", "postNotification Failure: " + response.toString());
                                    }
                                });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void getData() {
        DatabaseReference newReference = database.getReference("Chats");
        Query query = newReference.orderByChild("usermessagetime");
        // Read from the database
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                chatMessages.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) ds.getValue();
                    String usermail = hashMap.get("usermail");
                    String usermessages = hashMap.get("usermessages");
                    //   String usermessagetime = hashMap.get("usermessagetime").toString();

                    chatMessages.add(usermessages);
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.options_menu_signout) {
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.options_menu_profile) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}