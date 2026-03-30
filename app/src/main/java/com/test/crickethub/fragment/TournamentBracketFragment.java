package com.test.crickethub.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.crickethub.R;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Match;
import com.test.crickethub.model.Tournament;

import java.util.List;

public class TournamentBracketFragment extends Fragment {

    private RecyclerView rvBracket;
    private LinearLayout layoutEmpty;
    private TextView tvInfo;
    private CricketDbHelper db;
    private BracketAdapter adapter;
    private long tournamentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournament_bracket, container, false);
        db = CricketDbHelper.getInstance(requireContext());

        if (getArguments() != null) {
            tournamentId = getArguments().getLong("tournament_id", -1);
        }

        rvBracket = view.findViewById(R.id.rv_tournament_bracket);
        layoutEmpty = view.findViewById(R.id.layout_empty_bracket);
        tvInfo = view.findViewById(R.id.tv_bracket_info);

        rvBracket.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BracketAdapter();
        rvBracket.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBracket();
    }

    private void loadBracket() {
        if (tournamentId == -1) return;
        
        Tournament trn = db.getTournamentById(tournamentId);
        if (trn != null && Tournament.FORMAT_ROUND_ROBIN.equals(trn.getFormat())) {
            tvInfo.setText("Not applicable for Round Robin format.");
            rvBracket.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
            return;
        }

        List<Match> list = db.getMatchesForTournament(tournamentId);
        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvBracket.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvBracket.setVisibility(View.VISIBLE);
            adapter.setMatches(list);
        }
    }

    private class BracketAdapter extends RecyclerView.Adapter<BracketAdapter.Holder> {

        private List<Match> items;

        public void setMatches(List<Match> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_match, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Match m = items.get(position);
            holder.tvTeam1.setText(m.getTeamAName());
            holder.tvTeam2.setText(m.getTeamBName());
            holder.tvStatus.setText("Knockout Stage • Match " + m.getTournamentMatchNumber());
            
            if (m.isMatchComplete() || Match.STATUS_LIVE.equals(m.getStatus())) {
                holder.tvResult.setText(m.getResult() + "\n(Advances to next round)");
                holder.tvResult.setVisibility(View.VISIBLE);
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvTeam1Score.setVisibility(View.VISIBLE);
                holder.tvTeam2Score.setVisibility(View.VISIBLE);
                
                holder.tvTeam1Score.setText(m.getInnings1Score() + "/" + m.getInnings1Wickets() + " (" + m.getInnings1Overs() + "." + m.getInnings1Balls() + ")");
                holder.tvTeam2Score.setText(m.getInnings2Score() + "/" + m.getInnings2Wickets() + " (" + m.getInnings2Overs() + "." + m.getInnings2Balls() + ")");
                
                if (Match.STATUS_LIVE.equals(m.getStatus())) {
                     holder.tvBadge.setText("LIVE");
                     holder.tvBadge.setTextColor(Color.RED); 
                } else {
                     holder.tvBadge.setText("WON"); 
                     holder.tvBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_primary));
                }
            } else {
                holder.tvResult.setVisibility(View.GONE);
                holder.tvBadge.setVisibility(View.GONE);
                holder.tvTeam1Score.setVisibility(View.GONE);
                holder.tvTeam2Score.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (m.isMatchComplete()) {
                    Intent intent = new Intent(getContext(), com.test.crickethub.activity.ScorecardActivity.class);
                    intent.putExtra("match_id", m.getId());
                    startActivity(intent);
                } else if (Match.STATUS_LIVE.equals(m.getStatus())) {
                    Intent intent = new Intent(getContext(), com.test.crickethub.activity.LiveScoringActivity.class);
                    intent.putExtra("match_id", m.getId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), com.test.crickethub.activity.CreateMatchActivity.class);
                    intent.putExtra("match_id", m.getId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvTeam1, tvTeam2, tvStatus, tvResult, tvTeam1Score, tvTeam2Score, tvBadge;
            public Holder(@NonNull View iv) {
                super(iv);
                tvTeam1 = iv.findViewById(R.id.tv_team_a_name);
                tvTeam2 = iv.findViewById(R.id.tv_team_b_name);
                tvStatus = iv.findViewById(R.id.tv_match_date);
                tvResult = iv.findViewById(R.id.tv_match_summary);
                tvTeam1Score = iv.findViewById(R.id.tv_team_a_score);
                tvTeam2Score = iv.findViewById(R.id.tv_team_b_score);
                tvBadge = iv.findViewById(R.id.tv_match_result_badge);
            }
        }
    }
}
