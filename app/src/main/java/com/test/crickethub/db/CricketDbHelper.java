package com.test.crickethub.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.test.crickethub.model.BallEvent;
import com.test.crickethub.model.Match;
import com.test.crickethub.model.Player;
import com.test.crickethub.model.Team;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CricketDbHelper.java
 * =====================
 * SQLite database helper for CricketHub — offline-first storage.
 *
 * Tables:
 *   teams       — team registry
 *   players     — player roster (linked to team)
 *   matches     — match records
 *   ball_events — ball-by-ball delivery log
 */
public class CricketDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "CricketDbHelper";

    // Database Metadata
    private static final String DB_NAME    = "crickethub.db";
    private static final int    DB_VERSION = 2;

    // ============================================================
    // Table: teams
    // ============================================================
    private static final String TABLE_TEAMS      = "teams";
    private static final String COL_TEAM_ID      = "_id";
    private static final String COL_TEAM_NAME    = "name";
    private static final String COL_TEAM_LOGO    = "logo_path";
    private static final String COL_TEAM_DESC    = "description";
    private static final String COL_TEAM_CREATED = "created_at";

    private static final String CREATE_TABLE_TEAMS =
            "CREATE TABLE " + TABLE_TEAMS + " (" +
            COL_TEAM_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TEAM_NAME    + " TEXT NOT NULL, " +
            COL_TEAM_LOGO    + " TEXT, " +
            COL_TEAM_DESC    + " TEXT, " +
            COL_TEAM_CREATED + " INTEGER" +
            ")";

    // ============================================================
    // Table: players
    // ============================================================
    private static final String TABLE_PLAYERS   = "players";
    private static final String COL_PLY_ID      = "_id";
    private static final String COL_PLY_TEAM_ID = "team_id";
    private static final String COL_PLY_NAME    = "name";
    private static final String COL_PLY_JERSEY  = "jersey_number";
    private static final String COL_PLY_ROLE    = "role";

    private static final String CREATE_TABLE_PLAYERS =
            "CREATE TABLE " + TABLE_PLAYERS + " (" +
            COL_PLY_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_PLY_TEAM_ID + " INTEGER NOT NULL, " +
            COL_PLY_NAME    + " TEXT NOT NULL, " +
            COL_PLY_JERSEY  + " INTEGER DEFAULT 0, " +
            COL_PLY_ROLE    + " TEXT DEFAULT 'Batsman', " +
            "FOREIGN KEY (" + COL_PLY_TEAM_ID + ") REFERENCES " + TABLE_TEAMS + "(" + COL_TEAM_ID + ")" +
            ")";

    // ============================================================
    // Table: matches
    // ============================================================
    private static final String TABLE_MATCHES        = "matches";
    private static final String COL_MTH_ID           = "_id";
    private static final String COL_MTH_TEAM_A_ID    = "team_a_id";
    private static final String COL_MTH_TEAM_B_ID    = "team_b_id";
    private static final String COL_MTH_TEAM_A_NAME  = "team_a_name";
    private static final String COL_MTH_TEAM_B_NAME  = "team_b_name";
    private static final String COL_MTH_TOTAL_OVERS  = "total_overs";
    private static final String COL_MTH_INNINGS      = "innings_number";
    private static final String COL_MTH_TOSS_WINNER  = "toss_winner_id";
    private static final String COL_MTH_TOSS_ELECT   = "toss_election";
    private static final String COL_MTH_BAT_TEAM     = "batting_team_id";
    private static final String COL_MTH_BOWL_TEAM    = "bowling_team_id";
    private static final String COL_MTH_INN1_SCORE   = "innings1_score";
    private static final String COL_MTH_INN1_WKTS    = "innings1_wickets";
    private static final String COL_MTH_INN1_OVERS   = "innings1_overs";
    private static final String COL_MTH_INN1_BALLS   = "innings1_balls";
    private static final String COL_MTH_INN2_SCORE   = "innings2_score";
    private static final String COL_MTH_INN2_WKTS    = "innings2_wickets";
    private static final String COL_MTH_INN2_OVERS   = "innings2_overs";
    private static final String COL_MTH_INN2_BALLS   = "innings2_balls";
    private static final String COL_MTH_STATUS       = "status";
    private static final String COL_MTH_RESULT       = "result";
    private static final String COL_MTH_WINNER_ID    = "winner_id";
    private static final String COL_MTH_CREATED_AT   = "created_at";

    private static final String CREATE_TABLE_MATCHES =
            "CREATE TABLE " + TABLE_MATCHES + " (" +
            COL_MTH_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_MTH_TEAM_A_ID   + " INTEGER, " +
            COL_MTH_TEAM_B_ID   + " INTEGER, " +
            COL_MTH_TEAM_A_NAME + " TEXT, " +
            COL_MTH_TEAM_B_NAME + " TEXT, " +
            COL_MTH_TOTAL_OVERS + " INTEGER DEFAULT 20, " +
            COL_MTH_INNINGS     + " INTEGER DEFAULT 1, " +
            COL_MTH_TOSS_WINNER + " INTEGER, " +
            COL_MTH_TOSS_ELECT  + " TEXT, " +
            COL_MTH_BAT_TEAM    + " INTEGER, " +
            COL_MTH_BOWL_TEAM   + " INTEGER, " +
            COL_MTH_INN1_SCORE  + " INTEGER DEFAULT 0, " +
            COL_MTH_INN1_WKTS   + " INTEGER DEFAULT 0, " +
            COL_MTH_INN1_OVERS  + " INTEGER DEFAULT 0, " +
            COL_MTH_INN1_BALLS  + " INTEGER DEFAULT 0, " +
            COL_MTH_INN2_SCORE  + " INTEGER DEFAULT 0, " +
            COL_MTH_INN2_WKTS   + " INTEGER DEFAULT 0, " +
            COL_MTH_INN2_OVERS  + " INTEGER DEFAULT 0, " +
            COL_MTH_INN2_BALLS  + " INTEGER DEFAULT 0, " +
            COL_MTH_STATUS      + " TEXT DEFAULT 'setup', " +
            COL_MTH_RESULT      + " TEXT, " +
            COL_MTH_WINNER_ID   + " INTEGER DEFAULT 0, " +
            COL_MTH_CREATED_AT  + " INTEGER" +
            ")";

    // ============================================================
    // Table: ball_events
    // ============================================================
    private static final String TABLE_BALLS      = "ball_events";
    private static final String COL_BALL_ID      = "_id";
    private static final String COL_BALL_MTH_ID  = "match_id";
    private static final String COL_BALL_INNINGS = "innings_number";
    private static final String COL_BALL_OVER    = "over_number";
    private static final String COL_BALL_IN_OVER = "ball_in_over";
    private static final String COL_BALL_TYPE    = "delivery_type";
    private static final String COL_BALL_RUNS    = "runs_scored";
    private static final String COL_BALL_EXTRAS  = "extras";
    private static final String COL_BALL_BAT_ID  = "batsman_id";
    private static final String COL_BALL_BOW_ID  = "bowler_id";
    private static final String COL_BALL_WICKET  = "is_wicket";
    private static final String COL_BALL_DIS_TYP = "dismissal_type";
    private static final String COL_BALL_DIS_PLY = "dismissed_player_id";
    private static final String COL_BALL_FIELDER = "fielder_id";
    private static final String COL_BALL_LABEL   = "display_label";

    private static final String CREATE_TABLE_BALLS =
            "CREATE TABLE " + TABLE_BALLS + " (" +
            COL_BALL_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_BALL_MTH_ID  + " INTEGER NOT NULL, " +
            COL_BALL_INNINGS + " INTEGER, " +
            COL_BALL_OVER    + " INTEGER, " +
            COL_BALL_IN_OVER + " INTEGER, " +
            COL_BALL_TYPE    + " TEXT, " +
            COL_BALL_RUNS    + " INTEGER DEFAULT 0, " +
            COL_BALL_EXTRAS  + " INTEGER DEFAULT 0, " +
            COL_BALL_BAT_ID  + " INTEGER DEFAULT 0, " +
            COL_BALL_BOW_ID  + " INTEGER DEFAULT 0, " +
            COL_BALL_WICKET  + " INTEGER DEFAULT 0, " +
            COL_BALL_DIS_TYP + " TEXT, " +
            COL_BALL_DIS_PLY + " INTEGER DEFAULT 0, " +
            COL_BALL_FIELDER + " INTEGER DEFAULT 0, " +
            COL_BALL_LABEL   + " TEXT" +
            ")";

    // ============================================================
    // Singleton instance
    // ============================================================
    private static CricketDbHelper instance;

    public static synchronized CricketDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CricketDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private CricketDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ============================================================
    // Lifecycle
    // ============================================================

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TEAMS);
        db.execSQL(CREATE_TABLE_PLAYERS);
        db.execSQL(CREATE_TABLE_MATCHES);
        db.execSQL(CREATE_TABLE_BALLS);
        Log.d(TAG, "Database created: " + DB_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Version 2 migration -> safely inject description column into existing team table
            db.execSQL("ALTER TABLE " + TABLE_TEAMS + " ADD COLUMN " + COL_TEAM_DESC + " TEXT");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ============================================================
    // TEAM OPERATIONS
    // ============================================================

    public long insertTeam(Team team) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TEAM_NAME, team.getName());
        cv.put(COL_TEAM_LOGO, team.getLogoPath());
        cv.put(COL_TEAM_DESC, team.getDescription());
        cv.put(COL_TEAM_CREATED, System.currentTimeMillis());
        long id = db.insert(TABLE_TEAMS, null, cv);
        team.setId(id);
        return id;
    }

    public void updateTeam(Team team) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TEAM_NAME, team.getName());
        cv.put(COL_TEAM_LOGO, team.getLogoPath());
        cv.put(COL_TEAM_DESC, team.getDescription());
        db.update(TABLE_TEAMS, cv, COL_TEAM_ID + "=?", new String[]{String.valueOf(team.getId())});
    }

    /** Load all teams ordered by name. */
    public List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TEAMS, null, null, null, null, null, COL_TEAM_NAME + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                Team t = new Team();
                t.setId(c.getLong(c.getColumnIndexOrThrow(COL_TEAM_ID)));
                t.setName(c.getString(c.getColumnIndexOrThrow(COL_TEAM_NAME)));
                t.setLogoPath(c.getString(c.getColumnIndexOrThrow(COL_TEAM_LOGO)));
                // We use getColumnIndex because old versions without migration might lack it, but we migrated everything
                int descIdx = c.getColumnIndex(COL_TEAM_DESC);
                if (descIdx != -1) {
                    t.setDescription(c.getString(descIdx));
                }
                teams.add(t);
            }
            c.close();
        }
        return teams;
    }

    public Team getTeamById(long teamId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TEAMS, null, COL_TEAM_ID + "=?", new String[]{String.valueOf(teamId)}, null, null, null);
        Team t = null;
        if (c != null) {
            if (c.moveToFirst()) {
                t = new Team();
                t.setId(c.getLong(c.getColumnIndexOrThrow(COL_TEAM_ID)));
                t.setName(c.getString(c.getColumnIndexOrThrow(COL_TEAM_NAME)));
                t.setLogoPath(c.getString(c.getColumnIndexOrThrow(COL_TEAM_LOGO)));
                int descIdx = c.getColumnIndex(COL_TEAM_DESC);
                if (descIdx != -1) {
                    t.setDescription(c.getString(descIdx));
                }
            }
            c.close();
        }
        return t;
    }

    /** Get team count for dashboard stat. */
    public int getTeamCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TEAMS, null);
        int count = 0;
        if (c != null) {
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
        }
        return count;
    }

    /** Delete team and its players. */
    public void deleteTeam(long teamId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PLAYERS, COL_PLY_TEAM_ID + "=?", new String[]{String.valueOf(teamId)});
        db.delete(TABLE_TEAMS, COL_TEAM_ID + "=?", new String[]{String.valueOf(teamId)});
    }

    // ============================================================
    // PLAYER OPERATIONS
    // ============================================================

    /** Insert a single player for a given team. */
    public long insertPlayer(Player player) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PLY_TEAM_ID, player.getTeamId());
        cv.put(COL_PLY_NAME, player.getName());
        cv.put(COL_PLY_JERSEY, player.getJerseyNumber());
        cv.put(COL_PLY_ROLE, player.getRole());
        long id = db.insert(TABLE_PLAYERS, null, cv);
        player.setId(id);
        return id;
    }

    /** Get total player count across all teams. */
    public int getTotalPlayers() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLAYERS, null);
        int count = 0;
        if (c != null) {
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
        }
        return count;
    }

    /** Save a list of players for a team (replaces existing). */
    public void savePlayersForTeam(long teamId, List<Player> players) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_PLAYERS, COL_PLY_TEAM_ID + "=?", new String[]{String.valueOf(teamId)});
            for (Player p : players) {
                p.setTeamId(teamId);
                ContentValues cv = new ContentValues();
                cv.put(COL_PLY_TEAM_ID, teamId);
                cv.put(COL_PLY_NAME, p.getName());
                cv.put(COL_PLY_JERSEY, p.getJerseyNumber());
                cv.put(COL_PLY_ROLE, p.getRole());
                db.insert(TABLE_PLAYERS, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Get all players for a given team. */
    public List<Player> getPlayersForTeam(long teamId) {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PLAYERS, null,
                COL_PLY_TEAM_ID + "=?", new String[]{String.valueOf(teamId)},
                null, null, COL_PLY_JERSEY + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                Player p = new Player();
                p.setId(c.getLong(c.getColumnIndexOrThrow(COL_PLY_ID)));
                p.setTeamId(teamId);
                p.setName(c.getString(c.getColumnIndexOrThrow(COL_PLY_NAME)));
                p.setJerseyNumber(c.getInt(c.getColumnIndexOrThrow(COL_PLY_JERSEY)));
                p.setRole(c.getString(c.getColumnIndexOrThrow(COL_PLY_ROLE)));
                players.add(p);
            }
            c.close();
        }
        return players;
    }

    // ============================================================
    // MATCH OPERATIONS
    // ============================================================

    /** Insert a new match record. Returns the generated match ID. */
    public long insertMatch(Match match) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = buildMatchContentValues(match);
        cv.put(COL_MTH_CREATED_AT, System.currentTimeMillis());
        long id = db.insert(TABLE_MATCHES, null, cv);
        match.setId(id);
        return id;
    }

    /** Update existing match state (score, wickets, overs, status). */
    public void updateMatch(Match match) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = buildMatchContentValues(match);
        db.update(TABLE_MATCHES, cv, COL_MTH_ID + "=?",
                new String[]{String.valueOf(match.getId())});
    }

    private ContentValues buildMatchContentValues(Match match) {
        ContentValues cv = new ContentValues();
        cv.put(COL_MTH_TEAM_A_ID,   match.getTeamAId());
        cv.put(COL_MTH_TEAM_B_ID,   match.getTeamBId());
        cv.put(COL_MTH_TEAM_A_NAME, match.getTeamAName());
        cv.put(COL_MTH_TEAM_B_NAME, match.getTeamBName());
        cv.put(COL_MTH_TOTAL_OVERS, match.getTotalOvers());
        cv.put(COL_MTH_INNINGS,     match.getInningsNumber());
        cv.put(COL_MTH_TOSS_WINNER, match.getTossWinnerId());
        cv.put(COL_MTH_TOSS_ELECT,  match.getTossElection());
        cv.put(COL_MTH_BAT_TEAM,    match.getBattingTeamId());
        cv.put(COL_MTH_BOWL_TEAM,   match.getBowlingTeamId());
        cv.put(COL_MTH_INN1_SCORE,  match.getInnings1Score());
        cv.put(COL_MTH_INN1_WKTS,   match.getInnings1Wickets());
        cv.put(COL_MTH_INN1_OVERS,  match.getInnings1Overs());
        cv.put(COL_MTH_INN1_BALLS,  match.getInnings1Balls());
        cv.put(COL_MTH_INN2_SCORE,  match.getInnings2Score());
        cv.put(COL_MTH_INN2_WKTS,   match.getInnings2Wickets());
        cv.put(COL_MTH_INN2_OVERS,  match.getInnings2Overs());
        cv.put(COL_MTH_INN2_BALLS,  match.getInnings2Balls());
        cv.put(COL_MTH_STATUS,      match.getStatus());
        cv.put(COL_MTH_RESULT,      match.getResult());
        cv.put(COL_MTH_WINNER_ID,   match.getWinnerId());
        return cv;
    }

    /** Get match by ID. Returns null if not found. */
    public Match getMatchById(long matchId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_MATCHES, null,
                COL_MTH_ID + "=?", new String[]{String.valueOf(matchId)},
                null, null, null);
        Match match = null;
        if (c != null && c.moveToFirst()) {
            match = cursorToMatch(c);
            c.close();
        }
        return match;
    }

    /** Get all matches ordered by date descending. */
    public List<Match> getAllMatches() {
        List<Match> matches = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_MATCHES, null, null, null, null, null,
                COL_MTH_CREATED_AT + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                matches.add(cursorToMatch(c));
            }
            c.close();
        }
        return matches;
    }

    /** Get completed matches for history tab. */
    public List<Match> getCompletedMatches() {
        List<Match> matches = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_MATCHES, null,
                COL_MTH_STATUS + "=?", new String[]{Match.STATUS_COMPLETED},
                null, null, COL_MTH_CREATED_AT + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                matches.add(cursorToMatch(c));
            }
            c.close();
        }
        return matches;
    }

    /** Get count of completed matches for dashboard stats. */
    public int getMatchCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_MATCHES +
                " WHERE " + COL_MTH_STATUS + "=?",
                new String[]{Match.STATUS_COMPLETED});
        int count = 0;
        if (c != null) {
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
        }
        return count;
    }

    /** Get the highest innings score among completed matches. */
    public int getHighestScore() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT MAX(" + COL_MTH_INN1_SCORE + ") FROM " + TABLE_MATCHES, null);
        int max = 0;
        if (c != null) {
            if (c.moveToFirst()) max = c.getInt(0);
            c.close();
        }
        return max;
    }

    private Match cursorToMatch(Cursor c) {
        Match m = new Match();
        m.setId(c.getLong(c.getColumnIndexOrThrow(COL_MTH_ID)));
        m.setTeamAId(c.getLong(c.getColumnIndexOrThrow(COL_MTH_TEAM_A_ID)));
        m.setTeamBId(c.getLong(c.getColumnIndexOrThrow(COL_MTH_TEAM_B_ID)));
        m.setTeamAName(c.getString(c.getColumnIndexOrThrow(COL_MTH_TEAM_A_NAME)));
        m.setTeamBName(c.getString(c.getColumnIndexOrThrow(COL_MTH_TEAM_B_NAME)));
        m.setTotalOvers(c.getInt(c.getColumnIndexOrThrow(COL_MTH_TOTAL_OVERS)));
        m.setInningsNumber(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INNINGS)));
        m.setTossWinnerId(c.getLong(c.getColumnIndexOrThrow(COL_MTH_TOSS_WINNER)));
        m.setTossElection(c.getString(c.getColumnIndexOrThrow(COL_MTH_TOSS_ELECT)));
        m.setBattingTeamId(c.getLong(c.getColumnIndexOrThrow(COL_MTH_BAT_TEAM)));
        m.setBowlingTeamId(c.getLong(c.getColumnIndexOrThrow(COL_MTH_BOWL_TEAM)));
        m.setInnings1Score(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INN1_SCORE)));
        m.setInnings1Wickets(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INN1_WKTS)));
        m.setInnings1Overs(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INN1_OVERS)));
        m.setInnings1Balls(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INN1_BALLS)));
        m.setInnings2Score(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INN2_SCORE)));
        m.setInnings2Wickets(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INN2_WKTS)));
        m.setInnings2Overs(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INN2_OVERS)));
        m.setInnings2Balls(c.getInt(c.getColumnIndexOrThrow(COL_MTH_INN2_BALLS)));
        m.setStatus(c.getString(c.getColumnIndexOrThrow(COL_MTH_STATUS)));
        m.setResult(c.getString(c.getColumnIndexOrThrow(COL_MTH_RESULT)));
        m.setWinnerId(c.getLong(c.getColumnIndexOrThrow(COL_MTH_WINNER_ID)));
        long createdAt = c.getLong(c.getColumnIndexOrThrow(COL_MTH_CREATED_AT));
        m.setCreatedAt(createdAt);
        m.setDateFormatted(new SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
                .format(new Date(createdAt)));
        return m;
    }

    // ============================================================
    // BALL EVENT OPERATIONS
    // ============================================================

    /** Record a single ball delivery. Returns the inserted row ID. */
    public long insertBallEvent(BallEvent ball) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_BALL_MTH_ID,  ball.getMatchId());
        cv.put(COL_BALL_INNINGS, ball.getInningsNumber());
        cv.put(COL_BALL_OVER,    ball.getOverNumber());
        cv.put(COL_BALL_IN_OVER, ball.getBallInOver());
        cv.put(COL_BALL_TYPE,    ball.getDeliveryType());
        cv.put(COL_BALL_RUNS,    ball.getRunsScored());
        cv.put(COL_BALL_EXTRAS,  ball.getExtras());
        cv.put(COL_BALL_BAT_ID,  ball.getBatsmanId());
        cv.put(COL_BALL_BOW_ID,  ball.getBowlerId());
        cv.put(COL_BALL_WICKET,  ball.isWicket() ? 1 : 0);
        cv.put(COL_BALL_DIS_TYP, ball.getDismissalType());
        cv.put(COL_BALL_DIS_PLY, ball.getDismissedPlayerId());
        cv.put(COL_BALL_FIELDER, ball.getFielderId());
        cv.put(COL_BALL_LABEL,   ball.getDisplayLabel());
        long id = db.insert(TABLE_BALLS, null, cv);
        ball.setId(id);
        return id;
    }

    /** Delete last ball event (for undo functionality). */
    public void deleteLastBallEvent(long matchId, int inningsNumber) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
            "DELETE FROM " + TABLE_BALLS +
            " WHERE " + COL_BALL_ID + " = (" +
            "SELECT MAX(" + COL_BALL_ID + ") FROM " + TABLE_BALLS +
            " WHERE " + COL_BALL_MTH_ID + "=? AND " + COL_BALL_INNINGS + "=?)",
            new Object[]{matchId, inningsNumber}
        );
    }

    /** Get all balls for a given innings (for scorecard replay). */
    public List<BallEvent> getBallsForInnings(long matchId, int inningsNumber) {
        List<BallEvent> balls = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_BALLS, null,
                COL_BALL_MTH_ID + "=? AND " + COL_BALL_INNINGS + "=?",
                new String[]{String.valueOf(matchId), String.valueOf(inningsNumber)},
                null, null, COL_BALL_ID + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                BallEvent b = new BallEvent();
                b.setId(c.getLong(c.getColumnIndexOrThrow(COL_BALL_ID)));
                b.setMatchId(matchId);
                b.setInningsNumber(c.getInt(c.getColumnIndexOrThrow(COL_BALL_INNINGS)));
                b.setOverNumber(c.getInt(c.getColumnIndexOrThrow(COL_BALL_OVER)));
                b.setBallInOver(c.getInt(c.getColumnIndexOrThrow(COL_BALL_IN_OVER)));
                b.setDeliveryType(c.getString(c.getColumnIndexOrThrow(COL_BALL_TYPE)));
                b.setRunsScored(c.getInt(c.getColumnIndexOrThrow(COL_BALL_RUNS)));
                b.setExtras(c.getInt(c.getColumnIndexOrThrow(COL_BALL_EXTRAS)));
                b.setBatsmanId(c.getLong(c.getColumnIndexOrThrow(COL_BALL_BAT_ID)));
                b.setBowlerId(c.getLong(c.getColumnIndexOrThrow(COL_BALL_BOW_ID)));
                b.setWicket(c.getInt(c.getColumnIndexOrThrow(COL_BALL_WICKET)) == 1);
                b.setDismissalType(c.getString(c.getColumnIndexOrThrow(COL_BALL_DIS_TYP)));
                b.setDismissedPlayerId(c.getLong(c.getColumnIndexOrThrow(COL_BALL_DIS_PLY)));
                b.setFielderId(c.getLong(c.getColumnIndexOrThrow(COL_BALL_FIELDER)));
                b.setDisplayLabel(c.getString(c.getColumnIndexOrThrow(COL_BALL_LABEL)));
                balls.add(b);
            }
            c.close();
        }
        return balls;
    }

    /** Get the last N balls for the "This Over" strip. */
    public List<BallEvent> getCurrentOverBalls(long matchId, int inningsNumber, int overNumber) {
        List<BallEvent> balls = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String where = COL_BALL_MTH_ID + "=? AND " + COL_BALL_INNINGS + "=? AND " + COL_BALL_OVER + "=?";
        Cursor c = db.query(TABLE_BALLS, new String[]{COL_BALL_LABEL, COL_BALL_TYPE},
                where,
                new String[]{String.valueOf(matchId), String.valueOf(inningsNumber), String.valueOf(overNumber)},
                null, null, COL_BALL_ID + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                BallEvent b = new BallEvent();
                b.setDisplayLabel(c.getString(0));
                b.setDeliveryType(c.getString(1));
                balls.add(b);
            }
            c.close();
        }
        return balls;
    }
}
