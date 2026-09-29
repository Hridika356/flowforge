import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Logo } from '../components/Layout';
import { ErrorMessage, Field } from '../components/ui';
import { useAuth } from '../context/AuthContext';

function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="auth-page">
      <div className="auth-card card">
        <Logo />
        <h1>{title}</h1>
        <p className="muted">{subtitle}</p>
        {children}
        <p className="auth-footer">{footer}</p>
      </div>
    </div>
  );
}

function useAuthForm(initial, submit) {
  const [form, setForm] = useState(initial);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  async function onSubmit(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setErrors({});
    try {
      await submit(form);
    } catch (err) {
      setErrors(err.fieldErrors || {});
      setError(err);
      setBusy(false);
    }
  }
  return { form, set, errors, error, busy, onSubmit };
}

export function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const target = location.state?.from || '/dashboard';
  const { form, set, errors, error, busy, onSubmit } = useAuthForm({ email: '', password: '' }, async (f) => {
    await login(f);
    navigate(target, { replace: true });
  });

  if (isAuthenticated) return <Navigate to={target} replace />;

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Log in to your FlowForge workspace."
      footer={
        <>
          New here? <Link to="/register">Create an account</Link>
        </>
      }
    >
      <form className="form" onSubmit={onSubmit} noValidate>
        <ErrorMessage error={error} />
        <Field label="Email" error={errors.email}>
          <input type="email" autoComplete="email" value={form.email} onChange={set('email')} autoFocus />
        </Field>
        <Field label="Password" error={errors.password}>
          <input type="password" autoComplete="current-password" value={form.password} onChange={set('password')} />
        </Field>
        <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Logging in…' : 'Log in'}
        </button>
      </form>
    </AuthShell>
  );
}

export function RegisterPage() {
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const { form, set, errors, error, busy, onSubmit } = useAuthForm(
    { name: '', email: '', password: '' },
    async (f) => {
      await register(f);
      navigate('/dashboard', { replace: true });
    },
  );

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  return (
    <AuthShell
      title="Create your account"
      subtitle="Projects, tasks and progress in one place."
      footer={
        <>
          Already have an account? <Link to="/login">Log in</Link>
        </>
      }
    >
      <form className="form" onSubmit={onSubmit} noValidate>
        <ErrorMessage error={error} />
        <Field label="Name" error={errors.name}>
          <input autoComplete="name" value={form.name} onChange={set('name')} autoFocus />
        </Field>
        <Field label="Email" error={errors.email}>
          <input type="email" autoComplete="email" value={form.email} onChange={set('email')} />
        </Field>
        <Field label="Password" error={errors.password} hint="At least 8 characters.">
          <input type="password" autoComplete="new-password" value={form.password} onChange={set('password')} />
        </Field>
        <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
          {busy ? 'Creating account…' : 'Create account'}
        </button>
      </form>
    </AuthShell>
  );
}
