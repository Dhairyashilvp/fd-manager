# Project: Fixed Deposit Tracker
## Problem
User has bank account in multiple banks. each bank has multiple FDs which have different maturity dates and interest rates and other details. User wants to track these FDs and get email reminders before maturity.

## When User Opens an FD
When user open a Fixed Deposit (FD), the bank issues an FD receipt or a digital certificate. This document contains a specific set of parameters that govern how users money grows and what happens when the term ends.

## Mandatory Attributes

These are essential details required by regulatory guidelines and banking systems to validate and track the deposit.

* **Fixed Deposit Receipt (FDR) / Account Number:** A unique identification number assigned to users specific deposit.
* **Customer ID / CIF Number:** users unique customer identification number with the bank.
* **Primary Holder's Name:** The full legal name of the person who owns the deposit.
* **Mode of Holding:** Specifies how the account is operated (e.g., *Single, Jointly, Either or Survivor, Former or Survivor*).
* **Joint Holder’s Name(s):** Required if the mode of holding is anything other than "Single".
* **Principal Amount (Deposit Amount):** The exact booking amount or initial sum of money invested.
* **Date of Deposit (Value Date):** The exact date the FD became active and money was locked in.
* **Maturity Date:** The final date on which the deposit tenure ends and the money becomes available.
* **Tenure / Period:** The exact duration of the deposit, usually displayed in Days, Months, or Years (e.g., *1 Year, 45 Days*).
* **Rate of Interest:** The fixed per-annum interest percentage ($X\%$) locked in at the time of booking.
* **Maturity Amount:** The total estimated payout (Principal + Cumulative Interest) that will be paid on the maturity date.
* **Maturity Instruction (Auto-Renewal status):** A definitive instruction on what to do when the FD expires. Standard options include:
* *Auto-Renew Principal and Interest*
* *Auto-Renew Principal Only*
* *Do Not Renew / Close and Payout*
* **Payout Account Details:** The specific savings or current account number linked to the FD where interest or the final maturity amount will be credited.

---

## Optional & Conditional Attributes

These details may or may not appear on the face of the receipt depending on users specific choices, tax status, or the bank's internal systems.

* **Nominee Name:** The individual designated to receive the funds in the event of the holder's death. (Highly recommended, but technically optional to fill out).
* **Interest Payout Frequency:** Required only if user choose a non-cumulative FD. It specifies when regular interest is paid out (e.g., *Monthly, Quarterly, Half-Yearly, Annually*).
* **Tax Status / TDS Flag:** Indicates whether Tax Deducted at Source (TDS) is applicable, or if forms like 15H/15G have been submitted to claim exemption.
* **Special Category Flag:** Notes whether the investor qualifies for special rates, most commonly a **Senior Citizen** tag or a **Staff/Ex-staff** benefit.
* **Tax-Saver Flag:** Indicates if the FD is locked under a specific tax-saving scheme (which usually comes with a mandatory 5-year lock-in period where premature withdrawal is completely blocked).
* **Branch Code & IFS Code / BIC:** The specific routing codes of the banking branch where the FD was physically or digitally opened.


## Features 
Here are the essential features a Multi-Bank FD Organizer should have to take the stress out of managing scattered savings:

## 1. Frictionless Data Entry & Ingestion

The biggest barrier to using a tracking app is the friction of manual entry. The system must make getting data into the app as easy as possible.

* **Smart Document Parser (OCR):** The user simply uploads an FD receipt PDF or takes a screenshot from their mobile banking app. The system automatically reads the image and extracts the Bank Name, Principal, ROI, Maturity Date, and FD Number.
* **Manual Form (With Automatic Calculations):** If entering manually, the user only plugs in Bank, Principal, Start Date, Tenure, and ROI. The app automatically calculates the Maturity Date and Maturity Amount so the user doesn't have to look it up.
* **Account Aggregator Fetch (Optional Sync):** Secure integration via open banking consent frameworks (like the Account Aggregator system in India or Plaid) to pull FD details directly from bank balances automatically.

---

## 2. The Unified "True Net Worth" Dashboard

Once the data is in, it needs to be organized into a single cockpit view.

* **Consolidated Financial Summary:** A snapshot at the top showing: Total Invested Principal, Current Accrued Interest (updated daily or monthly based on the compounding type), and Total Expected Maturity Value across *all* banks.
* **Bank-Wise Risk Heatmap:** A visual breakdown showing exactly how much money is parked in each institution. This is critical for monitoring exposure and ensuring holdings stay under federal deposit insurance safety limits per bank.
* **Weighted Average Portfolio Yield:** Calculates the single "true" interest rate the entire portfolio is earning, helping the user understand if their money is beating inflation.

---

## 3. Maturity Calendar & Liquidity Planning

The most frequent question a user has is: *"When do I get my cash back?"*

* **Maturity Timeline Calendar:** A clean, color-coded visual calendar mapping out exactly when each FD matures. FDs maturing within the next 30 days are highlighted.
* **Cash Flow Forecast:** A bar chart mapping out expected payouts over the next 12 to 24 months, allowing the user to plan for big-ticket life expenses (like vacations, insurance premiums, or down payments) using upcoming maturities.

---

## 4. Smart Alerts & Reminders (The Core Pain Killer)

This is the feature that prevents costly mistakes, like forgetting a deposit exists and letting it auto-renew at a terrible rate.

* **Multi-Stage Maturity Alerts:** Customizable push notifications, emails, or SMS alerts sent 14 days, 7 days, and 1 day before maturity.
* **The "Grace Period" Tracker:** Most banks offer a 7 to 14-day window after an FD matures to change renewal instructions without penalties. The app should explicitly track this countdown timer so the user doesn't miss the window.

---

## 5. Tax (TDS) Optimization Tools

Managing taxes across multiple banks is incredibly painful because each bank calculates tax independently.

* **Consolidated TDS Threshold Monitor:** Tracks total projected interest earned *per bank* for the financial year. If the interest income is approaching the regulatory tax-deduction threshold, the app flags it so the user can plan accordingly.
* **Form 15G/15H Assistant:** At the beginning of the financial year, the app checks the user's age/profile and generates a checklist of exactly which banks require a non-deduction form submission to prevent unnecessary tax lock-ups.

---

## 6. Strategic Decision Support

Tools that help the user make smart moves when money needs to be shuffled.

* **Emergency "Break-FD" Calculator:** If the user suddenly needs emergency cash, they can type the required amount into the app. The system analyzes all active FDs across all banks and calculates *which specific FD* will charge the lowest premature withdrawal penalty, telling the user exactly which bank account to liquidate.
* **FD Laddering Visualizer:** A tool that analyzes if the user's FDs are evenly spaced out (e.g., one maturing every 3 months) to maximize both yield and steady liquidity.


## Visualization Features
* User should be able to view all FDs in a list view.
* User should be able to view all FDs in a calendar view.
* User should be able to view all FDs in a chart view.
* User should be able to view all FDs in a table view.
* User should be able to view all FDs in a card view.
* User should be able to view all FDs in a grid view.
