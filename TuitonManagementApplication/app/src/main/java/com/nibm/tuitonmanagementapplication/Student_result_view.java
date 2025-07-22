package com.nibm.tuitonmanagementapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.tuitonmanagementapplication.model.ResultItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student_result_view extends AppCompatActivity {

    private Spinner spinnerBatch, spinnerStudent;
    private Button btnViewResults;
    private RecyclerView recyclerViewResults;
    private ResultAdapter adapter;
    private List<ResultItem> resultItems = new ArrayList<>();
    private FirebaseFirestore db;

    // Batch list
    private final String[] batches = {
            "DSE233F", "DSE241F", "HNDSE241F", "HNDSE242F",
            "HNDSE233F", "DE241F", "DE242F", "HNDE241F", "HDE242F"
    };

    // Student map: batch -> list of students
    private final Map<String, List<String>> batchStudentsMap = new HashMap<>();
    private String selectedBatch = "";
    private String selectedStudent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_result);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        spinnerBatch = findViewById(R.id.spinnerBatch);
        spinnerStudent = findViewById(R.id.spinnerStudent);
        btnViewResults = findViewById(R.id.btnViewResults);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);

        // Setup RecyclerView
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultAdapter(resultItems);
        recyclerViewResults.setAdapter(adapter);

        // Setup batch spinner
        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                batches
        );
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBatch.setAdapter(batchAdapter);

        // Setup student spinner (initially empty)
        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudent.setAdapter(studentAdapter);

        // Batch selection listener
        spinnerBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = parent.getItemAtPosition(position).toString();
                loadStudentsForBatch(selectedBatch);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Student selection listener
        spinnerStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStudent = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // View Results button listener
        btnViewResults.setOnClickListener(v -> {
            if (!selectedBatch.isEmpty() && !selectedStudent.isEmpty()) {
                loadResults(selectedBatch, selectedStudent);
            } else {
                Toast.makeText(this, "Please select both batch and student", Toast.LENGTH_SHORT).show();
            }
        });

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();
    }

    private void loadStudentsForBatch(String batch) {
        db.collection("StudentResults")
                .whereEqualTo("batch", batch)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> studentNames = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentName = document.getString("studentName");
                            if (studentName != null && !studentNames.contains(studentName)) {
                                studentNames.add(studentName);
                            }
                        }

                        // Update student spinner
                        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                studentNames
                        );
                        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerStudent.setAdapter(studentAdapter);

                        if (studentNames.isEmpty()) {
                            Toast.makeText(this, "No students found in this batch", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadResults(String batch, String studentName) {
        Log.d("Firestore", "Loading results for: " + batch + " - " + studentName);

        db.collection("StudentResults")
                .whereEqualTo("batch", batch)
                .whereEqualTo("studentName", studentName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        resultItems.clear();
                        Log.d("Firestore", "Query returned " + task.getResult().size() + " documents");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Log.d("Firestore", "Document ID: " + document.getId());
                                Log.d("Firestore", "Document data: " + document.getData());

                                // Get fields with fallbacks
                                String module = "N/A"; // Not present in your document
                                String finalGrade = document.getString("result");
                                String courseTitle = document.getString("batch"); // Using batch as course title

                                // Add to results
                                resultItems.add(new ResultItem(courseTitle, module, finalGrade));
                                Log.d("Firestore", "Added result: " + finalGrade);
                            } catch (Exception e) {
                                Log.e("Firestore", "Error parsing document", e);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (resultItems.isEmpty()) {
                            Toast.makeText(this, "No results found for this student", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Firestore", "Loaded " + resultItems.size() + " results");
                        }
                    } else {
                        Log.e("Firestore", "Error getting results", task.getException());
                        Toast.makeText(this, "Failed to load results", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}