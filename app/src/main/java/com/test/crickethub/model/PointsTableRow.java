package com.test.crickethub.model;

public class PointsTableRow implements Comparable<PointsTableRow> {

    private long teamId;
    private String teamName;
    private int matchesPlayed;
    private int won;
    private int lost;
    private int tied;
    private int points;
    private double netRunRate;

    public PointsTableRow() {}

    public long getTeamId() { return teamId; }
    public void setTeamId(long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    public int getWon() { return won; }
    public void setWon(int won) { this.won = won; }

    public int getLost() { return lost; }
    public void setLost(int lost) { this.lost = lost; }

    public int getTied() { return tied; }
    public void setTied(int tied) { this.tied = tied; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public double getNetRunRate() { return netRunRate; }
    public void setNetRunRate(double netRunRate) { this.netRunRate = netRunRate; }

    @Override
    public int compareTo(PointsTableRow o) {
        // Sort by points descending
        if (this.points != o.points) {
            return Integer.compare(o.points, this.points);
        }
        // If points tied, sort by NRR descending
        return Double.compare(o.netRunRate, this.netRunRate);
    }
}
