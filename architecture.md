# Architecture — Fixed Deposit Tracker (Android)

This document defines the software architecture for the FD-Tracker Android application: module structure, layer responsibilities, data flow, and cross-cutting concerns.

---

## 1. Architectural Pattern

The project follows **Clean Architecture** with **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**.

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│  Jetpack Compose UI  ←→  ViewModel  ←→  UI State        │
│  (Screens, Components)   (StateFlow)   (Data Classes)    │
└──────────────────────────┬──────────────────────────────┘
                           │ calls
┌──────────────────────────▼──────────────────────────────┐
│                      DOMAIN LAYER                        │
│  Use Cases  ←→  Domain Models  ←→  Repository Interfaces │
│  (Business Logic)  (Pure Kotlin)    (Abstractions)       │
└──────────────────────────┬──────────────────────────────┘
                           │ implements
┌──────────────────────────▼──────────────────────────────┐
│                       DATA LAYER                         │
│  Repository Impl  ←→  Room DAOs  ←→  SQLite Database     │
│  (Concrete)           (Queries)     (Entities)           │
│                                                          │
│  SMTP Service  ←→  DataStore  ←→  ML Kit / CameraX       │
└─────────────────────────────────────────────────────────┘
```

### Key Principles

- **Dependency Rule:** Outer layers depend on inner layers, never the reverse. Domain has zero Android dependencies.
- **UDF:** UI emits user **intents/events** → ViewModel processes → produces new **UI State** → Compose recomposes.
- **Single Source of Truth:** Room database is the SSOT for all FD data. ViewModels observe Room via Kotlin `Flow`.
- **Separation of Concerns:** Each feature module is self-contained with its own screens, ViewModels, and navigation.

---

## 2. Multi-Module Gradle Structure

The project is organized into a multi-module Gradle build for build speed, enforced boundaries, and reusability.

```
FD-Tracker/
├── app/                          ← Application module (entry point, nav host, Hilt setup)
├── core/
│   ├── data/                     ← Room DB, Repository implementations, SMTP service
│   ├── domain/                   ← Use cases, domain models, repository interfaces
│   ├── ui/                       ← Shared Compose components, theme, design tokens
│   └── common/                   ← Utility functions, extensions, constants
├── feature/
│   ├── dashboard/                ← Home screen: KPI cards, heatmap, weighted yield
│   ├── fdlist/                   ← FD directory: list, table, card, grid views
│   ├── fddetail/                 ← FD detail / edit screen
│   ├── calendar/                 ← Maturity calendar & cash flow forecast
│   ├── ocr/                      ← Camera capture, PDF import, OCR pipeline
│   ├── tax/                      ← TDS monitor, 15G/15H assistant
│   ├── strategy/                 ← Break-FD calculator, laddering visualizer
│   └── settings/                 ← SMTP config, notification prefs, app lock, about
├── gradle/
│   └── libs.versions.toml        ← Version Catalog
├── build.gradle.kts              ← Root build file
└── settings.gradle.kts           ← Module declarations
```

### Module Dependency Graph

```
                          ┌─────────┐
                          │   app   │
                          └────┬────┘
               ┌───────────────┼───────────────┐
               ▼               ▼               ▼
        ┌──────────┐    ┌──────────┐    ┌──────────┐
        │ feature/ │    │ feature/ │    │ feature/ │  ... (all feature modules)
        │dashboard │    │  fdlist  │    │   ocr    │
        └────┬─────┘    └────┬─────┘    └────┬─────┘
             │               │               │
             ▼               ▼               ▼
        ┌─────────────────────────────────────────┐
        │              core/domain                 │
        │              core/ui                     │
        │              core/common                 │
        └──────────────────┬──────────────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  core/data  │
                    └─────────────┘
```

**Rules:**
- `feature/*` modules depend on `core/domain`, `core/ui`, and `core/common`.
- `feature/*` modules **never** depend on each other (strict horizontal isolation).
- `core/data` depends on `core/domain` (implements repository interfaces).
- `app` depends on all `feature/*` and `core/*` modules (wires everything together via Hilt and Navigation).

---

## 3. Data Layer

### 3.1 Room Database

The central Room database is named `fd_tracker_db` and is defined in `core/data`.

#### Entity: `FdEntity`

```kotlin
@Entity(
    tableName = "fixed_deposits",
    foreignKeys = [
        ForeignKey(
            entity = BankEntity::class,
            parentColumns = ["bankName"],
            childColumns = ["bankName"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("bankName"), Index("maturityDate")]
)
data class FdEntity(
    @PrimaryKey
    val fdAccountNumber: String,
    val bankName: String,
    val cifCustomerId: String,
    val primaryHolderName: String,
    val holdingMode: String,              // Enum stored as String
    val jointHolderNames: String?,        // JSON-serialized list
    val principalAmount: String,          // BigDecimal stored as String to avoid floating-point precision loss
    val valueDate: Long,                  // Epoch millis
    val maturityDate: Long,              // Epoch millis
    val tenureDays: Int,
    val interestRatePA: String,           // BigDecimal stored as String
    val compoundingFrequency: String,     // Enum stored as String (QUARTERLY, HALF_YEARLY, ANNUALLY, MONTHLY)
    val estimatedMaturityAmount: String,  // BigDecimal stored as String
    val autoRenewalInstruction: String,   // Enum stored as String
    val payoutAccountId: String,
    val gracePeriodDays: Int = 7,         // Bank-specific grace period (typically 7-14 days)
    // Optional fields
    val nomineeName: String?,
    val interestPayoutFrequency: String?, // Enum stored as String (null = Cumulative)
    val taxTdsApplicable: Boolean?,
    val taxExemptionForm: String?,        // Enum stored as String: NONE, FORM_15G, FORM_15H
    val specialCategory: String?,         // Enum stored as String
    val isTaxSaver: Boolean,
    val branchCode: String?,              // Bank branch code
    val ifscCode: String?,                // IFSC / BIC routing code
    // Metadata
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean
)
```

> **Note — Financial Precision:** Room does not natively support `BigDecimal`. All monetary and rate fields are persisted as `String` in the database and mapped to `BigDecimal` in the domain layer via `FdMapper`. This avoids `Double` floating-point precision loss on large amounts (e.g., ₹10,00,000.50).

#### Entity: `BankEntity`

```kotlin
@Entity(
    tableName = "banks",
    indices = [Index(value = ["bankName"], unique = true)]
)
data class BankEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,
    val bankCode: String?,
    val iconResName: String?,             // Drawable resource name
    val colorHex: String?,                // Brand color for heatmap
    val depositInsuranceLimitPaise: Long = 500_000_00L  // DICGC limit per depositor per bank (default ₹5,00,000)
)
```

#### Entity: `ReminderEntity`

```kotlin
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = FdEntity::class,
            parentColumns = ["fdAccountNumber"],
            childColumns = ["fdAccountNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fdAccountNumber"), Index("triggerDate")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fdAccountNumber: String,          // FK to fixed_deposits
    val reminderType: String,             // PUSH, EMAIL, SMS
    val triggerDate: Long,                // Epoch millis
    val daysBefore: Int,                  // 14, 7, 1, 0 (maturity day), -7 (grace end)
    val isSent: Boolean,
    val sentAt: Long?
)
```

#### Entity: `SmtpConfigEntity`

```kotlin
@Entity(tableName = "smtp_config")
data class SmtpConfigEntity(
    @PrimaryKey
    val id: Int = 1,                      // Singleton row
    val host: String,
    val port: Int,
    val username: String,                 // Encrypted
    val password: String,                 // Encrypted
    val fromAddress: String,
    val toAddress: String,
    val useTls: Boolean
)
```

#### Key DAOs

```kotlin
// Projection tuple for bank-wise aggregation
data class BankPrincipalTuple(
    val bankName: String,
    val total: Double          // SUM result from SQLite (cast from String columns)
)

@Dao
interface FdDao {
    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 ORDER BY maturityDate ASC")
    fun observeAllActive(): Flow<List<FdEntity>>

    @Query("SELECT * FROM fixed_deposits WHERE fdAccountNumber = :id")
    fun observeById(id: String): Flow<FdEntity?>

    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 AND maturityDate BETWEEN :from AND :to")
    fun observeByMaturityRange(from: Long, to: Long): Flow<List<FdEntity>>

    @Query("SELECT bankName, SUM(CAST(principalAmount AS REAL)) as total FROM fixed_deposits WHERE isActive = 1 GROUP BY bankName")
    fun observeBankWisePrincipal(): Flow<List<BankPrincipalTuple>>

    @Query("SELECT SUM(CAST(principalAmount AS REAL)) FROM fixed_deposits WHERE isActive = 1")
    fun observeTotalPrincipal(): Flow<Double?>

    // Sort & filter queries
    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 AND bankName = :bankName ORDER BY maturityDate ASC")
    fun observeByBank(bankName: String): Flow<List<FdEntity>>

    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 ORDER BY CAST(principalAmount AS REAL) DESC")
    fun observeAllActiveSortedByPrincipal(): Flow<List<FdEntity>>

    @Query("SELECT * FROM fixed_deposits WHERE isActive = 1 ORDER BY CAST(interestRatePA AS REAL) DESC")
    fun observeAllActiveSortedByRate(): Flow<List<FdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(fd: FdEntity)

    @Delete
    suspend fun delete(fd: FdEntity)
}
```

#### Type Converters

```kotlin
class Converters {
    // Long ↔ LocalDate (via Epoch millis)
    // String ↔ List<String> (via JSON serialization for joint holder names)
    // String ↔ Enum (HoldingMode, RenewalInstruction, PayoutFrequency, SpecialCategory, TaxExemptionForm)
    // String ↔ BigDecimal (for principalAmount, interestRatePA, estimatedMaturityAmount)
}
```

### 3.2 Repository Pattern

```
┌──────────────┐       ┌────────────────────┐       ┌──────────┐
│   Use Case   │──────▶│  FdRepository      │──────▶│  FdDao   │
│  (Domain)    │       │  (Interface in      │       │  (Room)  │
│              │       │   core/domain)      │       │          │
└──────────────┘       └────────────────────┘       └──────────┘
                              ▲
                              │ implements
                       ┌──────┴─────────────┐
                       │ FdRepositoryImpl    │
                       │ (core/data)         │
                       └────────────────────┘
```

**Repositories:**

| Repository              | Responsibility                                      |
| ----------------------- | --------------------------------------------------- |
| `FdRepository`          | CRUD for FD records, queries (by bank, date range)  |
| `BankRepository`        | Bank metadata management                            |
| `ReminderRepository`    | Reminder scheduling, status tracking                |
| `SmtpRepository`        | SMTP configuration persistence                      |
| `UserPrefsRepository`   | App settings via DataStore                           |

### 3.3 SMTP Email Service

```kotlin
class SmtpEmailService @Inject constructor(
    private val smtpRepository: SmtpRepository
) {
    suspend fun sendMaturityReminder(
        fdNumber: String,
        bankName: String,
        maturityDate: LocalDate,
        maturityAmount: BigDecimal
    ): Result<Unit>
}
```

- Runs on `Dispatchers.IO`.
- Uses Jakarta Mail to compose and send MIME messages.
- Called by `ReminderWorker` (WorkManager) when a reminder triggers.
- Failures are logged and retried on next WorkManager run (exponential backoff).

---

## 4. Domain Layer

The domain layer is a **pure Kotlin module** with no Android dependencies. It contains use cases, domain models, and repository interfaces.

### 4.1 Domain Models

```kotlin
data class FixedDeposit(
    // Mandatory fields
    val fdAccountNumber: String,
    val bankName: String,
    val cifCustomerId: String,
    val primaryHolderName: String,
    val holdingMode: HoldingMode,
    val jointHolderNames: List<String>,
    val principalAmount: BigDecimal,
    val valueDate: LocalDate,
    val maturityDate: LocalDate,
    val tenureDays: Int,
    val interestRatePA: BigDecimal,
    val compoundingFrequency: PayoutFrequency,
    val estimatedMaturityAmount: BigDecimal,
    val autoRenewalInstruction: RenewalInstruction,
    val payoutAccountId: String,
    val gracePeriodDays: Int = 7,
    // Optional fields
    val nomineeName: String? = null,
    val interestPayoutFrequency: PayoutFrequency? = null,  // null = Cumulative
    val taxTdsApplicable: Boolean? = null,
    val taxExemptionForm: TaxExemptionForm = TaxExemptionForm.NONE,
    val specialCategory: SpecialCategory = SpecialCategory.STANDARD,
    val isTaxSaver: Boolean = false,
    val branchCode: String? = null,
    val ifscCode: String? = null,
    // Metadata
    val isActive: Boolean = true
)

data class UserProfile(
    val fullName: String,
    val dateOfBirth: LocalDate,
    val panNumber: String? = null,
    val isSeniorCitizen: Boolean = false   // Derived from DOB (age >= 60)
)

enum class HoldingMode { SINGLE, JOINTLY, EITHER_OR_SURVIVOR, FORMER_OR_SURVIVOR }
enum class RenewalInstruction { RENEW_BOTH, RENEW_PRINCIPAL, PAYOUT }
enum class PayoutFrequency { MONTHLY, QUARTERLY, HALF_YEARLY, ANNUALLY, CUMULATIVE }
enum class SpecialCategory { STANDARD, SENIOR_CITIZEN, STAFF }
enum class TaxExemptionForm { NONE, FORM_15G, FORM_15H }
```

> **Note — UserProfile:** `UserProfile` is stored via `DataStore Preferences` (managed by `UserPrefsRepository`). It is needed by `GetForm15GChecklistUseCase` to determine the correct form type (15G for age < 60, 15H for age ≥ 60).

### 4.2 Use Cases

Each use case encapsulates a single business action and is injected into ViewModels via Hilt.

| Use Case                       | Input                              | Output                         | Description                                           |
| ------------------------------ | ---------------------------------- | ------------------------------ | ----------------------------------------------------- |
| `GetAllActiveFdsUseCase`       | —                                  | `Flow<List<FixedDeposit>>`     | Stream all active FDs                                 |
| `GetFdByIdUseCase`             | `fdAccountNumber: String`          | `Flow<FixedDeposit?>`          | Stream single FD details                              |
| `AddOrUpdateFdUseCase`         | `FixedDeposit`                     | `Result<Unit>`                 | Validate and persist an FD record                     |
| `DeleteFdUseCase`              | `fdAccountNumber: String`          | `Result<Unit>`                 | Soft-delete an FD (set `isActive = false`)            |
| `CalculateMaturityUseCase`     | `principal, rate, tenure, compounding` | `MaturityResult`           | Compute maturity amount and date                      |
| `CalculateAccruedInterestUseCase` | `FixedDeposit, currentDate`     | `BigDecimal`                   | Real-time accrued interest based on elapsed days      |
| `GetDashboardSummaryUseCase`   | —                                  | `Flow<DashboardSummary>`       | Aggregated KPIs: total principal, accrued, yield      |
| `GetBankExposureUseCase`       | —                                  | `Flow<List<BankExposure>>`     | Bank-wise principal distribution for heatmap          |
| `GetWeightedYieldUseCase`      | —                                  | `Flow<BigDecimal>`             | Portfolio weighted average interest rate              |
| `GetMaturityTimelineUseCase`   | `startDate, endDate`               | `Flow<List<FixedDeposit>>`     | FDs maturing in a given window                        |
| `GetCashFlowForecastUseCase`   | `months: Int`                      | `Flow<List<MonthlyCashFlow>>`  | Monthly payout projections                            |
| `CalculateBreakFdPenaltyUseCase` | `targetAmount: BigDecimal`       | `List<BreakFdRecommendation>`  | Optimal FD(s) to break with minimum penalty           |
| `AnalyzeLadderingUseCase`      | —                                  | `LadderAnalysis`               | Maturity spread analysis and recommendations          |
| `GetTdsThresholdStatusUseCase` | `financialYear`                    | `Flow<List<BankTdsStatus>>`    | Per-bank interest vs TDS threshold                    |
| `GetForm15GChecklistUseCase`   | `userProfile`                      | `List<Form15GAction>`          | Which banks need 15G/15H submission                   |
| `ParseOcrResultUseCase`        | `rawText: String`                  | `OcrParsedFd`                  | Extract structured FD fields from raw OCR text        |
| `ScheduleRemindersUseCase`     | `FixedDeposit`                     | `Result<Unit>`                 | Create reminder entries at T-14, T-7, T-1, T+grace   |
| `CheckInsuranceLimitUseCase`   | —                                  | `Flow<List<BankExposure>>`     | Flag banks where total deposits exceed DICGC insurance limit (default ₹5,00,000) |

### 4.3 Business Logic: Interest Calculation

```
Compound Interest (Cumulative):
  A = P × (1 + r/n)^(n×t)
  Where:
    P = Principal Amount
    r = Annual Interest Rate (decimal)
    n = Compounding frequency per year (1, 2, 4, 12)
    t = Tenure in years

Simple Interest (Non-Cumulative Payout):
  I = P × r × t
  Payout per period = I / number_of_periods

Accrued Interest (as of today):
  elapsed = daysBetween(valueDate, today)
  A_today = P × (1 + r/n)^(n × elapsed/365) - P

Premature Withdrawal Penalty:
  effective_rate = applicable_rate_for_elapsed_tenure - penalty_rate (typically 1%)
  payout = P × (1 + effective_rate/n)^(n × elapsed/365)
```

---

## 5. Presentation Layer

### 5.1 Screen Map & Navigation Graph

The app uses **Navigation Compose** with type-safe routes defined via `@Serializable` data classes/objects.

```
┌──────────────────────────────────────────────────────────────────┐
│                         NavHost (app module)                      │
│                                                                  │
│  BottomNavBar:                                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │Dashboard │  │ FD List  │  │ Calendar │  │ Settings │         │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘         │
│       │              │              │              │              │
│       ▼              ▼              ▼              ▼              │
│  DashboardScreen  FdListScreen  CalendarScreen  SettingsScreen   │
│       │              │                             │              │
│       │              ├──▶ FdDetailScreen            ├──▶ SmtpConfigScreen
│       │              │      │                      ├──▶ NotificationPrefsScreen
│       │              │      └──▶ EditFdScreen       └──▶ AppLockScreen
│       │              │                                           │
│       ├──▶ TaxScreen │                                           │
│       │      ├──▶ TdsMonitorScreen                               │
│       │      └──▶ Form15GScreen                                  │
│       │                                                          │
│       ├──▶ StrategyScreen                                        │
│       │      ├──▶ BreakFdScreen                                  │
│       │      └──▶ LadderingScreen                                │
│       │                                                          │
│       └──▶ OcrCaptureScreen ──▶ OcrReviewScreen ──▶ EditFdScreen │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### 5.2 ViewModel & UI State Pattern

Every screen follows a consistent UDF pattern:

```kotlin
// UI State — immutable data class
data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalPrincipal: BigDecimal = BigDecimal.ZERO,
    val totalAccrued: BigDecimal = BigDecimal.ZERO,
    val totalMaturityValue: BigDecimal = BigDecimal.ZERO,
    val weightedYield: BigDecimal = BigDecimal.ZERO,
    val bankExposures: List<BankExposure> = emptyList(),
    val upcomingMaturities: List<FixedDeposit> = emptyList(),
    val error: String? = null
)

// UI Events — sealed interface
sealed interface DashboardEvent {
    data object Refresh : DashboardEvent
    data class NavigateToFd(val fdId: String) : DashboardEvent
    data object NavigateToOcr : DashboardEvent
}

// ViewModel
@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardSummary: GetDashboardSummaryUseCase,
    getBankExposure: GetBankExposureUseCase,
    // ...
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        getDashboardSummary(),
        getBankExposure(),
        // ...
    ) { summary, exposures ->
        DashboardUiState(
            isLoading = false,
            totalPrincipal = summary.totalPrincipal,
            // ...
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun onEvent(event: DashboardEvent) { /* handle navigation, refresh */ }
}
```

### 5.3 Theme & Design System (`core/ui`)

- **Material 3 Dynamic Color** on Android 12+ with a fallback static theme.
- Custom color scheme tokens: `fdMaturing` (amber), `fdActive` (green), `fdExpired` (red), `fdGracePeriod` (orange).
- Typography scale using `GoogleFont` provider (e.g., Inter or Roboto).
- Shared composables: `FdCard`, `KpiCard`, `BankChip`, `StatusBadge`, `InsuranceLimitBanner`, `EmptyState`, `LoadingShimmer`.
- Dark mode support with auto-detection via `isSystemInDarkTheme()`.

#### UI Behavior Notes

- **`FdCard` — Maturity Progress Bar:** Each card displays a `LinearProgressIndicator` showing how close the FD is to maturity: `progress = (today - valueDate) / (maturityDate - valueDate)`. Color transitions from `fdActive` (green) → `fdMaturing` (amber) → `fdExpired` (red).
- **`InsuranceLimitBanner`:** Shown on the Dashboard when any bank's total deposits exceed its configured `depositInsuranceLimitPaise` (default DICGC ₹5,00,000).
- **Calendar — 30-Day Highlight:** FDs maturing within the next 30 days are highlighted with the `fdMaturing` color on the `MaturityCalendar` composable.
- **FD List — Sort & Filter:** `FdListScreen` supports sorting by maturity date (default), principal amount, or interest rate, and filtering by bank name. Sort/filter state is held in `FdListViewModel`.

---

## 6. Cross-Cutting Concerns

### 6.1 Dependency Injection (Hilt)

```
@HiltAndroidApp
class FdTrackerApp : Application()

Hilt Modules:
├── DatabaseModule        → Provides Room DB instance, all DAOs
├── RepositoryModule      → Binds Repository interfaces to implementations
├── SmtpModule            → Provides SmtpEmailService
├── DispatcherModule      → Provides IO, Default, Main dispatchers
└── UseCaseModule         → (Optional) Provides use cases if not constructor-injected
```

### 6.2 WorkManager — Reminder Scheduling

```
┌─────────────────────────────────────────────────────────────────┐
│                    Reminder Scheduling Flow                       │
│                                                                  │
│  User adds/edits FD                                              │
│       │                                                          │
│       ▼                                                          │
│  ScheduleRemindersUseCase                                        │
│       │                                                          │
│       ├──▶ Insert ReminderEntity (T-14 days) into Room           │
│       ├──▶ Insert ReminderEntity (T-7 days) into Room            │
│       ├──▶ Insert ReminderEntity (T-1 day) into Room             │
│       ├──▶ Insert ReminderEntity (T+0 maturity day) into Room    │
│       └──▶ Insert ReminderEntity (T+grace end) into Room         │
│                                                                  │
│  WorkManager PeriodicWorkRequest (runs every 6 hours)            │
│       │                                                          │
│       ▼                                                          │
│  ReminderCheckWorker                                             │
│       │                                                          │
│       ├──▶ Query unsent reminders where triggerDate <= now        │
│       ├──▶ For each: show local push notification                │
│       ├──▶ For each: send SMTP email (if email enabled)          │
│       └──▶ Mark reminder as sent in Room                         │
│                                                                  │
│  BootReceiver (RECEIVE_BOOT_COMPLETED)                           │
│       └──▶ Re-enqueue WorkManager periodic work after reboot     │
└─────────────────────────────────────────────────────────────────┘
```

> **Clarification — AlarmManager vs WorkManager:**
> - **WorkManager** is the **primary** reminder mechanism. It runs a periodic check every 6 hours, evaluates pending reminders, and fires notifications + emails.
> - **AlarmManager** (exact alarms) is used **only** for maturity-day (T+0) notifications where precise timing matters. This requires `SCHEDULE_EXACT_ALARM` permission on API 31+ and `USE_EXACT_ALARM` on API 33+. If exact alarm permission is denied by the user, the system gracefully falls back to the WorkManager periodic check.
> - **SMS reminders** (if enabled by the user) are sent via `SmsManager` within `ReminderCheckWorker`, requiring the `SEND_SMS` runtime permission.

### 6.3 OCR Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                        OCR Pipeline Flow                         │
│                                                                  │
│  ┌─────────────┐     ┌────────────┐     ┌───────────────┐       │
│  │  User picks  │────▶│  Image     │────▶│  ML Kit Text  │       │
│  │  source:     │     │  acquired  │     │  Recognition  │       │
│  │  • Camera    │     │  (Bitmap)  │     │  (on-device)  │       │
│  │  • Gallery   │     │            │     │               │       │
│  │  • PDF file  │     │ (PDF pages │     │  Returns raw  │       │
│  │              │     │  rendered  │     │  text blocks   │       │
│  │              │     │  via       │     │  with coords  │       │
│  │              │     │PdfRenderer)│     │               │       │
│  └─────────────┘     └────────────┘     └──────┬────────┘       │
│                                                 │                │
│                                                 ▼                │
│                                        ┌────────────────┐        │
│                                        │ ParseOcrResult │        │
│                                        │ UseCase        │        │
│                                        │                │        │
│                                        │ Regex + NLP    │        │
│                                        │ patterns to    │        │
│                                        │ extract:       │        │
│                                        │ • Bank Name    │        │
│                                        │ • FD Number    │        │
│                                        │ • Principal    │        │
│                                        │ • Rate         │        │
│                                        │ • Dates        │        │
│                                        │ • Tenure       │        │
│                                        └───────┬────────┘        │
│                                                │                 │
│                                                ▼                 │
│                                       ┌─────────────────┐        │
│                                       │ OcrReviewScreen │        │
│                                       │                 │        │
│                                       │ User verifies & │        │
│                                       │ corrects parsed │        │
│                                       │ fields          │        │
│                                       └────────┬────────┘        │
│                                                │                 │
│                                                ▼                 │
│                                       ┌─────────────────┐        │
│                                       │ AddOrUpdateFd   │        │
│                                       │ UseCase         │        │
│                                       │ → Room DB       │        │
│                                       └─────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

### 6.4 SMTP Email Flow

```
┌─────────────────────────────────────────────────────────┐
│                   SMTP Email Flow                        │
│                                                          │
│  ReminderCheckWorker (Dispatchers.IO)                    │
│       │                                                  │
│       ▼                                                  │
│  SmtpRepository.getConfig()                              │
│       │                                                  │
│       ▼                                                  │
│  Decrypt credentials (EncryptedSharedPreferences)        │
│       │                                                  │
│       ▼                                                  │
│  SmtpEmailService.sendMaturityReminder()                 │
│       │                                                  │
│       ├──▶ Create Jakarta Mail Session (TLS/SSL)         │
│       ├──▶ Compose MimeMessage (subject, HTML body)      │
│       ├──▶ Transport.send(message)                       │
│       │                                                  │
│       ├── Success ──▶ Mark ReminderEntity.isSent = true  │
│       └── Failure ──▶ Log error, WorkManager retries     │
│                       with exponential backoff           │
└─────────────────────────────────────────────────────────┘
```

---

## 7. Room Database Schema (ER Diagram)

```
┌────────────────────────┐        ┌──────────────────────────┐
│    fixed_deposits      │        │       banks               │
├────────────────────────┤        ├──────────────────────────┤
│ PK fdAccountNumber     │        │ PK id (auto)             │
│ FK bankName ───────────┼───────▶│ UQ bankName               │
│    cifCustomerId       │        │    bankCode               │
│    primaryHolderName   │        │    iconResName            │
│    holdingMode         │        │    colorHex               │
│    jointHolderNames    │        │    depositInsuranceLimit  │
│    principalAmount ¹   │        └──────────────────────────┘
│    valueDate           │
│    maturityDate        │        ┌──────────────────────────┐
│    tenureDays          │        │     reminders            │
│    interestRatePA ¹    │        ├──────────────────────────┤
│    compoundingFreq     │        │ PK id (auto)             │
│    estimatedMaturity ¹ │        │ FK fdAccountNumber ──────┼──┐
│    autoRenewalInstr    │        │    reminderType           │  │
│    payoutAccountId     │        │    triggerDate            │  │
│    gracePeriodDays     │        │    daysBefore             │  │
│    nomineeName         │        │    isSent                 │  │
│    interestPayoutFrq   │        │    sentAt                 │  │
│    taxTdsApplicable    │        └──────────────────────────┘  │
│    taxExemptionForm    │                                      │
│    specialCategory     │                                      │
│    isTaxSaver          │◀─────────────────────────────────────┘
│    branchCode          │
│    ifscCode            │        ┌──────────────────────────┐
│    createdAt           │        │    smtp_config            │
│    updatedAt           │        ├──────────────────────────┤
│    isActive            │        │ PK id (singleton=1)      │
└────────────────────────┘        │    host                   │
                                  │    port                   │
¹ Stored as String                │    username (enc)         │
  (mapped to BigDecimal           │    password (enc)         │
   in domain layer)               │    fromAddress            │
                                  │    toAddress              │
                                  │    useTls                 │
                                  └──────────────────────────┘
```

---

## 8. Navigation Architecture

### 8.1 Route Definitions

```kotlin
// Top-level destinations (Bottom Nav)
@Serializable object DashboardRoute
@Serializable object FdListRoute
@Serializable object CalendarRoute
@Serializable object SettingsRoute

// Nested destinations
@Serializable data class FdDetailRoute(val fdId: String)
@Serializable data class EditFdRoute(val fdId: String? = null)   // null = new FD
@Serializable object OcrCaptureRoute
@Serializable data class OcrReviewRoute(val parsedDataJson: String)
@Serializable object TaxDashboardRoute
@Serializable object TdsMonitorRoute
@Serializable object Form15GRoute
@Serializable object StrategyRoute
@Serializable object BreakFdRoute
@Serializable object LadderingRoute
@Serializable object SmtpConfigRoute
@Serializable object NotificationPrefsRoute
@Serializable object AppLockRoute
```

### 8.2 NavHost Setup (`app` module)

```kotlin
@Composable
fun FdTrackerNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = DashboardRoute) {
        // Bottom nav tabs
        composable<DashboardRoute> { DashboardScreen(navController) }
        composable<FdListRoute> { FdListScreen(navController) }
        composable<CalendarRoute> { CalendarScreen(navController) }
        composable<SettingsRoute> { SettingsScreen(navController) }

        // Detail & Edit
        composable<FdDetailRoute> { FdDetailScreen(navController) }
        composable<EditFdRoute> { EditFdScreen(navController) }

        // OCR
        composable<OcrCaptureRoute> { OcrCaptureScreen(navController) }
        composable<OcrReviewRoute> { OcrReviewScreen(navController) }

        // Tax
        composable<TaxDashboardRoute> { TaxDashboardScreen(navController) }
        composable<TdsMonitorRoute> { TdsMonitorScreen(navController) }
        composable<Form15GRoute> { Form15GScreen(navController) }

        // Strategy
        composable<StrategyRoute> { StrategyScreen(navController) }
        composable<BreakFdRoute> { BreakFdScreen(navController) }
        composable<LadderingRoute> { LadderingScreen(navController) }

        // Settings sub-screens
        composable<SmtpConfigRoute> { SmtpConfigScreen(navController) }
        composable<NotificationPrefsRoute> { NotificationPrefsScreen(navController) }
        composable<AppLockRoute> { AppLockScreen(navController) }
    }
}
```

---

## 9. Error Handling Strategy

| Layer          | Strategy                                                                     |
| -------------- | ---------------------------------------------------------------------------- |
| **Data**       | Repository methods return `Result<T>` or throw domain-specific exceptions    |
| **Domain**     | Use cases catch data-layer exceptions and wrap into `Result<T>`              |
| **ViewModel**  | Maps `Result.Failure` into `UiState.error` message string                    |
| **UI**         | Displays `Snackbar` or inline error composable; offers retry action          |
| **WorkManager**| Uses `Result.retry()` with exponential backoff for transient SMTP failures   |

---

## 10. Security Architecture

```
┌─────────────────────────────────────────────────────┐
│                  Security Layers                     │
│                                                      │
│  ┌──────────────────────────────────────────────┐    │
│  │  App Lock (Biometric / PIN)                  │    │
│  │  AndroidX Biometric API                      │    │
│  │  Triggered on app launch / resume from bg    │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  ┌──────────────────────────────────────────────┐    │
│  │  Credential Encryption                       │    │
│  │  EncryptedSharedPreferences (AES-256)        │    │
│  │  Used for: SMTP username/password            │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  ┌──────────────────────────────────────────────┐    │
│  │  Database Encryption (Optional - Phase 2)    │    │
│  │  SQLCipher for Room (net.zetetic:sqlcipher)   │    │
│  │  Encrypts entire FD database at rest         │    │
│  └──────────────────────────────────────────────┘    │
│                                                      │
│  ┌──────────────────────────────────────────────┐    │
│  │  Network Security                            │    │
│  │  SMTP over TLS only (port 587)               │    │
│  │  Certificate pinning (optional)              │    │
│  │  network_security_config.xml                 │    │
│  └──────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

---

## 11. Package Structure (Detailed)

```
com.fdtracker/
├── app/
│   ├── FdTrackerApp.kt                    // @HiltAndroidApp
│   ├── MainActivity.kt                   // Single Activity, setContent
│   └── navigation/
│       └── FdTrackerNavHost.kt            // NavHost + route wiring
│
├── core/
│   ├── data/
│   │   ├── db/
│   │   │   ├── FdTrackerDatabase.kt      // @Database
│   │   │   ├── dao/
│   │   │   │   ├── FdDao.kt
│   │   │   │   ├── BankDao.kt
│   │   │   │   ├── ReminderDao.kt
│   │   │   │   └── SmtpConfigDao.kt
│   │   │   ├── entity/
│   │   │   │   ├── FdEntity.kt
│   │   │   │   ├── BankEntity.kt
│   │   │   │   ├── ReminderEntity.kt
│   │   │   │   ├── SmtpConfigEntity.kt
│   │   │   │   └── BankPrincipalTuple.kt  // Projection data class for aggregation queries
│   │   │   └── converter/
│   │   │       └── Converters.kt
│   │   ├── repository/
│   │   │   ├── FdRepositoryImpl.kt
│   │   │   ├── BankRepositoryImpl.kt
│   │   │   ├── ReminderRepositoryImpl.kt
│   │   │   ├── SmtpRepositoryImpl.kt
│   │   │   └── UserPrefsRepositoryImpl.kt
│   │   ├── mapper/
│   │   │   ├── FdMapper.kt               // Entity ↔ Domain model
│   │   │   └── ReminderMapper.kt
│   │   ├── smtp/
│   │   │   └── SmtpEmailService.kt
│   │   ├── worker/
│   │   │   ├── ReminderCheckWorker.kt
│   │   │   └── WorkManagerInitializer.kt
│   │   └── di/
│   │       ├── DatabaseModule.kt
│   │       ├── RepositoryModule.kt
│   │       ├── SmtpModule.kt
│   │       └── DispatcherModule.kt
│   │
│   ├── domain/
│   │   ├── model/
│   │   │   ├── FixedDeposit.kt
│   │   │   ├── UserProfile.kt
│   │   │   ├── BankExposure.kt
│   │   │   ├── DashboardSummary.kt
│   │   │   ├── MonthlyCashFlow.kt
│   │   │   ├── BreakFdRecommendation.kt
│   │   │   ├── LadderAnalysis.kt
│   │   │   ├── BankTdsStatus.kt
│   │   │   ├── Form15GAction.kt
│   │   │   ├── MaturityResult.kt
│   │   │   └── OcrParsedFd.kt
│   │   ├── repository/
│   │   │   ├── FdRepository.kt           // Interface
│   │   │   ├── BankRepository.kt
│   │   │   ├── ReminderRepository.kt
│   │   │   ├── SmtpRepository.kt
│   │   │   └── UserPrefsRepository.kt
│   │   ├── usecase/
│   │   │   ├── fd/
│   │   │   │   ├── GetAllActiveFdsUseCase.kt
│   │   │   │   ├── GetFdByIdUseCase.kt
│   │   │   │   ├── AddOrUpdateFdUseCase.kt
│   │   │   │   └── DeleteFdUseCase.kt
│   │   │   ├── calculation/
│   │   │   │   ├── CalculateMaturityUseCase.kt
│   │   │   │   ├── CalculateAccruedInterestUseCase.kt
│   │   │   │   └── CalculateBreakFdPenaltyUseCase.kt
│   │   │   ├── dashboard/
│   │   │   │   ├── GetDashboardSummaryUseCase.kt
│   │   │   │   ├── GetBankExposureUseCase.kt
│   │   │   │   ├── GetWeightedYieldUseCase.kt
│   │   │   │   └── CheckInsuranceLimitUseCase.kt
│   │   │   ├── calendar/
│   │   │   │   ├── GetMaturityTimelineUseCase.kt
│   │   │   │   └── GetCashFlowForecastUseCase.kt
│   │   │   ├── tax/
│   │   │   │   ├── GetTdsThresholdStatusUseCase.kt
│   │   │   │   └── GetForm15GChecklistUseCase.kt
│   │   │   ├── strategy/
│   │   │   │   └── AnalyzeLadderingUseCase.kt
│   │   │   ├── ocr/
│   │   │   │   └── ParseOcrResultUseCase.kt
│   │   │   └── reminder/
│   │   │       └── ScheduleRemindersUseCase.kt
│   │   └── util/
│   │       └── InterestCalculator.kt     // Pure math functions
│   │
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Theme.kt
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   └── Shape.kt
│   │   ├── component/
│   │   │   ├── FdCard.kt                // Includes maturity progress bar
│   │   │   ├── KpiCard.kt
│   │   │   ├── BankChip.kt
│   │   │   ├── StatusBadge.kt
│   │   │   ├── InsuranceLimitBanner.kt   // Warning banner when bank exposure exceeds DICGC limit
│   │   │   ├── EmptyState.kt
│   │   │   ├── LoadingShimmer.kt
│   │   │   └── ConfirmDialog.kt
│   │   └── util/
│   │       ├── CurrencyFormatter.kt
│   │       └── DateFormatter.kt
│   │
│   └── common/
│       ├── Constants.kt
│       ├── Extensions.kt
│       └── Result.kt                     // Sealed class for Result wrapper
│
├── feature/
│   ├── dashboard/
│   │   ├── DashboardScreen.kt
│   │   ├── DashboardViewModel.kt
│   │   ├── DashboardUiState.kt
│   │   └── components/
│   │       ├── SummaryCards.kt
│   │       ├── BankHeatmap.kt
│   │       └── UpcomingMaturities.kt
│   │
│   ├── fdlist/
│   │   ├── FdListScreen.kt
│   │   ├── FdListViewModel.kt
│   │   ├── FdListUiState.kt
│   │   └── components/
│   │       ├── FdListView.kt
│   │       ├── FdTableView.kt
│   │       ├── FdCardView.kt
│   │       └── FdGridView.kt
│   │
│   ├── fddetail/
│   │   ├── FdDetailScreen.kt
│   │   ├── FdDetailViewModel.kt
│   │   ├── EditFdScreen.kt
│   │   └── EditFdViewModel.kt
│   │
│   ├── calendar/
│   │   ├── CalendarScreen.kt
│   │   ├── CalendarViewModel.kt
│   │   └── components/
│   │       ├── MaturityCalendar.kt
│   │       └── CashFlowChart.kt
│   │
│   ├── ocr/
│   │   ├── OcrCaptureScreen.kt
│   │   ├── OcrReviewScreen.kt
│   │   ├── OcrViewModel.kt
│   │   └── util/
│   │       └── TextBlockParser.kt
│   │
│   ├── tax/
│   │   ├── TaxDashboardScreen.kt
│   │   ├── TdsMonitorScreen.kt
│   │   ├── Form15GScreen.kt
│   │   └── TaxViewModel.kt
│   │
│   ├── strategy/
│   │   ├── StrategyScreen.kt
│   │   ├── BreakFdScreen.kt
│   │   ├── LadderingScreen.kt
│   │   ├── BreakFdViewModel.kt
│   │   └── LadderingViewModel.kt
│   │
│   └── settings/
│       ├── SettingsScreen.kt
│       ├── SmtpConfigScreen.kt
│       ├── NotificationPrefsScreen.kt
│       ├── AppLockScreen.kt
│       └── SettingsViewModel.kt
```

---

## 12. Build Variants & Flavors

| Variant       | Purpose                                      | Config                            |
| ------------- | -------------------------------------------- | --------------------------------- |
| **debug**     | Development with logging, mock data, no ProGuard | `isDebuggable = true`          |
| **release**   | Production with R8 minification, no logging  | `isMinifyEnabled = true`          |

No product flavors are needed initially (single app variant). Flavors can be added later if free/premium tiers are introduced.

---

## 13. Future Architecture Considerations (Phase 2+)

> **Note:** Account Aggregator integration is listed as a feature in the product requirements (`project-init.md`, Feature 1, bullet 3) but is explicitly **deferred to Phase 2** due to regulatory and SDK complexity. Phase 1 supports manual entry and OCR only.

| Feature                        | Phase | Architecture Impact                                                |
| ------------------------------ | ----- | ------------------------------------------------------------------ |
| **Account Aggregator Sync**    | 2     | Add `core/network` module with Retrofit, AA SDK (Sahamati) integration |
| **Cloud Backup (Google Drive)**| 2     | Add `core/sync` module with Drive API, conflict resolution logic   |
| **Widget (Glance)**            | 2     | Add `feature/widget` module using Jetpack Glance for home screen   |
| **SMS Notifications**          | 2     | Add `SmsService` in `core/data`, `SEND_SMS` runtime permission    |
| **Wear OS Companion**          | 3     | Separate `:wear` module sharing `core/domain`                      |
| **Multi-user / Family Mode**   | 3     | Add `UserEntity` and FK relationships to all tables                |
