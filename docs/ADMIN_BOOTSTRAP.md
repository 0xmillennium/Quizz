# Admin Bootstrap

Public registration creates `USER` accounts only. This project does not include an admin creation UI, user management screen, seed data, or a `DevDataInitializer`.

Admin bootstrap is manual:

- `password_hash` must contain a BCrypt encoded password.
- `role` must be `ADMIN`.
- `enabled` must be `true`.
- Use a real BCrypt hash generated outside the application. Do not store a plaintext password.

Example SQL with a placeholder hash:

```sql
INSERT INTO users (
    created_at,
    updated_at,
    full_name,
    email,
    password_hash,
    role,
    enabled
) VALUES (
    now(),
    now(),
    'Admin User',
    'admin@example.com',
    '<BCrypt hash here>',
    'ADMIN',
    true
);
```

The placeholder is intentional. The project does not provide a default admin password.
