import { useState } from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { initials } from '../utils/format';

export function Logo() {
  return (
    <Link to="/dashboard" className="logo">
      <svg width="26" height="26" viewBox="0 0 32 32" aria-hidden="true">
        <rect width="32" height="32" rx="8" fill="currentColor" />
        <path d="M9 10h14M9 16h10M9 22h6" stroke="white" strokeWidth="3" strokeLinecap="round" />
      </svg>
      <span>FlowForge</span>
    </Link>
  );
}

const NAV = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/projects', label: 'Projects' },
  { to: '/profile', label: 'Profile' },
];

/** App shell for signed-in pages: top navbar, sidebar, and the routed page. */
export default function Layout() {
  const { user, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="shell">
      <header className="navbar">
        <button
          type="button"
          className="icon-btn menu-toggle"
          aria-label="Toggle navigation"
          onClick={() => setMenuOpen((o) => !o)}
        >
          ☰
        </button>
        <Logo />
        <div className="navbar-right">
          <Link to="/profile" className="avatar" title={user?.name}>
            {initials(user?.name)}
          </Link>
          <button type="button" className="btn btn-ghost btn-small" onClick={logout}>
            Log out
          </button>
        </div>
      </header>

      <aside className={`sidebar${menuOpen ? ' open' : ''}`}>
        <nav>
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
              onClick={() => setMenuOpen(false)}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
