package com.nibm.tuitonmanagementapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Map;

public class QRCodeActivity extends AppCompatActivity {

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference realtimeDB;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private FirebaseFirestore db;
    private AttendanceManager attendanceManager;
    private TextView tvLastScanned;
    private Button btnScanQR, btnViewReports, btnStudentLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);

        // Initialize views
        tvLastScanned = findViewById(R.id.tv_last_scanned);
        btnScanQR = findViewById(R.id.btn_scan_qr);
        btnViewReports = findViewById(R.id.btn_view_reports);
        btnStudentLogin = findViewById(R.id.btn_student_login);

        realtimeDB = FirebaseDatabase.getInstance().getReference();


        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        attendanceManager = new AttendanceManager(db);

        setupClickListeners();

        if (getSupportActionBar() != null) getSupportActionBar().hide();
    }

    private void setupClickListeners() {
        btnScanQR.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                startQRScanner();
            } else {
                requestCameraPermission();
            }
        });

        btnViewReports.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentAttendanceActivity.class);
            startActivity(intent);
        });

        btnStudentLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan student QR code for attendance");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String scannedData = result.getContents();
                processScannedQR(scannedData);
            }
        }
    }

    private void processScannedQR(String qrContent) {
        Map<String, String> qrData = QRGenerator.parseQRContent(qrContent);
        String studentId = qrData.get("studentId");
        String uuid = qrData.get("uuid");

        // Verify against Realtime DB
        DatabaseReference ref = realtimeDB.child("student_qr_codes").child(studentId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedUUID = snapshot.child("content").getValue(String.class)
                            .split("uuid=")[1].split(";")[0];

                    if (uuid.equals(storedUUID)) {
                        // Valid QR - proceed with attendance
                    } else {
                        // Invalid QR
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}