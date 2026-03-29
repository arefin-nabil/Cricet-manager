package com.test.crickethub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.crickethub.R;
import com.test.crickethub.adapter.ManageTeamAdapter;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Team;

import java.util.List;

public class ManageTeamsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvManageTeams;
    private TextView tvNoTeams;
    private FloatingActionButton fabAddTeam;
    
    private CricketDbHelper db;
    private ManageTeamAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_teams);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        db = CricketDbHelper.getInstance(this);

        toolbar = findViewById(R.id.toolbar_manage_teams);
        tvNoTeams = findViewById(R.id.tv_no_teams_manage);
        rvManageTeams = findViewById(R.id.rv_manage_teams);
        fabAddTeam = findViewById(R.id.fab_add_team);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        fabAddTeam.setOnClickListener(v -> {
            startActivity(new Intent(ManageTeamsActivity.this, CreateTeamActivity.class));
        });

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeams();
    }

    private void setupRecyclerView() {
        rvManageTeams.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageTeamAdapter(
            team -> {
                // Edit team
                Intent intent = new Intent(this, CreateTeamActivity.class);
                intent.putExtra("team_id", team.getId());
                startActivity(intent);
            },
            team -> {
                // Delete team
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Team")
                    .setMessage("Are you sure you want to delete " + team.getName() + "? This cannot be undone.")
                    .setPositiveButton("Delete", (d, w) -> {
                        db.deleteTeam(team.getId());
                        Toast.makeText(this, "Team deleted", Toast.LENGTH_SHORT).show();
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
