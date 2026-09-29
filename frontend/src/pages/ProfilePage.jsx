import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { formatDate, initials } from '../utils/format';

export default function ProfilePage() {
  const { user, logout } = useAuth();
  return (
    <div className="page narrow">
      <h1>Profile</h1>
      <div className="card profile-card">
        <span className="avatar avatar-large">{initials(user.name)}</span>
        <div>
          <h2>{user.name}</h2>
          <p className="muted">{user.email}</p>
          {user.createdAt && <p className="muted small">Member since {formatDate(user.createdAt)}</p>}
        </div>
      </div>
      <div className="profile-actions">
        <Link to="/projects" className="btn btn-ghost">
          Your projects
        </Link>
        <button type="button" className="btn btn-danger-ghost" onClick={logout}>
          Log out
        </button>
      </div>
    </div>
  );
}

export function NotFoundPage() {
  return (
    <div className="page narrow">
      <h1>Page not found</h1>
      <p className="muted">That page doesn't exist.</p>
      <Link to="/dashboard" className="btn btn-primary">
        Go to dashboard
      </Link>
    </div>
  );
}
