# Quiz Randomization and Attempt Rights

Published quizzes act as question pools. Each fresh attempt samples from that pool and stores an immutable snapshot for scoring and review.

## Quiz Pool Policy

| Field | Meaning |
| --- | --- |
| `questionCount` | Number of pool questions sampled for each fresh attempt. |
| `attemptLimit` | Number of start/restart rights available before cooldown. |
| `retakeCooldownMinutes` | Cooldown duration after rights are exhausted and no active attempt remains. |

Publish validation ensures `questionCount` does not exceed the pool size.

## Fresh Attempt Sampling

A fresh attempt:

1. Shuffles the quiz question pool.
2. Takes `questionCount` questions.
3. Shuffles the sampled question order.
4. Shuffles answer-option order per question.
5. Stores question and option snapshots.

The attempt does not depend on later edits to live questions or quizzes.

## Resume and Restart

Resume returns the active attempt and keeps the same snapshot.

Restart creates a replacement attempt from the same snapshot rather than drawing a new random pool. This protects the question pool from repeated restart sampling and keeps attempt rights meaningful.

## Attempt Rights and Cooldown

- Start consumes one attempt right when it creates a fresh attempt.
- Restart consumes one attempt right when it creates a replacement attempt.
- Resume does not consume a right.
- Completion does not consume another right.
- Cooldown starts only when remaining attempts are exhausted and no active attempt remains.
- When cooldown expires, remaining attempts reset to the quiz attempt limit.

Allowance updates are performed through the attempt command service and repository locking so concurrent start/restart flows coordinate on the user and quiz allowance row.
