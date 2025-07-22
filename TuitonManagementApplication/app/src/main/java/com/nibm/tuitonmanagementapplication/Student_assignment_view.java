package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nibm.tuitonmanagementapplication.model.Assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student_assignment_view extends AppCompatActivity
        implements AssignmentAdapter.OnAssignmentClickListener {

    private static final String TAG = "AssignmentView";
    private static final int PICK_PDF_REQUEST = 101;

    private Spinner spinnerBatch;
    private RecyclerView recyclerViewAssignments;
    private AssignmentAdapter adapter;
    private List<Assignment> assignmentList = new ArrayList<>();
    private FirebaseFirestore db;

    private Uri pdfUri;
    private Assignment selectedAssignment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_assignment_view);

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        spinnerBatch = findViewById(R.id.spinnerBatch);
        recyclerViewAssignments = findViewById(R.id.recyclerViewAssignments);

        if (spinnerBatch == null || recyclerViewAssignments == null) {
            Toast.makeText(this, "UI initialization failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView
        recyclerViewAssignments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentAdapter(assignmentList, this);
        recyclerViewAssignments.setAdapter(adapter);

        // Setup batch spinner
        setupBatchSpinner();

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
    }

    private void setupBatchSpinner() {
        String[] batches = {"DSE233F", "DSE241F", "HNDSE241F", "HNDSE242F",
                "HNDSE233F", "DE241F", "DE242F", "HNDE241F", "HDE242F"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                batches
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBatch.setAdapter(adapter);

        spinnerBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBatch = parent.getItemAtPosition(position).toString();
                loadAssignments(selectedBatch);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        if (batches.length > 0) {
            loadAssignments(batches[0]);
        }
    }

    private void loadAssignments(String batch) {
        Log.d(TAG, "Loading assignments for batch: " + batch);

        db.collection("courseworkAssignments")
                .whereEqualTo("batch", batch)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        assignmentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Assignment assignment = new Assignment();
                                assignment.setId(document.getId());
                                assignment.setTitle(getStringOrEmpty(document, "title"));
                                assignment.setCourse(getStringOrEmpty(document, "course"));
                                assignment.setDescription(getStringOrEmpty(document, "description"));
                                assignment.setFileUrl(getStringOrEmpty(document, "fileUrl"));
                                assignment.setBatch(getStringOrEmpty(document, "batch"));

                                assignmentList.add(assignment);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing document", e);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (assignmentList.isEmpty()) {
                            Toast.makeText(Student_assignment_view.this,
                                    "No assignments found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(Student_assignment_view.this,
                                "Failed to load: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getStringOrEmpty(QueryDocumentSnapshot document, String field) {
        return document.contains(field) ? document.getString(field) : "";
    }

    @Override
    public void onAssignmentClick(Assignment assignment) {
        String fileUrl = assignment.getFileUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(fileUrl), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

// Check if a PDF viewer app is available
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback to browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
            startActivity(browserIntent);
        }


    }



    private void showUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_pdf, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.dialog_title);
        Button btnSelect = dialogView.findViewById(R.id.btn_select_pdf);
        Button btnUpload = dialogView.findViewById(R.id.btn_upload);
        ProgressBar progressBar = dialogView.findViewById(R.id.upload_progress);

        tvTitle.setText("Upload for: " + selectedAssignment.getTitle());

        AlertDialog dialog = builder.create();

        btnSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, PICK_PDF_REQUEST);
        });

        btnUpload.setOnClickListener(v -> {
            if (pdfUri != null) {
                progressBar.setVisibility(View.VISIBLE);
                uploadPdfToFirestore(progressBar);
            } else {
                Toast.makeText(this, "Please select a PDF file first", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();
            Toast.makeText(this, "PDF selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPdfToFirestore(ProgressBar progressBar) {
        if (pdfUri == null || selectedAssignment == null) return;

        // Get current batch from spinner
        String currentBatch = spinnerBatch.getSelectedItem().toString();

        // Create storage reference
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("coursework_uploads/" + System.currentTimeMillis() + ".pdf");

        // Upload file to Firebase Storage
        storageRef.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // Create Firestore document
                        Map<String, Object> uploadData = new HashMap<>();
                        uploadData.put("title", selectedAssignment.getTitle());
                        uploadData.put("batch", currentBatch);
                        uploadData.put("pdfUrl", downloadUrl);
                        uploadData.put("timestamp", FieldValue.serverTimestamp());
                        uploadData.put("assignmentId", selectedAssignment.getId());

                        // Save to Firestore
                        db.collection("courseworkUploads")
                                .add(uploadData)
                                .addOnSuccessListener(documentReference -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Student_assignment_view.this,
                                            "Upload successful!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Student_assignment_view.this,
                                            "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Student_assignment_view.this,
                            "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}