# Domain Model

Quizz uses aggregate ownership and immutable attempt snapshots to keep authoring data separate from attempt-time behavior.

## Main Aggregates

| Model | Purpose | Ownership |
| --- | --- | --- |
| `User` | Application account with role, enablement, email, and password hash | Owns account identity state |
| `Category` | Master data for grouping questions and quizzes | Referenced by questions and quizzes |
| `Question` | Question-bank aggregate root | Owns `AnswerOption` children |
| `AnswerOption` | Authored answer option | Owned by `Question` |
| `Quiz` | Quiz definition with duration, question count, attempt limit, cooldown, and status | Owns `QuizQuestion` pool memberships |
| `QuizQuestion` | Membership of a question in a quiz pool | Owned by `Quiz` |
| `QuizAttempt` | User attempt snapshot and lifecycle state | Owns `AttemptQuestion` children |
| `AttemptQuestion` | Snapshot of a live question for an attempt | Owns `AttemptAnswerOption` children |
| `AttemptAnswerOption` | Snapshot of a live answer option | Owned by `AttemptQuestion` |
| `QuizAttemptAllowance` | Per-user, per-quiz attempt rights and cooldown | One row per user and quiz |

## Read Models

| Read model | Source | Purpose |
| --- | --- | --- |
| Leaderboard | Completed submitted attempts | Public ranking without email exposure |
| Admin dashboard | Users, categories, questions, quizzes, attempts | Operational counts and recent attempts |
| Admin results | Attempt snapshot tables | Reporting details without score recalculation |

## Aggregate Ownership

- `Question` owns `AnswerOption`.
- `Quiz` owns `QuizQuestion`.
- `QuizAttempt` owns `AttemptQuestion`.
- `AttemptQuestion` owns `AttemptAnswerOption`.

Owned children are updated through their aggregate root rather than through separate services.

## Intentional Scalar IDs

Attempt snapshots keep some identifiers as scalar values:

- `originalQuestionId` records the live question id at attempt creation.
- `originalAnswerOptionId` records the live answer option id at attempt creation.
- `selectedOptionId` points to the selected attempt answer option, not the live answer option.

These scalar IDs preserve audit context without making result scoring depend on mutable live question-bank rows.
