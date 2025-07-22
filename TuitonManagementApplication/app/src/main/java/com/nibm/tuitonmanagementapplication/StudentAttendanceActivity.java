package com.nibm.tuitonmanagementapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;
    private TextView tvTotalAttendance;
    private FirebaseFirestore db;
    private AttendanceManager attendanceManager;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        studentId = getIntent().getStringExtra("DOCUMENT_ID");
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initializeViews();
        setupRecyclerView();
        loadAttendanceData();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_attendance);
        tvTotalAttendance = findViewById(R.id.tv_total_attendance);

        db = FirebaseFirestore.getInstance();
        attendanceManager = new AttendanceManager(db);
    }

    private void setupRecyclerView() {
        adapter = new AttendanceAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadAttendanceData() {
        attendanceManager.getStudentAttendance(studentId, new AttendanceManager.AttendanceHistoryCallback() {
            @Override
            public void onSuccess(List<DocumentSnapshot> attendanceRecords) {
                runOnUiThread(() -> {
                    adapter.updateData(attendanceRecords);
                    tvTotalAttendance.setText("Total Days Present: " + attendanceRecords.size());
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(StudentAttendanceActivity.this, "Error loading attendance: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
