package com.test.crickethub.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.test.crickethub.R;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.BallEvent;
import com.test.crickethub.model.Match;
import com.test.crickethub.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * LiveScoringActivity.java
 * =========================
 * THE CORE SCREEN — Real-time ball-by-ball cricket scoring.
 *
 * Responsibilities:
 * 1. Display live scoreboard (score, overs, CRR, partnership)
 * 2. Show current batsmen stats (striker & non-striker)
 * 3. Show current bowler stats
 * 4. Display ball-by-ball chips for the current over
 * 5. Handle all scoring actions:
 * - Runs (0,1,2,3,4,6)
 * - Extras (Wide, No Ball, Bye, Leg Bye)
 * - Wicket (with dismissal dialog)
 * - Undo last ball
 * - Next Over (change bowler)
 * 6. Auto-detect innings change and match completion
 * 7. Persist every ball event to SQLite
 */
public class LiveScoringActivity extends AppCompatActivity {

    // ============================================================
    // Views — Scoreboard
    // ============================================================
    private MaterialToolbar toolbar;
    private TextView tvMainScore, tvOvers, tvCRR, tvRRR, tvPartnership;
    private TextView tvTargetLabel, tvLiveIndicator;

    // Views — Batsmen
    private TextView tvStrikerName, tvStrikerRuns, tvStrikerBalls, tvStrikerSR;
    private TextView tvNonStrikerName, tvNonStrikerRuns, tvNonStrikerBalls, tvNonStrikerSR;

    // Views — Bowler
    private TextView tvBowlerName;
    private TextView tvBowlerOvers;
    private TextView tvBowlerMaidens;
    private TextView tvBowlerRuns;
    private TextView tvBowlerWickets;
    private TextView tvBowlerEco;

    // Views — This Over ball chips
    private LinearLayout layoutBallChips;
    private TextView tvNoBallsYet;

    // Views — Action Buttons
    private TextView btnRun0, btnRun1, btnRun2, btnRun3, btnRun4, btnRun6;
    private MaterialButton btnWide, btnNoBall, btnBye, btnLegBye;
    private MaterialButton btnWicket, btnUndo, btnNextOver;

    // ============================================================
    // Data
    // ============================================================
    private CricketDbHelper db;
    private Match match;
    private long matchId;

    // Live playing state
    private Player striker; // current on-strike batsman
    private Player nonStriker; // non-striking batsman
    private Player currentBowler; // current bowler

    // Batting team roster (playing XI)
    private List<Player> battingTeamPlayers = new ArrayList<>();
    private List<Player> bowlingTeamPlayers = new ArrayList<>();

    // Index of next batsman to come in (0 and 1 are the openers)
    private int nextBatsmanIndex = 2;

    // Ball-by-ball log for current over (for "this over" strip)
    private List<BallEvent> currentOverBalls = new ArrayList<>();

    // Partnership tracking
    private int partnershipRuns = 0;
    private int partnershipBalls = 0;

    // ============================================================
    // Lifecycle
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_scoring);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Keep screen alive during active scoring
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        db = CricketDbHelper.getInstance(this);
        matchId = getIntent().getLongExtra("match_id", -1);

        if (matchId == -1) {
            Toast.makeText(this, "Invalid match!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        match = db.getMatchById(matchId);
        if (match == null) {
            Toast.makeText(this, "Match not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupToolbar();
        loadTeams();
        setupRunButtons();
        setupExtrasButtons();
        setupWicketButton();
        setupUndoButton();
        setupNextOverButton();
        refreshScoreboardUI();
    }

    // ============================================================
    // View Binding
    // ============================================================

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar_live);

        // Scoreboard
        tvMainScore = findViewById(R.id.tv_main_score);
        tvOvers = findViewById(R.id.tv_overs);
        tvCRR = findViewById(R.id.tv_crr_value);
        tvRRR = findViewById(R.id.tv_rrr_value);
        tvPartnership = findViewById(R.id.tv_partnership);
        tvTargetLabel = findViewById(R.id.tv_target_label);
        tvLiveIndicator = findViewById(R.id.tv_live_indicator);

        // Batsmen
        tvStrikerName = findViewById(R.id.tv_striker_name);
        tvStrikerRuns = findViewById(R.id.tv_striker_runs);
        tvStrikerBalls = findViewById(R.id.tv_striker_balls);
        tvStrikerSR = findViewById(R.id.tv_striker_sr);
        tvNonStrikerName = findViewById(R.id.tv_non_striker_name);
        tvNonStrikerRuns = findViewById(R.id.tv_non_striker_runs);
        tvNonStrikerBalls = findViewById(R.id.tv_non_striker_balls);
        tvNonStrikerSR = findViewById(R.id.tv_non_striker_sr);

        // Bowler
        tvBowlerName = findViewById(R.id.tv_bowler_name);
        tvBowlerOvers = findViewById(R.id.tv_bowler_overs);
        tvBowlerMaidens = findViewById(R.id.tv_bowler_maidens);
        tvBowlerRuns = findViewById(R.id.tv_bowler_runs);
        tvBowlerWickets = findViewById(R.id.tv_bowler_wickets);
        tvBowlerEco = findViewById(R.id.tv_bowler_eco);

        // Ball chips
        layoutBallChips = findViewById(R.id.layout_ball_chips);
        tvNoBallsYet = findViewById(R.id.tv_no_balls_yet);

        // Run buttons (TextViews acting as buttons)
        btnRun0 = findViewById(R.id.btn_run_0);
        btnRun1 = findViewById(R.id.btn_run_1);
        btnRun2 = findViewById(R.id.btn_run_2);
        btnRun3 = findViewById(R.id.btn_run_3);
        btnRun4 = findViewById(R.id.btn_run_4);
        btnRun6 = findViewById(R.id.btn_run_6);

        // Extras
        btnWide = findViewById(R.id.btn_wide);
        btnNoBall = findViewById(R.id.btn_no_ball);
        btnBye = findViewById(R.id.btn_bye);
        btnLegBye = findViewById(R.id.btn_leg_bye);

        // Action
        btnWicket = findViewById(R.id.btn_wicket);
        btnUndo = findViewById(R.id.btn_undo);
        btnNextOver = findViewById(R.id.btn_next_over);


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showPauseDialog();
            }
        });

    }

    // ============================================================
    // Toolbar
    // ============================================================

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // Using custom view in XML
            getSupportActionBar().setSubtitle(getInningsLabel() + " • " +
                    match.getTeamAName() + " vs " + match.getTeamBName());
        }
        toolbar.setNavigationOnClickListener(v -> showPauseDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_live_scoring, menu);
        
        // Tint abandon icon red
        MenuItem abandonItem = menu.findItem(R.id.action_abandon_match);
        if (abandonItem != null && abandonItem.getIcon() != null) {
            abandonItem.getIcon().setTint(ContextCompat.getColor(this, R.color.color_wicket));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_abandon_match) {
            confirmAbandonMatch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmAbandonMatch() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Abandon Match?")
                .setMessage("Are you sure you want to completely abandon this match? Data will be lost.")
                .setPositiveButton("Abandon", (dialog, which) -> {
                    match.setStatus(Match.STATUS_COMPLETED);
                    match.setResult("Match Abandoned");
                    db.updateMatch(match);
                    Toast.makeText(this, "Match Abandoned", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ============================================================
    // Team / Player Loading
    // ============================================================

    private void loadTeams() {
        battingTeamPlayers = db.getPlayersForTeam(match.getBattingTeamId());
        bowlingTeamPlayers = db.getPlayersForTeam(match.getBowlingTeamId());

        // For simplicity: openers and first bowler are the first players in the list.
        // In a full implementation, these would be selected by the scorer.
        if (battingTeamPlayers.size() >= 2) {
            striker = battingTeamPlayers.get(0);
            nonStriker = battingTeamPlayers.get(1);
        } else if (battingTeamPlayers.size() == 1) {
            striker = battingTeamPlayers.get(0);
            nonStriker = createPlaceholderPlayer("Batsman 2");
        } else {
            striker = createPlaceholderPlayer("Batsman 1");
            nonStriker = createPlaceholderPlayer("Batsman 2");
        }

        if (!bowlingTeamPlayers.isEmpty()) {
            currentBowler = bowlingTeamPlayers.get(0);
        } else {
            currentBowler = createPlaceholderPlayer("Bowler");
        }

        if (match.getCurrentBalls() == 0 && match.getCurrentOvers() == 0) {
            if ((match.getInningsNumber() == 1 && match.getInnings1Score() == 0) ||
                    (match.getInningsNumber() == 2 && match.getInnings2Score() == 0)) {
                showInitialSelectionDialogs();
            }
        }
    }

    private void showInitialSelectionDialogs() {
        if (battingTeamPlayers.isEmpty() || bowlingTeamPlayers.isEmpty())
            return;

        // 1. Select Striker
        String[] allBatNames = new String[battingTeamPlayers.size()];
        for (int i = 0; i < battingTeamPlayers.size(); i++)
            allBatNames[i] = battingTeamPlayers.get(i).getName();

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Select Striker")
                .setItems(allBatNames, (d1, w1) -> {
                    striker = battingTeamPlayers.get(w1);

                    // 2. Select Non-Striker (Filter out striker)
                    List<Player> filteredBatsmen = new ArrayList<>();
                    for (Player p : battingTeamPlayers) {
                        if (p.getId() != striker.getId()) {
                            filteredBatsmen.add(p);
                        }
                    }

                    String[] filteredBatNames = new String[filteredBatsmen.size()];
                    for (int i = 0; i < filteredBatsmen.size(); i++)
                        filteredBatNames[i] = filteredBatsmen.get(i).getName();

                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                            .setTitle("Select Non-Striker")
                            .setItems(filteredBatNames, (d2, w2) -> {
                                nonStriker = filteredBatsmen.get(w2);

                                // 3. Select Opening Bowler
                                String[] bowlNames = new String[bowlingTeamPlayers.size()];
                                for (int i = 0; i < bowlingTeamPlayers.size(); i++)
                                    bowlNames[i] = bowlingTeamPlayers.get(i).getName();

                                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                                        .setTitle("Select Opening Bowler")
                                        .setItems(bowlNames, (d3, w3) -> {
                                            currentBowler = bowlingTeamPlayers.get(w3);
                                            refreshScoreboardUI();
                                        })
                                        .setCancelable(false)
                                        .show();
                            })
                            .setCancelable(false)
                            .show();
                })
                .setCancelable(false)
                .show();
    }

    private Player createPlaceholderPlayer(String name) {
        Player p = new Player();
        p.setName(name);
        return p;
    }

    // ============================================================
    // RUN BUTTONS SETUP
    // ============================================================

    private void setupRunButtons() {
        btnRun0.setOnClickListener(v -> recordRuns(0));
        btnRun1.setOnClickListener(v -> recordRuns(1));
        btnRun2.setOnClickListener(v -> recordRuns(2));
        btnRun3.setOnClickListener(v -> recordRuns(3));
        btnRun4.setOnClickListener(v -> recordRuns(4));
        btnRun6.setOnClickListener(v -> recordRuns(6));
    }

    // ============================================================
    // EXTRAS BUTTONS SETUP
    // ============================================================

    private void setupExtrasButtons() {
        btnWide.setOnClickListener(v -> recordExtra(BallEvent.TYPE_WIDE, 0));
        btnNoBall.setOnClickListener(v -> recordExtra(BallEvent.TYPE_NO_BALL, 0));
        btnBye.setOnClickListener(v -> recordBye(BallEvent.TYPE_BYE));
        btnLegBye.setOnClickListener(v -> recordBye(BallEvent.TYPE_LEG_BYE));
    }

    // ============================================================
    // WICKET BUTTON SETUP
    // ============================================================

    private void setupWicketButton() {
        btnWicket.setOnClickListener(v -> showWicketDialog());
    }

    // ============================================================
    // UNDO BUTTON SETUP
    // ============================================================

    private void setupUndoButton() {
        btnUndo.setOnClickListener(v -> undoLastBall());
    }

    // ============================================================
    // NEXT OVER BUTTON
    // ============================================================

    private void setupNextOverButton() {
        btnNextOver.setOnClickListener(v -> {
            // Allow manual over completion (for edge cases)
            if (currentOverBalls.isEmpty()) {
                Toast.makeText(this, "No balls bowled yet", Toast.LENGTH_SHORT).show();
                return;
            }
            endOverAndChangeBowler();
        });
    }

    // ============================================================
    // SCORING LOGIC — RUNS
    // ============================================================

    /**
     * Record a normal delivery with run(s).
     * Updates: batsman stats, bowler stats, match score, overs count.
     */
    private void recordRuns(int runs) {
        if (striker == null || currentBowler == null)
            return;

        // Update batsman
        striker.addRuns(runs);
        striker.addBall();
        if (runs == 4)
            striker.incrementFours();
        if (runs == 6)
            striker.incrementSixes();

        // Update bowler
        currentBowler.addBallBowled();
        currentBowler.addRunsConceded(runs);

        // Update match score
        addRunsToMatch(runs);

        // Increment ball count in over
        advanceBall(true);

        // Partnership
        partnershipRuns += runs;
        partnershipBalls++;

        // Rotate strike on odd runs
        if (runs % 2 != 0) {
            swapStriker();
        }

        // Create and log ball event
        BallEvent ball = BallEvent.runs(runs, striker.getId(), currentBowler.getId());
        ball.setMatchId(matchId);
        ball.setInningsNumber(match.getInningsNumber());
        ball.setOverNumber(match.getCurrentOvers());
        db.insertBallEvent(ball);

        // Add chip to over strip
        addBallChip(ball);
        currentOverBalls.add(ball);

        // Save match state
        db.updateMatch(match);
        refreshScoreboardUI();

        boolean isInningsOver = checkInningsCompletion();
        if (!isInningsOver && match.getCurrentBalls() == 0) {
            endOverAndChangeBowler();
        }
    }

    // ============================================================
    // SCORING LOGIC — EXTRAS
    // ============================================================

    /** Record wide or no-ball (does not consume a legal delivery). */
    private void recordExtra(String type, int extraRuns) {
        int penalty = 1; // wide or no-ball = 1 penalty run
        addRunsToMatch(penalty + extraRuns);
        currentBowler.addRunsConceded(penalty + extraRuns);

        // Wide/NB: does NOT advance the legal ball count
        if (type.equals(BallEvent.TYPE_NO_BALL)) {
            currentBowler.addBallBowled(); // no-ball counts for bowler but not over count
        }

        BallEvent ball = type.equals(BallEvent.TYPE_WIDE)
                ? BallEvent.wide(extraRuns, currentBowler.getId())
                : BallEvent.noBall(extraRuns, striker != null ? striker.getId() : 0, currentBowler.getId());
        ball.setMatchId(matchId);
        ball.setInningsNumber(match.getInningsNumber());
        ball.setOverNumber(match.getCurrentOvers());
        db.insertBallEvent(ball);
        addBallChip(ball);
        currentOverBalls.add(ball);

        db.updateMatch(match);
        refreshScoreboardUI();
    }

    /** Record bye or leg-bye (regular ball consumed, runs credited to extras). */
    private void recordBye(String type) {
        // Simple implementation: prompt for runs
        showRunInputDialog("How many " + (type.equals(BallEvent.TYPE_BYE) ? "Byes" : "Leg Byes") + "?",
                byeRuns -> {
                    striker.addBall(); // ball counts for batsman
                    currentBowler.addBallBowled();
                    currentBowler.addRunsConceded(byeRuns);
                    addRunsToMatch(byeRuns);
                    advanceBall(true);
                    partnershipBalls++;

                    if (byeRuns % 2 != 0)
                        swapStriker();

                    BallEvent ball = new BallEvent();
                    ball.setDeliveryType(type);
                    ball.setExtras(byeRuns);
                    ball.setBowlerId(currentBowler.getId());
                    ball.setMatchId(matchId);
                    ball.setInningsNumber(match.getInningsNumber());
                    ball.setOverNumber(match.getCurrentOvers());
                    ball.setDisplayLabel(type.equals(BallEvent.TYPE_BYE)
                            ? "B+" + byeRuns
                            : "LB+" + byeRuns);
                    db.insertBallEvent(ball);
                    addBallChip(ball);
                    currentOverBalls.add(ball);

                    db.updateMatch(match);
                    refreshScoreboardUI();

                    if (!checkInningsCompletion()) {
                        if (match.getCurrentBalls() == 0) {
                            endOverAndChangeBowler();
                        }
                    }
                });
    }

    // ============================================================
    // WICKET DIALOG
    // ============================================================

    private void showWicketDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_wicket, null);

        // Batsman out spinner
        Spinner spinnerBatsmanOut = dialogView.findViewById(R.id.spinner_batsman_out);
        List<String> batNames = new ArrayList<>();
        if (striker != null)
            batNames.add(striker.getName() + " (on strike)");
        if (nonStriker != null)
            batNames.add(nonStriker.getName() + " (non-striker)");
        ArrayAdapter<String> batAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, batNames);
        batAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBatsmanOut.setAdapter(batAdapter);

        // Dismissal chips
        ChipGroup chipGroupDismissal = dialogView.findViewById(R.id.chip_group_dismissal);

        // Next batsman
        Spinner spinnerNextBatsman = dialogView.findViewById(R.id.spinner_next_batsman);
        List<String> upcoming = new ArrayList<>();
        for (int i = nextBatsmanIndex; i < battingTeamPlayers.size(); i++) {
            upcoming.add(battingTeamPlayers.get(i).getName());
        }
        if (upcoming.isEmpty())
            upcoming.add("All Out");
        ArrayAdapter<String> nextAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, upcoming);
        nextAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNextBatsman.setAdapter(nextAdapter);

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(
                this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btn_dismiss_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_dismiss_confirm).setOnClickListener(v -> {
            // Get selected dismissal type
            int checkedChipId = chipGroupDismissal.getCheckedChipId();
            String dismissalType = getDismissalTypeFromChip(checkedChipId);

            // Determine who got out
            int outPos = spinnerBatsmanOut.getSelectedItemPosition();
            Player dismissed = (outPos == 0) ? striker : nonStriker;

            // Record wicket
            processWicket(dismissed, dismissalType);

            // Bring in next batsman
            int nextPos = spinnerNextBatsman.getSelectedItemPosition();
            if (nextBatsmanIndex + nextPos < battingTeamPlayers.size()) {
                Player nextBat = battingTeamPlayers.get(nextBatsmanIndex + nextPos);
                if (dismissed == striker) {
                    striker = nextBat;
                } else {
                    nonStriker = nextBat;
                }
                nextBatsmanIndex++;
            }

            // Reset partnership
            partnershipRuns = 0;
            partnershipBalls = 0;

            dialog.dismiss();
            refreshScoreboardUI();
        });

        dialog.show();
    }

    private String getDismissalTypeFromChip(int chipId) {
        if (chipId == R.id.chip_caught)
            return BallEvent.DIS_CAUGHT;
        if (chipId == R.id.chip_lbw)
            return BallEvent.DIS_LBW;
        if (chipId == R.id.chip_runout)
            return BallEvent.DIS_RUN_OUT;
        if (chipId == R.id.chip_stumped)
            return BallEvent.DIS_STUMPED;
        if (chipId == R.id.chip_hitwicket)
            return BallEvent.DIS_HIT_WKT;
        return BallEvent.DIS_BOWLED;
    }

    private void processWicket(Player dismissed, String dismissalType) {
        // Mark batsman as out
        if (dismissed != null) {
            dismissed.setOut(true);
            dismissed.setHowOut(dismissalType + " b " + currentBowler.getName());
        }

        // Credit bowler with wicket (except run out)
        if (!dismissalType.equals(BallEvent.DIS_RUN_OUT)) {
            currentBowler.incrementWickets();
        }

        // Advance over ball and update match wickets
        striker.addBall();
        currentBowler.addBallBowled();
        advanceBall(true);
        partnershipBalls++;

        if (match.getInningsNumber() == 1) {
            match.incrementInnings1Wickets();
        } else {
            match.incrementInnings2Wickets();
        }

        // Record ball event
        BallEvent ball = BallEvent.wicket(
                dismissalType,
                dismissed != null ? dismissed.getId() : 0,
                currentBowler.getId(),
                0 // fielder – simplified
        );
        ball.setMatchId(matchId);
        ball.setInningsNumber(match.getInningsNumber());
        ball.setOverNumber(match.getCurrentOvers());
        ball.setBallInOver(match.getCurrentBalls());
        db.insertBallEvent(ball);
        addBallChip(ball);
        currentOverBalls.add(ball);

        db.updateMatch(match);

        boolean isInningsOver = checkInningsCompletion();
        if (!isInningsOver && match.getCurrentBalls() == 0) {
            endOverAndChangeBowler();
        }
    }

    // ============================================================
    // UNDO LAST BALL
    // ============================================================

    private void undoLastBall() {
        if (currentOverBalls.isEmpty()) {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
            return;
        }

        BallEvent last = currentOverBalls.remove(currentOverBalls.size() - 1);
        db.deleteLastBallEvent(matchId, match.getInningsNumber());

        // Reverse score changes (simplified)
        int totalToReverse = last.getRunsScored() + last.getExtras();
        addRunsToMatch(-totalToReverse);

        if (last.isWicket()) {
            if (match.getInningsNumber() == 1)
                match.setInnings1Wickets(Math.max(0, match.getInnings1Wickets() - 1));
            else
                match.setInnings2Wickets(Math.max(0, match.getInnings2Wickets() - 1));
        }

        if (last.isLegalDelivery()) {
            // Reverse ball count
            if (match.getInningsNumber() == 1) {
                if (match.getInnings1Balls() > 0)
                    match.setInnings1Balls(match.getInnings1Balls() - 1);
                else {
                    match.setInnings1Overs(Math.max(0, match.getInnings1Overs() - 1));
                    match.setInnings1Balls(5);
                }
            } else {
                if (match.getInnings2Balls() > 0)
                    match.setInnings2Balls(match.getInnings2Balls() - 1);
                else {
                    match.setInnings2Overs(Math.max(0, match.getInnings2Overs() - 1));
                    match.setInnings2Balls(5);
                }
            }
        }

        db.updateMatch(match);
        rebuildBallChips();
        refreshScoreboardUI();

        Toast.makeText(this, "Last ball undone", Toast.LENGTH_SHORT).show();
    }

    // ============================================================
    // OVER MANAGEMENT
    // ============================================================

    private void endOverAndChangeBowler() {
        // Swap striker positions at end of over
        swapStriker();

        // Clear ball chips
        currentOverBalls.clear();
        rebuildBallChips();

        // Show bowler change dialog
        showBowlerChangeDialog();
    }

    private void showBowlerChangeDialog() {
        String[] bowlerNames = new String[bowlingTeamPlayers.size()];
        for (int i = 0; i < bowlingTeamPlayers.size(); i++) {
            bowlerNames[i] = bowlingTeamPlayers.get(i).getName();
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Select Next Bowler")
                .setItems(bowlerNames, (dialog, which) -> {
                    currentBowler = bowlingTeamPlayers.get(which);
                    // Reset bowler stats for UI display if needed (but usually they persist)
                    refreshScoreboardUI();
                })
                .setCancelable(false)
                .show();
    }

    // ============================================================
    // INNINGS / MATCH COMPLETION
    // ============================================================

    public boolean isInningsComplete(int teamSize) {
        int maxWickets = Math.max(1, teamSize - 1);
        return match.getCurrentWickets() >= maxWickets || (match.getCurrentOvers() >= match.getTotalOvers() && match.getCurrentBalls() == 0);
    }

    private boolean checkInningsCompletion() {
        boolean oversComplete = match.getCurrentOvers() >= match.getTotalOvers()
                && match.getCurrentBalls() == 0;
        int maxWickets = Math.max(1, battingTeamPlayers.size() - 1);
        boolean allOut = match.getCurrentWickets() >= maxWickets;

        if (oversComplete || allOut) {
            if (match.getInningsNumber() == 1) {
                startSecondInnings();
            } else {
                completeMatch();
            }
            return true;
        }

        // 2nd innings: check if target chased
        if (match.getInningsNumber() == 2) {
            if (match.getInnings2Score() >= match.getTarget()) {
                completeMatch();
                return true;
            }
        }
        return false;
    }

    private void startSecondInnings() {
        match.setInningsNumber(2);

        // Swap teams
        long tempTeam = match.getBattingTeamId();
        match.setBattingTeamId(match.getBowlingTeamId());
        match.setBowlingTeamId(tempTeam);

        db.updateMatch(match);

        // Reload teams
        battingTeamPlayers = db.getPlayersForTeam(match.getBattingTeamId());
        bowlingTeamPlayers = db.getPlayersForTeam(match.getBowlingTeamId());

        // Reset partnership
        partnershipRuns = 0;
        partnershipBalls = 0;
        currentOverBalls.clear();

        // Refresh Subtitle
        toolbar.setSubtitle(getInningsLabel() + " • " +
                match.getTeamAName() + " vs " + match.getTeamBName());

        // SHOW MANDATORY TRANSITION DIALOG
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Innings Break!")
                .setMessage("1st Innings Over.\nTarget: " + match.getTarget() + " runs")
                .setCancelable(false)
                .setPositiveButton("Start 2nd Innings", (d, w) -> {
                    // Reset openers now that user clicked start
                    nextBatsmanIndex = 2;
                    if (battingTeamPlayers.size() >= 2) {
                        striker = battingTeamPlayers.get(0);
                        nonStriker = battingTeamPlayers.get(1);
                    }
                    if (!bowlingTeamPlayers.isEmpty()) {
                        currentBowler = bowlingTeamPlayers.get(0);
                    }

                    tvTargetLabel.setVisibility(View.VISIBLE);
                    tvTargetLabel.setText("Target: " + match.getTarget());

                    rebuildBallChips();
                    refreshScoreboardUI();
                    showInitialSelectionDialogs();
                })
                .show();
    }

    private void completeMatch() {
        match.setStatus(Match.STATUS_COMPLETED);

        // Determine result
        String result;
        if (match.getInnings2Score() > match.getInnings1Score()) {
            // Team size might have changed or be different, use current batting team (Team B)
            int teamSize = battingTeamPlayers.size();
            int maxWickets = Math.max(1, teamSize - 1);
            int wicketsLeft = maxWickets - match.getInnings2Wickets();
            result = match.getTeamBName() + " won by " + wicketsLeft + " wickets";
            match.setWinnerId(match.getTeamBId());
        } else if (match.getInnings1Score() > match.getInnings2Score()) {
            int runMargin = match.getInnings1Score() - match.getInnings2Score();
            result = match.getTeamAName() + " won by " + runMargin + " runs";
            match.setWinnerId(match.getTeamAId());
        } else {
            result = "Match Tied!";
        }

        match.setResult(result);
        db.updateMatch(match);

        // Show result dialog
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Match Complete!")
                .setMessage(result)
                .setPositiveButton("View Scorecard", (d, w) -> {
                    Intent intent = new Intent(this, ScorecardActivity.class);
                    intent.putExtra("match_id", matchId);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Go Home", (d, w) -> {
                    finish(); // Back to MainActivity
                })
                .setCancelable(false)
                .show();
    }

    // ============================================================
    // HELPERS — Score / Ball Tracking
    // ============================================================

    private void addRunsToMatch(int runs) {
        if (match.getInningsNumber() == 1) {
            match.addInnings1Score(runs);
        } else {
            match.addInnings2Score(runs);
        }
    }

    /**
     * Advance the legal ball counter. completedBall=true for actual legal
     * deliveries.
     */
    private void advanceBall(boolean legalDelivery) {
        if (!legalDelivery)
            return;

        if (match.getInningsNumber() == 1) {
            int balls = match.getInnings1Balls() + 1;
            if (balls >= 6) {
                match.setInnings1Balls(0);
                match.setInnings1Overs(match.getInnings1Overs() + 1);
            } else {
                match.setInnings1Balls(balls);
            }
        } else {
            int balls = match.getInnings2Balls() + 1;
            if (balls >= 6) {
                match.setInnings2Balls(0);
                match.setInnings2Overs(match.getInnings2Overs() + 1);
            } else {
                match.setInnings2Balls(balls);
            }
        }
    }

    private void swapStriker() {
        Player temp = striker;
        striker = nonStriker;
        nonStriker = temp;
    }

    private String getInningsLabel() {
        return match.getInningsNumber() == 1 ? "1st Innings" : "2nd Innings";
    }

    // ============================================================
    // BALL CHIP UI
    // ============================================================

    /** Add a colored ball chip to the "This Over" horizontal strip. */
    private void addBallChip(BallEvent ball) {
        tvNoBallsYet.setVisibility(View.GONE);

        TextView chip = new TextView(this);
        int size = (int) getResources().getDimension(R.dimen.ball_chip_size);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMarginEnd((int) getResources().getDimension(R.dimen.spacing_xs));
        chip.setLayoutParams(params);
        chip.setGravity(Gravity.CENTER);
        chip.setText(ball.getDisplayLabel());
        chip.setTextSize(getResources().getDimension(R.dimen.ball_chip_text) /
                getResources().getDisplayMetrics().scaledDensity);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setTextColor(getColor(R.color.text_on_green));

        // Background color by type
        if (ball.isWicket()) {
            chip.setBackgroundResource(R.drawable.bg_ball_wicket);
        } else if (ball.getRunsScored() == 4 || ball.getRunsScored() == 6) {
            chip.setBackgroundResource(R.drawable.bg_ball_boundary);
        } else if (ball.getDeliveryType().equals(BallEvent.TYPE_WIDE)
                || ball.getDeliveryType().equals(BallEvent.TYPE_NO_BALL)) {
            chip.setBackgroundResource(R.drawable.bg_ball_wide);
            chip.setTextColor(getColor(android.R.color.black));
        } else if (ball.getRunsScored() == 0) {
            chip.setBackgroundResource(R.drawable.bg_ball_dot);
        } else {
            chip.setBackgroundResource(R.drawable.bg_ball_dot);
        }

        layoutBallChips.addView(chip);
    }

    /** Rebuild all chips from current over list (after undo). */
    private void rebuildBallChips() {
        layoutBallChips.removeAllViews();
        if (currentOverBalls.isEmpty()) {
            tvNoBallsYet.setVisibility(View.VISIBLE);
        } else {
            tvNoBallsYet.setVisibility(View.GONE);
            for (BallEvent ball : currentOverBalls) {
                addBallChip(ball);
            }
        }
    }

    // ============================================================
    // SCOREBOARD UI REFRESH
    // ============================================================

    /** Refreshes all UI elements from current match & player state. */
    private void refreshScoreboardUI() {
        // Main score
        tvMainScore.setText(match.getCurrentScore() + "/" + match.getCurrentWickets());
        tvOvers.setText(match.getOversFormatted() + " / " + match.getTotalOvers() + " Overs");

        // Run rates
        tvCRR.setText(String.format("%.2f", match.getCurrentRunRate()));

        if (match.getInningsNumber() == 2) {
            tvRRR.setText(String.format("%.2f", match.getRequiredRunRate()));
        } else {
            tvRRR.setText("—");
        }

        // Partnership
        tvPartnership.setText(partnershipRuns + "(" + partnershipBalls + ")");

        // Striker
        if (striker != null) {
            tvStrikerName.setText(striker.getName());
            tvStrikerRuns.setText(String.valueOf(striker.getRuns()));
            tvStrikerBalls.setText(String.valueOf(striker.getBalls()));
            tvStrikerSR.setText(String.format("SR: %.1f", striker.getStrikeRate()));
        }

        // Non-striker
        if (nonStriker != null) {
            tvNonStrikerName.setText(nonStriker.getName());
            tvNonStrikerRuns.setText(String.valueOf(nonStriker.getRuns()));
            tvNonStrikerBalls.setText(String.valueOf(nonStriker.getBalls()));
            tvNonStrikerSR.setText(String.format("SR: %.1f", nonStriker.getStrikeRate()));
        }

        // Bowler
        if (currentBowler != null) {
            tvBowlerName.setText(currentBowler.getName());
            tvBowlerOvers.setText(currentBowler.getOversBowledFormatted());
            tvBowlerMaidens.setText(String.valueOf(currentBowler.getMaidens()));
            tvBowlerRuns.setText(String.valueOf(currentBowler.getRunsConceded()));
            tvBowlerWickets.setText(String.valueOf(currentBowler.getWickets()));
            tvBowlerEco.setText(String.format("%.2f", currentBowler.getEconomyRate()));
        }

        // Target (2nd innings only)
        if (match.getInningsNumber() == 2) {
            tvTargetLabel.setVisibility(View.VISIBLE);
            tvTargetLabel.setText("Target: " + match.getTarget());
        }
    }

    // ============================================================
    // PAUSE DIALOG
    // ============================================================

    private void showPauseDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Pause Match")
                .setMessage("What would you like to do?")
                .setPositiveButton("Resume", null)
                .setNeutralButton("View Scorecard", (d, w) -> {
                    Intent intent = new Intent(this, ScorecardActivity.class);
                    intent.putExtra("match_id", matchId);
                    startActivity(intent);
                })
                .setNegativeButton("Save & Exit", (d, w) -> {
                    db.updateMatch(match);
                    finish();
                })
                .show();
    }

    // ============================================================
    // HELPER — Simple run input dialog (for byes)
    // ============================================================

    interface RunInputCallback {
        void onRuns(int runs);
    }

    private void showRunInputDialog(String title, RunInputCallback callback) {
        String[] options = { "0", "1", "2", "3", "4", "5", "6" };
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setItems(options, (dialog, which) -> callback.onRuns(which))
                .show();
    }
}
