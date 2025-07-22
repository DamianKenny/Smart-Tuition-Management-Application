package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Login extends AppCompatActivity {

    private Spinner userTypeSpinner;
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView registerNow;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_form);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userTypeSpinner = findViewById(R.id.userTypeSpinner);
        emailEditText = findViewById(R.id.Email);
        passwordEditText = findViewById(R.id.Password);
        loginButton = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        registerNow = findViewById(R.id.registernow);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        registerNow.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Register.class));
            finish();
        });

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String selectedUserType = userTypeSpinner.getSelectedItem().toString();

        // Handle admin login when no user type is selected
        if (selectedUserType.equals("Select User Type")) {
            if (email.equals("admin@gmail.com") && password.equals("admin123")) {
                startActivity(new Intent(Login.this, MainActivity.class));
                finish();
                return;
            } else {
                Toast.makeText(this, "Please select your user type", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validate fields
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Query Firestore for matching credentials
        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .whereEqualTo("userType", selectedUserType) // Match the spinner value directly
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        String documentId = document.getId();

                        Intent intent;
                        // Corrected comparison values (match spinner strings)
                        if (selectedUserType.equals("Student")) {
                            intent = new Intent(Login.this, Student_Dashboard.class);
                        } else if (selectedUserType.equals("Teacher")) {
                            intent = new Intent(Login.this, Teacher_Dashboard.class);
                        } else {
                            // Fallback for other types
                            intent = new Intent(Login.this, MainActivity.class);
                        }

                        intent.putExtra("DOCUMENT_ID", documentId);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Invalid credentials or user type", Toast.LENGTH_LONG).show();
                    }
                });
    }
}