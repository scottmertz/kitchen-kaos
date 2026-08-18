package com.kitchenkaos.game.save;

/**
 * Everything that survives a job change, a restaurant closing, or a
 * new career being started fresh — until the career itself is
 * deliberately, permanently deleted. Deliberately minimal right now:
 * only fields with a REAL system behind them today. Career stats,
 * Chef reputation, personal wallet, global staff pool, etc. get added
 * here as their own systems are designed and built — not invented
 * ahead of time as empty placeholders.
 *
 * Plain public-field DTO — libGDX's Json class serializes it directly,
 * no custom logic needed. Not used for gameplay behavior, only storage.
 */
public class PersistentChefProfile {

    public static final int CURRENT_VERSION = 1;

    public int saveVersion = CURRENT_VERSION;
    public String chefName = "";
    public String birthday = ""; // ISO-8601 date string once Origin Story writes it
}