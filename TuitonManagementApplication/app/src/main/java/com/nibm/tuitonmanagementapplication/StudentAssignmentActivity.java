package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.tuitonmanagementapplication.model.Teacher;
import com.nibm.tuitonmanagementapplication.model.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentAssignmentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner spinnerStudents, spinnerTeachers, spinnerCourses;
    private Button btnAssignStudent;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private List<Student> studentList;
    private List<Teacher> teacherList;

    private ArrayAdapter<String> studentAdapter;
    private ArrayAdapter<String> teacherAdapter;
    private ArrayAdapter<String> courseAdapter;

    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuButton;

    // Fixed course values
    private final List<String> fixedCourses = Arrays.asList(
            "Select Course",
            "Certificate in Software Engineering",
            "Diploma in Software Engineering",
            "Higher National Diploma in Software Engineering",
            "Certificate in Engineering",
            "Diploma in Computer Science",
            "Bachelor of Science in Software Engineering",
            "Master of Science in Computer Science"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_assignment);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupDrawer();
        loadData();
        setupCourseSpinner();
        setupListeners();
    }

    private void initializeViews() {
        spinnerStudents = findViewById(R.id.spinnerStudents);
        spinnerTeachers = findViewById(R.id.spinnerTeachers);
        spinnerCourses = findViewById(R.id.spinnerCourses);
        btnAssignStudent = findViewById(R.id.btnAssignStudent);
        menuButton = findViewById(R.id.menuButton);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        studentList = new ArrayList<>();
        teacherList = new ArrayList<>();
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void loadData() {
        loadStudents();
        loadTeachers();
    }

    private void loadStudents() {
        db.collection("users")
                .whereEqualTo("userType", "Student")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Student student = new Student();
                            student.setId(doc.getId());
                            student.setStudentName(doc.getString("fullName"));
                            student.setStudentEmail(doc.getString("email"));
                            student.setStudentModule(doc.getString("module"));
                            student.setStudentBatch(doc.getString("batch"));
                            studentList.add(student);
                        }
                        setupStudentSpinner();
                    } else {
                        Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTeachers() {
        db.collection("users")
                .whereEqualTo("userType", "Teacher")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        teacherList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Teacher teacher = new Teacher();
                            teacher.setId(doc.getId());
                            teacher.setFullName(doc.getString("fullName"));
                            teacher.setEmail(doc.getString("email"));
                            teacher.setSubject(doc.getString("subject"));
                            teacher.setQualification(doc.getString("qualification"));
                            teacherList.add(teacher);
                        }
                        setupTeacherSpinner();
                    } else {
                        Toast.makeText(this, "Error loading teachers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupStudentSpinner() {
        List<String> studentNames = new ArrayList<>();
        studentNames.add("Select Student");
        for (Student student : studentList) {
            studentNames.add(student.getFullName() + " (" + student.getBatch() + ")");
        }
        studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNames);
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudents.setAdapter(studentAdapter);
    }

    private void setupTeacherSpinner() {
        List<String> teacherNames = new ArrayList<>();
        teacherNames.add("Select Teacher");
        for (Teacher teacher : teacherList) {
            teacherNames.add(teacher.getFullName() + " (" + teacher.getSubject() + ")");
        }
        teacherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherNames);
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeachers.setAdapter(teacherAdapter);
    }

    private void setupCourseSpinner() {
        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fixedCourses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(courseAdapter);
    }

    private void setupListeners() {
        btnAssignStudent.setOnClickListener(v -> assignStudent());
    }

    private void assignStudent() {
        if (spinnerStudents.getSelectedItemPosition() == 0 ||
                spinnerTeachers.getSelectedItemPosition() == 0 ||
                spinnerCourses.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select student, teacher, and course", Toast.LENGTH_SHORT).show();
            return;
        }

        Student selectedStudent = studentList.get(spinnerStudents.getSelectedItemPosition() - 1);
        Teacher selectedTeacher = teacherList.get(spinnerTeachers.getSelectedItemPosition() - 1);
        String selectedCourse = fixedCourses.get(spinnerCourses.getSelectedItemPosition());

        Map<String, Object> assignment = new HashMap<>();
        assignment.put("studentId", selectedStudent.getId());
        assignment.put("studentName", selectedStudent.getFullName());
        assignment.put("studentEmail", selectedStudent.getEmail());
        assignment.put("studentModule", selectedStudent.getModule());
        assignment.put("studentBatch", selectedStudent.getBatch());

        assignment.put("teacherId", selectedTeacher.getId());
        assignment.put("teacherName", selectedTeacher.getFullName());
        assignment.put("teacherEmail", selectedTeacher.getEmail());
        assignment.put("teacherSubject", selectedTeacher.getSubject());
        assignment.put("teacherQualification", selectedTeacher.getQualification());

        assignment.put("courseName", selectedCourse);
        assignment.put("assignmentDate", System.currentTimeMillis());

        db.collection("assignments")
                .add(assignment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Assignment created successfully!", Toast.LENGTH_SHORT).show();
                    resetSpinners();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating assignment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetSpinners() {
        spinnerStudents.setSelection(0);
        spinnerTeachers.setSelection(0);
        spinnerCourses.setSelection(0);
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
            Intent intent = new Intent(StudentAssignmentActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
