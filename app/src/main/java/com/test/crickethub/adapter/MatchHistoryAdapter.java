package com.test.crickethub.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.test.crickethub.R;
import com.test.crickethub.model.BallEvent;

import java.util.List;

/**
 * MatchHistoryAdapter.java
 * Adapter for the Match History screen RecyclerView.
 */
public class MatchHistoryAdapter extends RecyclerView.Adapter<MatchHistoryAdapter.ViewHolder> {

    public interface OnMatchHistoryClickListener {
        void onMatchHistoryClick(com.test.crickethub.model.Match match);
    }

    private final List<com.test.crickethub.model.Match> matches;
    private final OnMatchHistoryClickListener listener;

    public MatchHistoryAdapter(List<com.test.crickethub.model.Match> matches,
                               OnMatchHistoryClickListener listener) {
        this.matches  = matches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(matches.get(position));
    }

    @Override
    public int getItemCount() { return matches.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvFormat, tvTeamAName, tvTeamAScore;
        TextView tvTeamBName, tvTeamBScore, tvResult;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate      = itemView.findViewById(R.id.tv_history_date);
            tvFormat    = itemView.findViewById(R.id.tv_history_format);
            tvTeamAName = itemView.findViewById(R.id.tv_history_team_a_name);
            tvTeamAScore= itemView.findViewById(R.id.tv_history_team_a_score);
            tvTeamBName = itemView.findViewById(R.id.tv_history_team_b_name);
            tvTeamBScore= itemView.findViewById(R.id.tv_history_team_b_score);
            tvResult    = itemView.findViewById(R.id.tv_history_result);
        }

        void bind(com.test.crickethub.model.Match match) {
            tvDate.setText(match.getDateFormatted());
            tvFormat.setText(match.getTotalOvers() + " ov");
            tvTeamAName.setText(match.getTeamAName());
            tvTeamAScore.setText(match.getInnings1Score() + "/" +
                    match.getInnings1Wickets() + " (" + match.getInnings1Overs() + ")");
            tvTeamBName.setText(match.getTeamBName());
            tvTeamBScore.setText(match.getInnings2Score() + "/" +
                    match.getInnings2Wickets() + " (" + match.getInnings2Overs() + ")");
            tvResult.setText(match.getResult() != null ? match.getResult() : "");

            itemView.setOnClickListener(v -> listener.onMatchHistoryClick(match));
        }
    }

    public void filter(String query) {
        // Filtering logic can be implemented in Java using a backup list
        notifyDataSetChanged();
    }
}
