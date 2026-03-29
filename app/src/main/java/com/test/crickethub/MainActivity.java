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
import androidx.activity.OnBackPressedCallback;
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
    private long lastBackPressTime;
    private android.widget.Toast backToast;

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
            
            // 2. Bottom Nav Positioning (Extend height and add padding for system bar)
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            android.view.ViewGroup.LayoutParams navLp = bottomNav.getLayoutParams();
            // Add an extra 8dp (converted to px) buffer to prevent label cropping
            int bufferPx = (int) (8 * getResources().getDisplayMetrics().density);
            navLp.height = navHeightPx + systemBars.bottom + bufferPx;
            bottomNav.setLayoutParams(navLp);
            
            // 3. Content Area Positioning (Above Bottom Nav)
            if (findViewById(R.id.main_content_area).getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                android.view.ViewGroup.MarginLayoutParams contentLp = (android.view.ViewGroup.MarginLayoutParams) findViewById(R.id.main_content_area).getLayoutParams();
                contentLp.bottomMargin = navHeightPx + systemBars.bottom;
                findViewById(R.id.main_content_area).setLayoutParams(contentLp);
            }
            
            return insets;
        });

        setupBottomNav();
        setupBackPressed();
        
        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNav() {
        ColorStateList navColors = ContextCompat.getColorStateList(this, R.color.nav_color_state);
        bottomNav.setItemIconTintList(navColors);
        bottomNav.setItemTextColor(navColors);
        
        // Force icons to be treated as templates for reliable tinting
        android.view.Menu menu = bottomNav.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            android.view.MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) {
                // This forces the drawable to use the tint list
                androidx.core.graphics.drawable.DrawableCompat.setTintList(item.getIcon(), navColors);
            }
        }
        
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

    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNav.getSelectedItemId() != R.id.nav_home) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    if (lastBackPressTime + 2000 > System.currentTimeMillis()) {
                        if (backToast != null) backToast.cancel();
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    } else {
                        backToast = android.widget.Toast.makeText(MainActivity.this, "Tap again to exit", android.widget.Toast.LENGTH_SHORT);
                        backToast.show();
                        lastBackPressTime = System.currentTimeMillis();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        
        // Programmatically tint the Settings icon for visibility
        android.view.MenuItem settingsItem = menu.findItem(R.id.action_settings);
        if (settingsItem != null && settingsItem.getIcon() != null) {
            settingsItem.getIcon().setTintList(ContextCompat.getColorStateList(this, R.color.text_primary));
        }
        
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