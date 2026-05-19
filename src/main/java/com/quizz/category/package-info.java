/**
 * Category master data for grouping questions and quizzes.
 *
 * <p>Categories use an active/inactive lifecycle rather than hard deletion.
 * This package should not grow bidirectional question or quiz collections;
 * feature packages reference categories through explicit service and repository
 * queries.</p>
 */
package com.quizz.category;
