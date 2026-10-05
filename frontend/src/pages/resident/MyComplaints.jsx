import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api';
import Layout from '../../components/Layout';
import { StatusBadge, PriorityBadge } from '../../components/Badges';

export default function MyComplaints() {
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    setLoading(true);
    api.get(`/complaints/my?page=${page}&size=10&sort=createdAt,desc`)
      .then(r => setData(r.data))
      .finally(() => setLoading(false));
  }, [page]);

  useEffect(() => { load(); }, [load]);

  return (
    <Layout title="My Complaints">
      <div className="page-header">
        <h1>My Complaints</h1>
        <Link to="/resident/complaints/new" className="btn btn-primary">+ Raise Complaint</Link>
      </div>

      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrap">
          {loading ? <div className="loading">Loading complaints…</div> : (
            <table>
              <thead>
                <tr><th>#</th><th>Title</th><th>Category</th><th>Status</th><th>Priority</th><th>Date</th><th></th></tr>
              </thead>
              <tbody>
                {data.content.length === 0 && (
                  <tr><td colSpan={7} style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-subtle)' }}>
                    No complaints found.
                  </td></tr>
                )}
                {data.content.map(c => (
                  <tr key={c.id} className={c.isOverdue && c.status !== 'RESOLVED' ? 'row-overdue' : ''}>
                    <td style={{ color: 'var(--text-muted)', fontWeight: 600 }}>#{c.id}</td>
                    <td style={{ fontWeight: 500 }}>{c.title}</td>
                    <td style={{ color: 'var(--text-muted)' }}>{c.category}</td>
                    <td><StatusBadge status={c.status} isOverdue={c.isOverdue} /></td>
                    <td><PriorityBadge priority={c.priority} /></td>
                    <td style={{ fontSize: '.8rem', color: 'var(--text-muted)' }}>{new Date(c.createdAt).toLocaleDateString()}</td>
                    <td><Link to={`/resident/complaints/${c.id}`} className="btn btn-ghost btn-sm">View</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        {data.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: '.5rem', padding: '1rem' }}>
            <button className="btn btn-ghost btn-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Prev</button>
            <span style={{ padding: '.35rem .75rem', fontSize: '.875rem', color: 'var(--text-muted)' }}>
              Page {page + 1} of {data.totalPages}
            </span>
            <button className="btn btn-ghost btn-sm" disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}>Next →</button>
          </div>
        )}
      </div>
    </Layout>
  );
}
