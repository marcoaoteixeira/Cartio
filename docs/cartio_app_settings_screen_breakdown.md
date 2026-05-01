# Shopping Cart List App — Settings Screen Breakdown

## Epic 1 — Settings Screen Foundation (UI + Navigation)

### Story 1.1 — Build Settings Screen Layout

**Developer tasks**

Create grouped sections:
- Backup & Sync
- Support Cartio
- Data
- About

Implement reusable settings components:
- `SettingsSection`
- `SettingsListItem`
- `ToggleSettingItem`
- `PromoCard`

Each row should support:
- Leading icon
- Title
- Subtitle
- Optional trailing switch
- Optional chevron navigation

**Acceptance Criteria**
- Matches visual hierarchy from mock
- Supports scrolling
- Supports dark mode / theming
- Accessible tap targets (44dp+)

---

# Epic 2 — Google Drive Sync

## Story 2.1 — Google Sign-In Integration

**Developer tasks**
- Integrate Google authentication
- Request minimal scopes required
- Handle:
  - Sign in
  - Sign out
  - Token refresh
  - Permission revoked

**State model**
- Sync disabled (default)
- Sync enabled
- Sync in progress
- Sync error

**Acceptance Criteria**
- Toggle ON prompts Google auth
- Cancel keeps sync disabled
- User can disconnect account

---

## Story 2.2 — Data Sync Architecture

Define sync strategy before coding.

### Recommendation
Use **backup-style sync**, not real-time collaboration.

**Sync payload example**

```json
{
  "lists": [],
  "items": [],
  "prices": [],
  "settings": {},
  "lastModified": ""
}
```

**Developer decisions needed**
- Full backup overwrite or merge?
- Conflict resolution:
  - Last write wins?
  - Ask user?
- Manual sync vs auto sync

**MVP Recommendation**
Automatic daily backup + restore option.

Use **Google Drive App Folder**:
- Hidden app-specific storage
- No user file clutter

---

## Story 2.3 — Restore / Sync Recovery

Handle:
- First install restore
- Reinstall recovery
- Corrupt backup
- Sync failure states

Add:
- “Last synced at …”
- “Restore from backup”

---

## Story 2.4 — Security

Developer requirements:
- Encrypt backup before upload
- Secure tokens in device keystore / keychain
- Never store raw credentials

---

# Epic 3 — Support Cartio (Tip + Remove Ads)

Treat this as an **In-App Purchase**.

## Story 3.1 — One-Time Purchase Product

Product setup:
- Non-consumable purchase:
  - “Support Cartio”

Entitlement unlocks:
- Remove ads permanently
- Optional support badge

**Developer tasks**
- Configure App Store / Play Billing product
- Purchase flow
- Purchase restore

**Acceptance Criteria**
- Buy once removes ads permanently
- Reinstall restores entitlement

---

## Story 3.2 — Ads Removal Logic

Add feature flag:

```text
ads_enabled = !support_purchase
```

If purchased:
- Hide ad placements
- Disable ad requests entirely

**Important**
Do not just hide ad UI — stop loading ad SDKs.

---

## Story 3.3 — Failure States

Handle:
- Payment canceled
- Payment failed
- Purchase verification failed
- Restore purchases option

---

# Epic 4 — Clear All Data

## Story 4.1 — Data Reset Action

Flow:
Tap → confirmation modal

**Dialog**
Title:
- Clear all data?

Message:
- This removes lists, items and history permanently.

Actions:
- Cancel
- Delete All

**Developer tasks**
- Wrap delete in transaction
- Clear:
  - Lists
  - Items
  - Products
  - Expense records
  - Optional preferences

---

## Story 4.2 — Sync Interaction Rule

If Google sync is enabled, ask:
- Delete local only?
- Delete local + cloud backup?

This needs a product decision.

---

# Epic 5 — About Section

## Story 5.1 — App Info

Show:
- App name
- Semantic version
- Build number

Pull from runtime config, not hardcoded.

Example:

```text
Version 0.3 (Build 12)
```

---

## Story 5.2 — Open Source Licenses

Developer tasks:
- Generate OSS license screen
- Include third-party packages
- Automate from dependency metadata when possible

---

# Suggested MVP Release Order

## Phase 1 (Low Risk)
- Settings screen UI
- About section
- Clear all data

## Phase 2
- Support Cartio purchase
- Ad removal entitlement

## Phase 3
- Google Drive sync

**Recommendation:** Do not combine Sync and Payments in the same milestone.

---

# Product Decisions to Make Early

## Sync
Define:
- What is synced?
- Conflict resolution rule
- Backup frequency
- Multi-device expectations

Without this, implementation will drift.

---

## Support Purchase
Define:
- One fixed amount or multiple tip tiers?
- Lifetime or subscription?
- Restore purchases required? (recommended: yes)

---

## Clear Data
Define:
- Does it wipe cloud backup too?
- Does it sign user out of Google?

---

# Suggested Ticket Structure

```text
SET-01 Settings Screen UI

SYNC-01 Google Sign-In
SYNC-02 Drive Backup Upload
SYNC-03 Restore Backup
SYNC-04 Sync Error Handling

SUP-01 In-App Purchase Setup
SUP-02 Remove Ads Entitlement
SUP-03 Restore Purchases

DATA-01 Clear Local Data Flow

ABOUT-01 App Version Info
ABOUT-02 OSS Licenses Screen
```

---

# Risks to Flag to Engineering

Highest-risk items:
1. Google Drive conflict resolution
2. Purchase entitlement restoration
3. Data deletion + sync interaction
4. Backup encryption

These deserve technical design before implementation.

