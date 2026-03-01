# Personal Finance Android App (Offline-first)

## Questions asked before coding + defaults used
1. Preferred currency and locale? **Defaulted to USD + device locale formatting**.
2. Multi-currency required? **Default: no (single-currency MVP)**.
3. Cloud sync/backup required? **Default: no (local-only first)**.
4. Receipts/attachments required? **Default: optional (metadata modeled)**.
5. Budgets required in MVP? **Default: yes**.

## MVP spec (concise)
- Account management: create/edit/delete, opening + current balance.
- Transactions: expense, income, transfer, card purchase/payment, refund/adjustment hooks.
- Budget model by category and month.
- Credit support: cards + loans represented in schema, card purchases/payments in domain logic.
- Dashboard: monthly income/expense + net worth summary.
- Search/filter-ready transaction list scaffolding.
- CSV import/export for accounts + transactions.
- Offline-first Room persistence (no network dependencies).

## Stretch goals
- SQLCipher full DB encryption + encrypted cloud backup.
- OFX/QIF parsing.
- Receipt image storage and OCR tags.
- Amortization timeline chart and statement projection engine.

## Architecture
- **Pattern**: MVVM with Flow state streams and unidirectional UI updates.
- **UI**: Jetpack Compose + bottom navigation (Dashboard/Transactions/Accounts/Credit/Settings).
- **Data**: Room DAOs + repositories.
- **Domain**: use cases for transfer invariants, card purchase/payment, net worth, timezone-safe month key.
- **DI**: Hilt.

### Layers
- `ui/` Compose screens + `MainViewModel`.
- `domain/` models, repository contracts, use cases.
- `data/` Room entities/DAOs + repository implementations.
- `util/` CSV import/export port.

## Database schema and relationships
- `accounts`: stores assets/liabilities with strict mode toggle.
- `transactions`: ledger entries tied to `accountId`; transfers are modeled as **two linked entries** (out + in). This preserves clear per-account history and scales to future fee side-entries.
- `categories`: optional parent relation for hierarchy.
- `budgets`: category + monthKey + planned amount.
- `credit_cards`: statement and due-date config + APR + limit.
- `loans`: principal/rate/remaining principal for liability tracking.
- `attachments`: transaction metadata reference.

### Migration strategy
- Current DB version: `1`.
- For production: add explicit migrations per release and remove `fallbackToDestructiveMigration`.
- Export schema enabled for migration diffing.

## Security/privacy choices
- Data is local by default (offline-first).
- No account credentials or bank API integrations.
- DB encryption: currently plain Room for MVP velocity; recommend SQLCipher integration in next milestone.
- App lock placeholder in Settings; integrate BiometricPrompt + encrypted PIN in secure prefs.

## Performance notes
- Designed for 10k+ transactions with lazy lists.
- Next step: Paging3-backed DAO queries for robust infinite scrolling and search.

## CSV format
### accounts.csv
`id,name,type,currency,opening_balance_minor,current_balance_minor,strict_mode,archived,created_at_epoch`

### transactions.csv
`id,account_id,type,amount_minor,occurred_at_epoch,category_id,merchant,notes,tags_csv,linked_transfer_id`

Amounts are integer minor units (e.g., cents) to avoid precision loss.

## Build and test
```bash
./gradlew test
./gradlew assembleDebug
```

## Milestones
1. **M1 Data foundation (done):** schema, repositories, use cases, tests.
2. **M2 UX skeleton (done):** core tabs + seed flow + quick-add.
3. **M3 Transaction UX (next):** add/edit form, filters, attachment picker.
4. **M4 Credit UX (next):** statement cycle detail, due projection, minimum due card.
5. **M5 Hardening (next):** SQLCipher, biometrics lock, migrations, paging, UI tests.
