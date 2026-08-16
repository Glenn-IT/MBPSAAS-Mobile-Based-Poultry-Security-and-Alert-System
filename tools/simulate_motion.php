<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MBPSAAS — Motion Event Simulator</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #0f172a; color: #f8fafc; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: #1e293b; padding: 24px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
        h1 { font-size: 1.5rem; color: #38bdf8; margin-top: 0; }
        label { display: block; margin-top: 12px; font-weight: 600; color: #94a3b8; }
        select, button { width: 100%; padding: 12px; margin-top: 8px; border-radius: 8px; border: none; font-size: 1rem; }
        select { background: #334155; color: #fff; }
        .btn-detected { background: #ef4444; color: white; font-weight: bold; cursor: pointer; margin-top: 16px; }
        .btn-detected:hover { background: #dc2626; }
        .btn-stopped { background: #22c55e; color: white; font-weight: bold; cursor: pointer; margin-top: 8px; }
        .btn-stopped:hover { background: #16a34a; }
        .btn-check { background: #0284c7; color: white; font-weight: bold; cursor: pointer; }
        #log { margin-top: 20px; background: #090d16; padding: 12px; border-radius: 6px; font-family: monospace; font-size: 0.9rem; min-height: 80px; color: #a7f3d0; word-break: break-all; }
    </style>
</head>
<body>
    <div class="container">
        <h1>MBPSAAS Motion Simulator</h1>
        <p style="color:#94a3b8; font-size:0.9rem;">Simulate PIR Motion events to test the PHP API and Android App without hardware.</p>

        <button class="btn-check" onclick="checkApi()">Check API Connection</button>

        <label for="zoneSelect">Select Farm Zone:</label>
        <select id="zoneSelect">
            <option value="ROOMA">Coop Zone A (ROOMA)</option>
            <option value="ROOMB">Coop Zone B (ROOMB)</option>
            <option value="ROOMC">Coop Zone C (ROOMC)</option>
            <option value="ROOMD">Perimeter Gate (ROOMD)</option>
        </select>

        <button class="btn-detected" onclick="sendEvent('MOTION_DETECTED')">Simulate MOTION_DETECTED</button>
        <button class="btn-stopped" onclick="sendEvent('MOTION_STOPPED')">Simulate MOTION_STOPPED</button>

        <label>API Output Log:</label>
        <div id="log">Click a button above to run test...</div>
    </div>

    <script>
        const API_URL = '../api/log_motion_event.php';
        const GET_URL = '../api/get_motion_events.php';

        function log(msg) {
            document.getElementById('log').innerText = msg;
        }

        async function checkApi() {
            log('Connecting to API...');
            try {
                const res = await fetch(GET_URL);
                const data = await res.json();
                log('API Connection OK!\n' + JSON.stringify(data, null, 2));
            } catch (e) {
                log('API Connection ERROR: ' + e.message);
            }
        }

        async function sendEvent(type) {
            const zone = document.getElementById('zoneSelect').value;
            log(`Sending ${type} for ${zone}...`);
            try {
                const formData = new FormData();
                formData.append('event_type', type);
                formData.append('zone', zone);
                formData.append('source', 'WEB_SIMULATOR');

                const res = await fetch(API_URL, { method: 'POST', body: formData });
                const data = await res.json();
                log('Response: ' + JSON.stringify(data, null, 2));
            } catch (e) {
                log('ERROR: ' + e.message);
            }
        }
    </script>
</body>
</html>
