package com.project.chatappfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SignupActivity.class.getName();

    private EditText signup_user_email_et, signup_password_et;
    private Button signup_signup_btn, signup_signin_btn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signup_user_email_et = findViewById(R.id.signup_user_email_et);
        signup_password_et = findViewById(R.id.signup_password_et);
        signup_signup_btn = findViewById(R.id.signup_signup_btn);
        signup_signin_btn = findViewById(R.id.signup_signin_btn);

        signup_signup_btn.setOnClickListener(this);
        signup_signin_btn.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.signup_signup_btn) {
            signupBtnPressed();
        } else if (view.getId() == R.id.signup_signin_btn) {
            signInBtnPressed();
        }
    }

    private void signInBtnPressed() {
        mAuth.signInWithEmailAndPassword(signup_user_email_et.getText().toString(), signup_password_et.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(getApplicationContext(), "Authentication success.", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }

                    // ...
                });
    }

    private void signupBtnPressed() {
        mAuth.createUserWithEmailAndPassword(signup_user_email_et.getText().toString(), signup_password_et.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(getApplicationContext(), "Authentication success.", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(getApplicationContext(), "Authentication failed." + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}