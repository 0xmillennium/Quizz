# UI Architecture

Quizz uses server-rendered Thymeleaf templates with modular CSS and small vanilla JavaScript files.

## Rendering Model

- Spring MVC controllers select Thymeleaf templates.
- Layout templates live under `templates/layout`.
- Public/user views live under feature template folders.
- Admin views share the admin layout and shell.

## Styling

CSS is split by concern:

- `tokens.css` for design tokens.
- `base.css`, `layout.css`, and `components.css` for shared structure.
- `forms.css`, `tables.css`, `quiz.css`, and `admin.css` for feature surfaces.
- `utilities.css` for small reusable utility classes.
- `app.css` as the main stylesheet composition point.

## JavaScript

JavaScript is minimal and feature-specific:

- Admin question and quiz form helpers.
- Attempt autosave.
- Quiz timer.
- Result chart.
- Leaderboard behavior.

There is no frontend framework and no npm build pipeline.

## Security and Leakage Boundary

The play page renders attempt snapshot DTOs that exclude correctness. Correct answer state appears only in completed result views and admin reporting contexts.

## Responsive and Accessibility Baseline

The UI is server-rendered and progressively enhanced. Forms, tables, admin screens, and attempt views should remain usable without introducing a separate frontend build stack.
