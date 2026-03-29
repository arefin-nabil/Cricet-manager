package com.test.crickethub;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.test.crickethub.fragment.HomeFragment;
import com.test.crickethub.fragment.ManageTeamsFragment;
import com.test.crickethub.fragment.MatchHistoryFragment;

/**
 * MainActivity.java — CricketHub Dashboard
 * ==========================================
 */
public class MainActivity extends AppCompatActivity implements HomeFragment.OnHomeNavigationListener {

    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        bottomNav = findViewById(R.id.bottom_nav);
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        
        // Handle Insets for Top (Toolbar), Bottom (Nav), and Content area
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinator_main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int navHeightPx = getResources().getDimensionPixelSize(R.dimen.nav_height);
            
            // 1. Top Insets (Toolbar)
            findViewById(R.id.app_bar_main).setPadding(0, systemBars.top, 0, 0);
            
            // 2. Bottom Nav Positioning (Above system buttons)
            if (bottomNav.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                android.view.ViewGroup.MarginLayoutParams lp = (android.view.ViewGroup.MarginLayoutParams) bottomNav.getLayoutParams();
                lp.bottomMargin = systemBars.bottom;
                bottomNav.setLayoutParams(lp);
            }
            
            // 3. Content Area Positioning (Above Bottom Nav)
            if (findViewById(R.id.main_content_area).getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                android.view.ViewGroup.MarginLayoutParams lp = (android.view.ViewGroup.MarginLayoutParams) findViewById(R.id.main_content_area).getLayoutParams();
                lp.bottomMargin = navHeightPx + systemBars.bottom;
                findViewById(R.id.main_content_area).setLayoutParams(lp);
            }
            
            return insets;
        });

        setupBottomNav();
        
        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNav() {
        ColorStateList navColors = ContextCompat.getColorStateList(this, R.color.nav_color_state);
        bottomNav.setItemIconTintList(navColors);
        bottomNav.setItemTextColor(navColors);
        bottomNav.setItemActiveIndicatorEnabled(false);
        
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
                toolbar.setTitle(R.string.dashboard_title);
            } else if (itemId == R.id.nav_teams) {
                fragment = new ManageTeamsFragment();
                toolbar.setTitle("Manage Teams");
            } else if (itemId == R.id.nav_history) {
                fragment = new MatchHistoryFragment();
                toolbar.setTitle(R.string.history_title);
            }
            
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, fragment)
                .commit();
    }

    @Override
    public void onNavigateToTeams() {
        bottomNav.setSelectedItemId(R.id.nav_teams);
    }

    @Override
    public void onNavigateToHistory() {
        bottomNav.setSelectedItemId(R.id.nav_history);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, com.test.crickethub.activity.SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}