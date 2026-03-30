package com.test.crickethub.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import com.test.crickethub.R;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.fragment.TournamentBracketFragment;
import com.test.crickethub.fragment.TournamentMatchesFragment;
import com.test.crickethub.fragment.TournamentPointsFragment;
import com.test.crickethub.fragment.TournamentStatsFragment;
import com.test.crickethub.model.Tournament;

public class TournamentDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private CricketDbHelper db;
    private long tournamentId;
    private Tournament tournament;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = CricketDbHelper.getInstance(this);

        tournamentId = getIntent().getLongExtra("tournament_id", -1);
        if (tournamentId == -1) {
            finish();
            return;
        }

        tournament = db.getTournamentById(tournamentId);
        if (tournament == null) {
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar_tournament_detail);
        toolbar.setTitle(tournament.getName());
        toolbar.setNavigationOnClickListener(v -> finish());

        tabLayout = findViewById(R.id.tab_layout_tournament);
        viewPager = findViewById(R.id.view_pager_tournament);

        TournamentPagerAdapter adapter = new TournamentPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Matches"); break;
                case 1: tab.setText("Points Table"); break;
                case 2: tab.setText("Bracket"); break;
                case 3: tab.setText("Stats"); break;
            }
        }).attach();

        // If knockout, hide points table tab using mediation or just let it be empty
        // Mvp: let it be empty but present.
    }

    private class TournamentPagerAdapter extends FragmentStateAdapter {

        public TournamentPagerAdapter(@NonNull AppCompatActivity fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment f = null;
            switch (position) {
                case 0: f = new TournamentMatchesFragment(); break;
                case 1: f = new TournamentPointsFragment(); break;
                case 2: f = new TournamentBracketFragment(); break;
                case 3: f = new TournamentStatsFragment(); break;
                default: f = new TournamentMatchesFragment(); break;
            }
            Bundle args = new Bundle();
            args.putLong("tournament_id", tournamentId);
            f.setArguments(args);
            return f;
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
