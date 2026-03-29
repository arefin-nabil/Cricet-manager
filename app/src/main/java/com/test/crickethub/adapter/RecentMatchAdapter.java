package com.test.crickethub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.crickethub.R;
import com.test.crickethub.model.Match;

import java.util.List;

/**
 * RecentMatchAdapter.java
 * Displays recent matches on the Dashboard RecyclerView
 */
public class RecentMatchAdapter extends RecyclerView.Adapter<RecentMatchAdapter.ViewHolder> {

    public interface OnMatchClickListener {
        void onMatchClick(Match match);
    }

    private final List<Match>           matches;
    private final OnMatchClickListener  listener;

    public RecentMatchAdapter(List<Match> matches, OnMatchClickListener listener) {
        this.matches  = matches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Match match = matches.get(position);
        holder.bind(match, listener);
    }

    @Override
    public int getItemCount() { return matches.size(); }

    // ============================================================
    // ViewHolder
    // ============================================================
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvResult, tvTeamAName, tvTeamAScore;
        TextView tvTeamBName, tvTeamBScore, tvSummary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate      = itemView.findViewById(R.id.tv_match_date);
            tvResult    = itemView.findViewById(R.id.tv_match_result_badge);
            tvTeamAName = itemView.findViewById(R.id.tv_team_a_name);
            tvTeamAScore= itemView.findViewById(R.id.tv_team_a_score);
            tvTeamBName = itemView.findViewById(R.id.tv_team_b_name);
            tvTeamBScore= itemView.findViewById(R.id.tv_team_b_score);
            tvSummary   = itemView.findViewById(R.id.tv_match_summary);
        }

        void bind(Match match, OnMatchClickListener listener) {
            // Date + format label
            tvDate.setText(match.getDateFormatted() + " • " + match.getTotalOvers() + " overs");

            // Team A
            tvTeamAName.setText(match.getTeamAName());
            tvTeamAScore.setText(formatScore(match.getInnings1Score(),
                    match.getInnings1Wickets(), match.getInnings1Overs()));

            // Team B
            tvTeamBName.setText(match.getTeamBName());
            tvTeamBScore.setText(formatScore(match.getInnings2Score(),
                    match.getInnings2Wickets(), match.getInnings2Overs()));

            // Result
            if (match.getResult() != null && !match.getResult().isEmpty()) {
                tvSummary.setText(match.getResult());
                tvResult.setText(match.isMatchComplete() ? "DONE" : "LIVE");
            } else {
                tvSummary.setText("");
                tvResult.setText("LIVE");
            }

            itemView.setOnClickListener(v -> listener.onMatchClick(match));
        }

        private String formatScore(int runs, int wickets, int overs) {
            return runs + "/" + wickets + " (" + overs + ")";
        }
    }

    // ============================================================
    // Data Update
    // ============================================================
    public void updateMatches(List<Match> newMatches) {
        matches.clear();
        matches.addAll(newMatches);
        notifyDataSetChanged();
    }
}
