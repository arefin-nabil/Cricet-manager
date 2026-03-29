package com.test.crickethub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.test.crickethub.R;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Match;
import com.test.crickethub.model.Team;

import java.util.List;

/**
 * CreateMatchActivity.java
 * =========================
 * Match setup screen:
 *   - Select Team A & Team B
 *   - Choose overs (quick chips + custom)
 *   - Configure toss (winner + election)
 *   - Start Match → navigate to LiveScoringActivity
 */
public class CreateMatchActivity extends AppCompatActivity {

    // ============================================================
    // Views
    // ============================================================
    private MaterialToolbar   toolbar;
    private MaterialCardView  cardTeamA, cardTeamB;
    private TextView          tvTeamASelected, tvTeamBSelected;
    private ChipGroup         chipGroupOvers;
    private Chip              chip5, chip10, chip20, chip15, chipCustom;
    private TextInputLayout   tilCustomOvers;
    private Spinner           spinnerTossWinner;
    private MaterialButton    btnRunToss;
    private TextView          tvTossResult;
    private com.google.android.material.materialswitch.MaterialSwitch switchRandomToss;
    private android.view.View layoutRandomToss, layoutManualToss;
    private MaterialButtonToggleGroup groupManualToss;
    private MaterialButton    btnManualTeamA, btnManualTeamB;
    private MaterialButtonToggleGroup toggleTossElection;
    private MaterialButton    btnStartMatch;

    private long              tossWinnerId = -1; // New field to store toss result

    // ============================================================
    // Data
    // ============================================================
    private CricketDbHelper db;
    private List<Team>      teams;
    private Team            selectedTeamA;
    private Team            selectedTeamB;
    private int             selectedOvers  = 20;    // default T20
    private String          tossElection   = "bat"; // default is bat

    // ============================================================
    // Lifecycle
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_match);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db    = CricketDbHelper.getInstance(this);
        teams = db.getAllTeams();

        bindViews();
        setupToolbar();
        setupTeamSelectors();
        setupOversChips();
        setupToss();
        setupStartButton();
    }

    // ============================================================
    // View Binding
    // ============================================================

    private void bindViews() {
        toolbar           = findViewById(R.id.toolbar_create_match);
        cardTeamA         = findViewById(R.id.card_team_a);
        cardTeamB         = findViewById(R.id.card_team_b);
        tvTeamASelected   = findViewById(R.id.tv_team_a_selected);
        tvTeamBSelected   = findViewById(R.id.tv_team_b_selected);
        chipGroupOvers    = findViewById(R.id.chip_group_overs);
        chip5             = findViewById(R.id.chip_5_overs);
        chip10            = findViewById(R.id.chip_10_overs);
        chip20            = findViewById(R.id.chip_20_overs);
        chip15            = findViewById(R.id.chip_15_overs);
        chipCustom        = findViewById(R.id.chip_custom_overs);
        tilCustomOvers    = findViewById(R.id.til_custom_overs);
        spinnerTossWinner = null; // Spinner removed from XML
        btnRunToss        = findViewById(R.id.btn_run_toss);
        tvTossResult      = findViewById(R.id.tv_toss_result);
        switchRandomToss  = findViewById(R.id.switch_random_toss);
        layoutRandomToss  = findViewById(R.id.layout_random_toss);
        layoutManualToss  = findViewById(R.id.layout_manual_toss);
        groupManualToss   = findViewById(R.id.group_manual_toss);
        btnManualTeamA    = findViewById(R.id.btn_manual_team_a);
        btnManualTeamB    = findViewById(R.id.btn_manual_team_b);
        toggleTossElection= findViewById(R.id.toggle_toss_election);
        btnStartMatch     = findViewById(R.id.btn_start_match);
    }

    // ============================================================
    // Toolbar
    // ============================================================

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    // ============================================================
    // Team Selectors
    // ============================================================

    private void setupTeamSelectors() {
        if (teams.isEmpty()) {
            Toast.makeText(this, "Please create teams first!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // No dialog needed for basic flow – tap card opens spinner-style dialog
        cardTeamA.setOnClickListener(v -> showTeamPicker(true));
        cardTeamB.setOnClickListener(v -> showTeamPicker(false));
    }

    private void showTeamPicker(boolean isTeamA) {
        // Build team name list for selection dialog
        String[] teamNames = new String[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            teamNames[i] = teams.get(i).getName();
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(isTeamA ? "Select Team A" : "Select Team B")
                .setItems(teamNames, (dialog, which) -> {
                    Team selected = teams.get(which);
                    if (isTeamA) {
                        selectedTeamA = selected;
                        tvTeamASelected.setText(selected.getName());
                        tvTeamASelected.setTextColor(getColor(R.color.text_primary));
                    } else {
                        selectedTeamB = selected;
                        tvTeamBSelected.setText(selected.getName());
                        tvTeamBSelected.setTextColor(getColor(R.color.text_primary));
                    }
                    // Refresh toss indicator and manual buttons
                    updateManualTossButtons();
                })
                .show();
    }

    private void updateManualTossButtons() {
        if (selectedTeamA != null) {
            btnManualTeamA.setText(selectedTeamA.getName());
        }
        if (selectedTeamB != null) {
            btnManualTeamB.setText(selectedTeamB.getName());
        }
    }

    // ============================================================
    // Overs Chips
    // ============================================================

    private void setupOversChips() {
        chip5.setOnCheckedChangeListener((btn, checked) -> { if (checked) { selectedOvers = 5; tilCustomOvers.setVisibility(View.GONE); } });
        chip10.setOnCheckedChangeListener((btn, checked) -> { if (checked) { selectedOvers = 10; tilCustomOvers.setVisibility(View.GONE); } });
        chip20.setOnCheckedChangeListener((btn, checked) -> { if (checked) { selectedOvers = 20; tilCustomOvers.setVisibility(View.GONE); } });
        chip15.setOnCheckedChangeListener((btn, checked) -> { if (checked) { selectedOvers = 15; tilCustomOvers.setVisibility(View.GONE); } });
        chipCustom.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                tilCustomOvers.setVisibility(View.VISIBLE);
            }
        });
    }

    // ============================================================
    // Toss
    // ============================================================

    private void setupToss() {
        switchRandomToss.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                layoutRandomToss.setVisibility(View.VISIBLE);
                layoutManualToss.setVisibility(View.GONE);
                tossWinnerId = -1; // Reset randomized result
            } else {
                layoutRandomToss.setVisibility(View.GONE);
                layoutManualToss.setVisibility(View.VISIBLE);
            }
        });

        btnRunToss.setOnClickListener(v -> {
            if (selectedTeamA == null || selectedTeamB == null) {
                Toast.makeText(this, "Select both teams first!", Toast.LENGTH_SHORT).show();
                return;
            }
            showTossDialog();
        });

        groupManualToss.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_manual_team_a) {
                    tossWinnerId = selectedTeamA.getId();
                } else if (checkedId == R.id.btn_manual_team_b) {
                    tossWinnerId = selectedTeamB.getId();
                }
            }
        });

        toggleTossElection.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                tossElection = (checkedId == R.id.btn_toss_bat) ? "bat" : "bowl";
            }
        });
    }

    private void showTossDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_toss_animation, null);
        // Note: LottieView can be added to dialog_toss_animation.xml later by the user.

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        // Simulate coin flip delay (2 seconds)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            int winnerIndex = new java.util.Random().nextInt(2);
            tossWinnerId = (winnerIndex == 0) ? selectedTeamA.getId() : selectedTeamB.getId();
            String winnerName = (winnerIndex == 0) ? selectedTeamA.getName() : selectedTeamB.getName();

            tvTossResult.setText("Toss Won by: " + winnerName);
            tvTossResult.setTextColor(getColor(R.color.green_primary));
            tvTossResult.setTypeface(null, android.graphics.Typeface.BOLD);

            dialog.dismiss();
            Toast.makeText(this, winnerName + " won the toss!", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    // ============================================================
    // Start Match
    // ============================================================

    private void setupStartButton() {
        btnStartMatch.setOnClickListener(v -> validateAndStartMatch());
    }

    private void validateAndStartMatch() {
        // Validate teams
        if (selectedTeamA == null || selectedTeamB == null) {
            Toast.makeText(this, getString(R.string.error_select_teams), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTeamA.getId() == selectedTeamB.getId()) {
            Toast.makeText(this, getString(R.string.error_same_teams), Toast.LENGTH_SHORT).show();
            return;
        }

        // Custom overs validation
        if (chipCustom.isChecked()) {
            String customText = tilCustomOvers.getEditText() != null
                    ? tilCustomOvers.getEditText().getText().toString() : "";
            if (customText.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_select_overs), Toast.LENGTH_SHORT).show();
                return;
            }
            selectedOvers = Integer.parseInt(customText);
            if (selectedOvers < 1 || selectedOvers > 50) {
                Toast.makeText(this, "Overs must be between 1 and 50", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (tossWinnerId == -1) {
            Toast.makeText(this, "Please run the toss first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Match object
        Match match = new Match();
        match.setTeamAId(selectedTeamA.getId());
        match.setTeamBId(selectedTeamB.getId());
        match.setTeamAName(selectedTeamA.getName());
        match.setTeamBName(selectedTeamB.getName());
        match.setTotalOvers(selectedOvers);
        match.setTossWinnerId(tossWinnerId);
        match.setTossElection(tossElection);
        match.setStatus(Match.STATUS_LIVE);

        // First innings batting team
        if (tossWinnerId == selectedTeamA.getId()) {
            if (tossElection.equals("bat")) {
                match.setBattingTeamId(selectedTeamA.getId());
                match.setBowlingTeamId(selectedTeamB.getId());
            } else {
                match.setBattingTeamId(selectedTeamB.getId());
                match.setBowlingTeamId(selectedTeamA.getId());
            }
        } else {
            if (tossElection.equals("bat")) {
                match.setBattingTeamId(selectedTeamB.getId());
                match.setBowlingTeamId(selectedTeamA.getId());
            } else {
                match.setBattingTeamId(selectedTeamA.getId());
                match.setBowlingTeamId(selectedTeamB.getId());
            }
        }

        long matchId = db.insertMatch(match);
        if (matchId != -1) {
            Intent intent = new Intent(this, LiveScoringActivity.class);
            intent.putExtra("match_id", matchId);
            startActivity(intent);
            finish();
        }
    }
}
