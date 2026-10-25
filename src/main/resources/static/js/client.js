const user = Session.require("CLIENT");
const el = id => document.getElementById(id);
const state = { plan: null, logs: [], adherence: null, chart: null };
const todayKey = new Date().toLocaleDateString("en-CA");

const showView = setupNav();

function checkItem(kind, name, detail) {
    return `
        <label class="check">
            <input type="checkbox" data-kind="${kind}" value="${esc(name)}">
            <span>${esc(name)}</span>
            <small class="muted">${esc(detail)}</small>
        </label>`;
}

function checkedNames(kind) {
    return [...document.querySelectorAll(`input[data-kind="${kind}"]:checked`)].map(box => box.value);
}

function updateSummary() {
    const boxes = [...document.querySelectorAll("#todayContent input[type=checkbox]")];
    boxes.forEach(box => box.closest(".check").classList.toggle("done", box.checked));
    const done = boxes.filter(box => box.checked).length;
    el("logSummary").textContent = `You have completed ${done} of ${boxes.length} items today.`;
}

function renderToday() {
    const box = el("todayContent");
    const plan = state.plan;
    if (!plan) {
        box.innerHTML = emptyBox("Your trainer has not assigned a plan yet. Check back soon.");
        el("logForm").classList.add("hidden");
        el("logEmpty").innerHTML = emptyBox("You can log progress once a plan is assigned.");
        return;
    }
    el("logForm").classList.remove("hidden");
    el("logEmpty").innerHTML = "";
    const dateLabel = new Date().toLocaleDateString(undefined, { weekday: "long", day: "numeric", month: "long" });
    const exercises = plan.exercises.map(item => checkItem("exercise", item.name, `${item.sets} sets × ${item.reps} reps`)).join("");
    const meals = plan.meals.map(item => checkItem("meal", item.name, `${item.calories} kcal`)).join("");
    box.innerHTML = `
        <div class="panel">
            <h2>${esc(plan.name)}</h2>
            <p class="muted">${plan.durationWeeks} week plan · ${esc(dateLabel)}</p>
        </div>
        <div class="panel">
            <h3>Exercises</h3>
            <div class="checklist">${exercises || '<p class="muted">No exercises in this plan.</p>'}</div>
        </div>
        <div class="panel">
            <h3>Meals</h3>
            <div class="checklist">${meals || '<p class="muted">No meals in this plan.</p>'}</div>
        </div>
        <button class="btn primary" type="button" id="toLog">Continue to log progress</button>`;
    el("toLog").addEventListener("click", () => showView("log"));
    prefillToday();
    updateSummary();
}

function prefillToday() {
    const todayLog = state.logs.find(log => log.date === todayKey);
    if (!todayLog) return;
    document.querySelectorAll("#todayContent input[type=checkbox]").forEach(box => {
        const source = box.dataset.kind === "exercise" ? todayLog.completedExercises : todayLog.completedMeals;
        box.checked = source.includes(box.value);
    });
    el("logWeight").value = todayLog.weight;
    el("logNotes").value = todayLog.notes;
}

function renderHistory() {
    const logs = state.logs;
    const percent = state.adherence ? state.adherence.adherence : 0;
    el("ring").style.setProperty("--p", percent);
    el("ringValue").textContent = percent + "%";
    el("ringNote").textContent = state.adherence ? `${state.adherence.daysLogged} of ${state.adherence.windowDays} days logged` : "";
    el("logTable").innerHTML = logRowsHtml(logs);
    el("historyEmpty").innerHTML = logs.length ? "" : emptyBox("No logs yet. Submit your first progress log.");
    el("chartBox").classList.toggle("hidden", !logs.length);
    el("chartEmpty").innerHTML = logs.length ? "" : emptyBox("No weight data yet.");
    if (logs.length) {
        state.chart = renderWeightChart(el("weightChart"), logs, state.chart);
    }
}

async function loadAll() {
    el("todayContent").innerHTML = loader();
    try {
        const [plan, logs, adherence] = await Promise.all([
            api(`/api/clients/${user.id}/plan`),
            api(`/api/clients/${user.id}/logs`),
            api(`/api/clients/${user.id}/adherence`)
        ]);
        state.plan = plan;
        state.logs = logs;
        state.adherence = adherence;
        renderToday();
        renderHistory();
    } catch (error) {
        el("todayContent").innerHTML = emptyBox(error.message);
        toast(error.message, "error");
    }
}

function init() {
    setupHeader(user);

    el("todayContent").addEventListener("change", updateSummary);

    el("logForm").addEventListener("submit", async event => {
        event.preventDefault();
        const error = el("logError");
        const weightText = el("logWeight").value;
        const weight = Number(weightText);
        const notes = el("logNotes").value.trim();
        if (!weightText || weight < 20 || weight > 500) {
            error.textContent = "Enter a valid weight between 20 and 500 kg";
            return;
        }
        if (notes.length > 500) {
            error.textContent = "Notes can be at most 500 characters";
            return;
        }
        error.textContent = "";
        const button = el("logSubmit");
        setBusy(button, true);
        try {
            await api(`/api/clients/${user.id}/logs`, {
                method: "POST",
                body: {
                    completedExercises: checkedNames("exercise"),
                    completedMeals: checkedNames("meal"),
                    weight,
                    notes
                }
            });
            toast("Progress saved");
            await loadAll();
            showView("history");
        } catch (failure) {
            error.textContent = failure.message;
            toast(failure.message, "error");
        } finally {
            setBusy(button, false);
        }
    });

    loadAll();
}

if (user) {
    init();
}
