/*
  ============================================================
  MBPSAAS - Mobile Based Poultry Security and Alert System
  File   : poultry_sensor.ino
  Board  : Arduino Uno (COM5)
  Sensors: 3x HC-SR501 PIR Motion Sensors (Pins 2, 3, 4)
  Buzzer : Active Alarm Buzzer (Pin 8)
  GSM    : SIM800L V2.2 GSM Module (Pins 10 TX, 11 RX, 12 RST)
  ============================================================
*/

#include <SoftwareSerial.h>

// --- PIN ASSIGNMENTS & ZONE CONFIGURATION ---
const int NUM_ZONES = 3;
const int PIR_PIN[NUM_ZONES]   = { 2,       3,       4       }; // Digital pins
const char* ZONE_NAME[NUM_ZONES] = { "ROOMC", "ROOMA", "ROOMB" }; // Tokens sent over serial
const char* ZONE_TEXT[NUM_ZONES] = { "Coop Zone C", "Coop Zone A", "Coop Zone B" }; // Text inside SMS

const int BUZZER_PIN = 8;  // Active alarm buzzer pin
const int LED_PIN    = 13; // Built-in status LED

// SIM800L GSM Module Pins
SoftwareSerial sim800(10, 11); // RX=10 (connect SIM800 TX), TX=11 (connect SIM800 RX)
const int GSM_RST_PIN = 12;

const char SMS_RECIPIENT[] = "+639169751409"; // Change to target phone number

// State Tracking Variables
bool lastSensorState[NUM_ZONES] = { false, false, false };
unsigned long lastStateChangeMs[NUM_ZONES] = { 0, 0, 0 };
unsigned long lastSmsSentMs[NUM_ZONES] = { 0, 0, 0 };

const unsigned long DEBOUNCE_MS      = 1000;  // Debounce time for PIR readings
const unsigned long SMS_COOLDOWN_MS  = 60000; // 60s per-zone SMS alert cooldown
const unsigned long WARMUP_SECONDS   = 30;    // PIR warm-up countdown

void setup() {
  Serial.begin(9600);
  sim800.begin(9600);

  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  pinMode(GSM_RST_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(LED_PIN, LOW);
  digitalWrite(GSM_RST_PIN, HIGH);

  for (int i = 0; i < NUM_ZONES; i++) {
    pinMode(PIR_PIN[i], INPUT);
  }

  Serial.println(F("[SYSTEM] MBPSAAS Poultry Security Initializing..."));
  Serial.println(F("[SYSTEM] Warming up PIR Motion Sensors (30s)..."));

  // 30-Second Warmup Loop
  for (int i = WARMUP_SECONDS; i > 0; i--) {
    digitalWrite(LED_PIN, !digitalRead(LED_PIN)); // Blink LED
    delay(1000);
  }
  digitalWrite(LED_PIN, LOW);
  Serial.println(F("[SYSTEM] Warm-up complete. System Ready."));

  initGsmModule();
}

void loop() {
  unsigned long now = millis();
  bool anyZoneActive = false;

  for (int i = 0; i < NUM_ZONES; i++) {
    bool currentReading = (digitalRead(PIR_PIN[i]) == HIGH);

    if (currentReading != lastSensorState[i]) {
      if (now - lastStateChangeMs[i] > DEBOUNCE_MS) {
        lastSensorState[i] = currentReading;
        lastStateChangeMs[i] = now;

        if (currentReading) {
          // Motion Started
          Serial.print(ZONE_NAME[i]);
          Serial.println(F("_MOTION_DETECTED"));

          // Send SMS if cooldown expired
          if (now - lastSmsSentMs[i] > SMS_COOLDOWN_MS || lastSmsSentMs[i] == 0) {
            sendGsmSms(ZONE_TEXT[i]);
            lastSmsSentMs[i] = now;
          }
        } else {
          // Motion Stopped
          Serial.print(ZONE_NAME[i]);
          Serial.println(F("_MOTION_STOPPED"));
        }
      }
    }

    if (lastSensorState[i]) {
      anyZoneActive = true;
    }
  }

  // Actuate local buzzer and LED if any zone is active
  digitalWrite(BUZZER_PIN, anyZoneActive ? HIGH : LOW);
  digitalWrite(LED_PIN, anyZoneActive ? HIGH : LOW);

  delay(50);
}

void initGsmModule() {
  Serial.println(F("[GSM] Initializing SIM800L Module..."));
  sim800.println("AT");
  delay(500);
  sim800.println("AT+CMGF=1"); // Set SMS text mode
  delay(500);
  Serial.println(F("[GSM] SIM800L Ready."));
}

void sendGsmSms(const char* zoneText) {
  Serial.print(F("[GSM] Sending SMS Alert for "));
  Serial.println(zoneText);

  sim800.print("AT+CMGS=\"");
  sim800.print(SMS_RECIPIENT);
  sim800.println("\"");
  delay(1000);

  sim800.print("[MBPSAAS ALERT] Intrusion detected in ");
  sim800.print(zoneText);
  sim800.print(" at ");
  sim800.println(millis() / 1000);
  delay(100);

  sim800.write(26); // Ctrl+Z to send
  delay(3000);
  Serial.println(F("[GSM] SMS Dispatch command sent."));
}
