package com.test.crickethub.model;

public class TournamentTeam {
    private long tournamentId;
    private long teamId;
    private String teamName; // convenient for joins

    public TournamentTeam() {}

    public long getTournamentId() { return tournamentId; }
    public void setTournamentId(long tournamentId) { this.tournamentId = tournamentId; }

    public long getTeamId() { return teamId; }
    public void setTeamId(long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
}
