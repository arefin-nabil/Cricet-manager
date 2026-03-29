package com.test.crickethub.model;

/**
 * Player.java — Represents a cricket player
 */
public class Player {

    // Player roles
    public static final String ROLE_BATSMAN    = "Batsman";
    public static final String ROLE_BOWLER     = "Bowler";
    public static final String ROLE_ALLROUNDER = "All-rounder";
    public static final String ROLE_KEEPER     = "Keeper";

    private long id;
    private long teamId;
    private String name;
    private int jerseyNumber;
    private String role;

    // ============================================================
    // In-match live stats (transient – not stored in DB directly)
    // ============================================================
    private int runs       = 0;
    private int balls      = 0;
    private int fours      = 0;
    private int sixes      = 0;
    private boolean isOut  = false;
    private String howOut  = ""; // e.g., "c Dhoni b Shami"

    // Bowling stats
    private int oversBowled   = 0;   // full overs
    private int ballsBowled   = 0;   // balls in current over
    private int maidens       = 0;
    private int runsConceded  = 0;
    private int wicketsTaken  = 0;

    // ============================================================
    // Constructors
    // ============================================================

    public Player() {}

    public Player(long teamId, String name, int jerseyNumber, String role) {
        this.teamId = teamId;
        this.name = name;
        this.jerseyNumber = jerseyNumber;
        this.role = role;
    }

    // ============================================================
    // Computed Properties
    // ============================================================

    /** Strike rate: (runs / balls) * 100, returns 0.0 if no balls faced */
    public double getStrikeRate() {
        if (balls == 0) return 0.0;
        return (double) runs / balls * 100.0;
    }

    /** Economics rate: (runs / overs), returns 0.0 if no overs bowled */
    public double getEconomy() {
        double totalOvers = oversBowled + (ballsBowled / 6.0);
        if (totalOvers <= 0) return 0.0;
        return runsConceded / totalOvers;
    }

    public double getEconomyRate() {
        return getEconomy();
    }

    public double getOversBowledDecimal() {
        return oversBowled + (ballsBowled / 6.0);
    }

    /** Formatted overs string e.g., "3.4" */
    public String getOversBowledFormatted() {
        return oversBowled + "." + ballsBowled;
    }

    /** Bowling figures: Overs-Maidens-Runs-Wickets */
    public String getBowlingFigures() {
        return getOversBowledFormatted() + "-0-" + runsConceded + "-" + wicketsTaken;
    }

    // ============================================================
    // Getters and Setters
    // ============================================================

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTeamId() { return teamId; }
    public void setTeamId(long teamId) { this.teamId = teamId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(int jerseyNumber) { this.jerseyNumber = jerseyNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }
    public void addRuns(int r) { this.runs += r; }

    public int getBalls() { return balls; }
    public void setBalls(int balls) { this.balls = balls; }
    public void addBall() { this.balls++; }

    public int getFours() { return fours; }
    public void incrementFours() { this.fours++; }

    public int getSixes() { return sixes; }
    public void incrementSixes() { this.sixes++; }

    public boolean isOut() { return isOut; }
    public void setOut(boolean out) { isOut = out; }

    public String getHowOut() { return howOut; }
    public void setHowOut(String howOut) { this.howOut = howOut; }

    public int getOversBowled() { return oversBowled; }
    public int getBallsBowled() { return ballsBowled; }

    public void addBallBowled() {
        this.ballsBowled++;
        if (this.ballsBowled >= 6) {
            this.oversBowled++;
            this.ballsBowled = 0;
        }
    }

    public int getRunsConceded() { return runsConceded; }
    public void addRunsConceded(int r) { this.runsConceded += r; }

    public int getWicketsTaken() { return wicketsTaken; }
    public int getWickets() { return wicketsTaken; }
    public void incrementWickets() { this.wicketsTaken++; }

    public int getMaidens() { return maidens; }
    public void setMaidens(int maidens) { this.maidens = maidens; }
    public void addMaiden() { this.maidens++; }

    @Override
    public String toString() {
        return name; // Used by Spinner adapters
    }
}
