package com.test.crickethub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.test.crickethub.R;
import com.test.crickethub.activity.CreateTournamentActivity;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Tournament;

import java.util.List;

public class TournamentsFragment extends Fragment {

    private RecyclerView rvTournaments;
    private LinearLayout layoutEmpty;
    private ExtendedFloatingActionButton fabCreate;
    private CricketDbHelper db;
    private TournamentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournaments, container, false);
        db = CricketDbHelper.getInstance(requireContext());
        rvTournaments = view.findViewById(R.id.rv_tournaments);
        layoutEmpty = view.findViewById(R.id.layout_empty_tournaments);
        fabCreate = view.findViewById(R.id.fab_create_tournament);

        rvTournaments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TournamentAdapter();
        rvTournaments.setAdapter(adapter);

        fabCreate.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CreateTournamentActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTournaments();
    }

    private void loadTournaments() {
        List<Tournament> list = db.getAllTournaments();
        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvTournaments.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvTournaments.setVisibility(View.VISIBLE);
            adapter.setTournaments(list);
        }
    }

    // ============================================================
    // Simple Adapter internal class
    // ============================================================
    private class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.Holder> {

        private List<Tournament> items;

        public void setTournaments(List<Tournament> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tournament, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Tournament t = items.get(position);
            holder.tvName.setText(t.getName());
            holder.tvFormat.setText(t.getFormat().toUpperCase());
            holder.tvTeams.setText(t.getTotalTeams() + " Teams");
            holder.tvOvers.setText("• " + t.getOversLimit() + " Overs");
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), com.test.crickethub.activity.TournamentDetailActivity.class);
                intent.putExtra("tournament_id", t.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvName, tvFormat, tvTeams, tvOvers;
            public Holder(@NonNull View iv) {
                super(iv);
                tvName = iv.findViewById(R.id.tv_tournament_name);
                tvFormat = iv.findViewById(R.id.tv_tournament_format);
                tvTeams = iv.findViewById(R.id.tv_tournament_teams);
                tvOvers = iv.findViewById(R.id.tv_tournament_overs);
            }
        }
    }
}
