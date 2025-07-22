package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterStudentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Firebase instance
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI elements
    private TextInputEditText etFullName, etEmail, etPassword, etModule, etBatch;
    private Button btnRegister;
    private ProgressBar progressBar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_register_student); // Replace with your XML file name

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Bind UI elements
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etModule = findViewById(R.id.etModule);
        etBatch = findViewById(R.id.etBatch);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);


        navigationView.setNavigationItemSelectedListener(this);

        // Register button click listener
        btnRegister.setOnClickListener(v -> registerStudent());

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        findViewById(R.id.menuButton).setOnClickListener(v -> toggleDrawer());
    }


    private void registerStudent() {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String module = etModule.getText().toString().trim();
        String batch = etBatch.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(module)){
            etModule.setError("Module cannot be Empty");
            etModule.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(batch)) {
            etBatch.setError("Batch cannot be Empty");
            etBatch.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(password)) {
            etPassword.setError("Password cannot be Empty");
            etPassword.requestFocus();
            return;
        }
        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Create data object for Firestore
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("batch", batch);
        user.put("module", module);
        user.put("password", password);
        user.put("userType", "com/nibm/tuitonmanagementapplication/Student"); // Fixed value as per requirement

        // Save to Firestore
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterStudentActivity.this,
                            "Student registered successfully", Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterStudentActivity.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void resetForm() {
        etFullName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etBatch.setText("");
        etModule.setText("");
        btnRegister.setEnabled(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_student) {
            startActivity(new Intent(this, ManageStudentsActivity.class));
        } else if (id == R.id.nav_teachers) {
            startActivity(new Intent(this, ManageTeachersActivity.class));
        } else if (id == R.id.nav_reports) {
            //startActivity(new Intent(this, ReportsActivity.class));
        } else if (id == R.id.nav_assignment) {
            startActivity(new Intent(this, StudentAssignmentActivity.class));
        } else if (id == R.id.nav_view_assignment) {
            startActivity(new Intent(this, AssignmentListActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(RegisterStudentActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}