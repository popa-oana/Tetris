let lastEventId = 0;
let isPaused = false;
let lineResetTimer = null;

function api(path, timeoutMs = 1200) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    return fetch(path, {
        cache: 'no-store',
        signal: controller.signal
    }).finally(() => clearTimeout(timer));
}

function setStatus(ok) {
    const status = document.getElementById('status');
    status.textContent = ok ? 'Connected' : 'Connection lost';
    status.classList.toggle('ok', ok);
    status.classList.toggle('lost', !ok);
}

function showControllerScreen() {
    document.getElementById('controllerScreen').classList.add('show');
}



function handleConnectionLost() {
    setStatus(false);
    setReadyState();
}

function bindTapButton(id, cmd, afterTap) {
    const btn = document.getElementById(id);

    btn.addEventListener('pointerdown', function (e) {
        e.preventDefault();
        api('/tap?c=' + encodeURIComponent(cmd)).catch(() => handleConnectionLost());
        if (afterTap) afterTap();
    });
}

function bindRepeatButton(id, cmd, firstDelay, repeatDelay) {
    const btn = document.getElementById(id);
    let timeoutId = null;
    let intervalId = null;

    const clearTimers = () => {
        if (timeoutId) {
            clearTimeout(timeoutId);
            timeoutId = null;
        }
        if (intervalId) {
            clearInterval(intervalId);
            intervalId = null;
        }
    };

    btn.addEventListener('pointerdown', function (e) {
        e.preventDefault();

        api('/tap?c=' + encodeURIComponent(cmd)).catch(() => handleConnectionLost());

        timeoutId = setTimeout(() => {
            intervalId = setInterval(() => {
                api('/tap?c=' + encodeURIComponent(cmd)).catch(() => handleConnectionLost());
            }, repeatDelay);
        }, firstDelay);
    });

    btn.addEventListener('pointerup', clearTimers);
    btn.addEventListener('pointercancel', clearTimers);
    btn.addEventListener('pointerleave', clearTimers);
}

bindTapButton('rotateBtn', 'ROTATE', null);
bindRepeatButton('leftBtn', 'LEFT', 260, 135);
bindRepeatButton('rightBtn', 'RIGHT', 260, 135);
bindRepeatButton('downBtn', 'DOWN', 140, 70);

bindTapButton('pauseBtn', 'PAUSE_TOGGLE', () => {
    isPaused = !isPaused;
    document.getElementById('pauseBtn').textContent = isPaused ? 'Resume' : 'Pause';
});

bindTapButton('restartBtn', 'RESTART', () => {
    isPaused = false;
    document.getElementById('pauseBtn').textContent = 'Pause';
    showControllerScreen();
    setReadyState();
});

document.addEventListener('dblclick', function (e) {
    e.preventDefault();
}, { passive: false });

let lastTouchEnd = 0;
document.addEventListener('touchend', function (e) {
    const now = Date.now();
    if (now - lastTouchEnd <= 300) {
        e.preventDefault();
    }
    lastTouchEnd = now;
}, { passive: false });

setInterval(() => {
    api('/ping')
        .then(resp => setStatus(!!resp && resp.ok))
        .catch(() => handleConnectionLost());
}, 1000);

setStatus(false);
showControllerScreen();
setReadyState();
