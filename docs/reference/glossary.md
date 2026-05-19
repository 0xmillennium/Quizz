# Glossary

| Term | Definition |
| --- | --- |
| attempt | One user's server-side run through a quiz. |
| attempt right | A consumable permission to start or restart a quiz attempt within the current allowance window. |
| cooldown | Delay after attempt rights are exhausted before rights reset. |
| question pool | Authored set of questions attached to a quiz. |
| snapshot | Attempt-time copy of quiz, question, and answer option data. |
| autosave | Saving one answer change while an attempt is active. |
| auto-submit | Server-side completion of an overdue attempt from saved answers. |
| completion reason | Terminal reason for a completed attempt, such as `MANUAL` or `TIME_EXPIRED`. |
| play DTO | Response model used by the active attempt play page. |
| read model | Query-shaped model for views or reports, often backed by JDBC SQL. |
| command service | Service contract that mutates domain state and enforces write invariants. |
| query service | Service contract that reads and shapes state without owning mutations. |
| admin bootstrap | Controlled operation that creates or updates the configured admin account. |
| Docker secret | File-mounted secret managed by Docker Compose for local runtime. |
| non-secret local configuration | Local values safe for `.env`, such as port, database name, and usernames. |
