package com.nibm.tuitonmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class Teacher_Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_teacher); // Use your XML layout name

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize CardViews
        CardView cardAttendance = findViewById(R.id.cardAttendance);
        CardView cardAssignments = findViewById(R.id.cardAssignments);
        CardView cardMaterials = findViewById(R.id.cardMaterials);
        CardView cardResults = findViewById(R.id.cardResults);

        // Set click listeners
        cardAttendance.setOnClickListener(v -> openAttendance());
        cardAssignments.setOnClickListener(v -> openUploadAssignments());
        cardMaterials.setOnClickListener(v -> openUploadMaterials());
        cardResults.setOnClickListener(v -> openReleaseResults());

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        findViewById(R.id.menuButton).setOnClickListener(v -> toggleDrawer());

    }



    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void openAttendance() {

        Intent intent = new Intent(this, QRCodeActivity.class);
        startActivity(intent);
        showToast("Take Attendance feature would open here");
    }

    private void openUploadAssignments() {
        Intent intent = new Intent(this, Assignment_upload_teacher.class);
        startActivity(intent);

    }

    private void openUploadMaterials() {

        Intent intent = new Intent(this, upload_course_material.class);
        startActivity(intent);

    }

    private void openReleaseResults() {

        Intent intent = new Intent(this, Teacher_upload_results.class);
        startActivity(intent);

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home_teacher) {
            startActivity(new Intent(this, Teacher_Dashboard.class));
        } else if (id == R.id.nav_attendance) {
            startActivity(new Intent(this, QRCodeActivity.class));
        } else if (id == R.id.nav_assignments) {
            startActivity(new Intent(this, Assignment_upload_teacher.class));
        } else if (id == R.id.nav_materials) {
            startActivity(new Intent(this, upload_course_material.class));
        } else if (id == R.id.nav_results) {
            startActivity(new Intent(this, Teacher_upload_results.class));
        } else if (id == R.id.nav_logout2) {
            // Updated logout without FirebaseAuth
            Intent intent = new Intent(Teacher_Dashboard.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
