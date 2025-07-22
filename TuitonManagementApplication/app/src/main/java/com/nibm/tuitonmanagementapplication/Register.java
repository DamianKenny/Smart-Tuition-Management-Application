package com.nibm.tuitonmanagementapplication;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    private Spinner userTypeSpinner;
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private Button registerButton;
    private ProgressBar progressBar;
    private TextView loginNow;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_form); // Your XML file name

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userTypeSpinner = findViewById(R.id.userTypeSpinner);
        nameEditText = findViewById(R.id.Name);
        emailEditText = findViewById(R.id.Email);
        passwordEditText = findViewById(R.id.Password);
        registerButton = findViewById(R.id.btn_Register);
        progressBar = findViewById(R.id.progressBar);
        loginNow = findViewById(R.id.loginnow);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ✅ Updated Spinner setup (from XML resource)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        // Register button click
        registerButton.setOnClickListener(v -> registerUser());

        // Login link click
        loginNow.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, Login.class));
            finish();
        });
    }


    private void registerUser() {
        String fullName = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String userType = userTypeSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(fullName)) {
            nameEditText.setError("Name required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email required");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("Password (min 6 chars) required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save additional user info in Firestore
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("fullName", fullName);
                            userMap.put("email", email);
                            userMap.put("userType", userType);

                            db.collection("users").document(user.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Register.this, Login.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
