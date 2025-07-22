package com.nibm.tuitonmanagementapplication;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QRGenerator {
    private static final String TAG = "QRGenerator";
    private static final String QR_NODE = "student_qr_codes";
    private final FirebaseFirestore firestore;
    private final DatabaseReference realtimeDB;

    public QRGenerator() {
        this.firestore = FirebaseFirestore.getInstance();
        this.realtimeDB = FirebaseDatabase.getInstance().getReference(QR_NODE);
    }

    public interface QRCallback {
        void onQRGenerated(Bitmap qrBitmap);
        void onError(String errorMessage);
    }

    public void generateStudentQR(String studentId, QRCallback callback) {
        // Check if QR already exists in Realtime DB
        realtimeDB.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // QR exists - generate from stored data
                    String qrContent = snapshot.child("content").getValue(String.class);
                    generateQRImage(qrContent, callback);
                } else {
                    // Create new QR code
                    createNewQR(studentId, callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Database error: " + error.getMessage());
            }
        });
    }

    private void createNewQR(String studentId, QRCallback callback) {
        firestore.collection("users").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String batch = documentSnapshot.getString("batch");
                        String email = documentSnapshot.getString("email");

                        // Create secure QR payload
                        String uuid = UUID.randomUUID().toString();
                        Map<String, String> qrPayload = new HashMap<>();
                        qrPayload.put("studentId", studentId);
                        qrPayload.put("name", name);
                        qrPayload.put("batch", batch);
                        qrPayload.put("email", email);
                        qrPayload.put("uuid", uuid);
                        qrPayload.put("timestamp", String.valueOf(System.currentTimeMillis()));

                        // Generate QR string
                        StringBuilder qrBuilder = new StringBuilder();
                        for (Map.Entry<String, String> entry : qrPayload.entrySet()) {
                            qrBuilder.append(entry.getKey())
                                    .append("=")
                                    .append(entry.getValue())
                                    .append(";");
                        }
                        String qrContent = qrBuilder.toString();

                        // Save to Realtime DB
                        Map<String, Object> qrData = new HashMap<>();
                        qrData.put("content", qrContent);
                        qrData.put("createdAt", System.currentTimeMillis());

                        realtimeDB.child(studentId).setValue(qrData)
                                .addOnSuccessListener(unused -> generateQRImage(qrContent, callback))
                                .addOnFailureListener(e -> callback.onError("Save failed: " + e.getMessage()));
                    } else {
                        callback.onError("Student not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Firestore error: " + e.getMessage()));
    }

    private void generateQRImage(String content, QRCallback callback) {
        try {
            // Use optimized QR generation with ZXing
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1); // Smaller border

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    512,
                    512,
                    hints
            );

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            callback.onQRGenerated(bitmap);
        } catch (WriterException e) {
            Log.e(TAG, "QR generation failed", e);
            callback.onError("QR creation failed");
        }
    }

    public void refreshQRCode(String studentId, QRCallback callback) {
        // Remove existing QR and generate new one
        realtimeDB.child(studentId).removeValue()
                .addOnSuccessListener(unused -> generateStudentQR(studentId, callback))
                .addOnFailureListener(e -> callback.onError("Refresh failed: " + e.getMessage()));
    }

    // QR Validation method for teacher side
    public static Map<String, String> parseQRContent(String qrContent) {
        Map<String, String> result = new HashMap<>();
        String[] pairs = qrContent.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }
}