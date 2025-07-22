package com.nibm.tuitonmanagementapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<DocumentSnapshot> attendanceList;

    public AttendanceAdapter(List<DocumentSnapshot> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        DocumentSnapshot attendance = attendanceList.get(position);

        String date = attendance.getString("date");
        Long attendanceCount = attendance.getLong("attendanceCount");
        String status = attendance.getString("status");

        holder.tvDate.setText(formatDate(date));
        holder.tvStatus.setText(status != null ? status : "Present");
        holder.tvCount.setText("Count: " + (attendanceCount != null ? attendanceCount : 1));

        // Set color based on status
        if ("Present".equals(status)) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.success));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.error));
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public void updateData(List<DocumentSnapshot> newAttendanceList) {
        this.attendanceList = newAttendanceList;
        notifyDataSetChanged();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvCount;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCount = itemView.findViewById(R.id.tv_count);
        }
    }


}
