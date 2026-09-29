import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import ProjectForm from '../components/ProjectForm';
import TaskBoard from '../components/TaskBoard';
import TaskForm from '../components/TaskForm';
import { ConfirmDialog, ErrorMessage, LoadingSpinner, ProgressBar, StatusBadge } from '../components/ui';
import { useAsync } from '../hooks/useAsync';
import { deleteProject, getProject, updateProject } from '../services/projectService';
import { createTask, deleteTask, getProjectTasks, updateTask, updateTaskStatus } from '../services/taskService';
import { formatDate } from '../utils/format';

function computeProgress(tasks) {
  if (!tasks.length) return 0;
  return Math.round((tasks.filter((t) => t.status === 'DONE').length * 100) / tasks.length);
}

export default function ProjectDetailsPage() {
  const { projectId } = useParams();
  const navigate = useNavigate();

  const { data, setData, loading, error, reload } = useAsync(
    () => Promise.all([getProject(projectId), getProjectTasks(projectId)]).then(([project, tasks]) => ({ project, tasks })),
    [projectId],
  );

  // dialog: null | {type:'editProject'} | {type:'deleteProject'} | {type:'task', task, status} | {type:'deleteTask', task}
  const [dialog, setDialog] = useState(null);
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState(null);

  if (loading && !data) return <LoadingSpinner />;
  if (error) {
    return (
      <div className="page">
        <Link to="/projects" className="back-link">
          ← Projects
        </Link>
        <ErrorMessage error={error} onRetry={error.status === 404 ? undefined : reload} />
      </div>
    );
  }

  const { project, tasks } = data;
  const close = () => setDialog(null);
  const setTasks = (fn) => setData((d) => ({ ...d, tasks: fn(d.tasks) }));

  async function handleMove(task, status) {
    setActionError(null);
    const previous = task.status;
    setTasks((ts) => ts.map((t) => (t.id === task.id ? { ...t, status } : t))); // optimistic
    try {
      const saved = await updateTaskStatus(task.id, status);
      setTasks((ts) => ts.map((t) => (t.id === saved.id ? saved : t)));
    } catch (err) {
      setTasks((ts) => ts.map((t) => (t.id === task.id ? { ...t, status: previous } : t)));
      setActionError(err);
    }
  }

  async function handleSaveTask(values) {
    if (dialog.task) {
      const saved = await updateTask(dialog.task.id, values);
      setTasks((ts) => ts.map((t) => (t.id === saved.id ? saved : t)));
    } else {
      const created = await createTask(project.id, values);
      setTasks((ts) => [...ts, created]);
    }
    close();
  }

  async function handleDeleteTask() {
    setBusy(true);
    try {
      await deleteTask(dialog.task.id);
      setTasks((ts) => ts.filter((t) => t.id !== dialog.task.id));
      close();
    } catch (err) {
      setActionError(err);
      close();
    } finally {
      setBusy(false);
    }
  }

  async function handleSaveProject(values) {
    const saved = await updateProject(project.id, values);
    setData((d) => ({ ...d, project: saved }));
    close();
  }

  async function handleDeleteProject() {
    setBusy(true);
    try {
      await deleteProject(project.id);
      navigate('/projects', { replace: true });
    } catch (err) {
      setActionError(err);
      setBusy(false);
      close();
    }
  }

  const progress = computeProgress(tasks);
  const done = tasks.filter((t) => t.status === 'DONE').length;

  return (
    <div className="page">
      <Link to="/projects" className="back-link">
        ← Projects
      </Link>

      <div className="page-header">
        <div className="project-heading">
          <div className="title-row">
            <h1>{project.name}</h1>
            <StatusBadge status={project.status} />
          </div>
          {project.description && <p className="project-description">{project.description}</p>}
          <p className="muted small">
            Owned by {project.owner.name} · Created {formatDate(project.createdAt)}
          </p>
        </div>
        <div className="header-actions">
          <button type="button" className="btn btn-ghost" onClick={() => setDialog({ type: 'editProject' })}>
            Edit
          </button>
          <button type="button" className="btn btn-danger-ghost" onClick={() => setDialog({ type: 'deleteProject' })}>
            Delete
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setDialog({ type: 'task', task: null, status: 'TODO' })}
          >
            Add task
          </button>
        </div>
      </div>

      <div className="card progress-card">
        <div>
          <span className="stat-label">Progress</span>
          <span className="muted small">
            {done} of {tasks.length} task{tasks.length === 1 ? '' : 's'} done
          </span>
        </div>
        <ProgressBar value={progress} />
      </div>

      <ErrorMessage error={actionError} />

      <TaskBoard
        tasks={tasks}
        onOpen={(task) => setDialog({ type: 'task', task })}
        onMove={handleMove}
        onAdd={(status) => setDialog({ type: 'task', task: null, status })}
      />

      {dialog?.type === 'editProject' && <ProjectForm project={project} onSubmit={handleSaveProject} onClose={close} />}
      {dialog?.type === 'deleteProject' && (
        <ConfirmDialog
          title="Delete project?"
          message={`"${project.name}" and its ${tasks.length} task${tasks.length === 1 ? '' : 's'} will be permanently deleted.`}
          confirmLabel="Delete project"
          busy={busy}
          onConfirm={handleDeleteProject}
          onCancel={close}
        />
      )}
      {dialog?.type === 'task' && (
        <TaskForm
          task={dialog.task}
          defaultStatus={dialog.status}
          onSubmit={handleSaveTask}
          onDelete={() => setDialog({ type: 'deleteTask', task: dialog.task })}
          onClose={close}
        />
      )}
      {dialog?.type === 'deleteTask' && (
        <ConfirmDialog
          title="Delete task?"
          message={`"${dialog.task.title}" will be permanently deleted.`}
          confirmLabel="Delete task"
          busy={busy}
          onConfirm={handleDeleteTask}
          onCancel={close}
        />
      )}
    </div>
  );
}
