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

public class DetailResultAdapter extends RecyclerView.Adapter {

    private final List<Result> results;
    private Context context;

    public DetailResultAdapter(List<Result> results, Context context) {
        this.results = results;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_detail_result, viewGroup, false);
        ResultsViewHolder holder = new ResultsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ResultsViewHolder holder = (ResultsViewHolder) viewHolder;
        holder.bind(results.get(i));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ResultsViewHolder extends RecyclerView.ViewHolder {

        final TextView txtInfo;
        final TextView txtChiliad;
        final TextView emoji;

        private final int[] emojiList = {
                0x1F95A,
                0x1F985,
                0x1F434,
                0x1F98B,
                0x1F436,
                0x1F410,
                0x1F40F,
                0x1F42B,
                0x1F40D,
                0x1F430,
                0x1F40E,
                0x1F418,
                0x1F413,
                0x1F431,
                0x1F40A,
                0x1F981,
                0x1F435,
                0x1F437,
                0x1F99A,
                0x1F983,
                0x1F402,
                0x1F42F,
                0x1F43B,
                0x1F98C,
                0x1F42E
        };

        public ResultsViewHolder(View view) {
            super(view);
            txtInfo = view.findViewById(R.id.txt_info);
            txtChiliad = view.findViewById(R.id.txt_chiliad);
            emoji = view.findViewById(R.id.emoji);
        }

        public void bind(final Result result) {
            txtInfo.setText(formatInfo(result));
            txtChiliad.setText(formatChiliad(result.getChiliad()+""));
            emoji.setText(new String(Character.toChars(emojiList[result.getDozen() - 1])));
        }

        private String formatInfo(Result result) {
            int position = result.getPosition();
            String animalName = result.getAnimalName();
            int dozen = result.getDozen();

            String info = position+"º - "+animalName+" #"+dozen;
            return info;
        }

        private String formatChiliad(String chiliad) {
            if(chiliad.length() < 4) {
                String zeros = "";
                for(int i = 0; i < (4 - chiliad.length()); i++) {
                    zeros += "0";
                }
                chiliad = zeros + chiliad;
            }

            return chiliad;
        }

    }
}
