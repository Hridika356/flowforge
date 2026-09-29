import { Link } from 'react-router-dom';
import ProjectCard from '../components/ProjectCard';
import { EmptyState, ErrorMessage, LoadingSpinner, PriorityBadge } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useAsync } from '../hooks/useAsync';
import { getDashboard } from '../services/projectService';
import { dueLabel, dueState } from '../utils/format';

function Stat({ label, value, tone }) {
  return (
    <div className={`stat card${tone ? ` stat-${tone}` : ''}`}>
      <span className="stat-label">{label}</span>
      <span className="stat-value">{value}</span>
    </div>
  );
}

export default function DashboardPage() {
  const { user } = useAuth();
  const { data, loading, error, reload } = useAsync(getDashboard, []);

  if (loading && !data) return <LoadingSpinner />;
  if (error) return <ErrorMessage error={error} onRetry={reload} />;

  const p = data.projectsByStatus;
  const t = data.tasksByStatus;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Hi, {user.name.split(' ')[0]}</h1>
          <p className="muted">Here's where your work stands.</p>
        </div>
        <Link to="/projects?new=1" className="btn btn-primary">
          New project
        </Link>
      </div>

      <h2 className="section-title">Projects</h2>
      <div className="stats">
        <Stat label="Total" value={data.totalProjects} />
        <Stat label="Active" value={p.ACTIVE} tone="active" />
        <Stat label="Planning" value={p.PLANNING} />
        <Stat label="Completed" value={p.COMPLETED} tone="done" />
      </div>

      <h2 className="section-title">Tasks</h2>
      <div className="stats">
        <Stat label="Total" value={data.totalTasks} />
        <Stat label="To do" value={t.TODO} />
        <Stat label="In progress" value={t.IN_PROGRESS} tone="active" />
        <Stat label="In review" value={t.REVIEW} tone="review" />
        <Stat label="Done" value={t.DONE} tone="done" />
      </div>

      <div className="dashboard-grid">
        <section>
          <div className="section-head">
            <h2 className="section-title">Recent projects</h2>
            <Link to="/projects">View all</Link>
          </div>
          {data.recentProjects.length === 0 ? (
            <EmptyState
              title="No projects yet"
              action={
                <Link to="/projects?new=1" className="btn btn-primary">
                  Create your first project
                </Link>
              }
            >
              Projects hold your tasks and track progress as they move to Done.
            </EmptyState>
          ) : (
            <div className="project-grid">
              {data.recentProjects.map((project) => (
                <ProjectCard key={project.id} project={project} />
              ))}
            </div>
          )}
        </section>

        <section>
          <div className="section-head">
            <h2 className="section-title">Due soon</h2>
          </div>
          <div className="card due-list">
            {data.dueSoon.length === 0 ? (
              <p className="muted">Nothing due in the next 7 days.</p>
            ) : (
              <ul>
                {data.dueSoon.map((task) => (
                  <li key={task.id}>
                    <Link to={`/projects/${task.projectId}`}>
                      <span className="task-title">{task.title}</span>
                      <span className="due-list-meta">
                        <span className="muted">{task.projectName}</span>
                        <PriorityBadge priority={task.priority} />
                        <span className={`due due-${dueState(task.dueDate, task.status)}`}>
                          {dueLabel(task.dueDate, task.status)}
                        </span>
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
