import { describe, expect, it } from 'vitest';
import { dueState, initials, label } from './format';

describe('dueState', () => {
  const today = new Date(2026, 8, 29); // 29 Sep 2026

  it('flags past dates as overdue', () => {
    expect(dueState('2026-09-28', 'TODO', today)).toBe('overdue');
  });

  it('never marks finished tasks overdue', () => {
    expect(dueState('2026-09-01', 'DONE', today)).toBeNull();
  });

  it('distinguishes today, soon and later', () => {
    expect(dueState('2026-09-29', 'TODO', today)).toBe('today');
    expect(dueState('2026-10-02', 'IN_PROGRESS', today)).toBe('soon');
    expect(dueState('2026-10-20', 'REVIEW', today)).toBe('later');
  });

  it('returns null without a due date', () => {
    expect(dueState(null, 'TODO', today)).toBeNull();
  });
});

describe('helpers', () => {
  it('builds initials', () => {
    expect(initials('Alex Morgan Lee')).toBe('AM');
    expect(initials('alex')).toBe('A');
  });

  it('labels enum values', () => {
    expect(label('IN_PROGRESS')).toBe('In progress');
    expect(label('UNKNOWN')).toBe('UNKNOWN');
  });
});
