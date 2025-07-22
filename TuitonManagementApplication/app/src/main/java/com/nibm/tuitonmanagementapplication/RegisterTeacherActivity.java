package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
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
import java.util.Objects;

public class RegisterTeacherActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ImageView menuButton;
    private NavigationView navigationView;

    private TextInputEditText etName, etEmail, etPhone, etSubject, etQualification, etExperience, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_register_teacher);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        menuButton = findViewById(R.id.menuButton);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSubject = findViewById(R.id.etSubject);
        etQualification = findViewById(R.id.etQualification);
        etExperience = findViewById(R.id.etExperience);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> registerTeacher());

        if (getSupportActionBar() != null) getSupportActionBar().hide();
    }

    private void registerTeacher() {
        // Get input values
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String qualification = etQualification.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be ≥6 characters");
            etPassword.requestFocus();
            return;
        }

        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Create authentication account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get Firebase UID
                        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        // Create teacher data map
                        Map<String, Object> teacher = new HashMap<>();
                        teacher.put("id", uid);
                        teacher.put("fullName", name);
                        teacher.put("email", email);
                        teacher.put("phone", phone);
                        teacher.put("subject", subject);
                        teacher.put("qualification", qualification);
                        teacher.put("experience", experience);
                        teacher.put("userType", "com/nibm/tuitonmanagementapplication/Teacher"); // Set user type

                        // Save to Firestore
                        db.collection("users").document(uid)
                                .set(teacher)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterTeacherActivity.this,
                                            "Teacher registered successfully", Toast.LENGTH_SHORT).show();
                                    clearForm();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterTeacherActivity.this,
                                            "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterTeacherActivity.this,
                                "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                    btnRegister.setEnabled(true);
                });
    }

    private void clearForm() {
        etName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etSubject.setText("");
        etQualification.setText("");
        etExperience.setText("");
        etPassword.setText("");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            Intent intent = new Intent(RegisterTeacherActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
