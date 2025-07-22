//package com.nibm.tuitonmanagementapplication;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.google.android.material.tabs.TabLayout;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.nibm.tuitonmanagementapplication.Student;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class ReportsActivity extends AppCompatActivity {
//
//    private static final String TAG = "ReportsActivity";
//
//    private Spinner studentSpinner;
//    private TabLayout tabLayout;
//    private RecyclerView recyclerView;
//    private TextView noDataText;
//    private Button backButton;
//
//    private FirebaseFirestore db;
//    private List<Student> studentList;
//    private ArrayAdapter<Student> studentAdapter;
//    private ReportsAdapter reportsAdapter;
//
//    private String selectedStudentId = "";
//    private String selectedTab = "attendance"; // attendance or results
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_reports);
//
//        db = FirebaseFirestore.getInstance();
//        studentList = new ArrayList<>();
//
//        initializeViews();
//        setupTabLayout();
//        setupRecyclerView();
//        loadStudents();
//
//        backButton.setOnClickListener(v -> finish());
//    }
//
//    private void initializeViews() {
//        studentSpinner = findViewById(R.id.studentSpinner);
//        tabLayout = findViewById(R.id.tabLayout);
//        recyclerView = findViewById(R.id.recyclerView);
//        noDataText = findViewById(R.id.noDataText);
//        backButton = findViewById(R.id.backButton);
//    }
//
//    private void setupTabLayout() {
//        tabLayout.addTab(tabLayout.newTab().setText("Attendance"));
//        tabLayout.addTab(tabLayout.newTab().setText("Results"));
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                selectedTab = tab.getPosition() == 0 ? "attendance" : "results";
//                if (!selectedStudentId.isEmpty()) {
//                    loadReportData();
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {}
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {}
//        });
//    }
//
//    private void setupRecyclerView() {
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        reportsAdapter = new ReportsAdapter(new ArrayList<>(), selectedTab);
//        recyclerView.setAdapter(reportsAdapter);
//    }
//
//    private void loadStudents() {
//        db.collection("students")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    studentList.clear();
//                    //studentList.add(new Student("", "Select a student", "", "")); // Placeholder
//
//                    for (DocumentSnapshot document : queryDocumentSnapshots) {
//                        Student student = document.toObject(Student.class);
//                        if (student != null) {
//                            student.setId(document.getId());
//                            studentList.add(student);
//                        }
//                    }
//
//                    setupSpinner();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error loading students", e);
//                    Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void setupSpinner() {
//        studentAdapter = new ArrayAdapter<Student>(this,
//                android.R.layout.simple_spinner_item, studentList) {
//            @Override
//            public View getView(int position, View convertView, android.view.ViewGroup parent) {
//                View view = super.getView(position, convertView, parent);
//                TextView textView = (TextView) view;
//                textView.setText(studentList.get(position).getName());
//                return view;
//            }
//
//            @Override
//            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
//                View view = super.getDropDownView(position, convertView, parent);
//                TextView textView = (TextView) view;
//                textView.setText(studentList.get(position).getName());
//                return view;
//            }
//        };
//
//        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        studentSpinner.setAdapter(studentAdapter);
//
//        studentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position > 0) { // Skip placeholder
//                    selectedStudentId = studentList.get(position).getId();
//                    loadReportData();
//                } else {
//                    selectedStudentId = "";
//                    showNoData();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });
//    }
//
//    private void loadReportData() {
//        if (selectedStudentId.isEmpty()) {
//            showNoData();
//            return;
//        }
//
//        String collection = selectedTab.equals("attendance") ? "attendance" : "results";
//
//        db.collection(collection)
//                .whereEqualTo("studentId", selectedStudentId)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<Map<String, Object>> reportData = new ArrayList<>();
//
//                    for (DocumentSnapshot document : queryDocumentSnapshots) {
//                        Map<String, Object> data = document.getData();
//                        if (data != null) {
//                            reportData.add(data);
//                        }
//                    }
//
//                    if (reportData.isEmpty()) {
//                        showNoData();
//                    } else {
//                        showData(reportData);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error loading report data", e);
//                    Toast.makeText(this, "Failed to load " + selectedTab, Toast.LENGTH_SHORT).show();
//                    showNoData();
//                });
//    }
//
//    private void showData(List<Map<String, Object>> data) {
//        noDataText.setVisibility(View.GONE);
//        recyclerView.setVisibility(View.VISIBLE);
//
//        reportsAdapter.updateData(data, selectedTab);
//    }
//
//    private void showNoData() {
//        recyclerView.setVisibility(View.GONE);
//        noDataText.setVisibility(View.VISIBLE);
//        noDataText.setText("No " + selectedTab + " data available");
//    }
//}
