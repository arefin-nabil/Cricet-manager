package com.test.crickethub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.test.crickethub.R;
import com.test.crickethub.activity.CreateMatchActivity;
import com.test.crickethub.activity.LiveScoringActivity;
import com.test.crickethub.activity.ScorecardActivity;
import com.test.crickethub.adapter.MatchHistoryAdapter;
import com.test.crickethub.db.CricketDbHelper;
import com.test.crickethub.model.Match;

import java.util.ArrayList;
import java.util.List;

public class MatchHistoryFragment extends Fragment {

    private TextInputEditText etSearch;
    private RecyclerView rvMatches;
    private TextView tvHistoryCount;
    private View layoutEmptyState;
    private MaterialButton btnStartFirst;

    private CricketDbHelper db;
    private MatchHistoryAdapter adapter;
    private List<Match> allMatches = new ArrayList<>();
    private List<Match> filteredMatches = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_history, container, false);
        
        db = CricketDbHelper.getInstance(requireContext());
        bindViews(view);
        setupRecyclerView();
        setupSearch();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMatches();
    }

    private void bindViews(View v) {
        etSearch = v.findViewById(R.id.et_search_history_fragment);
        rvMatches = v.findViewById(R.id.rv_match_history_fragment);
        tvHistoryCount = v.findViewById(R.id.tv_history_count_fragment);
        layoutEmptyState = v.findViewById(R.id.layout_empty_history_fragment);
        btnStartFirst = v.findViewById(R.id.btn_start_first_match_fragment);
    }

    private void setupRecyclerView() {
        adapter = new MatchHistoryAdapter(filteredMatches, match -> {
            Intent intent;
            if (Match.STATUS_LIVE.equals(match.getStatus())) {
                intent = new Intent(requireContext(), LiveScoringActivity.class);
            } else {
                intent = new Intent(requireContext(), ScorecardActivity.class);
            }
            intent.putExtra("match_id", match.getId());
            startActivity(intent);
        });
        rvMatches.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMatches.setAdapter(adapter);
    }

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

    private void loadMatches() {
        allMatches.clear();
        allMatches.addAll(db.getAllMatches());

        String currentQuery = etSearch.getText() != null ? etSearch.getText().toString() : "";
        filterMatches(currentQuery);
    }

    private void updateCountLabel() {
        tvHistoryCount.setText(filteredMatches.size() + " match" +
                (filteredMatches.size() != 1 ? "es" : ""));
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredMatches.isEmpty();
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvMatches.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            TextView tvEmptyText = layoutEmptyState.findViewById(R.id.tv_empty_history_text_fragment);
            if (allMatches.isEmpty()) {
                if(tvEmptyText != null) tvEmptyText.setText(R.string.no_history);
                btnStartFirst.setVisibility(View.VISIBLE);
                btnStartFirst.setOnClickListener(v -> {
                    startActivity(new Intent(requireContext(), CreateMatchActivity.class));
                });
            } else {
                if(tvEmptyText != null) tvEmptyText.setText("No selected matches found.");
                btnStartFirst.setVisibility(View.GONE);
            }
        }
    }
}
