package com.test.crickethub.model;

import java.io.Serializable;

public class Tournament implements Serializable {

    public static final String FORMAT_KNOCKOUT = "Knockout";
    public static final String FORMAT_ROUND_ROBIN = "Round Robin";
    public static final String FORMAT_HYBRID = "Hybrid";

    private long id;
    private String name;
    private String format;
    private String startDate;
    private String endDate;
    private int totalTeams;
    private int oversLimit;

    public Tournament() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getTotalTeams() { return totalTeams; }
    public void setTotalTeams(int totalTeams) { this.totalTeams = totalTeams; }

    public int getOversLimit() { return oversLimit; }
    public void setOversLimit(int oversLimit) { this.oversLimit = oversLimit; }
}
