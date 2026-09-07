// Builds the top navbar dynamically based on the logged-in user's role
function renderNavbar(activePage) {
  const user = getCurrentUser();
  if (!user) return;

  const links = {
    OWNER: [
      { href: "owner-dashboard.html", label: "Dashboard" },
      { href: "employees.html", label: "Employees" },
      { href: "managers.html", label: "Managers" },
      { href: "tasks.html", label: "Tasks" },
      { href: "attendance.html", label: "Attendance" },
      { href: "leave.html", label: "Leave" },
    ],
    MANAGER: [
      { href: "manager-dashboard.html", label: "Dashboard" },
      { href: "employees.html", label: "My Team" },
      { href: "tasks.html", label: "Tasks" },
      { href: "attendance.html", label: "Attendance" },
      { href: "leave.html", label: "Leave" },
    ],
    EMPLOYEE: [
      { href: "employee-dashboard.html", label: "Dashboard" },
      { href: "tasks.html", label: "My Tasks" },
      { href: "attendance.html", label: "Attendance" },
      { href: "leave.html", label: "Leave" },
      { href: "profile.html", label: "Profile" },
    ],
  };

  const navLinks = (links[user.role] || [])
    .map(
      (l) =>
        `<a class="nav-link ${activePage === l.href ? "active" : ""}" href="${l.href}">${l.label}</a>`
    )
    .join("");

  const navbarHtml = `
    <nav class="navbar navbar-expand-lg nf-navbar mb-4">
      <div class="container">
        <a class="navbar-brand" href="${dashboardUrlForRole(user.role)}">NexaFlow</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#nfNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="nfNav">
          <div class="navbar-nav me-auto">${navLinks}</div>
          <div class="d-flex align-items-center">
            <span class="text-light me-3">${user.name} <span class="badge bg-light text-dark badge-role">${user.role}</span></span>
            <button class="btn btn-sm btn-outline-light" onclick="logout()">Logout</button>
          </div>
        </div>
      </div>
    </nav>
  `;

  document.getElementById("navbar-container").innerHTML = navbarHtml;
}
