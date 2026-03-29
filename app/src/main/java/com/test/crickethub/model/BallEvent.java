package com.test.crickethub.model;

/**
 * BallEvent.java — Represents a single ball delivery event
 * Used for recording every ball in a match (ball-by-ball log)
 */
public class BallEvent {

    // ============================================================
    // Delivery types
    // ============================================================
    public static final String TYPE_NORMAL  = "normal";
    public static final String TYPE_WIDE    = "wide";
    public static final String TYPE_NO_BALL = "noball";
    public static final String TYPE_BYE     = "bye";
    public static final String TYPE_LEG_BYE = "legbye";
    public static final String TYPE_WICKET  = "wicket";

    // Wicket dismissal types
    public static final String DIS_BOWLED    = "Bowled";
    public static final String DIS_CAUGHT    = "Caught";
    public static final String DIS_LBW       = "LBW";
    public static final String DIS_RUN_OUT   = "Run Out";
    public static final String DIS_STUMPED   = "Stumped";
    public static final String DIS_HIT_WKT   = "Hit Wicket";

    private long id;
    private long matchId;
    private int inningsNumber;   // 1 or 2
    private int overNumber;      // 0-indexed
    private int ballInOver;      // 0-indexed (legal balls only for overs count)

    private String deliveryType; // normal, wide, noball, bye, legbye, wicket
    private int runsScored;      // runs off the bat
    private int extras;          // wide=1, no-ball=1, bye=runs, etc.

    private long batsmanId;
    private long bowlerId;

    private boolean isWicket;
    private String dismissalType;   // Bowled, Caught, LBW, Run Out, Stumped...
    private long dismissedPlayerId;
    private long fielderId;          // for Caught / Stumped / Run Out

    // Display label for ball chips (e.g. "4", "W", "WD+1", "·")
    private String displayLabel;

    // ============================================================
    // Constructors
    // ============================================================

    public BallEvent() {}

    /** Factory: normal runs ball */
    public static BallEvent runs(int runs, long batsmanId, long bowlerId) {
        BallEvent b = new BallEvent();
        b.deliveryType = TYPE_NORMAL;
        b.runsScored = runs;
        b.batsmanId = batsmanId;
        b.bowlerId = bowlerId;
        b.displayLabel = runs == 0 ? "·" : String.valueOf(runs);
        return b;
    }

    /** Factory: wide delivery */
    public static BallEvent wide(int extraRuns, long bowlerId) {
        BallEvent b = new BallEvent();
        b.deliveryType = TYPE_WIDE;
        b.extras = 1 + extraRuns;
        b.bowlerId = bowlerId;
        b.displayLabel = extraRuns > 0 ? "WD+" + extraRuns : "WD";
        return b;
    }

    /** Factory: no-ball delivery */
    public static BallEvent noBall(int runsScored, long batsmanId, long bowlerId) {
        BallEvent b = new BallEvent();
        b.deliveryType = TYPE_NO_BALL;
        b.runsScored = runsScored;
        b.extras = 1;
        b.batsmanId = batsmanId;
        b.bowlerId = bowlerId;
        b.displayLabel = "NB";
        return b;
    }

    /** Factory: wicket delivery */
    public static BallEvent wicket(String dismissalType, long batsmanId,
                                   long bowlerId, long fielderId) {
        BallEvent b = new BallEvent();
        b.deliveryType = TYPE_WICKET;
        b.isWicket = true;
        b.dismissalType = dismissalType;
        b.dismissedPlayerId = batsmanId;
        b.batsmanId = batsmanId;
        b.bowlerId = bowlerId;
        b.fielderId = fielderId;
        b.displayLabel = "W";
        return b;
    }

    // ============================================================
    // Computed
    // ============================================================

    /** Total runs from this delivery (batting + extras) */
    public int totalRuns() {
        return runsScored + extras;
    }

    /** Whether this delivery counts as a legal ball (adds to over count) */
    public boolean isLegalDelivery() {
        return !deliveryType.equals(TYPE_WIDE) && !deliveryType.equals(TYPE_NO_BALL);
    }

    // ============================================================
    // Getters & Setters (abbreviated for brevity)
    // ============================================================

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }

    public int getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(int inningsNumber) { this.inningsNumber = inningsNumber; }

    public int getOverNumber() { return overNumber; }
    public void setOverNumber(int overNumber) { this.overNumber = overNumber; }

    public int getBallInOver() { return ballInOver; }
    public void setBallInOver(int ballInOver) { this.ballInOver = ballInOver; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public int getRunsScored() { return runsScored; }
    public void setRunsScored(int runsScored) { this.runsScored = runsScored; }

    public int getExtras() { return extras; }
    public void setExtras(int extras) { this.extras = extras; }

    public long getBatsmanId() { return batsmanId; }
    public void setBatsmanId(long batsmanId) { this.batsmanId = batsmanId; }

    public long getBowlerId() { return bowlerId; }
    public void setBowlerId(long bowlerId) { this.bowlerId = bowlerId; }

    public boolean isWicket() { return isWicket; }
    public void setWicket(boolean wicket) { isWicket = wicket; }

    public String getDismissalType() { return dismissalType; }
    public void setDismissalType(String dismissalType) { this.dismissalType = dismissalType; }

    public long getDismissedPlayerId() { return dismissedPlayerId; }
    public void setDismissedPlayerId(long dismissedPlayerId) { this.dismissedPlayerId = dismissedPlayerId; }

    public long getFielderId() { return fielderId; }
    public void setFielderId(long fielderId) { this.fielderId = fielderId; }

    public String getDisplayLabel() { return displayLabel; }
    public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }
}
