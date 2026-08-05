# MBPSAAS — project notes for Claude

Mobile-Based Poultry Security and Alert System. Android (Kotlin/Compose) app +
PHP/MySQL API served by XAMPP as `http://localhost/mbpsaas_api/`. Reference
project for hardware work is **ABMDMS** (`C:\xampp\htdocs\ABMDMS`).

## Active plan: Arduino integration (multi-zone)

We are adding the Arduino → serial bridge → PHP → MySQL pipeline, modeled on
ABMDMS, and upgrading motion tracking to **multiple zones**.

**Full plan:** [`docs/ARDUINO_INTEGRATION_PLAN.md`](docs/ARDUINO_INTEGRATION_PLAN.md) — read this before starting.

To resume, I can just say: **"let's do the Arduino integration plan"** and Claude
should follow that doc, building in the order in §13 (DB → API → Android against a
simulator → hardware last).

### Decisions still open before implementing (see plan §15)
- Zone list & sensor count — default assumed: 4 zones (Coop 1, Coop 2, Perimeter, Gate).
- Buzzer type — active vs passive (changes the sketch).
- Old `motion_events` rows with no zone — default to `COOP1`, or purge on migration?

### Status
- [x] Plan written (`docs/ARDUINO_INTEGRATION_PLAN.md`)
- [ ] DB schema (`api/setup.php`)
- [ ] API (`api/config.php`, `log_motion_event.php`, `get_motion_events.php`)
- [ ] Android models + UI (zones)
- [ ] Arduino sketch + serial bridge + test tool
- [ ] Docs (`docs/ARDUINO_SETUP.md`)

## Where things are
- Android app: `app/src/main/java/.../` (data layer in `data/`, screens in `ui/`)
- PHP API source: `api/` (junctioned into XAMPP as `mbpsaas_api` — see `docs/SETUP.md`)
- Setup guide: `docs/SETUP.md`
