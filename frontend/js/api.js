// Base URL of the Spring Boot backend
const API_BASE = "https://nexaflow-backend-8dcj.onrender.com";

// Central fetch wrapper: attaches JWT token automatically and handles errors consistently
async function apiRequest(path, method = "GET", body = null) {
  const token = localStorage.getItem("nf_token");

  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = "Bearer " + token;

  const options = { method, headers };
  if (body) options.body = JSON.stringify(body);

  const response = await fetch(API_BASE + path, options);

  if (response.status === 401) {
    // Token missing/expired -> force re-login
    localStorage.removeItem("nf_token");
    localStorage.removeItem("nf_user");
    window.location.href = "index.html";
    throw new Error("Session expired. Please log in again.");
  }

  let data = null;
  const text = await response.text();
  if (text) {
    try { data = JSON.parse(text); } catch (e) { data = text; }
  }

  if (!response.ok) {
    const message = (data && data.message) ? data.message : "Something went wrong";
    throw new Error(message);
  }

  return data;
}

function getCurrentUser() {
  const raw = localStorage.getItem("nf_user");
  return raw ? JSON.parse(raw) : null;
}

function logout() {
  localStorage.removeItem("nf_token");
  localStorage.removeItem("nf_user");
  window.location.href = "index.html";
}

// Guard used at the top of every protected page.
// Redirects to login if not authenticated, and optionally restricts by role.
function requireAuth(allowedRoles = null) {
  const token = localStorage.getItem("nf_token");
  const user = getCurrentUser();

  if (!token || !user) {
    window.location.href = "index.html";
    return null;
  }
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    alert("You are not authorized to view this page.");
    window.location.href = dashboardUrlForRole(user.role);
    return null;
  }
  return user;
}

function dashboardUrlForRole(role) {
  if (role === "OWNER") return "owner-dashboard.html";
  if (role === "MANAGER") return "manager-dashboard.html";
  return "employee-dashboard.html";
}

function showError(message) {
  alert(message);
}
