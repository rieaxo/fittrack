const existingUser = Session.get();
if (existingUser) {
    window.location.href = existingUser.dashboardPath;
}

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const el = id => document.getElementById(id);

function showTab(name) {
    el("loginTab").classList.toggle("active", name === "login");
    el("registerTab").classList.toggle("active", name === "register");
    el("loginForm").classList.toggle("hidden", name !== "login");
    el("registerForm").classList.toggle("hidden", name !== "register");
}

function finish(user, message) {
    Session.set(user);
    toast(message + ", " + user.name);
    setTimeout(() => {
        window.location.href = user.dashboardPath;
    }, 700);
}

async function loadTrainers() {
    const select = el("regTrainer");
    try {
        const trainers = await api("/api/auth/trainers");
        if (!trainers.length) {
            select.innerHTML = '<option value="">No trainers registered yet</option>';
            return;
        }
        select.innerHTML = '<option value="">Choose your trainer</option>' +
            trainers.map(trainer => `<option value="${esc(trainer.id)}">${esc(trainer.name)}</option>`).join("");
    } catch (error) {
        select.innerHTML = '<option value="">Could not load trainers</option>';
    }
}

function validateRegister(data) {
    if (data.name.length < 2) return "Name must be at least 2 characters";
    if (!emailPattern.test(data.email)) return "Enter a valid email address";
    if (data.password.length < 6) return "Password must be at least 6 characters";
    if (data.role === "CLIENT" && !data.trainerId) return "Please choose your trainer";
    return null;
}

el("loginTab").addEventListener("click", () => showTab("login"));
el("registerTab").addEventListener("click", () => showTab("register"));

el("regRole").addEventListener("change", () => {
    const isClient = el("regRole").value === "CLIENT";
    el("trainerField").classList.toggle("hidden", !isClient);
    if (isClient) {
        loadTrainers();
    }
});

el("loginForm").addEventListener("submit", async event => {
    event.preventDefault();
    const email = el("loginEmail").value.trim();
    const password = el("loginPassword").value;
    const error = el("loginError");
    error.textContent = "";
    if (!emailPattern.test(email)) {
        error.textContent = "Enter a valid email address";
        return;
    }
    if (!password) {
        error.textContent = "Enter your password";
        return;
    }
    const button = el("loginSubmit");
    setBusy(button, true);
    try {
        const user = await api("/api/auth/login", { method: "POST", body: { email, password } });
        finish(user, "Welcome back");
    } catch (failure) {
        error.textContent = failure.message;
        toast(failure.message, "error");
    } finally {
        setBusy(button, false);
    }
});

el("registerForm").addEventListener("submit", async event => {
    event.preventDefault();
    const role = el("regRole").value;
    const data = {
        name: el("regName").value.trim(),
        email: el("regEmail").value.trim(),
        password: el("regPassword").value,
        role,
        trainerId: role === "CLIENT" ? el("regTrainer").value : null
    };
    const error = el("registerError");
    const problem = validateRegister(data);
    error.textContent = problem || "";
    if (problem) {
        return;
    }
    const button = el("registerSubmit");
    setBusy(button, true);
    try {
        const user = await api("/api/auth/register", { method: "POST", body: data });
        finish(user, "Account created");
    } catch (failure) {
        error.textContent = failure.message;
        toast(failure.message, "error");
    } finally {
        setBusy(button, false);
    }
});
