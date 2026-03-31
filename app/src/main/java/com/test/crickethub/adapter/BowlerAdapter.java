package com.test.crickethub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.crickethub.R;
import com.test.crickethub.model.Player;

import java.util.List;

/**
 * BowlerAdapter.java
 * Bowling scorecard tab RecyclerView adapter.
 * Columns: Bowler Name | OV | R | W | ECO
 */
public class BowlerAdapter extends RecyclerView.Adapter<BowlerAdapter.ViewHolder> {

    private final List<Player> bowlers;

    public BowlerAdapter(List<Player> bowlers) {
        this.bowlers = bowlers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bowler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(bowlers.get(position));
    }

    @Override
    public int getItemCount() { return bowlers.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvOvers, tvRuns, tvWickets, tvEco;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tv_bow_name);
            tvOvers   = itemView.findViewById(R.id.tv_bow_overs);
            tvRuns    = itemView.findViewById(R.id.tv_bow_runs);
            tvWickets = itemView.findViewById(R.id.tv_bow_wickets);
            tvEco     = itemView.findViewById(R.id.tv_bow_economy);
        }

        void bind(Player p) {
            tvName.setText(p.getDisplayName());
            tvOvers.setText(p.getOversBowledFormatted());
            tvRuns.setText(String.valueOf(p.getRunsConceded()));
            tvWickets.setText(String.valueOf(p.getWicketsTaken()));
            tvEco.setText(String.format("%.2f", p.getEconomy()));
        }
    }

    public void update(List<Player> newList) {
        bowlers.clear();
        bowlers.addAll(newList);
        notifyDataSetChanged();
    }
}
