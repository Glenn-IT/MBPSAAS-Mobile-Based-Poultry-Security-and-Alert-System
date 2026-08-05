# MBPSAAS — Arduino Integration Plan (multi-zone, modeled on ABMDMS)

**Status:** PLAN ONLY — nothing below is built yet. Review and approve before any code is written.
**Reference project:** ABMDMS (`C:\xampp\htdocs\ABMDMS`) — a finished, working Arduino → serial bridge → PHP API → MySQL chain.
**Target project:** MBPSAAS (this repo).
**Decision taken:** adopt the **multi-zone** model (like ABMDMS's ROOMA–ROOMD), not the current single buzzer flag.

---

## 1. Goal

MBPSAAS already has the *display* half working: the Android app polls PHP and shows motion events. What is missing is the *hardware* half — there is no Arduino sketch and no serial bridge feeding real sensor events into the database. This plan ports ABMDMS's proven hardware chain into MBPSAAS and upgrades MBPSAAS from a single on/off buzzer flag to per-zone tracking.

```
┌──────────┐  USB 9600  ┌──────────────────┐  HTTP POST   ┌────────────────────┐  SQL  ┌────────┐
│ Arduino  │ ─────────► │ serial_reader.php │ ───────────► │ log_motion_event   │ ────► │ MySQL  │
│ N×PIR +  │  tokens    │  (PHP bridge)     │  localhost   │ .php  (insert)     │  PDO  │ zones  │
│ buzzer   │            └──────────────────┘              └─────────┬──────────┘       └───┬────┘
└──────────┘                                                        │  HTTP GET            │
                                    ┌────────────────────┐          │                      │
                                    │  MBPSAAS Android    │◄─────────┘                      │
                                    │  app (this repo)    │  polls get_motion_events.php ───┘
                                    └────────────────────┘
```

---

## 2. Current state vs. what ABMDMS gives us

| Layer | ABMDMS (reference) | MBPSAAS today | Action |
|---|---|---|---|
| Arduino sketch | `arduino/motion_sensor.ino` — 4-zone PIR, prints `ROOMx_MOTION_DETECTED/STOPPED` | **none** | Port + adapt (add buzzer) |
| Serial bridge | `serial/serial_reader.php` — reads COM port, POSTs to API, dedupes, auto-reconnects | **none** | Port + adapt field names |
| Insert endpoint | `api/record_motion.php` — validates event_type + zone, PDO prepared insert | `api/log_motion_event.php` — buzzer_triggered + note only | Extend to accept zone + event_type |
| Read endpoint | `api/get_motion_logs.php` — status, per-zone map, stats, pagination | `api/get_motion_events.php` — flat list of 50 | Extend with zones + status (+ pagination) |
| DB schema | `motion_logs` (event_type, zone, source, detected_at) | `motion_events` (buzzer_triggered, note, detected_at) | Add zone / event_type / source columns |
| Test tool | `tools/simulate_motion.php` | **none** | Port |
| Android networking | (guide only) Retrofit | Retrofit `ApiClient`/`ApiService`/`ApiModels` — working | Extend models + UI for zones |
| Manifest | n/a | INTERNET + `usesCleartextTraffic="true"` already set ✅ | No change |

MBPSAAS keeps its own strengths (auth, forgot-password, Compose UI). We are only adding the sensor pipeline and widening motion to zones.

---

## 3. Zone model for a poultry farm

ABMDMS zones are rooms. For MBPSAAS, zones map to coop areas — rename, don't reinvent. Proposed default set (configurable in one place):

| Zone code | Label |
|---|---|
| `COOP1` | Coop 1 |
| `COOP2` | Coop 2 |
| `PERIMETER` | Perimeter fence |
| `GATE` | Main gate |

Event types stay ABMDMS's two: `MOTION_DETECTED`, `MOTION_STOPPED`. The buzzer is a *farm-wide actuator* driven by the Arduino whenever **any** zone is active; we still record which zone tripped it.

---

## 4. Database changes (`api/setup.php`)

Migrate `motion_events` from the single-flag shape to a zone-aware shape while staying backward compatible with the existing Android `MotionEvent` fields.

Target `motion_events` columns:

| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | unchanged |
| event_type | VARCHAR(20) | `MOTION_DETECTED` / `MOTION_STOPPED` — NEW |
| zone | VARCHAR(20) NOT NULL DEFAULT 'COOP1' | NEW, indexed |
| source | VARCHAR(50) DEFAULT 'ARDUINO_PIR' | NEW (`ARDUINO_PIR` / `SIMULATOR`) |
| buzzer_triggered | TINYINT(1) DEFAULT 1 | keep — set 1 on DETECTED, 0 on STOPPED |
| note | VARCHAR(255) NULL | keep |
| detected_at | DATETIME NOT NULL | unchanged |
| created_at | TIMESTAMP | unchanged |

- Add `setup.php` `CREATE TABLE` with the new columns, plus `ALTER TABLE ... ADD COLUMN` upgrade lines (commented) for laptops that already have the old table — mirrors how ABMDMS's `database.sql` documents its upgrade path.
- Add `KEY idx_zone (zone)`, `KEY idx_event_type (event_type)`.

## 5. `api/config.php` additions

Following ABMDMS's `config.php`, add allow-lists so the API rejects junk:

```php
const ALLOWED_EVENT_TYPES = ['MOTION_DETECTED', 'MOTION_STOPPED'];
const ALLOWED_ZONES       = ['COOP1', 'COOP2', 'PERIMETER', 'GATE'];
const ZONE_LABELS = [
    'COOP1' => 'Coop 1', 'COOP2' => 'Coop 2',
    'PERIMETER' => 'Perimeter fence', 'GATE' => 'Main gate',
];
const RECORDS_PER_PAGE = 20;
const MAX_RECORDS_PER_PAGE = 100;
date_default_timezone_set('Asia/Manila');
```

## 6. `api/log_motion_event.php` (insert) changes

- Accept POST fields: `event_type`, `zone`, `source`, plus keep `buzzer_triggered` / `note` optional.
- Validate `event_type` against `ALLOWED_EVENT_TYPES` and `zone` against `ALLOWED_ZONES` (default `COOP1`), exactly like ABMDMS's `record_motion.php`.
- Derive `buzzer_triggered` = 1 when `event_type === 'MOTION_DETECTED'`, else 0 (keeps old Android field meaningful).
- Prepared `INSERT` with the new columns. Return `id`, echoed fields.

## 7. `api/get_motion_events.php` (read) changes

Return the richer ABMDMS-style payload so the app can show live per-zone status, while keeping the existing `events` array so nothing currently on screen breaks:

```json
{
  "success": true,
  "status": "MOTION",
  "zones": {
    "COOP1": { "label": "Coop 1", "status": "NO_MOTION", "last_motion": "..." },
    "COOP2": { "label": "Coop 2", "status": "MOTION",    "last_motion": "..." }
  },
  "total_events": 125,
  "today_events": 25,
  "last_motion": "July 24, 2026 12:30 PM",
  "events": [ { "id":1, "event_type":"MOTION_DETECTED", "zone":"COOP1", "zone_label":"Coop 1", "buzzer_triggered":true, "detected_at":"...", "note":null } ],
  "page": 1, "total_pages": 7
}
```

- Per-zone status = newest row per zone (`MOTION_DETECTED` → MOTION else NO_MOTION), looping `ALLOWED_ZONES`.
- Overall `status` = newest row of any zone.
- Add `?page=`/`?limit=` pagination like ABMDMS.

## 8. Arduino sketch — new `arduino/poultry_sensor/poultry_sensor.ino`

Adapt `motion_sensor.ino`:

- Same multi-zone PIR loop (independent per-zone state, warm-up countdown, `STOP_CONFIRM_MS` debounce, per-zone `<ZONE>_MOTION_DETECTED/STOPPED` tokens).
- Rename `ZONE_NAME[]` to `{ "COOP1", "COOP2", "PERIMETER", "GATE" }` on pins 2–5.
- Add a **buzzer** on a digital pin (e.g. pin 8): sound while any zone is active (replaces ABMDMS's LED-only indicator). Keep the onboard LED too.
- Baud 9600 to match the bridge.
- New wiring doc `arduino/POULTRY_WIRING.md` (based on ABMDMS's `PIR_MULTI_ZONE_WIRING.md`) covering the PIRs + buzzer.

## 9. Serial bridge — new `serial/serial_reader.php`

Port ABMDMS's bridge nearly verbatim; only three things change:

- `$API_URL = 'http://localhost/mbpsaas_api/log_motion_event.php';`
- Token regex updated to the new zone codes: `^(COOP1|COOP2|PERIMETER|GATE)_(MOTION_DETECTED|MOTION_STOPPED)$`.
- POST field names matched to `log_motion_event.php` (`event_type`, `zone`, `source`).
- Keep: `mode` COM config, `\\.\COMxx` path fix for ports ≥10, duplicate window, auto-reconnect loop, `Asia/Manila` timezone.
- Add `serial/start_reader.bat` and `serial/list_ports.bat` helpers (ABMDMS parity), plus a short `serial/README.md`.

## 10. Test tool — new `tools/simulate_motion.php`

Port ABMDMS's tool; add a zone dropdown (the 4 codes) alongside the DETECTED/STOPPED buttons, POST to `log_motion_event.php` with `source=SIMULATOR`. Lets us prove the whole app + DB path before touching hardware.

## 11. Android app changes

- **`data/ApiModels.kt`** — extend `MotionEvent` with `event_type`, `zone`, `zone_label`; add `ZoneStatus(label, status, lastMotion)`; extend `MotionEventsResponse` with `status`, `zones: Map<String, ZoneStatus>`, `totalEvents`, `todayEvents`, `page`, `totalPages`. Existing `events`/`buzzerTriggered` stay.
- **`data/ApiService.kt`** — add optional `@Query("page")` to `getMotionEvents`.
- **`ui/DashboardScreen.kt` + `MotionEventComponents.kt`** — add a per-zone status strip (chip/card per zone showing MOTION/NO_MOTION), keep the overall status card and recent list.
- **`ui/ActivityLogScreen.kt`** — show `zone_label` on each row; optional paging.
- **`ui/HomeScreen.kt`** — already polls via `LaunchedEffect`; consume the new fields. (Consider a periodic refresh loop, ~3s, matching the web dashboard.)
- **`data/ApiClient.kt`** — no change; base-URL guidance already documented.

## 12. Docs

- Add `docs/ARDUINO_SETUP.md` (MBPSAAS-flavored version of ABMDMS's `ARDUINO_WEB_INTEGRATION_GUIDE.md` + `ANDROID_APP_INTEGRATION.md`): flashing the sketch, running the bridge, the `mbpsaas_api` junction, finding the LAN IP.
- Update `docs/SETUP.md` "What's where" section to list `arduino/`, `serial/`, `tools/`.

---

## 13. Build & test order (when approved)

1. DB: update `setup.php`, re-run `http://localhost/mbpsaas_api/setup.php`.
2. API: extend `config.php`, `log_motion_event.php`, `get_motion_events.php`. Verify with browser + `tools/simulate_motion.php`.
3. Android: update models/UI, run against simulated data — confirms the full software path with **no hardware**.
4. Hardware: flash `poultry_sensor.ino`, wire PIRs + buzzer, run `serial_reader.php`, confirm real events reach the app.

## 14. Verification checklist

- `simulate_motion.php` DETECTED on COOP2 → app shows COOP2 = MOTION within one poll; STOPPED clears it.
- Invalid zone/event POST is rejected (400) by the API.
- Old `events` consumers still render (backward compatibility).
- Bridge survives unplug/replug (auto-reconnect) and ignores warm-up chatter.
- Buzzer sounds while any zone active, silences when all clear.

## 15. Risks / open questions

- **Final zone list & pin count** — 4 is the assumed default; confirm how many sensors the farm actually has.
- **Buzzer wiring** — active vs passive buzzer changes the sketch (tone vs digitalWrite). Confirm the component.
- **Old rows** — existing `motion_events` rows have no `zone`; they'll default to `COOP1`. Acceptable, or purge on migration?
- **Bridge host** — PHP CLI bridge (chosen, matches ABMDMS). PowerShell alternative exists if PHP CLI serial access misbehaves on your Windows build.

---

### Files this plan will add / change (nothing touched yet)

Add: `arduino/poultry_sensor/poultry_sensor.ino`, `arduino/POULTRY_WIRING.md`, `serial/serial_reader.php`, `serial/start_reader.bat`, `serial/list_ports.bat`, `serial/README.md`, `tools/simulate_motion.php`, `docs/ARDUINO_SETUP.md`.
Change: `api/setup.php`, `api/config.php`, `api/log_motion_event.php`, `api/get_motion_events.php`, `app/.../data/ApiModels.kt`, `data/ApiService.kt`, `ui/DashboardScreen.kt`, `ui/MotionEventComponents.kt`, `ui/ActivityLogScreen.kt`, `ui/HomeScreen.kt`, `docs/SETUP.md`.
