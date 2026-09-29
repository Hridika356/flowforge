import { Link } from 'react-router-dom';
import { formatDate } from '../utils/format';
import { ProgressBar, StatusBadge } from './ui';

export default function ProjectCard({ project }) {
  const open = project.totalTasks - (project.taskCounts?.DONE || 0);
  return (
    <Link to={`/projects/${project.id}`} className="card project-card">
      <div className="project-card-top">
        <h3>{project.name}</h3>
        <StatusBadge status={project.status} />
      </div>
      <p className="project-card-desc">{project.description || <span className="muted">No description</span>}</p>
      <ProgressBar value={project.progress} />
      <div className="project-card-meta">
        <span>
          {project.totalTasks} task{project.totalTasks === 1 ? '' : 's'}
          {project.totalTasks > 0 && ` · ${open} open`}
        </span>
        <span>Updated {formatDate(project.updatedAt)}</span>
      </div>
    </Link>
  );
}
