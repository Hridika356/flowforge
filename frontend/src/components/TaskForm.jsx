import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { TASK_PRIORITIES, TASK_STATUSES } from '../utils/constants';
import { label } from '../utils/format';
import { ErrorMessage, Field, Modal } from './ui';

/** Create or edit a task. `task` is null when creating; `defaultStatus` presets the column. */
export default function TaskForm({ task, defaultStatus = 'TODO', onSubmit, onDelete, onClose }) {
  const { user } = useAuth();
  const [form, setForm] = useState({
    title: task?.title || '',
    description: task?.description || '',
    status: task?.status || defaultStatus,
    priority: task?.priority || 'MEDIUM',
    dueDate: task?.dueDate || '',
    assignedToMe: task ? task.assignedUser?.id === user?.id : false,
  });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  const set = (key) => (e) =>
    setForm((f) => ({ ...f, [key]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }));

  async function handleSubmit(e) {
    e.preventDefault();
    if (!form.title.trim()) {
      setErrors({ title: 'Task title cannot be empty' });
      return;
    }
    setSaving(true);
    setError(null);
    setErrors({});
    try {
      await onSubmit({
        title: form.title.trim(),
        description: form.description,
        status: form.status,
        priority: form.priority,
        dueDate: form.dueDate || null,
        assignedUserId: form.assignedToMe ? user.id : null,
      });
    } catch (err) {
      setErrors(err.fieldErrors || {});
      setError(err);
      setSaving(false);
    }
  }

  return (
    <Modal title={task ? 'Edit task' : 'New task'} onClose={onClose}>
      <form onSubmit={handleSubmit} className="form" noValidate>
        <ErrorMessage error={error} />
        <Field label="Title" error={errors.title}>
          <input value={form.title} onChange={set('title')} maxLength={200} autoFocus placeholder="Design homepage" />
        </Field>
        <Field label="Description" error={errors.description}>
          <textarea value={form.description} onChange={set('description')} rows={3} maxLength={4000} />
        </Field>
        <div className="form-row">
          <Field label="Status" error={errors.status}>
            <select value={form.status} onChange={set('status')}>
              {TASK_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {label(s)}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Priority" error={errors.priority}>
            <select value={form.priority} onChange={set('priority')}>
              {TASK_PRIORITIES.map((p) => (
                <option key={p} value={p}>
                  {label(p)}
                </option>
              ))}
            </select>
          </Field>
        </div>
        <div className="form-row">
          <Field label="Due date" error={errors.dueDate}>
            <input type="date" value={form.dueDate} onChange={set('dueDate')} />
          </Field>
          <label className="checkbox">
            <input type="checkbox" checked={form.assignedToMe} onChange={set('assignedToMe')} />
            <span>Assign to me</span>
          </label>
        </div>
        <div className="modal-actions">
          {task && onDelete && (
            <button type="button" className="btn btn-danger-ghost mr-auto" onClick={onDelete} disabled={saving}>
              Delete task
            </button>
          )}
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : task ? 'Save changes' : 'Add task'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
