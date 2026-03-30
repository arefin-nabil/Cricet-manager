package com.test.crickethub.model;

/**
 * Team.java — Represents a cricket team
 */
public class Team {

    private long id;
    private String name;
    private String logoPath; // nullable, local file path
    private String description;

    public Team() {}

    public Team(long id, String name, String logoPath) {
        this.id = id;
        this.name = name;
        this.logoPath = logoPath;
    }

    public Team(String name) {
        this.name = name;
    }

    // ============================================================
    // Getters and Setters
    // ============================================================

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Player count — set when loading teams from DB
    private int playerCount;
    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    @Override
    public String toString() {
        return name; // Used by Spinner adapters
    }
}
