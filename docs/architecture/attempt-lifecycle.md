# Attempt Lifecycle

An attempt is the server-side record of a user's interaction with one quiz. It contains immutable question and answer-option snapshots, timing, saved answers, scoring state, and terminal status.

## Statuses

| Status | Meaning |
| --- | --- |
| `IN_PROGRESS` | The user can still play the attempt if it has not expired. |
| `COMPLETED` | The attempt has been scored and has a completion reason. |
| `ABANDONED` | The attempt was replaced by a restart. |

## Completion Reasons

| Completion reason | Meaning |
| --- | --- |
| `MANUAL` | The user submitted before expiry. |
| `TIME_EXPIRED` | The server completed the attempt because the quiz time window expired. |

## Flows

### Start

Starting a quiz creates a fresh `QuizAttempt` when there is no active resumable attempt. The service consumes one attempt right, samples a randomized question snapshot, shuffles option order, and stores the snapshot.

### Resume

If the user already has a non-overdue `IN_PROGRESS` attempt for the quiz, start returns that attempt. Resume does not consume another attempt right and does not resample questions.

### Autosave

Autosave saves one selected attempt answer option for one attempt question. The request includes `answerRevision`; older revisions are rejected as stale so late browser requests do not overwrite newer state.

### Manual Submit

Before expiry, submit requires an answer entry for every attempt question. The service saves the submitted selections, evaluates the attempt snapshot, and completes the attempt with `MANUAL`.

### Auto-submit After Expiry

After expiry, incoming answer payloads are ignored. The service scores the saved answers, completes the attempt with `TIME_EXPIRED`, and sets `submittedAt` to `expiresAt`.

### Restart

Restart requires an active `IN_PROGRESS` attempt and an available attempt right. The old attempt is abandoned and the replacement attempt copies the same snapshot. This prevents restart from being used to sample the full question pool.

### Result Access

Result and chart data are available only for completed attempts. Result DTOs may expose correctness because the attempt is terminal.

### History Access

History lists the authenticated user's attempts and reconciles stale overdue attempts for that user boundary before rendering.

## Rules

- Play-page DTOs do not expose correctness.
- `TIME_EXPIRED` uses `submittedAt = expiresAt`.
- After-expiry submit uses saved answers.
- Resume returns the same snapshot.
- Restart copies the same snapshot.
- Query services do not own lifecycle mutations.

## Lifecycle Actions

| Action | Current state | Time condition | Result |
| --- | --- | --- | --- |
| Start quiz | No active attempt | Not in cooldown, right available | New `IN_PROGRESS` attempt, one right consumed |
| Start quiz | Active attempt exists | Before expiry | Existing attempt returned, no right consumed |
| Start quiz | Active attempt exists | At or after expiry | Existing attempt auto-submitted, then fresh attempt if allowed |
| Autosave | `IN_PROGRESS` | Before expiry | Answer saved or stale response returned |
| Autosave | `IN_PROGRESS` | At or after expiry | Attempt auto-submitted from saved answers |
| Submit | `IN_PROGRESS` | Before expiry | Submitted payload saved, attempt completed `MANUAL` |
| Submit | `IN_PROGRESS` | At or after expiry | Payload ignored, attempt completed `TIME_EXPIRED` |
| Auto-submit | `IN_PROGRESS` | At or after expiry | Attempt completed `TIME_EXPIRED` |
| Restart | `IN_PROGRESS` | Before expiry, right available | Old attempt `ABANDONED`, replacement `IN_PROGRESS` from same snapshot |
| Restart | `IN_PROGRESS` | At or after expiry | Attempt auto-submitted and restart rejected |
| Result | `COMPLETED` | Any | Result view available |
| Result | Not completed | Any | Result access rejected by service contract |
