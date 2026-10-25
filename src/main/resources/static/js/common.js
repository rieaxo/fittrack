const Session = {
    get() {
        try {
            return JSON.parse(sessionStorage.getItem("fittrackUser"));
        } catch (error) {
            return null;
        }
    },
    set(user) {
        sessionStorage.setItem("fittrackUser", JSON.stringify(user));
    },
    clear() {
        sessionStorage.removeItem("fittrackUser");
    },
    require(role) {
        const user = Session.get();
        if (!user || user.role !== role) {
            window.location.href = "index.html";
            return null;
        }
        return user;
    },
    logout() {
        Session.clear();
        window.location.href = "index.html";
    }
};

async function api(path, options = {}) {
    const config = { method: options.method || "GET", headers: {} };
    if (options.body !== undefined) {
        config.headers["Content-Type"] = "application/json";
        config.body = JSON.stringify(options.body);
    }
    let response;
    try {
        response = await fetch(path, config);
    } catch (error) {
        throw new Error("Cannot reach the server. Is it running?");
    }
    if (response.status === 204) {
        return null;
    }
    let data = null;
    try {
        data = await response.json();
    } catch (error) {
        data = null;
    }
    if (!response.ok) {
        throw new Error((data && data.message) || "Something went wrong");
    }
    return data;
}

function toast(message, type = "success") {
    let box = document.getElementById("toasts");
    if (!box) {
        box = document.createElement("div");
        box.id = "toasts";
        document.body.appendChild(box);
    }
    const item = document.createElement("div");
    item.className = "toast " + type;
    item.textContent = message;
    box.appendChild(item);
    setTimeout(() => item.classList.add("hide"), 3000);
    setTimeout(() => item.remove(), 3400);
}

function setBusy(button, busy) {
    button.disabled = busy;
    button.classList.toggle("loading", busy);
}

function esc(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function loader() {
    return '<div class="loader"></div>';
}

function emptyBox(message) {
    return `<div class="empty">${esc(message)}</div>`;
}

function setupHeader(user) {
    document.getElementById("userName").textContent = user.name;
    document.getElementById("logoutBtn").addEventListener("click", Session.logout);
}

function setupNav(onShow) {
    const buttons = document.querySelectorAll(".nav-btn");
    function show(name) {
        buttons.forEach(button => button.classList.toggle("active", button.dataset.view === name));
        document.querySelectorAll(".view").forEach(view => view.classList.toggle("hidden", view.id !== "view-" + name));
        if (onShow) {
            onShow(name);
        }
    }
    buttons.forEach(button => button.addEventListener("click", () => show(button.dataset.view)));
    return show;
}

function renderWeightChart(canvas, logs, previous) {
    if (previous) {
        previous.destroy();
    }
    return new Chart(canvas, {
        type: "line",
        data: {
            labels: logs.map(log => log.date),
            datasets: [{
                label: "Weight (kg)",
                data: logs.map(log => log.weight),
                borderColor: "#4f46e5",
                backgroundColor: "rgba(79, 70, 229, 0.12)",
                fill: true,
                tension: 0.3,
                pointRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { title: { display: true, text: "kg" } } }
        }
    });
}

function logRowsHtml(logs) {
    return [...logs].reverse().map(log => `
        <tr>
            <td>${esc(log.date)}</td>
            <td>${log.completionPercent}%</td>
            <td>${log.weight} kg</td>
            <td>${esc(log.notes) || "-"}</td>
        </tr>`).join("");
}
