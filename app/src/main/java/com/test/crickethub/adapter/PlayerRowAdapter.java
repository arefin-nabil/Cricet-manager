package com.test.crickethub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.test.crickethub.R;
import com.test.crickethub.model.Player;

import java.util.List;

/**
 * PlayerRowAdapter.java
 * Adapter for the dynamic player list in Create Team screen.
 * Each row: jersey number, name input, role spinner, delete icon.
 */
public class PlayerRowAdapter extends RecyclerView.Adapter<PlayerRowAdapter.ViewHolder> {

    public interface OnPlayerChangedListener {
        void onPlayerDeleted(int position);
        void onPlayerCountChanged(int count);
    }

    private final List<Player>            players;
    private final OnPlayerChangedListener listener;
    private final String[]                roles;

    public PlayerRowAdapter(List<Player> players, OnPlayerChangedListener listener) {
        this.players  = players;
        this.listener = listener;
        this.roles    = new String[]{
                Player.ROLE_BATSMAN,
                Player.ROLE_BOWLER,
                Player.ROLE_ALLROUNDER,
                Player.ROLE_KEEPER
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player, position, listener);
    }

    @Override
    public int getItemCount() { return players.size(); }

    /** Add a blank player row and refresh. */
    public void addPlayer() {
        Player p = new Player();
        p.setJerseyNumber(players.size() + 1);
        p.setRole(Player.ROLE_BATSMAN);
        players.add(p);
        notifyItemInserted(players.size() - 1);
        if (listener != null) listener.onPlayerCountChanged(players.size());
    }

    /** Retrieve all players with their current form data. */
    public List<Player> getPlayers() {
        return players;
    }

    // ============================================================
    // ViewHolder
    // ============================================================
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvJersey;
        TextInputEditText etName;
        Spinner        spinnerRole;
        CheckBox       cbCaptain;
        ImageButton    btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJersey    = itemView.findViewById(R.id.tv_jersey_number);
            etName      = itemView.findViewById(R.id.et_player_name);
            spinnerRole = itemView.findViewById(R.id.spinner_role);
            cbCaptain   = itemView.findViewById(R.id.cb_is_captain);
            btnDelete   = itemView.findViewById(R.id.btn_delete_player);
        }

        void bind(Player player, int position, OnPlayerChangedListener listener) {
            // Jersey number display
            tvJersey.setText("#" + (position + 1));

            // Pre-fill name if available
            if (player.getName() != null) {
                etName.setText(player.getName());
            }

            // Set role spinner adapter
            android.widget.ArrayAdapter<String> roleAdapter =
                    new android.widget.ArrayAdapter<>(itemView.getContext(),
                            android.R.layout.simple_spinner_item, roles);
            roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRole.setAdapter(roleAdapter);

            // Pre-select role
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].equals(player.getRole())) {
                    spinnerRole.setSelection(i);
                    break;
                }
            }

            // Track name changes back to model
            etName.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    player.setName(s.toString().trim());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });

            // Track role selection
            spinnerRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                           int pos, long id) {
                    player.setRole(roles[pos]);
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });

            // Track captain status
            cbCaptain.setOnCheckedChangeListener(null); // Clear first to avoid trigger loop
            cbCaptain.setChecked(player.isCaptain());
            cbCaptain.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck all other players
                    for (int i = 0; i < players.size(); i++) {
                        if (i != getAdapterPosition()) {
                            players.get(i).setCaptain(false);
                        }
                    }
                    player.setCaptain(true);
                    notifyDataSetChanged(); // Refresh all to show only one check
                } else {
                    player.setCaptain(false);
                }
            });

            // Delete
            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_ID) {
                    players.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, players.size());
                    if (listener != null) {
                        listener.onPlayerDeleted(pos);
                        listener.onPlayerCountChanged(players.size());
                    }
                }
            });
        }
    }
}
