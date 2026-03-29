package com.test.crickethub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.crickethub.R;
import com.test.crickethub.model.Team;

import java.util.List;

public class TeamChipAdapter extends RecyclerView.Adapter<TeamChipAdapter.ViewHolder> {

    private List<Team> teams;
    private OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    public TeamChipAdapter(List<Team> teams, OnTeamClickListener listener) {
        this.teams = teams;
        this.listener = listener;
    }

    public void setTeams(List<Team> newTeams) {
        this.teams.clear();
        this.teams.addAll(newTeams);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.tvName.setText(team.getName());
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onTeamClick(team));
        }
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_team_chip_name);
        }
    }
}
