package com.test.crickethub.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Match.java — Represents a cricket match with both innings
 */
public class Match {

    // Match status constants
    public static final String STATUS_SETUP      = "setup";
    public static final String STATUS_LIVE       = "live";
    public static final String STATUS_COMPLETED  = "completed";

    private long id;
    private long teamAId;
    private long teamBId;
    private String teamAName;
    private String teamBName;

    private int totalOvers;
    private int inningsNumber = 1; // current innings (1 or 2)

    // Toss
    private long tossWinnerId;
    private String tossElection; // "bat" or "bowl"

    // Innings scores
    private int innings1Score    = 0;
    private int innings1Wickets  = 0;
    private int innings1Overs    = 0;
    private int innings1Balls    = 0; // balls in current over

    private int innings2Score    = 0;
    private int innings2Wickets  = 0;
    private int innings2Overs    = 0;
    private int innings2Balls    = 0;

    // Batting / Bowling Teams (set at innings start)
    private long battingTeamId;
    private long bowlingTeamId;

    private String status = STATUS_SETUP;
    private String result; // e.g., "Team A won by 14 runs"
    private long winnerId;

    private long createdAt; // Unix timestamp
    private String dateFormatted; // For display

    // ============================================================
    // Computed properties
    // ============================================================

    public int getTarget() {
        return innings1Score + 1; // Target for 2nd innings
    }

    public int getCurrentScore() {
        return inningsNumber == 1 ? innings1Score : innings2Score;
    }

    public int getCurrentWickets() {
        return inningsNumber == 1 ? innings1Wickets : innings2Wickets;
    }

    public int getCurrentOvers() {
        return inningsNumber == 1 ? innings1Overs : innings2Overs;
    }

    public int getCurrentBalls() {
        return inningsNumber == 1 ? innings1Balls : innings2Balls;
    }

    /** Formatted overs string: "8.3" means 8 complete overs + 3 balls */
    public String getOversFormatted() {
        return getCurrentOvers() + "." + getCurrentBalls();
    }

    /** Current run rate: runs per over */
    public double getCurrentRunRate() {
        double totalOversCompleted = getCurrentOvers() + getCurrentBalls() / 6.0;
        if (totalOversCompleted == 0) return 0.0;
        return getCurrentScore() / totalOversCompleted;
    }

    /** Required run rate for 2nd innings */
    public double getRequiredRunRate() {
        if (inningsNumber != 2) return 0.0;
        int runsRequired = getTarget() - innings2Score;
        double oversLeft = (totalOvers - innings2Overs) - innings2Balls / 6.0;
        if (oversLeft <= 0) return 99.99;
        return runsRequired / oversLeft;
    }

    public boolean isInningsComplete(int teamSize) {
        int maxWickets = Math.max(1, teamSize - 1);
        return getCurrentWickets() >= maxWickets || (getCurrentOvers() >= totalOvers && getCurrentBalls() == 0);
    }

    public boolean isMatchComplete() {
        return STATUS_COMPLETED.equals(status);
    }

    // ============================================================
    // Constructors
    // ============================================================

    public Match() {
        this.createdAt = System.currentTimeMillis();
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTeamAId() { return teamAId; }
    public void setTeamAId(long teamAId) { this.teamAId = teamAId; }

    public long getTeamBId() { return teamBId; }
    public void setTeamBId(long teamBId) { this.teamBId = teamBId; }

    public String getTeamAName() { return teamAName; }
    public void setTeamAName(String teamAName) { this.teamAName = teamAName; }

    public String getTeamBName() { return teamBName; }
    public void setTeamBName(String teamBName) { this.teamBName = teamBName; }

    public int getTotalOvers() { return totalOvers; }
    public void setTotalOvers(int totalOvers) { this.totalOvers = totalOvers; }

    public int getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(int inningsNumber) { this.inningsNumber = inningsNumber; }

    public long getTossWinnerId() { return tossWinnerId; }
    public void setTossWinnerId(long tossWinnerId) { this.tossWinnerId = tossWinnerId; }

    public String getTossElection() { return tossElection; }
    public void setTossElection(String tossElection) { this.tossElection = tossElection; }

    public int getInnings1Score() { return innings1Score; }
    public void setInnings1Score(int innings1Score) { this.innings1Score = innings1Score; }
    public void addInnings1Score(int r) { this.innings1Score += r; }

    public int getInnings1Wickets() { return innings1Wickets; }
    public void setInnings1Wickets(int innings1Wickets) { this.innings1Wickets = innings1Wickets; }
    public void incrementInnings1Wickets() { this.innings1Wickets++; }

    public int getInnings1Overs() { return innings1Overs; }
    public void setInnings1Overs(int innings1Overs) { this.innings1Overs = innings1Overs; }

    public int getInnings1Balls() { return innings1Balls; }
    public void setInnings1Balls(int innings1Balls) { this.innings1Balls = innings1Balls; }

    public int getInnings2Score() { return innings2Score; }
    public void setInnings2Score(int innings2Score) { this.innings2Score = innings2Score; }
    public void addInnings2Score(int r) { this.innings2Score += r; }

    public int getInnings2Wickets() { return innings2Wickets; }
    public void setInnings2Wickets(int innings2Wickets) { this.innings2Wickets = innings2Wickets; }
    public void incrementInnings2Wickets() { this.innings2Wickets++; }

    public int getInnings2Overs() { return innings2Overs; }
    public void setInnings2Overs(int innings2Overs) { this.innings2Overs = innings2Overs; }

    public int getInnings2Balls() { return innings2Balls; }
    public void setInnings2Balls(int innings2Balls) { this.innings2Balls = innings2Balls; }

    public long getBattingTeamId() { return battingTeamId; }
    public void setBattingTeamId(long battingTeamId) { this.battingTeamId = battingTeamId; }

    public long getBowlingTeamId() { return bowlingTeamId; }
    public void setBowlingTeamId(long bowlingTeamId) { this.bowlingTeamId = bowlingTeamId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public long getWinnerId() { return winnerId; }
    public void setWinnerId(long winnerId) { this.winnerId = winnerId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getDateFormatted() { return dateFormatted; }
    public void setDateFormatted(String dateFormatted) { this.dateFormatted = dateFormatted; }
}
