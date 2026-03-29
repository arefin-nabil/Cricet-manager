package com.test.crickethub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.crickethub.R;
import com.test.crickethub.model.Team;

import java.util.ArrayList;
import java.util.List;

public class ManageTeamAdapter extends RecyclerView.Adapter<ManageTeamAdapter.ViewHolder> {

    private List<Team> teams = new ArrayList<>();
    private final OnTeamAction listenerEdit;
    private final OnTeamAction listenerDelete;

    public interface OnTeamAction {
        void onAction(Team team);
    }

    public ManageTeamAdapter(OnTeamAction listenerEdit, OnTeamAction listenerDelete) {
        this.listenerEdit = listenerEdit;
        this.listenerDelete = listenerDelete;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_team, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Team t = teams.get(position);
        holder.tvName.setText(t.getName());

        if (t.getDescription() != null && !t.getDescription().isEmpty()) {
            holder.tvDesc.setVisibility(View.VISIBLE);
            holder.tvDesc.setText(t.getDescription());
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listenerEdit.onAction(t));
        holder.btnDelete.setOnClickListener(v -> listenerDelete.onAction(t));
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_team_name);
            tvDesc = itemView.findViewById(R.id.tv_team_desc);
            btnDelete = itemView.findViewById(R.id.btn_delete_team);
        }
    }
}
