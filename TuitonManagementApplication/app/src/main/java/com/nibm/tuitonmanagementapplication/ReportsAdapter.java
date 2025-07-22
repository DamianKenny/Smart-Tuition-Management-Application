package com.nibm.tuitonmanagementapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {

    private List<Map<String, Object>> data;
    private String reportType;

    public ReportsAdapter(List<Map<String, Object>> data, String reportType) {
        this.data = data;
        this.reportType = reportType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = data.get(position);

        if (reportType.equals("attendance")) {
            bindAttendanceData(holder, item);
        } else {
            bindResultsData(holder, item);
        }
    }

    private void bindAttendanceData(ViewHolder holder, Map<String, Object> item) {
        holder.titleText.setText("Date: " + item.get("date"));
        holder.subtitleText.setText("Subject: " + item.get("subject"));

        String status = (String) item.get("status");
        holder.statusText.setText(status);

        // Set status color
        if ("Present".equalsIgnoreCase(status)) {
            holder.statusText.setTextColor(holder.itemView.getContext().getColor(R.color.success));
        } else if ("Absent".equalsIgnoreCase(status)) {
            holder.statusText.setTextColor(holder.itemView.getContext().getColor(R.color.error));
        } else {
            holder.statusText.setTextColor(holder.itemView.getContext().getColor(R.color.warning));
        }

        holder.extraText.setText("Teacher: " + item.get("teacherName"));
    }

    private void bindResultsData(ViewHolder holder, Map<String, Object> item) {
        holder.titleText.setText("Subject: " + item.get("subject"));
        holder.subtitleText.setText("Exam: " + item.get("examType"));

        String grade = (String) item.get("grade");
        Object marksObj = item.get("marks");
        String marks = marksObj != null ? marksObj.toString() : "N/A";

        holder.statusText.setText(grade + " (" + marks + "%)");

        // Set grade color
        if (grade != null) {
            if (grade.equals("A") || grade.equals("A+")) {
                holder.statusText.setTextColor(holder.itemView.getContext().getColor(R.color.success));
            } else if (grade.equals("B") || grade.equals("B+")) {
                holder.statusText.setTextColor(holder.itemView.getContext().getColor(R.color.primary));
            } else if (grade.equals("C") || grade.equals("C+")) {
                holder.statusText.setTextColor(holder.itemView.getContext().getColor(R.color.warning));
            } else {
                holder.statusText.setTextColor(holder.itemView.getContext().getColor(R.color.error));
            }
        }

        holder.extraText.setText("Date: " + item.get("examDate"));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(List<Map<String, Object>> newData, String newReportType) {
        this.data = newData;
        this.reportType = newReportType;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView subtitleText;
        TextView statusText;
        TextView extraText;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            subtitleText = itemView.findViewById(R.id.subtitleText);
            statusText = itemView.findViewById(R.id.statusText);
            extraText = itemView.findViewById(R.id.extraText);
        }
    }
}
