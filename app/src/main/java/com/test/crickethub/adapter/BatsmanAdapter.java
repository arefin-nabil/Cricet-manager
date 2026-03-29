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
 * BatsmanAdapter.java
 * Batting scorecard tab RecyclerView adapter.
 * Columns: Player + How Out | R | B | 4s | 6s | SR
 */
public class BatsmanAdapter extends RecyclerView.Adapter<BatsmanAdapter.ViewHolder> {

    private final List<Player> batsmen;

    public BatsmanAdapter(List<Player> batsmen) {
        this.batsmen = batsmen;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_batsman, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player p = batsmen.get(position);
        holder.bind(p);
    }

    @Override
    public int getItemCount() { return batsmen.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvHowOut, tvRuns, tvBalls, tvFours, tvSixes, tvSR;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tv_bat_player_name);
            tvHowOut = itemView.findViewById(R.id.tv_bat_how_out);
            tvRuns   = itemView.findViewById(R.id.tv_bat_runs);
            tvBalls  = itemView.findViewById(R.id.tv_bat_balls);
            tvFours  = itemView.findViewById(R.id.tv_bat_fours);
            tvSixes  = itemView.findViewById(R.id.tv_bat_sixes);
            tvSR     = itemView.findViewById(R.id.tv_bat_sr);
        }

        void bind(Player p) {
            tvName.setText(p.getName());
            tvHowOut.setText(p.isOut() ? p.getHowOut() : "not out *");
            tvRuns.setText(String.valueOf(p.getRuns()));
            tvBalls.setText(String.valueOf(p.getBalls()));
            tvFours.setText(String.valueOf(p.getFours()));
            tvSixes.setText(String.valueOf(p.getSixes()));
            tvSR.setText(String.format("%.1f", p.getStrikeRate()));
        }
    }

    public void update(List<Player> newList) {
        batsmen.clear();
        batsmen.addAll(newList);
        notifyDataSetChanged();
    }
}
