import { useState } from 'react';
import { PROJECT_STATUSES } from '../utils/constants';
import { label } from '../utils/format';
import { ErrorMessage, Field, Modal } from './ui';

/** Create or edit a project. `project` is null when creating. */
export default function ProjectForm({ project, onSubmit, onClose }) {
  const [form, setForm] = useState({
    name: project?.name || '',
    description: project?.description || '',
    status: project?.status || 'PLANNING',
  });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  async function handleSubmit(e) {
    e.preventDefault();
    if (!form.name.trim()) {
      setErrors({ name: 'Project name cannot be empty' });
      return;
    }
    setSaving(true);
    setError(null);
    setErrors({});
    try {
      await onSubmit({ ...form, name: form.name.trim() });
    } catch (err) {
      setErrors(err.fieldErrors || {});
      setError(err);
      setSaving(false);
    }
  }

  return (
    <Modal title={project ? 'Edit project' : 'New project'} onClose={onClose}>
      <form onSubmit={handleSubmit} className="form" noValidate>
        <ErrorMessage error={error} />
        <Field label="Name" error={errors.name}>
          <input value={form.name} onChange={set('name')} maxLength={150} autoFocus placeholder="Website redesign" />
        </Field>
        <Field label="Description" error={errors.description}>
          <textarea
            value={form.description}
            onChange={set('description')}
            rows={4}
            maxLength={2000}
            placeholder="What is this project about?"
          />
        </Field>
        <Field label="Status" error={errors.status}>
          <select value={form.status} onChange={set('status')}>
            {PROJECT_STATUSES.map((s) => (
              <option key={s} value={s}>
                {label(s)}
              </option>
            ))}
          </select>
        </Field>
        <div className="modal-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : project ? 'Save changes' : 'Create project'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
