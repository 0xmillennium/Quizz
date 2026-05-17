WITH existing AS (
    SELECT id
    FROM users
    WHERE lower(email) = lower(:'admin_email')
    LIMIT 1
),
updated AS (
    UPDATE users
    SET
        full_name = :'admin_full_name',
        password_hash = :'password_hash',
        role = 'ADMIN',
        enabled = true,
        updated_at = now()
    WHERE id IN (SELECT id FROM existing)
    RETURNING id
),
inserted AS (
    INSERT INTO users (
        created_at,
        updated_at,
        full_name,
        email,
        password_hash,
        role,
        enabled
    )
    SELECT
        now(),
        now(),
        :'admin_full_name',
        lower(:'admin_email'),
        :'password_hash',
        'ADMIN',
        true
    WHERE NOT EXISTS (SELECT 1 FROM existing)
    RETURNING id
)
SELECT
    COALESCE(
        (SELECT id FROM updated),
        (SELECT id FROM inserted)
    ) AS admin_user_id;
