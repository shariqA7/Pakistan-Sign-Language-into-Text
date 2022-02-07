package com.example.pakistansigntotext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pakistansigntotext.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    TextInputEditText edtName, edtEmail, edtPass, edtConfirmPass;
    CardView btnSignUp;
    TextView gotoSignIn;
    private FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference dbRef;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            Toast.makeText(this, "Email " + email, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("Users");

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        edtConfirmPass = findViewById(R.id.edtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        gotoSignIn = findViewById(R.id.gotoSignIn);

        gotoSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SignUp", "1");
                String name = edtName.getText().toString();
                String email = edtEmail.getText().toString();
                String pass = edtPass.getText().toString();
                String conPass = edtConfirmPass.getText().toString();

                Boolean valueBool = checktheFields();

                if (pass.equals(conPass) && valueBool) {
                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uid = mAuth.getCurrentUser().getUid();
                                UserModel model = new UserModel(name, email, pass);
                                dbRef.child(uid).setValue(model);

                                sendVerificationEmail();

                            } else {
                                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    edtConfirmPass.setError("Password not match. Please enter again");
                    edtConfirmPass.requestFocus();
                }
            }
        });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Toast.makeText(MainActivity.this, "Verification link send to your email. Please Verify", Toast.LENGTH_SHORT).show();

                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                finish();
                            } else {
                                overridePendingTransition(0, 0);
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                            }
                        }
                    });
        }
    }

    private Boolean checktheFields() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String pass = edtPass.getText().toString().trim();
        if (name.isEmpty()) {
            edtName.setError("Please Enter Name!");
            edtName.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Please Enter Email!");
            edtEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()) {
            edtEmail.setError("Please Enter Valid Email!");
            edtEmail.requestFocus();
            return false;
        }
        if (pass.isEmpty()) {
            edtPass.setError("Please Enter Password!");
            edtPass.requestFocus();
            return false;
        }

        return true;
    }
}