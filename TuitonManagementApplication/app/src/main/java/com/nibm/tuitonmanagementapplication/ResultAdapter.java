package com.nibm.tuitonmanagementapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.tuitonmanagementapplication.model.ResultItem;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private final List<ResultItem> resultItems;

    public ResultAdapter(List<ResultItem> resultItems) {
        this.resultItems = resultItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResultItem item = resultItems.get(position);

        holder.courseTitle.setText("Final Grade: " + item.getFinalGrade());
    }

    @Override
    public int getItemCount() {
        return resultItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView courseTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.txtCourseTitle);
        }
    }
}