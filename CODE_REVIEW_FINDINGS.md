# FD-Tracker — Full Code Review Findings

**Reviewed against:** `project-init.md` & `architecture.md`
**Date:** Review completed across all layers

---

## Executive Summary

| Area | Status | Critical Issues | Warnings |
|------|--------|----------------|----------|
| Data Layer | ✅ Solid | 0 | 2 |
| Domain Layer | ✅ Solid | 0 | 1 |
| Presentation Layer | ⚠️ Mostly Complete | 2 | 3 |
| App & Navigation | ✅ Solid | 0 | 0 |
| Core UI & Common | ✅ Solid | 0 | 0 |
| Build & Config | ✅ Solid | 0 | 1 |

**Overall:** The app is well-implemented against the specs. There are **2 critical functional bugs** and several minor warnings.

---

## CRITICAL ISSUES (Must Fix)

### CRITICAL-1: OCR → Edit FD Data Not Passed

**Files:**
- `feature/ocr/src/main/kotlin/.../OcrReviewScreen.kt` (line 158)
- `app/src/main/kotlin/.../navigation/AppNavHost.kt` (lines 91-99)

**Problem:** After OCR parses an FD receipt and the user reviews/corrects fields on `OcrReviewScreen`, clicking "Continue to Add FD" navigates to `EditFdRoute()` with **no data**. The locally edited fields (`bankName`, `fdNumber`, `principal`, `rate`, etc.) are discarded. The EditFd form opens blank.

**Root Cause:** `onNavigateToEditFd(null)` is called without passing any parsed data. The navigation route `EditFdRoute(fdId)` only accepts an optional FD ID for edit mode — there's no mechanism to pass OCR-parsed fields.

**Impact:** The entire OCR feature (Feature 1 in project-init.md) is non-functional end-to-end. Users can scan and review but the data never reaches the Add FD form.

**Fix Options:**
1. Pass OCR data via a shared ViewModel (scoped to navigation graph)
2. Pass serialized OCR data as a navigation argument
3. Store OCR result in a temporary DB/memory cache and read it in EditFdViewModel

---

### CRITICAL-2: Settings — Date of Birth Cannot Be Set

**Files:**
- `feature/settings/src/main/kotlin/.../SettingsScreen.kt` (lines 96-102)
- `feature/settings/src/main/kotlin/.../SettingsViewModel.kt` (line 117)

**Problem:** The DOB field is `readOnly = true` with no date picker trigger. `SettingsViewModel.updateDateOfBirth()` exists but is never called from the UI.

**Impact:** Users cannot set their date of birth. Since `saveProfile()` returns early if `dateOfBirth == null` (line 160), the entire user profile cannot be saved on first use. This cascades to:
- **Tax screen:** `GetForm15GChecklistUseCase` needs `UserProfile` to determine Form 15G vs 15H
- **Tax screen:** `GetTdsThresholdStatusUseCase` uses senior citizen flag (derived from DOB)
- Both tax features produce incorrect/empty results without DOB

---

## WARNINGS (Should Fix)

### WARN-1: Biometric App Lock — UI Toggle Only, No Implementation

**File:** `feature/settings/src/main/kotlin/.../SettingsScreen.kt` (line 152-158)

The "App Lock" toggle writes a boolean to DataStore but there is no `BiometricPrompt` integration anywhere in the codebase. The `MainActivity` doesn't check the flag on launch. `USE_BIOMETRIC` permission is declared in the manifest but unused.

**Impact:** Users can enable "App Lock" but the app remains unlocked. Misleading UX.

---

### WARN-2: Database Migration — Destructive Fallback

**File:** `core/data/src/main/kotlin/.../di/DatabaseModule.kt`

`fallbackToDestructiveMigration()` destroys all user data on any schema change. The database is version 2, meaning one migration has already happened destructively.

**Impact:** Any future schema update will erase all FD records. For production, proper `Migration` objects should be implemented.

---

### WARN-3: FdTableView — LazyColumn Inside Column with horizontalScroll

**File:** `feature/fdlist/src/main/kotlin/.../components/FdTableView.kt` (lines 29, 41)

A `LazyColumn` is nested inside a `Column` with `horizontalScroll`. The `LazyColumn` has no fixed height constraint, which will cause it to attempt to measure all items at once (defeating lazy loading) or crash with an unbounded height constraint.

**Impact:** May crash or cause poor scroll performance with large FD lists.

---

### WARN-4: InterestCalculator.pow() — Precision Loss

**File:** `core/domain/src/main/kotlin/.../util/InterestCalculator.kt`

The `pow()` function converts `BigDecimal` to `Double` for exponentiation (`Math.pow`), then converts back. For large principal amounts or long tenures, floating-point precision loss could produce incorrect maturity calculations (off by a few rupees).

**Impact:** Minor rounding discrepancies in displayed maturity amounts. Acceptable for tracking purposes but noted.

---

### WARN-5: Converters.kt — Missing TypeConverters (Spec Deviation)

**File:** `core/data/src/main/kotlin/.../db/converter/Converters.kt`

`architecture.md` §3.2 specifies converters for `Long↔LocalDate`, `String↔Enum`, `String↔BigDecimal`. Only `List<String>↔String` is implemented.

**Mitigated:** Entity fields use primitive types (String, Long, Int) and conversions are handled in `FdMapper.kt`. This is an intentional design choice that works correctly, but deviates from the architecture spec.

---

## FEATURE-BY-FEATURE VERIFICATION

### Feature 1: Frictionless Data Entry & Ingestion

| Sub-feature | Status | Notes |
|-------------|--------|-------|
| Smart Document Parser (OCR) | ⚠️ Partial | ML Kit + regex parsing works. Data not passed to Edit form (**CRITICAL-1**) |
| Manual Form + Auto Calculations | ✅ Complete | Auto-calculates maturity date/amount when principal, rate, tenure, compounding, or value date changes |
| Account Aggregator Fetch | ❌ Not Implemented | Marked as "Optional Sync" in spec — acceptable to skip |

### Feature 2: Unified Dashboard

| Sub-feature | Status | Notes |
|-------------|--------|-------|
| Consolidated Financial Summary | ✅ Complete | Total principal, accrued interest, maturity value, active FD count |
| Bank-Wise Risk Heatmap | ✅ Complete | Color-coded bars (red=over limit, yellow=>50%, green=safe) |
| Weighted Average Portfolio Yield | ✅ Complete | Displayed in summary cards and KPI card |

### Feature 3: Maturity Calendar & Liquidity Planning

| Sub-feature | Status | Notes |
|-------------|--------|-------|
| Maturity Timeline Calendar | ✅ Complete | Monthly grid, dot indicators, date selection, 30-day highlight |
| Cash Flow Forecast | ✅ Complete | Bar chart with 24-month forecast, compact currency labels |

### Feature 4: Smart Alerts & Reminders

| Sub-feature | Status | Notes |
|-------------|--------|-------|
| Multi-Stage Maturity Alerts | ✅ Complete | T-14, T-7, T-1, T-0 day push notifications |
| Grace Period Tracker | ✅ Complete | T+grace reminder, StatusBadge shows grace period countdown |
| Email Reminders | ✅ Complete | SMTP via Jakarta Mail, HTML email body, per-preference toggle |
| WorkManager Background | ✅ Complete | 6-hour periodic check, boot receiver re-schedule, retry logic |

### Feature 5: Tax (TDS) Optimization Tools

| Sub-feature | Status | Notes |
|-------------|--------|-------|
| TDS Threshold Monitor | ✅ Complete | Per-bank, per-FY calculation with FY-overlap handling. Senior citizen threshold (₹50K) vs general (₹40K) |
| Form 15G/H Assistant | ✅ Complete | Auto-determines 15G vs 15H based on age. Checklist with submitted/required status. **Depends on DOB fix (CRITICAL-2)** |

### Feature 6: Strategic Decision Support

| Sub-feature | Status | Notes |
|-------------|--------|-------|
| Break-FD Calculator | ✅ Complete | Input target amount, ranks FDs by penalty cost. Excludes tax-saver and matured FDs |
| FD Laddering Visualizer | ✅ Complete | Gap analysis (avg/max/min), well-laddered detection, maturity spread view, recommendations |

### Feature 7: Visualization Modes (from project-init.md)

| View | Status | Notes |
|------|--------|-------|
| List View | ✅ Complete | LazyColumn with FdCard components |
| Calendar View | ✅ Complete | MaturityCalendar with date selection |
| Chart View | ✅ Complete | CashFlowChart bar chart in Calendar screen |
| Table View | ⚠️ Has Bug | Implemented but LazyColumn height issue (**WARN-3**) |
| Card View | ✅ Complete | FdCardView component |
| Grid View | ✅ Complete | FdGridView component |

### Feature 8: DICGC Insurance Limit

| Sub-feature | Status | Notes |
|-------------|--------|-------|
| Per-bank limit check | ✅ Complete | Default ₹5,00,000 in BankEntity. CheckInsuranceLimitUseCase filters over-limit |
| Dashboard warning | ✅ Complete | InsuranceLimitBanner shows names of over-limit banks |

---

## DATA LAYER DETAILED REVIEW

### Entities ✅
- `FdEntity`: All 22+ fields, foreign key to `BankEntity`, composite indices
- `BankEntity`: Unique bank name, deposit insurance limit in paise
- `ReminderEntity`: FK to FD, reminder type, trigger date, sent status
- `SmtpConfigEntity`: Singleton (id=1), encrypted credentials fields
- `BankPrincipalTuple`: Projection for bank-aggregation queries

### DAOs ✅
- `FdDao`: Active FDs flow, maturity range filter, bank filter, sort by principal/rate, upsert, soft delete
- `BankDao`: All banks flow, bank by name flow, insert, delete
- `ReminderDao`: By FD flow, unsent reminders flow, insert single/batch, mark sent, delete by FD
- `SmtpConfigDao`: Observe flow, get suspend, upsert, delete

### Mappers ✅
- `FdMapper`: Full bidirectional mapping with safe enum parsing, LocalDate↔Long, BigDecimal↔String, JSON list serialization
- `ReminderMapper`: Direct field mapping

### Repositories ✅
- `FdRepositoryImpl`: Reactive flows + CRUD, preserves createdAt on upsert
- `BankRepositoryImpl`: Paise↔BigDecimal conversion for insurance limits
- `ReminderRepositoryImpl`: All CRUD with mapper
- `SmtpRepositoryImpl`: Singleton config management
- `UserPrefsRepositoryImpl`: DataStore-based, computes senior citizen from DOB

### SMTP Email Service ✅
- Jakarta Mail API, TLS/SSL support
- Differentiated email subjects (pre-maturity, maturity day, grace ending)
- HTML email body with FD details

### WorkManager ✅
- `ReminderCheckWorker`: HiltWorker, checks preferences, sends push/email, marks sent
- `WorkManagerInitializer`: 6-hour periodic work, boot receiver re-init
- Retry up to 3 attempts

### DI Modules ✅
- `DatabaseModule`: Room DB + 4 DAOs
- `RepositoryModule`: 5 interface→impl bindings
- `SmtpModule`: SmtpEmailService singleton
- `UseCaseModule`: All 15+ use cases
- `DispatcherModule`: IO, Default, Main dispatchers

---

## DOMAIN LAYER DETAILED REVIEW

### Models ✅
All domain models present with correct types:
- `FixedDeposit` + 5 enums (HoldingMode, RenewalInstruction, PayoutFrequency, SpecialCategory, TaxExemptionForm)
- `UserProfile`, `DashboardSummary`, `BankExposure`, `MonthlyCashFlow`
- `BreakFdRecommendation`, `LadderAnalysis` + `LadderPoint`
- `BankTdsStatus`, `Form15GAction`, `MaturityResult`, `OcrParsedFd`

### Repository Interfaces ✅
- `FdRepository`, `BankRepository`, `ReminderRepository`, `SmtpRepository`, `UserPrefsRepository`
- Co-located data classes: `BankInfo`, `Reminder`, `SmtpConfig` (minor architectural deviation — in interface files rather than model package, but functional)

### Use Cases ✅
All 19 use cases verified:
- **FD CRUD (4):** GetAllActive, GetById, AddOrUpdate (with validation), Delete
- **Calculations (3):** Maturity, AccruedInterest, BreakFdPenalty
- **Dashboard (4):** Summary, BankExposure, WeightedYield, InsuranceLimit
- **Calendar (2):** MaturityTimeline, CashFlowForecast
- **Tax (2):** TdsThresholdStatus, Form15GChecklist
- **Strategy (1):** AnalyzeLaddering
- **OCR (1):** ParseOcrResult
- **Reminder (1):** ScheduleReminders

### InterestCalculator ✅
- Compound interest: `A = P × (1 + r/n)^(n×t)` — correct
- Simple interest: `I = P × r × t` — correct
- Accrued interest: Uses compound formula with elapsed fraction — correct
- Premature withdrawal: `effective_rate = applicable_rate - penalty_rate` — correct
- Weighted average yield: `Σ(Pi×Ri)/Σ(Pi)` — correct
- BigDecimal throughout with HALF_UP rounding and scale=8 for intermediate calculations

### Business Logic Verification ✅
- `AddOrUpdateFdUseCase`: Validates FD number, bank name, principal>0, rate>0, tenure>0, maturity≥value date, auto-creates bank
- `CalculateBreakFdPenaltyUseCase`: Excludes tax-saver & matured FDs, 1% default penalty, sorted by penalty ascending
- `GetTdsThresholdStatusUseCase`: FY interest per bank, FY-overlap subtraction, 80% approaching threshold
- `GetForm15GChecklistUseCase`: Age-based 15G/15H, per-bank projected interest, submitted check
- `AnalyzeLadderingUseCase`: Well-laddered = maxGap≤120d AND (max-min)<60d, contextual recommendations
- `ScheduleRemindersUseCase`: Deletes old reminders, T-14/T-7/T-1/T-0/T+grace, skips past dates

---

## PRESENTATION LAYER DETAILED REVIEW

### Navigation ✅
- Type-safe Serializable routes (9 routes)
- Bottom nav: Dashboard, FD List, Calendar, Settings
- Proper back stack management: popUpTo, launchSingleTop, restoreState
- Bottom bar hidden for non-top-level destinations

### All Screens Verified:
- **DashboardScreen** ✅ — Summary, exposure heatmap, upcoming maturities, insurance banner, FAB
- **FdListScreen** ✅ — 4 view modes, 3 sort options, bank filter chips, empty state
- **FdDetailScreen** ✅ — All fields in cards, accrued interest, edit/delete, confirmation dialog
- **EditFdScreen** ✅ — All mandatory+optional fields, date picker, auto-calculation, enum dropdowns
- **CalendarScreen** ✅ — Monthly calendar, dot indicators, cash flow chart, date selection
- **OcrCaptureScreen** ✅ — Camera permission, image picker, PDF import, ML Kit processing
- **OcrReviewScreen** ⚠️ — Editable fields shown but not passed forward (CRITICAL-1)
- **TaxScreen** ✅ — TDS status cards, progress bars, Form 15G/H checklist
- **StrategyScreen** ✅ — Laddering analysis, break FD calculator with results
- **SettingsScreen** ⚠️ — Missing DOB picker (CRITICAL-2), biometric not implemented (WARN-1)

### Shared UI Components ✅
- `FdCard`: Progress bar, tenure visualization, status text, grace period handling
- `StatusBadge`: 5 states (Active, Maturing Soon, Matures Today, Grace Period, Matured)
- `KpiCard`, `InsuranceLimitBanner`, `LoadingShimmer`, `EmptyState`, `BankChip`, `ConfirmDialog`

---

## APP MODULE REVIEW ✅

- `FdTrackerApplication`: @HiltAndroidApp, custom WorkManager Configuration.Provider
- `MainActivity`: @AndroidEntryPoint, edge-to-edge, bottom nav with FdTrackerTheme
- `BootReceiver`: Re-initializes WorkManager on BOOT_COMPLETED
- `AndroidManifest.xml`: All permissions, boot receiver, custom WorkManager init, network security config

---

## BUILD & CONFIGURATION REVIEW ✅

- Multi-module structure: 4 core modules + 8 feature modules + 1 app module
- Gradle version catalog (`libs.versions.toml`)
- Plugins: Android, Kotlin, Compose Compiler, KSP, Hilt, Serialization
- **WARN-2:** Destructive migration fallback

---

## SUMMARY OF ACTION ITEMS

### Must Fix (2)
1. **CRITICAL-1:** Wire OCR parsed data from OcrReviewScreen → EditFdScreen
2. **CRITICAL-2:** Add DatePicker for DOB in SettingsScreen

### Should Fix (5)
3. **WARN-1:** Implement biometric authentication or remove the toggle
4. **WARN-2:** Replace destructive migration with proper Migration objects
5. **WARN-3:** Fix FdTableView LazyColumn unbounded height
6. **WARN-4:** Consider pure-BigDecimal power function for precision
7. **WARN-5:** Document the converter design decision (spec deviation)
