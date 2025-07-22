package com.nibm.tuitonmanagementapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Teacher_upload_results extends AppCompatActivity {

    private static final String TAG = "ReleaseActivity";

    private Spinner spinnerBatch, spinnerIndex;
    private EditText editResult;
    private Button btnRelease;

    private FirebaseFirestore db;
    private final List<String> studentNames = new ArrayList<>();
    private final List<String> studentIds = new ArrayList<>();
    private String selectedBatch, selectedStudentId, selectedStudentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_upload_results);

        try {
            // Initialize Firebase
            db = FirebaseFirestore.getInstance();

            // Initialize UI components
            spinnerBatch = findViewById(R.id.spinnerBatch);
            spinnerIndex = findViewById(R.id.spinnerIndex);
            editResult = findViewById(R.id.editResult);
            btnRelease = findViewById(R.id.btnRelease);

            // Hide ActionBar
            if (getSupportActionBar() != null) getSupportActionBar().hide();

            setupBatchSpinner();
            setupStudentSpinner();
            setupButton();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error: " + e.getMessage());
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupBatchSpinner() {
        // Fixed batch list
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
                selectedBatch = parent.getItemAtPosition(position).toString();
                loadStudents(selectedBatch);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBatch = null;
            }
        });

        spinnerBatch.post(() -> {
            if (spinnerBatch.getSelectedItem() != null) {
                selectedBatch = spinnerBatch.getSelectedItem().toString();
                loadStudents(selectedBatch);
            }
        });

    }

    private void loadStudents(String batch) {
        if (batch == null || batch.isEmpty()) {
            Toast.makeText(this, "Please select a batch", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading students for batch: " + batch);

        db.collection("users")
                .whereEqualTo("userType", "Student") // CORRECTED userType value
                .whereEqualTo("batch", batch)
                .get()
                .addOnCompleteListener(task -> {
                    studentNames.clear();
                    studentIds.clear();

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Query successful. Found " + task.getResult().size() + " documents");

                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("fullName");
                                if (name != null && !name.isEmpty()) {
                                    studentNames.add(name);
                                    studentIds.add(document.getId());
                                    Log.d(TAG, "Added student: " + name);
                                }
                            }
                            updateStudentSpinner();
                        } else {
                            Log.d(TAG, "No students found for batch: " + batch);
                            Toast.makeText(this, "No students found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Firestore error: " + task.getException());
                        Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStudentSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                studentNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIndex.setAdapter(adapter);

        // Reset selection
        if (!studentIds.isEmpty()) {
            spinnerIndex.setSelection(0);
            selectedStudentId = studentIds.get(0);
            selectedStudentName = studentNames.get(0);
        }
    }

    private void setupStudentSpinner() {
        spinnerIndex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < studentIds.size()) {
                    selectedStudentId = studentIds.get(position);
                    selectedStudentName = studentNames.get(position);
                } else {
                    selectedStudentId = null;
                    selectedStudentName = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStudentId = null;
                selectedStudentName = null;
            }
        });
    }

    private void setupButton() {
        btnRelease.setOnClickListener(v -> {
            if (selectedStudentId == null) {
                Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
                return;
            }

            String result = editResult.getText().toString().trim();
            if (result.isEmpty()) {
                Toast.makeText(this, "Please enter a result", Toast.LENGTH_SHORT).show();
                return;
            }

            saveResultToFirestore(result);
        });
    }

    private void saveResultToFirestore(String result) {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("studentId", selectedStudentId);
        resultData.put("studentName", selectedStudentName);  // Added student name
        resultData.put("batch", selectedBatch);
        resultData.put("result", result);
        resultData.put("timestamp", System.currentTimeMillis());

        db.collection("StudentResults")
                .add(resultData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Result saved successfully!", Toast.LENGTH_SHORT).show();
                    editResult.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Save failed: " + e.getMessage());
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}