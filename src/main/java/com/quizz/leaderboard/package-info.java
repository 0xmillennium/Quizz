/**
 * Read-only leaderboard ranking views.
 *
 * <p>Leaderboards are derived from completed submitted attempts and do not have
 * a dedicated leaderboard entity or table. The public ranking model exposes
 * display names, not email addresses, and keeps deterministic ordering in the
 * JDBC query model.</p>
 */
package com.quizz.leaderboard;
