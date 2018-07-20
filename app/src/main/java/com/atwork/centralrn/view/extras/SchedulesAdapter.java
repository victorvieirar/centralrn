package com.atwork.centralrn.view.extras;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atwork.centralrn.R;
import com.atwork.centralrn.model.Schedule;

import java.util.List;

public class SchedulesAdapter extends RecyclerView.Adapter {

    public final OnScheduleClickListener listener;
    private final List<Schedule> schedules;
    private Context context;

    public interface OnScheduleClickListener {
        void onItemClick(View view);
    }

    public SchedulesAdapter(List<Schedule> schedules, OnScheduleClickListener listener, Context context) {
        this.schedules = schedules;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_results, viewGroup, false);
        ResultsViewHolder holder = new ResultsViewHolder(view, schedules.get(i));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ResultsViewHolder holder = (ResultsViewHolder) viewHolder;
        holder.bind(schedules.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    static class ResultsViewHolder extends RecyclerView.ViewHolder {

        final TextView txtHours;
        final TextView txtIdentifier;

        public ResultsViewHolder(View view, Schedule schedule) {
            super(view);
            txtHours = view.findViewById(R.id.txt_hours);
            txtIdentifier = view.findViewById(R.id.txt_subtitle);
        }

        public void bind(final Schedule schedule, final OnScheduleClickListener listener) {
            txtIdentifier.setText(schedule.getIdentifier());
            txtIdentifier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(txtIdentifier);
                }
            });

            txtHours.setText("Sorteio das "+schedule.getScheduleTime());
            txtHours.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View item) {
                    listener.onItemClick(txtHours);
                }
            });

            txtIdentifier.setTag(schedule);
            txtHours.setTag(schedule);
        }

    }
}
