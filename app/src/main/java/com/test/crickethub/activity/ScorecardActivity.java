package com.test.crickethub.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.test.crickethub.R;
import com.test.crickethub.adapter.BatsmanAdapter;
import com.test.crickethub.adapter.BowlerAdapter;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Match;
import com.test.crickethub.model.Player;
import com.test.crickethub.model.BallEvent;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ScorecardActivity.java
 * =======================
 * Displays the full match scorecard with:
 * - Summary bar (score, overs, result)
 * - Innings switcher (1st / 2nd)
 * - Tab layout: Batting | Bowling | Fall of Wickets
 */
public class ScorecardActivity extends AppCompatActivity {

    // ============================================================
    // Views
    // ============================================================
    private MaterialToolbar toolbar;
    private TextView tvBattingTeam, tvScore, tvOvers, tvResult, tvInningsLabel;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MaterialButtonToggleGroup toggleInnings;

    // ============================================================
    // Data
    // ============================================================
    private CricketDbHelper db;
    private Match match;
    private long matchId;

    // Live player stats (loaded from DB / passed from LiveScoringActivity)
    private List<Player> innings1Batsmen = new ArrayList<>();
    private List<Player> innings1Bowlers = new ArrayList<>();
    private List<Player> innings2Batsmen = new ArrayList<>();
    private List<Player> innings2Bowlers = new ArrayList<>();

    private List<FowEvent> innings1Fow = new ArrayList<>();
    private List<FowEvent> innings2Fow = new ArrayList<>();

    private int displayInnings = 1; // which innings to show

    // ============================================================
    // Lifecycle
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = CricketDbHelper.getInstance(this);
        matchId = getIntent().getLongExtra("match_id", -1);
        match = matchId != -1 ? db.getMatchById(matchId) : null;

        bindViews();
        setupToolbar();

        if (match != null) {
            loadScorecardData();
            populateSummaryBar();
            setupViewPager();
            setupInningsSwitcher();
        }
    }

    // ============================================================
    // View Binding
    // ============================================================

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar_scorecard);
        tvBattingTeam = findViewById(R.id.tv_sc_batting_team);
        tvScore = findViewById(R.id.tv_sc_score);
        tvOvers = findViewById(R.id.tv_sc_overs);
        tvResult = findViewById(R.id.tv_sc_result);
        tvInningsLabel = findViewById(R.id.tv_sc_innings_label);
        tabLayout = findViewById(R.id.tab_layout_scorecard);
        viewPager = findViewById(R.id.view_pager_scorecard);
        toggleInnings = findViewById(R.id.toggle_innings);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    // ============================================================
    // Data Loading
    // ============================================================

    private void loadScorecardData() {
        // Load players for each team
        List<Player> teamAPlayers = db.getPlayersForTeam(match.getTeamAId());
        List<Player> teamBPlayers = db.getPlayersForTeam(match.getTeamBId());

        // Standard setup: assume A bats first, B bowls first
        List<Player> inn1Batters = teamAPlayers;
        List<Player> inn1Bowlers = teamBPlayers;
        List<Player> inn2Batters = teamBPlayers;
        List<Player> inn2Bowlers = teamAPlayers;

        // Try to verify batting order from ball events if they exist
        List<BallEvent> allBalls = db.getBallsForInnings(matchId, 1);
        if (!allBalls.isEmpty()) {
            long pId = allBalls.get(0).getBatsmanId();
            // Find which team this player belongs to
            boolean foundInA = false;
            for (Player p : teamAPlayers) if (p.getId() == pId) { foundInA = true; break; }

            if (!foundInA) {
                // Team B actually batted first
                inn1Batters = teamBPlayers;
                inn1Bowlers = teamAPlayers;
                inn2Batters = teamAPlayers;
                inn2Bowlers = teamBPlayers;
            }
        }

        innings1Batsmen.clear();
        innings1Batsmen.addAll(inn1Batters);
        innings1Bowlers.clear();
        innings1Bowlers.addAll(inn1Bowlers);
        computePlayerStats(innings1Batsmen, innings1Bowlers, 1, innings1Fow);

        innings2Batsmen.clear();
        innings2Batsmen.addAll(inn2Batters);
        innings2Bowlers.clear();
        innings2Bowlers.addAll(inn2Bowlers);
        computePlayerStats(innings2Batsmen, innings2Bowlers, 2, innings2Fow);
    }

    private void computePlayerStats(List<Player> battingTeam, List<Player> bowlingTeam, int inningsNum, List<FowEvent> fowList) {
        List<BallEvent> balls = db.getBallsForInnings(matchId, inningsNum);
        int currentScore = 0;
        int currentWickets = 0;
        fowList.clear();
        
        for (BallEvent ball : balls) {
            currentScore += ball.getRunsScored() + ball.getExtras();
            
            // 1. Identify Striker
            Player striker = null;
            for (Player p : battingTeam) {
                if (p.getId() == ball.getBatsmanId()) {
                    striker = p;
                    break;
                }
            }

            // 2. Identify Dismissed Player (if any)
            Player dismissed = null;
            if (ball.isWicket()) {
                for (Player p : battingTeam) {
                    if (p.getId() == ball.getDismissedPlayerId()) {
                        dismissed = p;
                        break;
                    }
                }
            }

            // 3. Update Striker Stats (Runs/Balls)
            if (striker != null) {
                if (!ball.getDeliveryType().equals(BallEvent.TYPE_WIDE)) {
                    striker.addBall();
                }
                if (ball.getRunsScored() > 0) {
                    striker.addRuns(ball.getRunsScored());
                    if (ball.getRunsScored() == 4) striker.incrementFours();
                    if (ball.getRunsScored() == 6) striker.incrementSixes();
                }
            }

            // 4. Handle Wicket & Fow
            if (ball.isWicket() && dismissed != null) {
                dismissed.setOut(true);
                dismissed.setHowOut(ball.getDismissalType());
                
                currentWickets++;
                String overStr = ball.getOverNumber() + "." + (ball.getBallInOver());
                fowList.add(new FowEvent(dismissed.getName(), currentWickets, currentScore, overStr));
            }

            // 5. Update Bowler Stats
            Player bowler = null;
            for (Player p : bowlingTeam) {
                if (p.getId() == ball.getBowlerId()) {
                    bowler = p;
                    break;
                }
            }

            if (bowler != null) {
                if (ball.isLegalDelivery() || ball.getDeliveryType().equals(BallEvent.TYPE_WICKET)) {
                    bowler.addBallBowled();
                }
                int runsConceded = ball.getRunsScored();
                if (ball.getExtras() > 0 && (ball.getDeliveryType().equals(BallEvent.TYPE_WIDE)
                        || ball.getDeliveryType().equals(BallEvent.TYPE_NO_BALL))) {
                    runsConceded += ball.getExtras();
                }
                bowler.addRunsConceded(runsConceded);
                if (ball.isWicket() && !ball.getDismissalType().equals(BallEvent.DIS_RUN_OUT)) {
                    bowler.incrementWickets();
                }
            }
        }
    }

    // ============================================================
    // Summary Bar
    // ============================================================

    private void populateSummaryBar() {
        if (displayInnings == 1) {
            tvBattingTeam.setText(match.getTeamAName());
            tvScore.setText(match.getInnings1Score() + "/" + match.getInnings1Wickets());
            tvOvers.setText("(" + match.getInnings1Overs() + "." +
                    match.getInnings1Balls() + "/" + match.getTotalOvers() + " ov)");
            tvInningsLabel.setText("1st Innings");
        } else {
            tvBattingTeam.setText(match.getTeamBName());
            tvScore.setText(match.getInnings2Score() + "/" + match.getInnings2Wickets());
            tvOvers.setText("(" + match.getInnings2Overs() + "." +
                    match.getInnings2Balls() + "/" + match.getTotalOvers() + " ov)");
            tvInningsLabel.setText("2nd Innings");
        }

        if (match.getResult() != null && !match.getResult().isEmpty()) {
            tvResult.setText(match.getResult());
        }
    }

    // ============================================================
    // ViewPager + Tabs
    // ============================================================

    private void setupViewPager() {
        ScorecardPagerAdapter pagerAdapter = new ScorecardPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("BATTING");
                    break;
                case 1:
                    tab.setText("BOWLING");
                    break;
                case 2:
                    tab.setText("FoW");
                    break;
            }
        }).attach();
    }

    private void setupInningsSwitcher() {
        toggleInnings.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                displayInnings = (checkedId == R.id.btn_innings_1) ? 1 : 2;
                populateSummaryBar();
                if (viewPager.getAdapter() != null) {
                    viewPager.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    // ============================================================
    // Pager Adapter
    // ============================================================

    class ScorecardPagerAdapter extends FragmentStateAdapter {
        private final long baseId = System.currentTimeMillis();

        ScorecardPagerAdapter(ScorecardActivity activity) {
            super(activity.getSupportFragmentManager(), activity.getLifecycle());
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @Override
        public long getItemId(int position) {
            // Force recreation by using unique ID for each innings + tab combo
            return (long) displayInnings * 10 + position;
        }

        @Override
        public boolean containsItem(long itemId) {
            // Check if the ID belongs to the currently displayed innings
            return itemId >= (long) displayInnings * 10 && itemId < (long) displayInnings * 10 + 3;
        }

        @Override
        public Fragment createFragment(int position) {
            List<Player> batsmen = (displayInnings == 1) ? innings1Batsmen : innings2Batsmen;
            List<Player> bowlers = (displayInnings == 1) ? innings1Bowlers : innings2Bowlers;
            List<FowEvent> fowEvents = (displayInnings == 1) ? innings1Fow : innings2Fow;

            switch (position) {
                case 0:
                    return ScorecardTabFragment.newBatting(batsmen);
                case 1:
                    return ScorecardTabFragment.newBowling(bowlers);
                case 2:
                    return ScorecardTabFragment.newFoW(fowEvents);
                default:
                    return ScorecardTabFragment.newBatting(batsmen);
            }
        }
    }

    // ============================================================
    // Inner Fragment for each tab
    // ============================================================

    public static class ScorecardTabFragment extends Fragment {

        private static final String ARG_TYPE = "type";
        private static final int TYPE_BATTING = 0;
        private static final int TYPE_BOWLING = 1;
        private static final int TYPE_FOW = 2;

        private List<Player> players;
        private List<FowEvent> fowList;
        private int type;

        public static ScorecardTabFragment newBatting(List<Player> players) {
            ScorecardTabFragment f = new ScorecardTabFragment();
            f.players = new ArrayList<>(players);
            f.type = TYPE_BATTING;
            return f;
        }

        public static ScorecardTabFragment newBowling(List<Player> players) {
            ScorecardTabFragment f = new ScorecardTabFragment();
            f.players = new ArrayList<>(players);
            f.type = TYPE_BOWLING;
            return f;
        }

        public static ScorecardTabFragment newFoW(List<FowEvent> events) {
            ScorecardTabFragment f = new ScorecardTabFragment();
            f.fowList = new ArrayList<>(events);
            f.type = TYPE_FOW;
            return f;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_scorecard_tab, container, false);

            RecyclerView rv = view.findViewById(R.id.rv_scorecard_tab);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));

            // Setup header labels
            TextView h1 = view.findViewById(R.id.tv_header_col1);
            TextView h2 = view.findViewById(R.id.tv_header_col2);
            TextView h3 = view.findViewById(R.id.tv_header_col3);
            TextView h4 = view.findViewById(R.id.tv_header_col4);
            TextView h5 = view.findViewById(R.id.tv_header_col5);

            if (type == TYPE_BATTING) {
                h1.setText("R");
                h2.setText("B");
                h3.setText("4s");
                h4.setText("6s");
                h5.setText("SR");
                h4.setVisibility(View.VISIBLE);
                h5.setVisibility(View.VISIBLE);
                if (players != null)
                    rv.setAdapter(new BatsmanAdapter(players));
            } else if (type == TYPE_BOWLING) {
                h1.setText("OV");
                h2.setText("R");
                h3.setText("W");
                h4.setText("ECO");
                h5.setVisibility(View.GONE);
                if (players != null)
                    rv.setAdapter(new BowlerAdapter(players));
            } else {
                h1.setText("WKT");
                h2.setText("SCORE");
                h3.setText("OV");
                h4.setVisibility(View.GONE);
                h5.setVisibility(View.GONE);
                if (fowList != null)
                    rv.setAdapter(new FowAdapter(fowList));
            }

            return view;
        }
    }

    public static class FowEvent {
        public String playerOut;
        public int wicketNumber;
        public int teamScore;
        public String over;

        public FowEvent(String p, int w, int s, String o) {
            playerOut = p; wicketNumber = w; teamScore = s; over = o;
        }
    }

    public static class FowAdapter extends RecyclerView.Adapter<FowAdapter.ViewHolder> {
        private List<FowEvent> events;

        public FowAdapter(List<FowEvent> events) { this.events = events; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fow, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FowEvent e = events.get(position);
            holder.tvPlayer.setText(e.playerOut);
            holder.tvWkt.setText(String.valueOf(e.wicketNumber));
            holder.tvScore.setText(String.valueOf(e.teamScore));
            holder.tvOv.setText(e.over);
        }

        @Override
        public int getItemCount() { return events.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPlayer, tvWkt, tvScore, tvOv;
            ViewHolder(View itemView) {
                super(itemView);
                tvPlayer = itemView.findViewById(R.id.tv_fow_player);
                tvWkt = itemView.findViewById(R.id.tv_fow_wkt);
                tvScore = itemView.findViewById(R.id.tv_fow_score);
                tvOv = itemView.findViewById(R.id.tv_fow_ov);
            }
        }
    }
}
