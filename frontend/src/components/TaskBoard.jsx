import { useState } from 'react';
import { TASK_STATUSES } from '../utils/constants';
import { dueLabel, dueState, initials, label } from '../utils/format';
import { PriorityBadge } from './ui';

function TaskCard({ task, onOpen, onMove, onDragStart }) {
  const index = TASK_STATUSES.indexOf(task.status);
  const due = dueState(task.dueDate, task.status);
  return (
    <article
      className={`task-card${task.status === 'DONE' ? ' task-done' : ''}`}
      draggable
      onDragStart={(e) => onDragStart(e, task)}
    >
      <button type="button" className="task-card-body" onClick={() => onOpen(task)}>
        <span className="task-title">{task.title}</span>
        <span className="task-meta">
          <PriorityBadge priority={task.priority} />
          {task.dueDate && <span className={`due due-${due || 'none'}`}>{dueLabel(task.dueDate, task.status)}</span>}
          {task.assignedUser && (
            <span className="avatar avatar-small" title={`Assigned to ${task.assignedUser.name}`}>
              {initials(task.assignedUser.name)}
            </span>
          )}
        </span>
      </button>
      <div className="task-card-actions">
        <button
          type="button"
          className="icon-btn"
          disabled={index === 0}
          aria-label={`Move "${task.title}" back to ${label(TASK_STATUSES[index - 1] || '')}`}
          onClick={() => onMove(task, TASK_STATUSES[index - 1])}
        >
          ‹
        </button>
        <button
          type="button"
          className="icon-btn"
          disabled={index === TASK_STATUSES.length - 1}
          aria-label={`Move "${task.title}" forward to ${label(TASK_STATUSES[index + 1] || '')}`}
          onClick={() => onMove(task, TASK_STATUSES[index + 1])}
        >
          ›
        </button>
      </div>
    </article>
  );
}

/**
 * Kanban board with one column per status. Tasks move with the arrow buttons or by
 * drag-and-drop; `onMove(task, status)` persists the change.
 */
export default function TaskBoard({ tasks, onOpen, onMove, onAdd }) {
  const [dragOver, setDragOver] = useState(null);

  function handleDragStart(e, task) {
    e.dataTransfer.setData('text/plain', String(task.id));
    e.dataTransfer.effectAllowed = 'move';
  }

  function handleDrop(e, status) {
    e.preventDefault();
    setDragOver(null);
    const id = Number(e.dataTransfer.getData('text/plain'));
    const task = tasks.find((t) => t.id === id);
    if (task && task.status !== status) onMove(task, status);
  }

  return (
    <div className="board">
      {TASK_STATUSES.map((status) => {
        const column = tasks.filter((t) => t.status === status);
        return (
          <section
            key={status}
            className={`column column-${status.toLowerCase()}${dragOver === status ? ' drag-over' : ''}`}
            onDragOver={(e) => {
              e.preventDefault();
              setDragOver(status);
            }}
            onDragLeave={(e) => !e.currentTarget.contains(e.relatedTarget) && setDragOver(null)}
            onDrop={(e) => handleDrop(e, status)}
            aria-label={`${label(status)} column`}
          >
            <header className="column-header">
              <h3>{label(status)}</h3>
              <span className="count">{column.length}</span>
            </header>
            <div className="column-body">
              {column.map((task) => (
                <TaskCard key={task.id} task={task} onOpen={onOpen} onMove={onMove} onDragStart={handleDragStart} />
              ))}
              {column.length === 0 && <p className="column-empty">No tasks</p>}
            </div>
            <button type="button" className="btn btn-ghost btn-small column-add" onClick={() => onAdd(status)}>
              + Add task
            </button>
          </section>
        );
      })}
    </div>
  );
}
