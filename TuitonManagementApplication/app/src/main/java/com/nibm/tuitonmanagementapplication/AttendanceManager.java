package com.nibm.tuitonmanagementapplication;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttendanceManager {

    private static final String TAG = "AttendanceManager";
    private static final String USERS_COLLECTION = "users";
    private static final String ATTENDANCE_COLLECTION = "attendance";

    private final FirebaseFirestore db;

    public AttendanceManager(FirebaseFirestore db) {
        this.db = db;
    }

    public interface AttendanceCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // QR Scanning entry point
    public void markAttendance(String qrData, AttendanceCallback callback) {
        StudentQRManager.StudentQRData studentData = StudentQRManager.parseQRData(qrData);

        if (studentData == null || studentData.studentId == null || studentData.uniqueId == null) {
            callback.onFailure("Invalid QR code format");
            return;
        }

        verifyStudentQRData(studentData, callback);
    }

    private void verifyStudentQRData(StudentQRManager.StudentQRData studentData, AttendanceCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(studentData.studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String dbUniqueId = documentSnapshot.getString("qrUniqueId");

                        if (dbUniqueId != null && dbUniqueId.equals(studentData.uniqueId)) {
                            String studentName = documentSnapshot.getString("fullName");
                            if (studentName == null) studentName = documentSnapshot.getString("name");
                            if (studentName == null) studentName = "Unknown";

                            String batch = documentSnapshot.getString("batch");
                            if (batch == null) batch = "Unknown";

                            checkTodayAttendance(studentData.studentId, studentName, batch, callback);
                        } else {
                            callback.onFailure("Invalid or expired QR code.");
                        }
                    } else {
                        callback.onFailure("Student not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Firestore error: " + e.getMessage());
                });
    }

    private void checkTodayAttendance(String studentId, String studentName, String batch, AttendanceCallback callback) {
        String today = getCurrentDate();

        db.collection(ATTENDANCE_COLLECTION)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && querySnapshot.isEmpty()) {
                        createAttendanceRecord(studentId, studentName, batch, callback);
                    } else if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot existingRecord = querySnapshot.getDocuments().get(0);
                        updateAttendanceRecord(existingRecord.getId(), callback);
                    } else {
                        callback.onFailure("Attendance check failed.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking today's attendance", e);
                    callback.onFailure("Error checking attendance: " + e.getMessage());
                });
    }

    private void createAttendanceRecord(String studentId, String studentName, String batch, AttendanceCallback callback) {
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("studentId", studentId);
        attendanceData.put("studentName", studentName);
        attendanceData.put("batch", batch);
        attendanceData.put("date", getCurrentDate());
        attendanceData.put("timestamp", FieldValue.serverTimestamp());
        attendanceData.put("attendanceCount", 1);
        attendanceData.put("lastMarked", FieldValue.serverTimestamp());
        attendanceData.put("status", "Present");

        db.collection(ATTENDANCE_COLLECTION)
                .add(attendanceData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Attendance recorded with ID: " + documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding attendance record", e);
                    callback.onFailure("Failed to create record: " + e.getMessage());
                });
    }

    private void updateAttendanceRecord(String recordId, AttendanceCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("attendanceCount", FieldValue.increment(1));
        updates.put("lastMarked", FieldValue.serverTimestamp());

        db.collection(ATTENDANCE_COLLECTION)
                .document(recordId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Attendance updated for record: " + recordId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating attendance", e);
                    callback.onFailure("Failed to update record: " + e.getMessage());
                });
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public interface AttendanceHistoryCallback {
        void onSuccess(java.util.List<DocumentSnapshot> attendanceRecords);
        void onFailure(String error);
    }

    public void getStudentAttendance(String studentId, AttendanceHistoryCallback callback) {
        db.collection(ATTENDANCE_COLLECTION)
                .whereEqualTo("studentId", studentId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.getDocuments());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching attendance history", e);
                    callback.onFailure("Error: " + e.getMessage());
                });
    }
}
