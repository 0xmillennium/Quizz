# Scripts

## bootstrap-admin.sh

Interactive Docker-friendly admin bootstrap script. It prompts for admin identity and password, generates a BCrypt hash through the Quizz Docker image, then applies `scripts/sql/upsert-admin.sql` through `docker compose exec` against the private PostgreSQL container.

## sql/upsert-admin.sql

Idempotent PostgreSQL script for inserting or updating one admin account. It uses psql variables for the admin email, full name, and password hash.
