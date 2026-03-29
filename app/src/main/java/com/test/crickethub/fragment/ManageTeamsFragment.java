package com.test.crickethub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.crickethub.R;
import com.test.crickethub.activity.CreateTeamActivity;
import com.test.crickethub.adapter.ManageTeamAdapter;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Team;

import java.util.List;

public class ManageTeamsFragment extends Fragment {

    private RecyclerView rvManageTeams;
    private TextView tvNoTeams;
    private FloatingActionButton fabAddTeam;
    
    private CricketDbHelper db;
    private ManageTeamAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_teams, container, false);
        
        db = CricketDbHelper.getInstance(requireContext());
        bindViews(view);
        setupRecyclerView();
        setupClickListeners();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTeams();
    }

    private void bindViews(View v) {
        rvManageTeams = v.findViewById(R.id.rv_manage_teams_fragment);
        tvNoTeams = v.findViewById(R.id.tv_no_teams_fragment);
        fabAddTeam = v.findViewById(R.id.fab_add_team_fragment);
    }

    private void setupClickListeners() {
        fabAddTeam.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CreateTeamActivity.class));
        });
    }

    private void setupRecyclerView() {
        rvManageTeams.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ManageTeamAdapter(
            team -> {
                Intent intent = new Intent(requireContext(), CreateTeamActivity.class);
                intent.putExtra("team_id", team.getId());
                startActivity(intent);
            },
            team -> {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Team")
                    .setMessage("Are you sure you want to delete " + team.getName() + "? This cannot be undone.")
                    .setPositiveButton("Delete", (d, w) -> {
                        db.deleteTeam(team.getId());
                        Toast.makeText(requireContext(), "Team deleted", Toast.LENGTH_SHORT).show();
                        loadTeams();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        );
        rvManageTeams.setAdapter(adapter);
    }

    private void loadTeams() {
        List<Team> teams = db.getAllTeams();
        if (teams.isEmpty()) {
            tvNoTeams.setVisibility(View.VISIBLE);
            rvManageTeams.setVisibility(View.GONE);
        } else {
            tvNoTeams.setVisibility(View.GONE);
            rvManageTeams.setVisibility(View.VISIBLE);
            adapter.setTeams(teams);
        }
    }
}
