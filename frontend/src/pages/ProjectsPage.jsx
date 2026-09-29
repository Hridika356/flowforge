import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import ProjectCard from '../components/ProjectCard';
import ProjectForm from '../components/ProjectForm';
import { EmptyState, ErrorMessage, LoadingSpinner } from '../components/ui';
import { useAsync } from '../hooks/useAsync';
import { createProject, getProjects } from '../services/projectService';
import { PROJECT_STATUSES } from '../utils/constants';
import { label } from '../utils/format';

export default function ProjectsPage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const status = params.get('status') || '';
  const [search, setSearch] = useState(params.get('q') || '');
  const [query, setQuery] = useState(search);
  const creating = params.get('new') === '1';

  // Debounce typing so we don't hit the API on every keystroke.
  useEffect(() => {
    const id = setTimeout(() => setQuery(search.trim()), 250);
    return () => clearTimeout(id);
  }, [search]);

  const { data: projects, loading, error, reload } = useAsync(
    () => getProjects({ status, q: query }),
    [status, query],
  );

  function updateParam(key, value) {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    setParams(next, { replace: true });
  }

  async function handleCreate(data) {
    const project = await createProject(data);
    navigate(`/projects/${project.id}`);
  }

  const filtered = Boolean(status || query);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Projects</h1>
          <p className="muted">Everything you own, most recently updated first.</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={() => updateParam('new', '1')}>
          New project
        </button>
      </div>

      <div className="toolbar">
        <input
          type="search"
          className="search"
          placeholder="Search projects…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          aria-label="Search projects"
        />
        <div className="chips" role="group" aria-label="Filter by status">
          <button type="button" className={`chip${!status ? ' active' : ''}`} onClick={() => updateParam('status', '')}>
            All
          </button>
          {PROJECT_STATUSES.map((s) => (
            <button
              type="button"
              key={s}
              className={`chip${status === s ? ' active' : ''}`}
              onClick={() => updateParam('status', s)}
            >
              {label(s)}
            </button>
          ))}
        </div>
      </div>

      {error ? (
        <ErrorMessage error={error} onRetry={reload} />
      ) : loading && !projects ? (
        <LoadingSpinner />
      ) : projects.length === 0 ? (
        filtered ? (
          <EmptyState title="No matching projects">Try a different search or status filter.</EmptyState>
        ) : (
          <EmptyState
            title="No projects yet"
            action={
              <button type="button" className="btn btn-primary" onClick={() => updateParam('new', '1')}>
                Create your first project
              </button>
            }
          >
            Create a project, then add tasks and move them across the board.
          </EmptyState>
        )
      ) : (
        <div className="project-grid">
          {projects.map((project) => (
            <ProjectCard key={project.id} project={project} />
          ))}
        </div>
      )}

      {creating && <ProjectForm project={null} onSubmit={handleCreate} onClose={() => updateParam('new', '')} />}
    </div>
  );
}
