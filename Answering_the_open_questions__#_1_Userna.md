### Finalized Specs Based on Your Decisions

Below is a consolidated, implementation-ready plan that encodes all answers you provided into concrete backend and frontend requirements, validation rules, database changes, API contracts, session shape, telemetry, and tests.

---

### Backend: Database and Models

#### Flyway migrations

1) Add display_name and username change tracking
- Filename example: Vxxx__add_display_name_and_username_change_tracking.sql

```sql
-- 1) Add display_name (nullable first), and last_username_changed_at
ALTER TABLE users
  ADD COLUMN display_name VARCHAR(32),
  ADD COLUMN last_username_changed_at TIMESTAMPTZ;

-- 2) Backfill display_name from username
UPDATE users
SET display_name = username
WHERE display_name IS NULL;

-- 3) Make display_name NOT NULL with max length
ALTER TABLE users
  ALTER COLUMN display_name SET NOT NULL;

-- 4) Enforce/assist case-insensitive uniqueness on username (application still lowercases)
-- If username already has a unique constraint, this index co-exists and enforces case-insensitive checks in queries.
-- NOTE: This won’t automatically enforce case-insensitive uniqueness unless you validate against this index on write,
-- but it enables fast existence checks (WHERE lower(username) = lower($1)).
CREATE UNIQUE INDEX IF NOT EXISTS users_username_lower_key ON users ((lower(username)));

-- Optional: small index if you will filter by last_username_changed_at
-- CREATE INDEX IF NOT EXISTS idx_users_last_username_changed_at ON users (last_username_changed_at);
```

Notes:
- Username uniqueness remains enforced by your existing unique constraint; the functional unique index on lower(username) gives you fast, consistent case-insensitive checks and prevents duplicates with different casing from being inserted.
- If you already use `citext`, prefer ALTER COLUMN username TYPE citext and keep a unique constraint; otherwise, keep the functional unique index shown above.
- We do not introduce email-specific schema here beyond what you already have. The “display email immediately but unverified until confirmed” can be implemented with your existing verification system and a boolean flag, or with a “pending” structure (see below).

2) Optional (only if you need a pending email column/flags)
- Filename: Vxxx__email_verification_support.sql

```sql
-- If you don’t already have these fields:
ALTER TABLE users
  ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN email_verification_sent_at TIMESTAMPTZ;

-- Option A: Use a tokens table to verify email changes
CREATE TABLE IF NOT EXISTS email_verification_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token TEXT NOT NULL UNIQUE,
  email TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_user_id ON email_verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_expires_at ON email_verification_tokens(expires_at);
```

Recommendation: Use the tokens table (Option A). It cleanly handles both initial verification and email-change verification.

#### Entities/Models (Kotlin/Exposed)
- Users table:
  - displayName: varchar("display_name", 32).notNull()
  - lastUsernameChangedAt: datetime/timestamp nullable
  - emailVerified: boolean (if not present, add per optional migration)
- Domain model and serializers should include: id, username, displayName, email, emailVerified.

#### Session payload
- Include displayName (per Q7).
- Consider also including emailVerified if you display banners/prompts.
- Ensure session is updated immediately after profile changes.

Example session data shape:
- id: Long
- username: String
- displayName: String
- email: String
- emailVerified: Boolean

---

### Validation and Normalization Rules

#### Username (Discord-like)
- Transform: to lowercase before validation/persistence.
- Length: 2–32 characters.
- Allowed characters: a-z, 0-9, underscore, period.
- No two consecutive periods allowed anywhere.
- Case-insensitive uniqueness.
- Change frequency: at most once per 7 days.

Regex suggestions:

- Application-level normalization: username = username.trim().lowercase()
- Validation regex: `^(?!.*\..)[a-z0-9._]{2,32}$`
  - This:
    - disallows ".."
    - allows a-z 0-9 . _
    - length 2–32
- Optional hardening (not required by your spec): disallow leading/trailing '.' with `^(?!\.)(?!.*\..)[a-z0-9._]{2,32}(?<!\.)$`. You did not require this, so default to the first regex.

Case-insensitive uniqueness approach:
- Persist lowercased username.
- Check existence via `SELECT 1 FROM users WHERE lower(username) = lower(:candidate)`.
- Rely on the unique index on lower(username) to prevent racing duplicates.

Rate limit:
- On update, verify last_username_changed_at <= now() - interval '7 days'.
- If allowed, update username and set last_username_changed_at = now().

Security requirement:
- Password confirmation required to change username.

#### Display name
- Length: 1–32 characters.
- Must contain at least one non-blank character.
- Trim leading/trailing whitespace; reject if trimmed length < 1.
- Allow full Unicode; recommend stripping newlines and control characters.

Validation:
- After trim, ensure length in [1, 32] and `\S` exists.

#### Email change
- Change the displayed email immediately.
- Mark email as unverified until user clicks verification link.
- Notify both old and new addresses.
- Password confirmation required to change email.

Implementation details:
- On email change:
  - Require current password check.
  - Write new email to users.email immediately.
  - Set users.email_verified = false.
  - Create a token in email_verification_tokens with the new email, expiration (e.g., 24 hours).
  - Send “email changed” notice to old email, and “verify your new email” with link to the new email.
- On GET /verify-email?token=:
  - Validate token (exists, not expired).
  - Ensure token.email equals users.email for the user; then set users.email_verified = true and delete token.

#### Password change
- Use your existing password policy (no changes).
- Require current password to change password.

---

### Backend: API Contracts (Ktor)

Base path: /api/me (authenticated)

1) GET /api/me
- Response 200:
  - { id, username, displayName, email, emailVerified }
- Purpose: Provide session user data.

2) PATCH /api/me/profile
- Body: { displayName: string }
- Validations:
  - Trim, 1–32 chars, at least one non-blank character.
- Responses:
  - 200: { displayName } and update session to include new displayName
  - 400: validation error schema
  - 401/403: if unauthenticated/forbidden
- Side effects:
  - Update user.display_name.
  - Invalidate any caches and update session.

3) PATCH /api/me/security/username
- Body: { username: string, password: string }
- Flow:
  - Verify password.
  - Normalize username: trim + lowercase.
  - Validate regex and uniqueness (case-insensitive).
  - Enforce 7-day cooldown via last_username_changed_at.
- Responses:
  - 200: { username }
  - 400: validation error or rate-limit error (include “nextAllowedAt”)
  - 401: bad credentials
  - 409: username already taken
- Side effects:
  - Update username and last_username_changed_at.
  - Consider notifying the user via email of the change (optional but recommended).
  - Update session.

4) PATCH /api/me/security/email
- Body: { email: string, password: string }
- Flow:
  - Verify password.
  - If email is same when normalized, no-op 200.
  - Immediately store new email; set email_verified = false.
  - Issue verification token for the new email.
  - Send notification to old email and verification to new email.
- Responses:
  - 200: { email, emailVerified: false }
  - 400/401: validation or auth errors
  - 429: if you choose to rate-limit email changes (optional)

5) POST /api/me/security/password
- Body: { currentPassword: string, newPassword: string }
- Flow:
  - Verify current password.
  - Apply your existing password policy and change to new password.
- Responses:
  - 204 No Content (or 200 with message)
  - 400/401: validation or auth errors
- Side effects:
  - Invalidate active sessions if your policy requires; at minimum, refresh current session.

6) Email verification
- GET /api/verify-email?token=abcd
- Responses:
  - 302 redirect to success/failure page OR 200 JSON depending on your app pattern.
  - On success, set email_verified = true and delete token.

Note: No account deletion endpoint in this revision (per Q6).

#### Error schema
Return structured errors suitable for superforms:
- { errors: { fieldName: "message" }, message?: "general error" }

---

### Security

- Password confirmation is required for changing username, email, and password.
- Rate limiting:
  - Username change: once per 7 days via last_username_changed_at column (DB-enforced check in service).
  - Optional per-IP or per-account 429 for repeated failed attempts on sensitive changes.
- CSRF: Ensure CSRF protection on form actions (SvelteKit + server middleware).
- Audit logging: Log security-sensitive changes (username/email/password). Avoid logging secrets.

---

### Telemetry and Logging (Q9)

- Log each settings endpoint call with:
  - userId (internal numeric ID), endpoint, outcome (success/failure), and error type/classification.
  - Exclude PII like password and full email tokens.
- Consider counters:
  - settings_profile_update_success_total
  - settings_username_update_success_total
  - settings_username_update_rate_limited_total
  - settings_email_update_success_total
  - settings_email_verification_success_total
  - settings_password_update_success_total

---

### Frontend (SvelteKit + shadcn-svelte + superforms)

Routes scaffold:

- /account/settings
  - Landing page with Cards:
    - Profile (Display name)
    - Login & Security
    - Blocked Users (Coming soon)
- /account/settings/profile
  - Form: displayName
  - Zod:
    - z.string().trim().min(1, "Display name is required").max(32, "Max 32 chars")
    - Optional: disallow only whitespace: .refine(v => /\S/.test(v), "Must contain a non-blank character")
  - Submit to PATCH /api/me/profile
  - Update session store on success; show toast.

- /account/settings/security
  - Sections:
    1) Username
       - Fields: username, password
       - Zod:
         - username: z.string().trim()
           .min(2).max(32)
           .regex(/^(?!.*\..)[a-z0-9._]{2,32}$/, "Allowed: a-z, 0-9, underscore, period; no two consecutive periods; 2–32 chars")
           .transform(v => v.toLowerCase())
         - password: z.string().min(1, "Password required")
       - Handle server 409 (taken) and cooldown messages; show nextAllowedAt if provided.
    2) Email
       - Fields: email, password
       - Zod: email: z.string().email(), password: z.string().min(1)
       - On success: show that email is now pending verification (emailVerified = false). Display the new email immediately.
    3) Password
       - Fields: currentPassword, newPassword, confirm
       - Zod: currentPassword: z.string().min(1); newPassword: per your existing policy; confirm must match.
       - On success: show toast and optionally sign the user out of other sessions.

- /account/settings/blocked
  - Scaffold UI: header “Blocked Users” and placeholder: “Coming soon”. No data fetch/actions now.

UI notes:
- Use shadcn-svelte components: Card, Input, Label, Button, Separator, Alert/Toast.
- Accessibility: Make sure labels are properly associated; use aria-live for form result messages.

---

### Session and Client State

- After successful displayName/username/email/password changes, refresh the client session store to reflect new values (especially displayName and emailVerified).
- Update any header/profile UI to show displayName instead of username.

---

### Tests

Backend tests (Ktor + KotlinTest):
- Profile
  - PATCH /api/me/profile success and validations (trim, non-blank, max 32)
- Username
  - Reject without password, wrong password, bad formats, uppercase inputs are lowered, consecutive dots are rejected.
  - Uniqueness conflict is 409.
  - Cooldown enforced with last_username_changed_at; success after 7+ days.
- Email
  - Requires password.
  - Updates users.email immediately; sets email_verified = false.
  - Creates verification token and sends emails to both addresses (mock email service).
  - Verification endpoint sets email_verified = true.
- Password
  - Requires current password; rejects invalid; applies policy; success path.

Frontend tests (Vitest + Playwright/snapshots as applicable):
- Profile form validation and successful submission updates UI display name.
- Security page:
  - Username form transforms to lowercase and shows rate limit and 409 errors properly.
  - Email form shows “verification required” state and displays new email immediately.
  - Password form validates match and success toast.

---

### Implementation Checklist (Updated)

Database
- [ ] Flyway migration for display_name and last_username_changed_at
- [ ] Optional: email_verification_tokens table and email_verified flag (if not already exist)
- [ ] Unique functional index on lower(username)

Backend
- [ ] Extend Exposed Users table and data classes with displayName, lastUsernameChangedAt, emailVerified
- [ ] Session payload includes displayName (and emailVerified)
- [ ] GET /api/me returns username, displayName, email, emailVerified
- [ ] PATCH /api/me/profile for displayName
- [ ] PATCH /api/me/security/username enforcing regex, lowercase, uniqueness, 7-day cooldown, and password confirmation
- [ ] PATCH /api/me/security/email with immediate change, emailVerified=false, verification token creation, notify old and new
- [ ] GET /api/verify-email handles email verification
- [ ] POST /api/me/security/password requiring current password
- [ ] Telemetry logs for all calls (no secrets)
- [ ] Unit + integration tests

Frontend
- [ ] /account/settings landing with cards
- [ ] /account/settings/profile displayName form (superforms + Zod)
- [ ] /account/settings/security Username/Email/Password sections
- [ ] /account/settings/blocked scaffold
- [ ] Hook up to endpoints, update session store, toasts
- [ ] Component/page tests

Docs
- [ ] Update README to note displayName behavior, username rules, weekly change limit, and email verification flow

---

### Example DTOs

Requests:
- PATCH /api/me/profile: { displayName: string }
- PATCH /api/me/security/username: { username: string, password: string }
- PATCH /api/me/security/email: { email: string, password: string }
- POST /api/me/security/password: { currentPassword: string, newPassword: string }

Responses:
- GET /api/me: { id: number, username: string, displayName: string, email: string, emailVerified: boolean }
- PATCH /api/me/profile: { displayName: string }
- PATCH /api/me/security/username: { username: string, nextAllowedAt?: string }
- PATCH /api/me/security/email: { email: string, emailVerified: boolean }
- POST /api/me/security/password: 204 No Content

Errors:
- { message: string, errors?: { [field: string]: string } }

---

### Notes and Rationale Tied to Your Answers

- Username rules mirror Discord’s: lowercase, a–z 0–9 . and _, 2–32 chars, no “..”, case-insensitive uniqueness. Weekly change limit implemented via last_username_changed_at.
- Display name capped at 32, requires at least one non-blank char; otherwise flexible and Unicode-friendly.
- Usernames will be suitable for mentions/URLs thanks to restrictions and weekly change rate limiting.
- Email change shows new email immediately but requires re-verification; both old and new addresses are notified.
- No changes to password policy; password confirmation is required for username and password changes (also for email change).
- Account deletion is deliberately out of scope for this revision.
- JWT not used; displayName added to session.
- Localization deferred.
- Telemetry: log endpoints and outcomes; keep PII out.
- Blocked users: UI scaffold only.

If you’d like, I can adapt these to your current package structure, Exposed table object names, and existing API routing conventions exactly.