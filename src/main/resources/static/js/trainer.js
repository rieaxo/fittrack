const user = Session.require("TRAINER");
const el = id => document.getElementById(id);
const state = { clients: [], plans: [], adherence: {}, editingId: null, chart: null };

const showView = setupNav();

function clientName(id) {
    const client = state.clients.find(item => item.id === id);
    return client ? client.name : "Unknown client";
}

function fillClientSelects(placeholder) {
    const options = state.clients.map(client => `<option value="${esc(client.id)}">${esc(client.name)}</option>`).join("");
    el("planClient").innerHTML = `<option value="">${placeholder}</option>` + options;
    el("progressClient").innerHTML = '<option value="">Select a client</option>' + options;
}

function renderClients() {
    const box = el("clientList");
    if (!state.clients.length) {
        box.innerHTML = emptyBox("No clients yet. Clients pick their trainer when they register.");
        return;
    }
    box.innerHTML = state.clients.map(client => {
        const percent = state.adherence[client.id] || 0;
        return `
            <div class="item-card">
                <div class="avatar">${esc(client.name.charAt(0).toUpperCase())}</div>
                <h3>${esc(client.name)}</h3>
                <p class="muted">${esc(client.email)}</p>
                <div class="meta"><span>Weekly adherence</span><strong>${percent}%</strong></div>
                <div class="bar"><div class="bar-fill" style="width:${percent}%"></div></div>
                <div class="actions"><button class="btn small primary" data-progress="${esc(client.id)}">View progress</button></div>
            </div>`;
    }).join("");
}

function renderPlans() {
    const box = el("planList");
    if (!state.plans.length) {
        box.innerHTML = emptyBox("No plans yet. Create your first plan.");
        return;
    }
    box.innerHTML = state.plans.map(plan => `
        <div class="item-card">
            <h3>${esc(plan.name)}</h3>
            <p class="muted">For ${esc(clientName(plan.clientId))}</p>
            <div>
                <span class="tag">${plan.durationWeeks} weeks</span>
                <span class="tag">${plan.exercises.length} exercises</span>
                <span class="tag">${plan.meals.length} meals</span>
            </div>
            <div class="actions">
                <button class="btn small primary" data-edit="${esc(plan.id)}">Edit</button>
                <button class="btn small danger" data-delete="${esc(plan.id)}">Delete</button>
            </div>
        </div>`).join("");
}

async function loadAll() {
    el("clientList").innerHTML = loader();
    el("planList").innerHTML = loader();
    try {
        const [clients, plans, adherence] = await Promise.all([
            api(`/api/trainer/${user.id}/clients`),
            api(`/api/plans?trainerId=${user.id}`),
            api(`/api/trainer/${user.id}/adherence`)
        ]);
        state.clients = clients;
        state.plans = plans;
        state.adherence = adherence;
        fillClientSelects("Select a client");
        renderClients();
        renderPlans();
    } catch (error) {
        el("clientList").innerHTML = emptyBox(error.message);
        el("planList").innerHTML = emptyBox(error.message);
        toast(error.message, "error");
    }
}

function addExerciseRow(data = {}) {
    const row = document.createElement("div");
    row.className = "item-row exercise-row";
    row.innerHTML = `
        <input class="ex-name" type="text" placeholder="Exercise name" value="${esc(data.name)}">
        <input class="ex-sets" type="number" min="1" placeholder="Sets" value="${data.sets || ""}">
        <input class="ex-reps" type="number" min="1" placeholder="Reps" value="${data.reps || ""}">
        <button class="icon-btn" type="button" title="Remove">✕</button>`;
    row.querySelector("button").addEventListener("click", () => row.remove());
    el("exerciseRows").appendChild(row);
}

function addMealRow(data = {}) {
    const row = document.createElement("div");
    row.className = "item-row meal-row";
    row.innerHTML = `
        <input class="meal-name" type="text" placeholder="Meal name" value="${esc(data.name)}">
        <input class="meal-cal" type="number" min="1" placeholder="Calories" value="${data.calories || ""}">
        <button class="icon-btn" type="button" title="Remove">✕</button>`;
    row.querySelector("button").addEventListener("click", () => row.remove());
    el("mealRows").appendChild(row);
}

function resetForm() {
    state.editingId = null;
    el("planForm").reset();
    el("planDuration").value = 4;
    el("exerciseRows").innerHTML = "";
    el("mealRows").innerHTML = "";
    el("planError").textContent = "";
    el("createTitle").textContent = "Create Plan";
    el("planSubmit").textContent = "Save plan";
    el("planCancel").classList.add("hidden");
}

function startEdit(plan) {
    resetForm();
    state.editingId = plan.id;
    el("planName").value = plan.name;
    el("planDuration").value = plan.durationWeeks;
    el("planClient").value = plan.clientId;
    plan.exercises.forEach(addExerciseRow);
    plan.meals.forEach(addMealRow);
    el("createTitle").textContent = "Edit Plan";
    el("planSubmit").textContent = "Update plan";
    el("planCancel").classList.remove("hidden");
    showView("create");
}

function collectPlan() {
    return {
        name: el("planName").value.trim(),
        durationWeeks: Number(el("planDuration").value),
        trainerId: user.id,
        clientId: el("planClient").value,
        exercises: [...el("exerciseRows").children].map(row => ({
            name: row.querySelector(".ex-name").value.trim(),
            sets: Number(row.querySelector(".ex-sets").value),
            reps: Number(row.querySelector(".ex-reps").value)
        })),
        meals: [...el("mealRows").children].map(row => ({
            name: row.querySelector(".meal-name").value.trim(),
            calories: Number(row.querySelector(".meal-cal").value)
        }))
    };
}

function validatePlan(plan) {
    if (plan.name.length < 2) return "Plan name must be at least 2 characters";
    if (!Number.isInteger(plan.durationWeeks) || plan.durationWeeks < 1 || plan.durationWeeks > 52) return "Duration must be between 1 and 52 weeks";
    if (!plan.clientId) return "Choose a client for this plan";
    if (!plan.exercises.length && !plan.meals.length) return "Add at least one exercise or meal";
    if (plan.exercises.some(item => !item.name || !Number.isInteger(item.sets) || item.sets < 1 || !Number.isInteger(item.reps) || item.reps < 1)) return "Each exercise needs a name, sets and reps";
    if (plan.meals.some(item => !item.name || !Number.isInteger(item.calories) || item.calories < 1)) return "Each meal needs a name and calories";
    return null;
}

async function loadProgress(clientId) {
    const body = el("progressBody");
    if (!clientId) {
        body.classList.add("hidden");
        return;
    }
    body.classList.remove("hidden");
    el("logTable").innerHTML = "";
    el("logEmpty").innerHTML = loader();
    try {
        const [logs, adherence] = await Promise.all([
            api(`/api/clients/${clientId}/logs`),
            api(`/api/clients/${clientId}/adherence`)
        ]);
        const percent = adherence.adherence;
        el("adherenceFill").style.width = percent + "%";
        el("adherenceText").textContent = `${percent}% adherence over the last ${adherence.windowDays} days (${adherence.daysLogged} days logged)`;
        el("logTable").innerHTML = logRowsHtml(logs);
        el("logEmpty").innerHTML = logs.length ? "" : emptyBox("This client has not logged anything yet.");
        el("chartBox").classList.toggle("hidden", !logs.length);
        el("chartEmpty").innerHTML = logs.length ? "" : emptyBox("No weight data yet.");
        if (logs.length) {
            state.chart = renderWeightChart(el("weightChart"), logs, state.chart);
        }
    } catch (error) {
        el("logEmpty").innerHTML = emptyBox(error.message);
        toast(error.message, "error");
    }
}

function openProgress(clientId) {
    showView("progress");
    el("progressClient").value = clientId;
    loadProgress(clientId);
}

function init() {
    setupHeader(user);

    el("clientList").addEventListener("click", event => {
        const button = event.target.closest("[data-progress]");
        if (button) openProgress(button.dataset.progress);
    });

    el("planList").addEventListener("click", async event => {
        const editButton = event.target.closest("[data-edit]");
        const deleteButton = event.target.closest("[data-delete]");
        if (editButton) {
            const plan = state.plans.find(item => item.id === editButton.dataset.edit);
            if (plan) startEdit(plan);
        }
        if (deleteButton) {
            if (!confirm("Delete this plan?")) return;
            setBusy(deleteButton, true);
            try {
                await api(`/api/plans/${deleteButton.dataset.delete}`, { method: "DELETE" });
                toast("Plan deleted");
                await loadAll();
            } catch (error) {
                toast(error.message, "error");
                setBusy(deleteButton, false);
            }
        }
    });

    el("addExercise").addEventListener("click", () => addExerciseRow());
    el("addMeal").addEventListener("click", () => addMealRow());

    el("planCancel").addEventListener("click", () => {
        resetForm();
        showView("plans");
    });

    el("planForm").addEventListener("submit", async event => {
        event.preventDefault();
        const plan = collectPlan();
        const problem = validatePlan(plan);
        el("planError").textContent = problem || "";
        if (problem) return;
        const button = el("planSubmit");
        setBusy(button, true);
        try {
            if (state.editingId) {
                await api(`/api/plans/${state.editingId}`, { method: "PUT", body: plan });
                toast("Plan updated");
            } else {
                await api("/api/plans", { method: "POST", body: plan });
                toast("Plan created");
            }
            resetForm();
            await loadAll();
            showView("plans");
        } catch (error) {
            el("planError").textContent = error.message;
            toast(error.message, "error");
        } finally {
            setBusy(button, false);
        }
    });

    el("progressClient").addEventListener("change", () => loadProgress(el("progressClient").value));

    loadAll();
}

if (user) {
    init();
}
