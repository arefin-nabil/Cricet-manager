package com.test.crickethub.fragment;

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

import com.test.crickethub.R;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.PointsTableRow;

import java.util.Collections;
import java.util.List;

public class TournamentPointsFragment extends Fragment {

    private RecyclerView rvPoints;
    private CricketDbHelper db;
    private PointsAdapter adapter;
    private long tournamentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournament_points, container, false);
        db = CricketDbHelper.getInstance(requireContext());

        if (getArguments() != null) {
            tournamentId = getArguments().getLong("tournament_id", -1);
        }

        rvPoints = view.findViewById(R.id.rv_points_table);
        rvPoints.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new PointsAdapter();
        rvPoints.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPoints();
    }

    private void loadPoints() {
        if (tournamentId == -1) return;
        List<PointsTableRow> rows = db.getPointsTable(tournamentId);
        // DB orders by points DESC, NRR DESC, but let's just make sure
        Collections.sort(rows);
        adapter.setRows(rows);
    }

    private class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.Holder> {

        private List<PointsTableRow> items;

        public void setRows(List<PointsTableRow> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_points_row, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            PointsTableRow row = items.get(position);
            holder.tvName.setText(row.getTeamName());
            holder.tvM.setText(String.valueOf(row.getMatchesPlayed()));
            holder.tvW.setText(String.valueOf(row.getWon()));
            holder.tvL.setText(String.valueOf(row.getLost()));
            holder.tvPts.setText(String.valueOf(row.getPoints()));
            holder.tvNrr.setText(String.format("%.2f", row.getNetRunRate()));
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvM, tvW, tvL, tvPts, tvNrr;
            public Holder(@NonNull View iv) {
                super(iv);
                tvName = iv.findViewById(R.id.tv_points_team_name);
                tvM = iv.findViewById(R.id.tv_points_m);
                tvW = iv.findViewById(R.id.tv_points_w);
                tvL = iv.findViewById(R.id.tv_points_l);
                tvPts = iv.findViewById(R.id.tv_points_pts);
                tvNrr = iv.findViewById(R.id.tv_points_nrr);
            }
        }
    }
}
