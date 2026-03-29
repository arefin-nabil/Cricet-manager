package com.test.crickethub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
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
import com.test.crickethub.adapter.MatchHistoryAdapter;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Match;

import java.util.ArrayList;
import java.util.List;

/**
 * MatchHistoryActivity.java
 * ==========================
 * Displays all completed matches in a scrollable list.
 * Features: search by team name, tap to view scorecard.
 */
public class MatchHistoryActivity extends AppCompatActivity {

    // ============================================================
    // Views
    // ============================================================
    private MaterialToolbar      toolbar;
    private TextInputEditText    etSearch;
    private RecyclerView         rvMatches;
    private TextView             tvHistoryCount;
    private View                 layoutEmptyState;
    private MaterialButton       btnStartFirst;

    // ============================================================
    // Data
    // ============================================================
    private CricketDbHelper      db;
    private MatchHistoryAdapter  adapter;
    private List<Match>          allMatches  = new ArrayList<>();
    private List<Match>          filteredMatches = new ArrayList<>();

    // ============================================================
    // Lifecycle
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = CricketDbHelper.getInstance(this);

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        loadMatches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMatches(); // Refresh if match just completed
    }

    // ============================================================
    // View Binding
    // ============================================================

    private void bindViews() {
        toolbar          = findViewById(R.id.toolbar_history);
        etSearch         = findViewById(R.id.et_search_history);
        rvMatches        = findViewById(R.id.rv_match_history);
        tvHistoryCount   = findViewById(R.id.tv_history_count);
        layoutEmptyState = findViewById(R.id.layout_empty_history);
        btnStartFirst    = findViewById(R.id.btn_start_first_match);
    }

    // ============================================================
    // Toolbar
    // ============================================================

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(item -> {

            return false;
        });
    }

    // ============================================================
    // RecyclerView
    // ============================================================

    private void setupRecyclerView() {
        adapter = new MatchHistoryAdapter(filteredMatches, match -> {
            Intent intent;
            if (Match.STATUS_LIVE.equals(match.getStatus())) {
                intent = new Intent(this, com.test.crickethub.activity.LiveScoringActivity.class);
            } else {
                intent = new Intent(this, com.test.crickethub.activity.ScorecardActivity.class);
            }
            intent.putExtra("match_id", match.getId());
            startActivity(intent);
        });
        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        rvMatches.setAdapter(adapter);
    }

    // ============================================================
    // Search / Filter
    // ============================================================

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMatches(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterMatches(String query) {
        filteredMatches.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredMatches.addAll(allMatches);
        } else {
            String lower = query.toLowerCase().trim();
            for (Match m : allMatches) {
                if ((m.getTeamAName() != null && m.getTeamAName().toLowerCase().contains(lower))
                 || (m.getTeamBName() != null && m.getTeamBName().toLowerCase().contains(lower))
                 || (m.getStatus() != null && m.getStatus().toLowerCase().contains(lower))
                 || (m.getDateFormatted() != null && m.getDateFormatted().toLowerCase().contains(lower))) {
                    filteredMatches.add(m);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateCountLabel();
        updateEmptyState();
    }

    // ============================================================
    // Data Loading
    // ============================================================

    private void loadMatches() {
        allMatches.clear();
        allMatches.addAll(db.getAllMatches()); // Shows live + completed

        // Apply current search
        String currentQuery = etSearch.getText() != null ? etSearch.getText().toString() : "";
        filterMatches(currentQuery);
    }

    // ============================================================
    // UI Helpers
    // ============================================================

    private void updateCountLabel() {
        tvHistoryCount.setText(filteredMatches.size() + " match" +
                (filteredMatches.size() != 1 ? "es" : ""));
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredMatches.isEmpty();
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMatches.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            TextView tvEmptyText = findViewById(R.id.tv_empty_history_text);
            if (allMatches.isEmpty()) {
                if(tvEmptyText != null) tvEmptyText.setText(R.string.no_history);
                btnStartFirst.setVisibility(View.VISIBLE);
                btnStartFirst.setOnClickListener(v -> {
                    startActivity(new Intent(this, CreateMatchActivity.class));
                });
            } else {
                if(tvEmptyText != null) tvEmptyText.setText("No selected matches found.");
                btnStartFirst.setVisibility(View.GONE);
            }
        }
    }

    // ============================================================
    // Sync (placeholder — extend for real API sync)
    // ============================================================

    private void syncMatches() {
        // TODO: Implement WorkManager-based sync to remote API
        Toast.makeText(this, "Sync coming soon! (Offline mode active)", Toast.LENGTH_SHORT).show();
    }
}
