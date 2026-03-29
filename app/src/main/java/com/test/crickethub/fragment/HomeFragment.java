package com.test.crickethub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.test.crickethub.R;
import com.test.crickethub.activity.CreateMatchActivity;
import com.test.crickethub.activity.CreateTeamActivity;
import com.test.crickethub.activity.LiveScoringActivity;
import com.test.crickethub.activity.ScorecardActivity;
import com.test.crickethub.adapter.RecentMatchAdapter;
import com.test.crickethub.adapter.TeamChipAdapter;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Match;
import com.test.crickethub.model.Team;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ExtendedFloatingActionButton fabStartMatch;
    private RecyclerView rvRecentMatches;
    private RecyclerView rvTeams;
    private TextView tvMatchesCount;
    private TextView tvTeamsCount;
    private TextView tvHighestScore;
    private TextView tvPlayersCount;
    private TextView tvNoMatches;
    private TextView tvNoTeams;
    private TextView tvCreateTeamLink;
    private TextView tvViewAll;

    private CricketDbHelper db;
    private RecentMatchAdapter recentMatchAdapter;
    private TeamChipAdapter teamChipAdapter;
    private List<Match> recentMatches = new ArrayList<>();
    private List<Team> dashboardTeams = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        db = CricketDbHelper.getInstance(requireContext());
        bindViews(view);
        setupClickListeners();
        setupRecentMatchesRecycler();
        setupTeamChipsRecycler();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void bindViews(View v) {
        fabStartMatch = v.findViewById(R.id.fab_start_match_home);
        rvRecentMatches = v.findViewById(R.id.rv_recent_matches_home);
        rvTeams = v.findViewById(R.id.rv_teams_home);
        tvMatchesCount = v.findViewById(R.id.tv_matches_count);
        tvTeamsCount = v.findViewById(R.id.tv_teams_count);
        tvHighestScore = v.findViewById(R.id.tv_highest_score);
        tvPlayersCount = v.findViewById(R.id.tv_players_count);
        tvNoMatches = v.findViewById(R.id.tv_no_matches_home);
        tvNoTeams = v.findViewById(R.id.tv_no_teams_home);
        tvCreateTeamLink = v.findViewById(R.id.tv_create_team_link_home);
        tvViewAll = v.findViewById(R.id.tv_view_all_home);
    }

    private void setupClickListeners() {
        fabStartMatch.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateMatchActivity.class))
        );

        tvCreateTeamLink.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateTeamActivity.class))
        );

        tvViewAll.setOnClickListener(v -> {
            // This will be handled by the activity to switch tabs
            if (getActivity() instanceof OnHomeNavigationListener) {
                ((OnHomeNavigationListener) getActivity()).onNavigateToHistory();
            }
        });
    }

    private void setupRecentMatchesRecycler() {
        recentMatchAdapter = new RecentMatchAdapter(recentMatches, match -> {
            Intent intent;
            if (Match.STATUS_LIVE.equals(match.getStatus())) {
                intent = new Intent(requireContext(), LiveScoringActivity.class);
            } else {
                intent = new Intent(requireContext(), ScorecardActivity.class);
            }
            intent.putExtra("match_id", match.getId());
            startActivity(intent);
        });
        rvRecentMatches.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentMatches.setAdapter(recentMatchAdapter);
    }

    private void setupTeamChipsRecycler() {
        teamChipAdapter = new TeamChipAdapter(dashboardTeams, team -> {
            if (getActivity() instanceof OnHomeNavigationListener) {
                ((OnHomeNavigationListener) getActivity()).onNavigateToTeams();
            }
        });
        rvTeams.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTeams.setAdapter(teamChipAdapter);
    }

    private void loadDashboardData() {
        int matchCount = db.getMatchCount();
        int teamCount = db.getTeamCount();
        int highScore = db.getHighestScore();
        int playersCount = db.getTotalPlayers();

        tvMatchesCount.setText(matchCount > 0 ? String.valueOf(matchCount) : "0");
        tvTeamsCount.setText(teamCount > 0 ? String.valueOf(teamCount) : "0");
        tvHighestScore.setText(highScore > 0 ? String.valueOf(highScore) : "0");
        tvPlayersCount.setText(playersCount > 0 ? String.valueOf(playersCount) : "0");

        List<Match> allMatches = db.getAllMatches();
        recentMatches.clear();
        int limit = Math.min(allMatches.size(), 10);
        for (int i = 0; i < limit; i++) {
            recentMatches.add(allMatches.get(i));
        }
        recentMatchAdapter.notifyDataSetChanged();

        tvNoMatches.setVisibility(recentMatches.isEmpty() ? View.VISIBLE : View.GONE);
        rvRecentMatches.setVisibility(recentMatches.isEmpty() ? View.GONE : View.VISIBLE);

        List<Team> teams = db.getAllTeams();
        tvNoTeams.setVisibility(teams.isEmpty() ? View.VISIBLE : View.GONE);
        dashboardTeams.clear();
        dashboardTeams.addAll(teams);
        teamChipAdapter.notifyDataSetChanged();
    }

    public interface OnHomeNavigationListener {
        void onNavigateToTeams();
        void onNavigateToHistory();
    }
}
