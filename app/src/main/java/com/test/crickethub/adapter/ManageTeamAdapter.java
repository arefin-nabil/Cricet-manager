package com.test.crickethub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.chip.Chip;
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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_team, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Team t = teams.get(position);

        // Name
        holder.tvName.setText(t.getName());

        // Description
        if (t.getDescription() != null && !t.getDescription().isEmpty()) {
            holder.tvDesc.setVisibility(View.VISIBLE);
            holder.tvDesc.setText(t.getDescription());
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Member count chip
        if (holder.chipMemberCount != null) {
            int playerCount = (t.getPlayerCount() > 0) ? t.getPlayerCount() : 0;
            holder.chipMemberCount.setText(playerCount + " members");
        }

        // Status chip — always "Active" for now
        if (holder.chipStatus != null) {
            holder.chipStatus.setText("Active");
        }

        // Play Lottie team icon animation on bind
        if (holder.lottieTeamIcon != null) {
            holder.lottieTeamIcon.playAnimation();
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> listenerEdit.onAction(t));
        holder.btnDelete.setOnClickListener(v -> listenerDelete.onAction(t));
        if (holder.btnMoreOptions != null) {
            holder.btnMoreOptions.setOnClickListener(v -> listenerEdit.onAction(t));
        }
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        ImageButton btnDelete;
        ImageButton btnMoreOptions;
        Chip chipMemberCount;
        Chip chipStatus;
        LottieAnimationView lottieTeamIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName          = itemView.findViewById(R.id.tv_team_name);
            tvDesc          = itemView.findViewById(R.id.tv_team_desc);
            btnDelete       = itemView.findViewById(R.id.btn_delete_team);
            btnMoreOptions  = itemView.findViewById(R.id.btn_more_options);
            chipMemberCount = itemView.findViewById(R.id.chip_member_count);
            chipStatus      = itemView.findViewById(R.id.chip_status);
            lottieTeamIcon  = itemView.findViewById(R.id.lottie_team_icon);
        }
    }
}
