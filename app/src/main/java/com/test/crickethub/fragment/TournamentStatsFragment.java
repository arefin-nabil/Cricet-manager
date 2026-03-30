package com.test.crickethub.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.test.crickethub.R;
import com.test.crickethub.db.CricketDbHelper;

public class TournamentStatsFragment extends Fragment {

    private TextView tvTopBatsmanName, tvTopBatsmanRuns;
    private TextView tvTopBowlerName, tvTopBowlerWickets;
    private CricketDbHelper db;
    private long tournamentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournament_stats, container, false);
        db = CricketDbHelper.getInstance(requireContext());

        if (getArguments() != null) {
            tournamentId = getArguments().getLong("tournament_id", -1);
        }

        tvTopBatsmanName = view.findViewById(R.id.tv_top_batsman_name);
        tvTopBatsmanRuns = view.findViewById(R.id.tv_top_batsman_runs);
        tvTopBowlerName = view.findViewById(R.id.tv_top_bowler_name);
        tvTopBowlerWickets = view.findViewById(R.id.tv_top_bowler_wickets);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        if (tournamentId == -1) return;
        SQLiteDatabase rdb = db.getReadableDatabase();

        // Query Most Runs
        String qRuns = "SELECT p.name AS player_name, SUM(b.runs_scored) AS total_runs " +
                "FROM ball_events b " +
                "INNER JOIN matches m ON b.match_id = m._id " +
                "INNER JOIN players p ON b.batsman_id = p._id " +
                "WHERE m.tournament_id = ? " +
                "GROUP BY b.batsman_id " +
                "ORDER BY total_runs DESC LIMIT 1";

        Cursor cRuns = rdb.rawQuery(qRuns, new String[]{String.valueOf(tournamentId)});
        if (cRuns != null) {
            if (cRuns.moveToFirst()) {
                tvTopBatsmanName.setText(cRuns.getString(0));
                tvTopBatsmanRuns.setText(cRuns.getInt(1) + " Runs");
            }
            cRuns.close();
        }

        // Query Most Wickets
        String qWkts = "SELECT p.name AS player_name, COUNT(b._id) AS total_wickets " +
                "FROM ball_events b " +
                "INNER JOIN matches m ON b.match_id = m._id " +
                "INNER JOIN players p ON b.bowler_id = p._id " +
                "WHERE m.tournament_id = ? AND b.is_wicket = 1 " +
                "GROUP BY b.bowler_id " +
                "ORDER BY total_wickets DESC LIMIT 1";

        Cursor cWkts = rdb.rawQuery(qWkts, new String[]{String.valueOf(tournamentId)});
        if (cWkts != null) {
            if (cWkts.moveToFirst()) {
                tvTopBowlerName.setText(cWkts.getString(0));
                tvTopBowlerWickets.setText(cWkts.getInt(1) + " Wickets");
            }
            cWkts.close();
        }
    }
}
