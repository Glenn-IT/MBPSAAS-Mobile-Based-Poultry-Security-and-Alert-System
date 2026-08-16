# MBPSAAS — project notes

Mobile-Based Poultry Security and Alert System. Android (Kotlin/Jetpack Compose) app + PHP/MySQL API served by XAMPP as `http://localhost/mbpsaas_api/`.

## Active plan: Arduino & Android integration (multi-zone)

We are connecting the **Arduino hardware** directly to the **ABMDMS MySQL Database (`motion_monitoring`)** and the **Android Compose app**.

- **Full Plan:** [`docs/ARDUINO_INTEGRATION_PLAN.md`](docs/ARDUINO_INTEGRATION_PLAN.md)
- **Integration Checklist:** [`docs/ARDUINO_INTEGRATION_CHECKLIST.md`](docs/ARDUINO_INTEGRATION_CHECKLIST.md)

### Database Decision:
- Using the **ABMDMS MySQL Database (`motion_monitoring`)** with `motion_logs` and `sms_logs` tables.

### Status
- [x] Integration Plan updated ([`docs/ARDUINO_INTEGRATION_PLAN.md`](docs/ARDUINO_INTEGRATION_PLAN.md))
- [x] Step-by-Step Checklist created ([`docs/ARDUINO_INTEGRATION_CHECKLIST.md`](docs/ARDUINO_INTEGRATION_CHECKLIST.md))
- [ ] PHP API configuration to connect `mbpsaas_api` to `motion_monitoring`
- [ ] Android Models & UI Updates (`data/ApiModels.kt`, `DashboardScreen.kt`)
- [ ] Arduino Firmware (`poultry_sensor.ino`) & Serial Reader Bridge execution

## File Structure
- Android app: `app/src/main/java/.../` (data layer in `data/`, screens in `ui/`)
- PHP API source: `api/` (junctioned into XAMPP as `mbpsaas_api`)
- Integration Checklist: `docs/ARDUINO_INTEGRATION_CHECKLIST.md`
- Integration Plan: `docs/ARDUINO_INTEGRATION_PLAN.md`
- Setup guide: `docs/SETUP.md`
