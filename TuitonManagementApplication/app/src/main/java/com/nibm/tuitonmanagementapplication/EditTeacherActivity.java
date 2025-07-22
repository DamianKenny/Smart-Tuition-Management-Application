package com.nibm.tuitonmanagementapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.tuitonmanagementapplication.model.Teacher;

import java.util.HashMap;
import java.util.Map;

public class EditTeacherActivity extends AppCompatActivity {

    private Teacher teacher;
    private FirebaseFirestore db;

    // Form fields
    private TextInputEditText etFullName, etEmail, etPhone, etSubject,
            etQualification, etExperience;
    private Button btnUpdate;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);

        // Get teacher object from intent
        teacher = getIntent().getParcelableExtra("teacher");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSubject = findViewById(R.id.etSubject);
        etQualification = findViewById(R.id.etQualification);
        etExperience = findViewById(R.id.etExperience);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);

        // Populate form with teacher data
        if (teacher != null) {
            etFullName.setText(teacher.getFullName());
            etEmail.setText(teacher.getEmail());
            etPhone.setText(teacher.getPhone());
            etSubject.setText(teacher.getSubject());
            etQualification.setText(teacher.getQualification());
            etExperience.setText(teacher.getExperience());
        }

        // Set update button click listener
        btnUpdate.setOnClickListener(v -> updateTeacher());
    }

    private void updateTeacher() {
        // Get updated values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String qualification = etQualification.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Create map with updated values
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("subject", subject);
        updates.put("qualification", qualification);
        updates.put("experience", experience);

        // Update in Firestore
        db.collection("users").document(teacher.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditTeacherActivity.this,
                            "Teacher updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditTeacherActivity.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}