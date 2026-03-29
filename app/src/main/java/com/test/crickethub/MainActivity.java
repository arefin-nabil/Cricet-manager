package com.test.crickethub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.test.crickethub.activity.LiveScoringActivity;
import com.test.crickethub.activity.ScorecardActivity;
import com.test.crickethub.activity.ManageTeamsActivity;
import com.test.crickethub.activity.CreateMatchActivity;
import com.test.crickethub.activity.CreateTeamActivity;
import com.test.crickethub.activity.MatchHistoryActivity;
import com.test.crickethub.adapter.RecentMatchAdapter;
import com.test.crickethub.adapter.TeamChipAdapter;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Match;
import com.test.crickethub.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity.java — CricketHub Dashboard
 * ==========================================
 * The landing screen with:
 *   - Quick stats (match count, teams, highest score)
 *   - My Teams horizontal list
 *   - Recent Matches list
 *   - FAB to start a new match
 *   - Bottom Navigation
 */
public class MainActivity extends AppCompatActivity {

    // ============================================================
    // Views
    // ============================================================
    private ExtendedFloatingActionButton fabStartMatch;
    private BottomNavigationView         bottomNav;
    private RecyclerView                 rvRecentMatches;
    private RecyclerView                 rvTeams;
    private TextView           tvMatchesCount;
    private TextView           tvTeamsCount;
    private TextView           tvHighestScore;
    private TextView           tvPlayersCount;
    private TextView           tvNoMatches;
    private TextView                     tvNoTeams;
    private TextView                     tvCreateTeamLink;
    private TextView                     tvViewAll;
    private MaterialToolbar toolbar;

    // ============================================================
    // Data
    // ============================================================
    private CricketDbHelper    db;
    private RecentMatchAdapter recentMatchAdapter;
    private TeamChipAdapter    teamChipAdapter;
    private List<Match>        recentMatches = new ArrayList<>();
    private List<Team>         dashboardTeams = new ArrayList<>();

    // ============================================================
    // Lifecycle
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinator_main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = CricketDbHelper.getInstance(this);

        bindViews();
        setupToolbar(); // Enable Settings menu
        setupBottomNav();
        setupFab();
        setupClickListeners();
        setupRecentMatchesRecycler();
        setupTeamChipsRecycler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh all data whenever we return to dashboard
        loadDashboardData();
    }

    // ============================================================
    // View Binding
    // ============================================================

    private void bindViews() {
        fabStartMatch    = findViewById(R.id.fab_start_match);
        bottomNav        = findViewById(R.id.bottom_nav);
        rvRecentMatches  = findViewById(R.id.rv_recent_matches);
        rvTeams          = findViewById(R.id.rv_teams);
        tvMatchesCount   = findViewById(R.id.tv_matches_count);
        tvTeamsCount     = findViewById(R.id.tv_teams_count);
        tvHighestScore   = findViewById(R.id.tv_highest_score);
        tvPlayersCount   = findViewById(R.id.tv_players_count);
        tvNoMatches      = findViewById(R.id.tv_no_matches);
        tvNoTeams        = findViewById(R.id.tv_no_teams);
        tvCreateTeamLink = findViewById(R.id.tv_create_team_link);
        tvViewAll        = findViewById(R.id.tv_view_all);
        toolbar          = findViewById(R.id.toolbar_main);
    }

    // ============================================================
    // Bottom Navigation
    // ============================================================

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_teams) {
                startActivity(new Intent(this, ManageTeamsActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, MatchHistoryActivity.class));
                return true;
            }
            return false;
        });
    }

    // ============================================================
    // FAB
    // ============================================================

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, com.test.crickethub.activity.SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupFab() {
        fabStartMatch.setOnClickListener(v ->
                startActivity(new Intent(this, CreateMatchActivity.class))
        );
    }

    // ============================================================
    // Click Listeners
    // ============================================================

    private void setupClickListeners() {
        // "Create" team shortcut
        tvCreateTeamLink.setOnClickListener(v ->
                startActivity(new Intent(this, CreateTeamActivity.class))
        );

        // "View All" matches → History
        tvViewAll.setOnClickListener(v ->
                startActivity(new Intent(this, MatchHistoryActivity.class))
        );
    }

    // ============================================================
    // RecyclerView Setup
    // ============================================================

    private void setupRecentMatchesRecycler() {
        recentMatchAdapter = new RecentMatchAdapter(recentMatches, match -> {
            Intent intent;
            if (Match.STATUS_LIVE.equals(match.getStatus())) {
                intent = new Intent(this, com.test.crickethub.activity.LiveScoringActivity.class);
            } else {
                intent = new Intent(this, com.test.crickethub.activity.ScorecardActivity.class);
            }
            intent.putExtra("match_id", match.getId());
            startActivity(intent);
        });
        rvRecentMatches.setLayoutManager(new LinearLayoutManager(this));
        rvRecentMatches.setAdapter(recentMatchAdapter);
        rvRecentMatches.setNestedScrollingEnabled(false);
    }

    private void setupTeamChipsRecycler() {
        teamChipAdapter = new TeamChipAdapter(dashboardTeams, team -> {
            // Open manage teams screen
            startActivity(new Intent(this, ManageTeamsActivity.class));
        });
        rvTeams.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTeams.setAdapter(teamChipAdapter);
        rvTeams.setNestedScrollingEnabled(false);
    }

    // ============================================================
    // Data Loading
    // ============================================================

    private void loadDashboardData() {
        // Quick stats
        int matchCount   = db.getMatchCount();
        int teamCount    = db.getTeamCount();
        int highScore    = db.getHighestScore();
        int playersCount = db.getTotalPlayers();

        tvMatchesCount.setText(matchCount > 0 ? String.valueOf(matchCount) : "0");
        tvTeamsCount.setText(teamCount > 0 ? String.valueOf(teamCount) : "0");
        tvHighestScore.setText(highScore > 0 ? String.valueOf(highScore) : "0");
        tvPlayersCount.setText(playersCount > 0 ? String.valueOf(playersCount) : "0");

        // Recent matches (last 10)
        List<Match> allMatches = db.getAllMatches();
        recentMatches.clear();
        int limit = Math.min(allMatches.size(), 10);
        for (int i = 0; i < limit; i++) {
            recentMatches.add(allMatches.get(i));
        }
        recentMatchAdapter.notifyDataSetChanged();

        // Empty state
        tvNoMatches.setVisibility(recentMatches.isEmpty() ? View.VISIBLE : View.GONE);
        rvRecentMatches.setVisibility(recentMatches.isEmpty() ? View.GONE : View.VISIBLE);

        // Teams empty state
        List<Team> teams = db.getAllTeams();
        tvNoTeams.setVisibility(teams.isEmpty() ? View.VISIBLE : View.GONE);
        dashboardTeams.clear();
        dashboardTeams.addAll(teams);
        teamChipAdapter.notifyDataSetChanged();
    }
}