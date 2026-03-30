package com.test.crickethub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import com.test.crickethub.MainActivity;
import com.test.crickethub.R;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Match;
import com.test.crickethub.model.Team;
import com.test.crickethub.model.Tournament;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateTournamentActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtOvers;
    private AutoCompleteTextView spinnerFormat;
    private RecyclerView rvTeams;
    private TextView tvSelectionCount;
    private MaterialButton btnGenerate;

    private CricketDbHelper db;
    private List<Team> allTeams;
    private List<Team> selectedTeams = new ArrayList<>();
    private TeamSelectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tournament);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        db = CricketDbHelper.getInstance(this);

        findViewById(R.id.toolbar_create_tournament).setOnClickListener(v -> finish());
        
        edtName = findViewById(R.id.edt_tournament_name);
        edtOvers = findViewById(R.id.edt_overs);
        spinnerFormat = findViewById(R.id.spinner_format);
        rvTeams = findViewById(R.id.rv_team_selector);
        tvSelectionCount = findViewById(R.id.tv_team_selection_count);
        btnGenerate = findViewById(R.id.btn_create_tournament);

        String[] formats = new String[]{Tournament.FORMAT_KNOCKOUT, Tournament.FORMAT_ROUND_ROBIN};
        ArrayAdapter<String> formatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formats);
        spinnerFormat.setAdapter(formatAdapter);
        spinnerFormat.setText(formats[0], false);

        allTeams = db.getAllTeams();
        rvTeams.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new TeamSelectionAdapter();
        rvTeams.setAdapter(adapter);

        btnGenerate.setOnClickListener(v -> generateTournament());
    }

    private void updateSelectionText() {
        tvSelectionCount.setText("Selected: " + selectedTeams.size());
    }

    private void generateTournament() {
        String name = edtName.getText().toString().trim();
        if (name.isEmpty()) {
            edtName.setError("Required");
            return;
        }

        int overs;
        try {
            overs = Integer.parseInt(edtOvers.getText().toString().trim());
            if (overs <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            edtOvers.setError("Invalid overs");
            return;
        }

        if (selectedTeams.size() < 2) {
            Toast.makeText(this, "Select at least 2 teams", Toast.LENGTH_SHORT).show();
            return;
        }

        String format = spinnerFormat.getText().toString();
        
        // 1. Create Tournament entry
        Tournament trn = new Tournament();
        trn.setName(name);
        trn.setFormat(format);
        trn.setTotalTeams(selectedTeams.size());
        trn.setOversLimit(overs);
        
        String dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        trn.setStartDate(dateStr);
        trn.setEndDate("");
        
        long trnId = db.insertTournament(trn);

        // 2. Link Teams & Initialize points tables
        for (Team t : selectedTeams) {
            db.addTeamToTournament(trnId, t.getId());
        }

        // 3. Generate Fixtures Matrix
        generateFixtures(trnId, format, overs, selectedTeams);

        Toast.makeText(this, "Tournament Generated!", Toast.LENGTH_SHORT).show();
        
        // Go back to main
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void generateFixtures(long trnId, String format, int overs, List<Team> teams) {
        // Create a copy to shuffle
        List<Team> pool = new ArrayList<>(teams);
        Collections.shuffle(pool); // random draws
        
        int matchNum = 1;

        if (format.equals(Tournament.FORMAT_ROUND_ROBIN)) {
            // Every team plays every other team once
            for (int i = 0; i < pool.size(); i++) {
                for (int j = i + 1; j < pool.size(); j++) {
                    createMatchEntity(trnId, matchNum++, overs, pool.get(i), pool.get(j));
                }
            }
        } else if (format.equals(Tournament.FORMAT_KNOCKOUT)) {
            // MVP Knockout logic: Random pairings for Round 1
            // If odd, one team gets a bye (doesn't play round 1).
            // Proper brackets require complex powers-of-two padding. 
            // For MVP: we just pair them up sequentially. 
            // Example for 5 teams: A vs B, C vs D. (E gets a bye, advances automatically later)
            // But right now we strictly schedule the defined pairs.
            int i = 0;
            while (i < pool.size() - 1) {
                createMatchEntity(trnId, matchNum++, overs, pool.get(i), pool.get(i + 1));
                i += 2;
            }
        }
    }

    private void createMatchEntity(long trnId, int matchIndex, int overs, Team tA, Team tB) {
        Match m = new Match();
        m.setTournamentId(trnId);
        m.setTournamentMatchNumber(matchIndex);
        m.setTeamAId(tA.getId());
        m.setTeamBId(tB.getId());
        m.setTeamAName(tA.getName());
        m.setTeamBName(tB.getName());
        m.setTotalOvers(overs);
        m.setStatus(Match.STATUS_SETUP);
        db.insertMatch(m);
    }

    // ============================================================
    // Team Multi-Selector Adapter
    // ============================================================
    private class TeamSelectionAdapter extends RecyclerView.Adapter<TeamSelectionAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_chip, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Team t = allTeams.get(position);
            holder.tvName.setText(t.getName());
            
            boolean isSelected = selectedTeams.contains(t);
            
            MaterialCardView card = (MaterialCardView) holder.itemView;
            if (isSelected) {
                card.setStrokeColor(ContextCompat.getColor(CreateTournamentActivity.this, R.color.green_primary));
                card.setStrokeWidth(4);
            } else {
                card.setStrokeColor(ContextCompat.getColor(CreateTournamentActivity.this, R.color.stroke_card));
                card.setStrokeWidth(1);
            }

            holder.itemView.setOnClickListener(v -> {
                if (isSelected) {
                    selectedTeams.remove(t);
                } else {
                    selectedTeams.add(t);
                }
                notifyItemChanged(position);
                updateSelectionText();
            });
        }

        @Override
        public int getItemCount() {
            return allTeams.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvName;
            public Holder(@NonNull View iv) {
                super(iv);
                tvName = iv.findViewById(R.id.tv_team_chip_name);
            }
        }
    }
}
