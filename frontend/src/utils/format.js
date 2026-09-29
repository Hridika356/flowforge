import { LABELS } from './constants';

export const label = (value) => LABELS[value] || value;

/** Parses "YYYY-MM-DD" as a local date (new Date("2026-10-15") would be UTC midnight). */
export function parseLocalDate(iso) {
  if (!iso) return null;
  const [y, m, d] = iso.slice(0, 10).split('-').map(Number);
  return new Date(y, m - 1, d);
}

export function formatDate(iso) {
  if (!iso) return '';
  const date = iso.length <= 10 ? parseLocalDate(iso) : new Date(iso);
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}

/**
 * Classifies a due date relative to `today`: 'overdue', 'today', 'soon' (within 3 days),
 * 'later', or null when there is no date. Completed tasks are never overdue.
 */
export function dueState(dueDate, status, today = new Date()) {
  if (!dueDate || status === 'DONE') return null;
  const due = parseLocalDate(dueDate);
  const start = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const days = Math.round((due - start) / 86_400_000);
  if (days < 0) return 'overdue';
  if (days === 0) return 'today';
  if (days <= 3) return 'soon';
  return 'later';
}

export function dueLabel(dueDate, status, today = new Date()) {
  const state = dueState(dueDate, status, today);
  if (state === 'overdue') return `Overdue · ${formatDate(dueDate)}`;
  if (state === 'today') return 'Due today';
  return dueDate ? `Due ${formatDate(dueDate)}` : '';
}

export function initials(name = '') {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('');
}
