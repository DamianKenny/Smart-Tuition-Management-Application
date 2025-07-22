package com.nibm.tuitonmanagementapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Assignment_upload_teacher extends AppCompatActivity {

    private EditText editTitle, editDescription, editFilePath;
    private Spinner spinnerCourse, spinnerBatch;
    private Button btnChooseFile, btnSubmitAssignment;
    private Uri fileUri;
    private ProgressDialog progressDialog;

    // Firebase instances
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_upload_assignment); // Use your XML layout name

        // Initialize views
        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        editFilePath = findViewById(R.id.editFilePath);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerBatch = findViewById(R.id.spinnerBatch);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnSubmitAssignment = findViewById(R.id.btnSubmitAssignment);

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Setup spinners
        setupSpinners();

        // File chooser button
        btnChooseFile.setOnClickListener(v -> openFileChooser());

        // Submit button
        btnSubmitAssignment.setOnClickListener(v -> validateAndUpload());
    }

    private void setupSpinners() {
        // Course options
        String[] courses = {
                "Diploma in Software Engineering",
                "Higher National Diploma in Software Engineering",
                "Diploma in Engineering",
                "Certificate in Engineering",
                "Diploma in Business Management"
        };

        // Batch options
        String[] batches = {
                "DSE233F", "DSE241F", "HNDSE241F", "HNDSE242F",
                "HNDSE233F", "DE241F", "DE242F", "HNDE241F", "HDE242F"
        };

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courses
        );
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(courseAdapter);

        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                batches
        );
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBatch.setAdapter(batchAdapter);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // All file types
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            editFilePath.setText(fileUri.getLastPathSegment());
        }
    }

    private void validateAndUpload() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String course = spinnerCourse.getSelectedItem().toString();
        String batch = spinnerBatch.getSelectedItem().toString();

        if (title.isEmpty()) {
            editTitle.setError("Title is required");
            return;
        }

        if (fileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog();
        uploadFileToStorage(title, description, course, batch);
    }

    private void uploadFileToStorage(String title, String description, String course, String batch) {
        String fileName = UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference("assignments/" + fileName);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                saveToFirestore(title, description, course, batch, uri.toString())
                        )
                )
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveToFirestore(String title, String description, String course,
                                 String batch, String downloadUrl) {
        Map<String, Object> assignment = new HashMap<>();
        assignment.put("title", title);
        assignment.put("description", description);
        assignment.put("course", course);
        assignment.put("batch", batch);
        assignment.put("fileUrl", downloadUrl);
        assignment.put("timestamp", System.currentTimeMillis());

        db.collection("courseworkAssignments")
                .add(assignment)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Assignment uploaded successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void clearForm() {
        editTitle.setText("");
        editDescription.setText("");
        editFilePath.setText("");
        fileUri = null;
        spinnerCourse.setSelection(0);
        spinnerBatch.setSelection(0);
    }
}
