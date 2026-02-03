const API = "http://localhost:8081/logs";
const WS_URL = "ws://localhost:8081/ws/logs";

const totalLogsEl = document.getElementById("totalLogs");
const lastLogTimeEl = document.getElementById("lastLogTime");
const logStreamEl = document.getElementById("logStream");

document.getElementById("sendLogBtn").onclick = sendTestLog;
document.getElementById("panicBtn").onclick = panicMode;

const socket = new WebSocket(WS_URL);
socket.onmessage = (event) => {
  logStreamEl.textContent += event.data + "\n";
};

// Send test log
function sendTestLog() {
  fetch(API, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      service: "auth",
      message: "User test@gmail.com logged in with phone 9876543210"
    })
  }).then(fetchMetrics);
}


async function panicMode() {
  await fetch("http://localhost:8081/logs/panic", { method: "POST" });

  document.getElementById("logs").innerHTML = "";
  document.getElementById("totalLogs").innerText = "0";
  document.getElementById("lastLogTime").innerText = "-";
}



function fetchMetrics() {
  fetch(API + "/metrics")
    .then(res => res.json())
    .then(data => {
      totalLogsEl.textContent = data.totalLogs;
      lastLogTimeEl.textContent = data.lastLogTime ?? "-";
    });
}


fetchMetrics();

