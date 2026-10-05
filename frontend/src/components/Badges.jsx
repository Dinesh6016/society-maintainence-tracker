export function StatusBadge({ status, isOverdue }) {
  if (isOverdue && status !== 'RESOLVED')
    return <span className="badge badge-overdue">Overdue</span>;
  const cls = { OPEN: 'badge-open', IN_PROGRESS: 'badge-in_progress', RESOLVED: 'badge-resolved' };
  return <span className={`badge ${cls[status] || ''}`}>{status?.replace('_', ' ')}</span>;
}

export function PriorityBadge({ priority }) {
  const cls = { LOW: 'badge-low', MEDIUM: 'badge-medium', HIGH: 'badge-high' };
  return <span className={`badge ${cls[priority] || 'badge-low'}`}>{priority}</span>;
}
