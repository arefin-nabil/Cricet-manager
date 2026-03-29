package com.test.crickethub.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.test.crickethub.R;
import com.test.crickethub.adapter.PlayerRowAdapter;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Player;
import com.test.crickethub.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateTeamActivity.java
 * ========================
 * Screen for creating or editing a cricket team.
 * Features dynamic player list with add/remove.
 */
public class CreateTeamActivity extends AppCompatActivity {

    // ============================================================
    // Views
    // ============================================================
    private MaterialToolbar    toolbar;
    private TextInputEditText  etTeamName;
    private TextInputEditText  etTeamDesc;
    private RecyclerView       rvPlayers;
    private MaterialButton     btnAddPlayer;
    private MaterialButton     btnSaveTeam;
    private TextView           tvPlayerCount;
    private TextView           tvNoPlayers;

    // ============================================================
    // Data
    // ============================================================
    private CricketDbHelper   db;
    private PlayerRowAdapter  playerAdapter;
    private List<Player>      playerList = new ArrayList<>();
    private long              editTeamId = -1; // -1 = creating new team

    // ============================================================
    // Lifecycle
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = CricketDbHelper.getInstance(this);

        // Check if we're editing an existing team
        if (getIntent().hasExtra("team_id")) {
            editTeamId = getIntent().getLongExtra("team_id", -1);
        }

        bindViews();
        setupToolbar();
        setupPlayerRecycler();
        setupClickListeners();

        // If editing, pre-load team data
        if (editTeamId != -1) {
            loadExistingTeam(editTeamId);
        }
    }

    // ============================================================
    // View Binding
    // ============================================================

    private void bindViews() {
        toolbar       = findViewById(R.id.toolbar_create_team);
        etTeamName    = findViewById(R.id.et_team_name);
        etTeamDesc    = findViewById(R.id.et_team_desc);
        rvPlayers     = findViewById(R.id.rv_players);
        btnAddPlayer  = findViewById(R.id.btn_add_player);
        btnSaveTeam   = findViewById(R.id.btn_save_team);
        tvPlayerCount = findViewById(R.id.tv_player_count);
        tvNoPlayers   = findViewById(R.id.tv_no_players);
    }

    // ============================================================
    // Toolbar
    // ============================================================

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        if (editTeamId != -1) {
            toolbar.setTitle("Edit Team");
        }
    }

    // ============================================================
    // Player RecyclerView
    // ============================================================

    private void setupPlayerRecycler() {
        playerAdapter = new PlayerRowAdapter(playerList, new PlayerRowAdapter.OnPlayerChangedListener() {
            @Override
            public void onPlayerDeleted(int position) {
                updatePlayerCountLabel();
                updateEmptyState();
            }
            @Override
            public void onPlayerCountChanged(int count) {
                updatePlayerCountLabel();
                updateEmptyState();
            }
        });
        rvPlayers.setLayoutManager(new LinearLayoutManager(this));
        rvPlayers.setAdapter(playerAdapter);
        rvPlayers.setNestedScrollingEnabled(false);
    }

    // ============================================================
    // Click Listeners
    // ============================================================

    private void setupClickListeners() {
        btnAddPlayer.setOnClickListener(v -> {
            Player p = new Player();
            p.setName("");
            playerList.add(p);
            playerAdapter.notifyItemInserted(playerList.size() - 1);
            updateEmptyState();

            // Auto-scroll to show the new player (prevent keyboard overlap)
            rvPlayers.post(() -> {
                rvPlayers.smoothScrollToPosition(playerList.size() - 1);
                View scrollView = findViewById(R.id.scroll_create_team);
                if (scrollView instanceof android.widget.ScrollView) {
                    ((android.widget.ScrollView) scrollView).fullScroll(View.FOCUS_DOWN);
                }
            });
        });

        btnSaveTeam.setOnClickListener(v -> saveTeam());
    }

    // ============================================================
    // Saving
    // ============================================================

    private void saveTeam() {
        // Validate team name
        String teamName = etTeamName.getText() != null
                ? etTeamName.getText().toString().trim() : "";
        String teamDesc = etTeamDesc.getText() != null
                ? etTeamDesc.getText().toString().trim() : "";

        if (teamName.isEmpty()) {
            etTeamName.setError(getString(R.string.error_team_name));
            etTeamName.requestFocus();
            return;
        }

        // Validate player count
        if (playerList.size() < 2) {
            Toast.makeText(this, getString(R.string.error_add_players), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate no empty player names
        for (Player p : playerList) {
            if (p.getName() == null || p.getName().trim().isEmpty()) {
                Toast.makeText(this, "All players must have a name", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Save team
        Team team = new Team();
        team.setName(teamName);
        team.setDescription(teamDesc);

        long teamId;
        if (editTeamId == -1) {
            teamId = db.insertTeam(team);
        } else {
            teamId = editTeamId;
            team.setId(teamId);
            db.updateTeam(team);
        }

        // Save players
        db.savePlayersForTeam(teamId, playerList);

        Toast.makeText(this, getString(R.string.toast_team_saved), Toast.LENGTH_SHORT).show();
        finish();
    }

    // ============================================================
    // Load Existing Team
    // ============================================================

    private void loadExistingTeam(long teamId) {
        Team existingTeam = db.getTeamById(teamId);
        if (existingTeam != null) {
            etTeamName.setText(existingTeam.getName());
            if (existingTeam.getDescription() != null) {
                etTeamDesc.setText(existingTeam.getDescription());
            }
        }
        
        List<Player> existing = db.getPlayersForTeam(teamId);
        playerList.clear();
        playerList.addAll(existing);
        playerAdapter.notifyDataSetChanged();
        updatePlayerCountLabel();
        updateEmptyState();
    }

    // ============================================================
    // UI Helpers
    // ============================================================

    private void updatePlayerCountLabel() {
        tvPlayerCount.setText(playerList.size() + " / 11");
    }

    private void updateEmptyState() {
        boolean isEmpty = playerList.isEmpty();
        tvNoPlayers.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvPlayers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
