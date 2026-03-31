package com.test.crickethub.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
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

/**
 * ScorecardActivity.java - Displays full match scorecard with Innings switcher and Tabs.
 */
public class ScorecardActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvBattingTeam, tvScore, tvOvers, tvResult, tvInningsLabel;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MaterialButtonToggleGroup toggleInnings;

    private CricketDbHelper db;
    private Match match;
    private long matchId;

    private List<Player> innings1Batsmen = new ArrayList<>();
    private List<Player> innings1Bowlers = new ArrayList<>();
    private List<Player> innings2Batsmen = new ArrayList<>();
    private List<Player> innings2Bowlers = new ArrayList<>();

    private List<FowEvent> innings1Fow = new ArrayList<>();
    private List<FowEvent> innings2Fow = new ArrayList<>();

    // Extras tracking
    private int inn1Wide=0, inn1NB=0, inn1Bye=0, inn1LB=0;
    private int inn2Wide=0, inn2NB=0, inn2Bye=0, inn2LB=0;

    // Ball-by-ball history
    private List<BallEvent> innings1BallsFull = new ArrayList<>();
    private List<BallEvent> innings2BallsFull = new ArrayList<>();

    private int displayInnings = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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

    private void loadScorecardData() {
        List<Player> teamAPlayers = db.getPlayersForTeam(match.getTeamAId());
        List<Player> teamBPlayers = db.getPlayersForTeam(match.getTeamBId());

        List<Player> inn1Batters = teamAPlayers;
        List<Player> inn1Bowlers = teamBPlayers;
        List<Player> inn2Batters = teamBPlayers;
        List<Player> inn2Bowlers = teamAPlayers;

        List<BallEvent> allBalls = db.getBallsForInnings(matchId, 1);
        if (!allBalls.isEmpty()) {
            long pId = allBalls.get(0).getBatsmanId();
            boolean foundInA = false;
            for (Player p : teamAPlayers) if (p.getId() == pId) { foundInA = true; break; }
            if (!foundInA) {
                inn1Batters = teamBPlayers; inn1Bowlers = teamAPlayers;
                inn2Batters = teamAPlayers; inn2Bowlers = teamBPlayers;
            }
        }

        innings1Batsmen.clear(); innings1Batsmen.addAll(inn1Batters);
        innings1Bowlers.clear(); innings1Bowlers.addAll(inn1Bowlers);
        innings1BallsFull.clear();
        computePlayerStats(innings1Batsmen, innings1Bowlers, 1, innings1Fow, innings1BallsFull, 1);

        innings2Batsmen.clear(); innings2Batsmen.addAll(inn2Batters);
        innings2Bowlers.clear(); innings2Bowlers.addAll(inn2Bowlers);
        innings2BallsFull.clear();
        computePlayerStats(innings2Batsmen, innings2Bowlers, 2, innings2Fow, innings2BallsFull, 2);
    }

    private void computePlayerStats(List<Player> battingTeam, List<Player> bowlingTeam, int inningsNum, 
                                   List<FowEvent> fowList, List<BallEvent> historyList, int innIdx) {
        List<BallEvent> balls = db.getBallsForInnings(matchId, inningsNum);
        int currentScore = 0;
        int currentWickets = 0;
        int w = 0, nb = 0, b = 0, lb = 0;
        fowList.clear();
        historyList.clear();
        historyList.addAll(balls);
        
        for (BallEvent ball : balls) {
            int extras = ball.getExtras();
            currentScore += ball.getRunsScored() + extras;
            
            String type = ball.getDeliveryType();
            if (type.equals(BallEvent.TYPE_WIDE)) w += (1 + extras);
            else if (type.equals(BallEvent.TYPE_NO_BALL)) nb += (1 + extras);
            else if (type.equals(BallEvent.TYPE_BYE)) b += extras;
            else if (type.equals(BallEvent.TYPE_LEG_BYE)) lb += extras;

            for (Player p : battingTeam) {
                if (p.getId() == ball.getBatsmanId()) {
                    if (!type.equals(BallEvent.TYPE_WIDE)) p.addBall();
                    if (ball.getRunsScored() > 0) {
                        p.addRuns(ball.getRunsScored());
                        if (ball.getRunsScored() == 4) p.incrementFours();
                        if (ball.getRunsScored() == 6) p.incrementSixes();
                    }
                    break;
                }
            }

            if (ball.isWicket()) {
                for (Player p : battingTeam) {
                    if (p.getId() == ball.getDismissedPlayerId()) {
                        p.setOut(true);
                        p.setHowOut(ball.getDismissalType());
                        currentWickets++;
                        String overStr = ball.getOverNumber() + "." + (ball.getBallInOver());
                        fowList.add(new FowEvent(p.getName(), currentWickets, currentScore, overStr));
                        break;
                    }
                }
            }

            for (Player p : bowlingTeam) {
                if (p.getId() == ball.getBowlerId()) {
                    if (ball.isLegalDelivery() || type.equals(BallEvent.TYPE_WICKET)) p.addBallBowled(true);
                    int runsConceded = ball.getRunsScored();
                    if (extras > 0 && (type.equals(BallEvent.TYPE_WIDE) || type.equals(BallEvent.TYPE_NO_BALL))) {
                        runsConceded += extras + 1;
                    }
                    p.addRunsConceded(runsConceded);
                    if (ball.isWicket() && !ball.getDismissalType().equals(BallEvent.DIS_RUN_OUT)) {
                        p.incrementWickets();
                    }
                    break;
                }
            }
        }

        if (innIdx == 1) { inn1Wide = w; inn1NB = nb; inn1Bye = b; inn1LB = lb; }
        else { inn2Wide = w; inn2NB = nb; inn2Bye = b; inn2LB = lb; }
    }

    private void populateSummaryBar() {
        if (displayInnings == 1) {
            tvBattingTeam.setText(match.getTeamAName());
            tvScore.setText(match.getInnings1Score() + "/" + match.getInnings1Wickets());
            tvOvers.setText("(" + match.getInnings1Overs() + "." + match.getInnings1Balls() + "/" + match.getTotalOvers() + " ov)");
            tvInningsLabel.setText("1st Innings");
        } else {
            tvBattingTeam.setText(match.getTeamBName());
            tvScore.setText(match.getInnings2Score() + "/" + match.getInnings2Wickets());
            tvOvers.setText("(" + match.getInnings2Overs() + "." + match.getInnings2Balls() + "/" + match.getTotalOvers() + " ov)");
            tvInningsLabel.setText("2nd Innings");
        }
        if (match.getResult() != null && !match.getResult().isEmpty()) tvResult.setText(match.getResult());
    }

    private void setupViewPager() {
        ScorecardPagerAdapter pagerAdapter = new ScorecardPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("BATTING"); break;
                case 1: tab.setText("BOWLING"); break;
                case 2: tab.setText("FoW"); break;
                case 3: tab.setText("HISTORY"); break;
            }
        }).attach();
    }

    private void setupInningsSwitcher() {
        toggleInnings.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                displayInnings = (checkedId == R.id.btn_innings_1) ? 1 : 2;
                populateSummaryBar();
                if (viewPager.getAdapter() != null) viewPager.getAdapter().notifyDataSetChanged();
            }
        });
    }

    // ============================================================
    // Pager Adapter
    // ============================================================

    class ScorecardPagerAdapter extends FragmentStateAdapter {
        ScorecardPagerAdapter(ScorecardActivity activity) {
            super(activity.getSupportFragmentManager(), activity.getLifecycle());
        }

        @Override public int getItemCount() { return 4; }
        @Override public long getItemId(int position) { return (long) displayInnings * 10 + position; }
        @Override public boolean containsItem(long itemId) { return itemId >= (long) displayInnings * 10 && itemId < (long) displayInnings * 10 + 4; }

        @Override
        public Fragment createFragment(int position) {
            List<Player> batsmen = (displayInnings == 1) ? innings1Batsmen : innings2Batsmen;
            List<Player> bowlers = (displayInnings == 1) ? innings1Bowlers : innings2Bowlers;
            List<FowEvent> fowEvents = (displayInnings == 1) ? innings1Fow : innings2Fow;
            List<BallEvent> balls = (displayInnings == 1) ? innings1BallsFull : innings2BallsFull;
            int[] extras = (displayInnings == 1) ? new int[]{inn1Wide, inn1NB, inn1Bye, inn1LB} : new int[]{inn2Wide, inn2NB, inn2Bye, inn2LB};

            switch (position) {
                case 0: return ScorecardTabFragment.newBatting(batsmen, extras);
                case 1: return ScorecardTabFragment.newBowling(bowlers);
                case 2: return ScorecardTabFragment.newFoW(fowEvents);
                case 3: return ScorecardTabFragment.newHistory(balls);
                default: return ScorecardTabFragment.newBatting(batsmen, extras);
            }
        }
    }

    // ============================================================
    // Inner Fragment
    // ============================================================

    public static class ScorecardTabFragment extends Fragment {
        private List<Player> players;
        private List<FowEvent> fowList;
        private List<BallEvent> ballList;
        private int[] extrasData;
        private int type;

        public static ScorecardTabFragment newBatting(List<Player> players, int[] extras) {
            ScorecardTabFragment f = new ScorecardTabFragment(); f.players = new ArrayList<>(players); f.extrasData = extras; f.type = 0; return f;
        }
        public static ScorecardTabFragment newBowling(List<Player> players) {
            ScorecardTabFragment f = new ScorecardTabFragment(); f.players = new ArrayList<>(players); f.type = 1; return f;
        }
        public static ScorecardTabFragment newFoW(List<FowEvent> events) {
            ScorecardTabFragment f = new ScorecardTabFragment(); f.fowList = new ArrayList<>(events); f.type = 2; return f;
        }
        public static ScorecardTabFragment newHistory(List<BallEvent> balls) {
            ScorecardTabFragment f = new ScorecardTabFragment(); f.ballList = new ArrayList<>(balls); f.type = 3; return f;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_scorecard_tab, container, false);
            RecyclerView rv = view.findViewById(R.id.rv_scorecard_tab);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));

            TextView h1 = view.findViewById(R.id.tv_header_col1);
            TextView h2 = view.findViewById(R.id.tv_header_col2);
            TextView h3 = view.findViewById(R.id.tv_header_col3);
            TextView h4 = view.findViewById(R.id.tv_header_col4);
            TextView h5 = view.findViewById(R.id.tv_header_col5);

            if (type == 0) { // BATTING
                h1.setText("R"); h2.setText("B"); h3.setText("4s"); h4.setText("6s"); h5.setText("SR");
                if (players != null) rv.setAdapter(new BatsmanAdapter(players));
                LinearLayout extrasContainer = view.findViewById(R.id.layout_extras_container);
                if (extrasData != null) {
                    extrasContainer.setVisibility(View.VISIBLE);
                    int total = extrasData[0]+extrasData[1]+extrasData[2]+extrasData[3];
                    ((TextView)view.findViewById(R.id.tv_extras_total)).setText(String.valueOf(total));
                    ((TextView)view.findViewById(R.id.tv_extras_breakdown)).setText(String.format("(wd %d, nb %d, b %d, lb %d)", extrasData[0], extrasData[1], extrasData[2], extrasData[3]));
                }
            } else if (type == 1) { // BOWLING
                h1.setText("OV"); h2.setText("R"); h3.setText("W"); h4.setText("ECO"); h5.setVisibility(View.GONE);
                if (players != null) rv.setAdapter(new BowlerAdapter(players));
            } else if (type == 2) { // FOW
                h1.setText("WKT"); h2.setText("SCORE"); h3.setText("OV"); h4.setVisibility(View.GONE); h5.setVisibility(View.GONE);
                if (fowList != null) rv.setAdapter(new FowAdapter(fowList));
            } else { // HISTORY
                view.findViewById(R.id.layout_scorecard_header).setVisibility(View.GONE);
                if (ballList != null) rv.setAdapter(new HistoryAdapter(ballList));
            }
            return view;
        }
    }

    public static class FowEvent {
        public String playerOut; public int wicketNumber, teamScore; public String over;
        public FowEvent(String p, int w, int s, String o) { playerOut = p; wicketNumber = w; teamScore = s; over = o; }
    }

    public static class FowAdapter extends RecyclerView.Adapter<FowAdapter.ViewHolder> {
        private List<FowEvent> events; public FowAdapter(List<FowEvent> events) { this.events = events; }
        @Override public ViewHolder onCreateViewHolder(ViewGroup p, int vt) { return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_fow, p, false)); }
        @Override public void onBindViewHolder(ViewHolder h, int p) {
            FowEvent e = events.get(p); h.tvPlayer.setText(e.playerOut); h.tvWkt.setText(String.valueOf(e.wicketNumber)); h.tvScore.setText(String.valueOf(e.teamScore)); h.tvOv.setText(e.over);
        }
        @Override public int getItemCount() { return events.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPlayer, tvWkt, tvScore, tvOv;
            ViewHolder(View v) { super(v); tvPlayer = v.findViewById(R.id.tv_fow_player); tvWkt = v.findViewById(R.id.tv_fow_wkt); tvScore = v.findViewById(R.id.tv_fow_score); tvOv = v.findViewById(R.id.tv_fow_ov); }
        }
    }

    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<BallEvent> balls; public HistoryAdapter(List<BallEvent> balls) { this.balls = balls; }
        @Override public ViewHolder onCreateViewHolder(ViewGroup p, int vt) { return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false)); }
        @Override public void onBindViewHolder(ViewHolder h, int p) {
            BallEvent b = balls.get(p);
            h.t1.setText(b.getOverNumber() + "." + b.getBallInOver() + "  " + b.getDisplayLabel());
            h.t2.setText(b.getDeliveryType() + " | Runs: " + b.getRunsScored() + " | Extra: " + b.getExtras());
        }
        @Override public int getItemCount() { return balls.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView t1, t2;
            ViewHolder(View v) { super(v); t1 = v.findViewById(android.R.id.text1); t2 = v.findViewById(android.R.id.text2); }
        }
    }
}
