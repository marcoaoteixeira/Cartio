# Google Drive Synchronization for Android App Database (Room/SQLite)

## Goal

Implement a feature that allows the Android app to:

1. Back up the local Room/SQLite database to the user’s Google Drive.
2. Restore the database from a previous backup.
3. Support future evolution toward automatic backups and versioned restore points.

---

# Phase 1 — Architecture Decisions

## 1. Backup Strategy

### Option A — Full Database File Backup (Recommended MVP)

Back up the actual database files:

- `app.db`
- `app.db-wal` (if WAL enabled)
- `app.db-shm` (if WAL enabled)

### Pros

- Simple implementation
- Exact snapshot
- Fast restore
- Preserves relationships and indexes

### Cons

- Full file uploaded every time
- Schema compatibility concerns

**Recommendation:** Use this for V1.

---

## 2. Storage Strategy in Google Drive

### Preferred: `appDataFolder`

Use Google Drive’s hidden app-specific folder.

Benefits:

- Private application storage
- No Drive clutter for users
- Simplified permissions
- Ideal for backup use case

Alternative:

- User-visible folder in My Drive (optional later)

---

## 3. Versioning Policy

Define:

- Multiple backups with retention policy
- Retention policy: keep the last 3 backups
- Timestamp-based naming

Example:

```text
backup_2026_04_26_14_30.db
metadata.json
```

Store metadata:

Retention behavior:

- On each successful new backup:
  1. Upload the new backup
  2. List existing backups sorted by creation date
  3. Keep the 3 newest backups
  4. Delete older backups beyond retention limit

Deletion should happen only after the new backup is confirmed uploaded to avoid accidental data loss.


```json
{
  "appVersion": "2.1.0",
  "schemaVersion": 14,
  "backupDate": "..."
}
```

---

# Phase 2 — Authentication and Authorization

## 4. Google Sign-In

Use:

- Google Sign-In
- OAuth scope:

```text
https://www.googleapis.com/auth/drive.appdata
```

Plan for:

- Token management
- Reauthentication flow
- Sign-out behavior

---

# Phase 3 — Database Backup Mechanics

## 5. Safe Database Snapshot

Do **not** upload a live Room database directly.

Create a consistent snapshot.

Steps:

1. Ensure pending writes are flushed.
2. Force WAL checkpoint if needed:

```sql
PRAGMA wal_checkpoint(FULL);
```

3. Copy database to temporary file:

```text
/cache/backup-temp.db
```

4. Upload copied snapshot, never live DB.

---

## 6. Compression

Compress backup:

```text
backup.zip
```

Contents:

- Database file(s)
- Metadata

Benefits:

- Smaller uploads
- Cleaner restore package

---

# Phase 4 — Drive Integration Layer

## 7. Create Backup Repository

Create abstraction:

```kotlin
interface BackupProvider {
   suspend fun uploadBackup(...)
   suspend fun listBackups()
   suspend fun restoreBackup(...)
}
```

Implementation responsibilities:

- Authenticate
- Create/find Drive folder
- Upload backups
- List available backups
- Download backups
- Delete old backups

Suggested implementation:

```kotlin
DriveBackupRepository
```

---

# Phase 5 — Restore Design

## 8. Restore Flow

Restore sequence:

1. User selects restore
2. Show overwrite warning
3. Download backup
4. Validate metadata
5. Close Room database
6. Replace DB files
7. Reopen database
8. Restart app (recommended)

---

## 9. Schema Compatibility Policy

For MVP:

Restore only when schema versions match.

Reject restore when:

- Backup schema > installed schema
- Installed schema > backup schema

Possible future enhancement:

- Allow Room migrations after restore

---

# Phase 6 — Security

## 10. Encrypt Backups

Recommended pipeline:

```text
DB → ZIP → AES Encrypt → Upload
```

Restore:

```text
Download → Decrypt → Restore
```

Use:

- AES encryption
- Android Keystore for key management

Especially important for sensitive user data.

---

# Phase 7 — Reliability and Recovery

## 11. Handle Failure Scenarios

Design for:

- No internet
- Expired auth
- Interrupted upload
- Corrupted backup
- Drive quota full
- Partial restore failure

---

## 12. Rollback Protection

Before restore:

Create emergency local backup.

```text
restore safety snapshot
```

If restore fails:

- Roll back automatically.

---

# Phase 8 — Background Execution

## 13. WorkManager Integration

Use WorkManager for:

- Manual backup execution
- Scheduled backups (future)
- Retry support
- Network constraints

Possible constraints:

- Wi-Fi only
- Battery not low

---

# Phase 9 — User Experience

## 14. Backup UI

Settings section:

```text
Sign in to Google
Back up now
Last backup date
Restore from backup
```

---

## 15. Restore UI

Flow:

```text
Select backup
Show metadata
Confirm overwrite
Restore
Restart app
```

Include progress states:

- Uploading...
- Verifying...
- Restoring...

---

# Phase 10 — Testing Strategy

## 16. Functional Testing

Validate:

- Backup success
- Restore success
- Multiple backups
- Backup listing

---

## 17. Data Integrity Testing

Verify:

- Restored database equals original
- Relationships preserved
- Indexes preserved

---

## 18. Failure Testing

Inject failures:

- Kill app during restore
- Drop network mid-upload
- Corrupt backup files

Destructive testing is mandatory.

---

# Suggested Architecture

```text
Presentation
 └─ BackupViewModel

Domain
 ├─ BackupDatabaseUseCase
 └─ RestoreDatabaseUseCase

Data
 ├─ RoomDatabaseSnapshotProvider
 ├─ DriveBackupRepository
 └─ CryptoManager
```

---

# MVP Delivery Order

## Sprint 1

- Google Sign-In
- Drive appDataFolder integration
- Manual backup upload

## Sprint 2

- Backup listing
- Manual restore

## Sprint 3

- Encryption
- Schema validation
- Rollback safety

## Sprint 4

- WorkManager scheduled backups
- Retention policy automation (keep last 3)
- Versioning improvements

---

# Technical Spikes (Do First)

Prototype these before full implementation:

1. Safe Room snapshot with WAL
2. Database replacement while app installed
3. Google Drive appDataFolder behavior
4. Encryption + restore pipeline

These should be validated before coding the complete feature.

---

## 19. Retention Policy Details

Retention rule:

- Maximum 3 backups stored in Google Drive.

Suggested naming:

```text
backup_YYYY_MM_DD_HH_mm.db.enc
```

Deletion policy:

- FIFO by backup creation date (oldest removed first).

Failure handling:

- Never delete old backups before confirming new backup upload.
- If cleanup fails, mark backup as successful and retry cleanup later.

Optional enhancement later:

- Let user choose retention count.

---

# Recommended MVP Scope

Include:

- Full DB snapshot backup
- Google Drive appDataFolder
- Manual backup and restore
- Schema version check
- AES encrypted backup
- WorkManager-ready architecture

This provides a strong production-grade foundation.

