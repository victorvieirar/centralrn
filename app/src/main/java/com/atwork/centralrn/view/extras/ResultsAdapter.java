package com.atwork.centralrn.view.extras;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atwork.centralrn.R;
import com.atwork.centralrn.model.Result;

import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter {

    public final OnResultClickListener listener;
    private final List<Result> results;
    private Context context;

    public interface OnResultClickListener {
        void onItemClick(View view);
    }

    public ResultsAdapter(List<Result> results, OnResultClickListener listener, Context context) {
        this.results = results;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_results, viewGroup, false);
        ResultsViewHolder holder = new ResultsViewHolder(view, results.get(i));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ResultsViewHolder holder = (ResultsViewHolder) viewHolder;
        holder.bind(results.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ResultsViewHolder extends RecyclerView.ViewHolder {

        final TextView txtHours;
        final TextView txtDate;

        public ResultsViewHolder(View view, Result result) {
            super(view);
            txtHours = view.findViewById(R.id.txt_hours);
            txtDate = view.findViewById(R.id.txt_subtitle);
        }

        public void bind(final Result result, final OnResultClickListener listener) {
            txtDate.setText(formatDate(result.getDatetime()));
            txtDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(txtDate);
                }
            });

            txtHours.setText("Sorteio das "+formatHours(result.getDatetime()));
            txtHours.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View item) {
                    listener.onItemClick(txtHours);
                }
            });

            txtDate.setTag(result);
            txtHours.setTag(result);
        }

        private String formatHours(String datetime) {
            String hour = datetime.substring(11,13);
            String minutes = datetime.substring(14,16);
            String sep = ":";

            return hour+sep+minutes;
        }

        private String formatDate(String datetime) {
            String year = datetime.substring(0, 4);
            String month = datetime.substring(5, 7);
            String day = datetime.substring(8, 10);
            String sep = "/";

            return day+sep+month+sep+year;
        }

    }
}
