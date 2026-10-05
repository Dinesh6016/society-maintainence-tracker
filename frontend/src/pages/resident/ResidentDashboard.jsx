import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api';
import Layout from '../../components/Layout';
import { StatusBadge, PriorityBadge } from '../../components/Badges';
import { useAuth } from '../../context/AuthContext';

export default function ResidentDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [complaints, setComplaints] = useState([]);
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    try {
      const [statsRes, complaintsRes, noticesRes] = await Promise.all([
        api.get('/resident/dashboard'),
        api.get('/complaints/my?size=5&sort=createdAt,desc'),
        api.get('/notices'),
      ]);
      setStats(statsRes.data);
      setComplaints(complaintsRes.data.content || []);
      setNotices(noticesRes.data.filter(n => n.important).slice(0, 3));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  if (loading) return <Layout title="Dashboard"><div className="loading">Loading dashboard…</div></Layout>;

  const statCards = [
    { label: 'Total',       val: stats?.total || 0,      color: '#4f46e5' },
    { label: 'Open',        val: stats?.open || 0,       color: '#2563eb' },
    { label: 'In Progress', val: stats?.inProgress || 0, color: '#d97706' },
    { label: 'Resolved',    val: stats?.resolved || 0,   color: '#16a34a' },
  ];

  return (
    <Layout title="Dashboard">
      <div className="page-header">
        <h1>Welcome back, {user?.name?.split(' ')[0]} 👋</h1>
        <Link to="/resident/complaints/new" className="btn btn-primary">+ Raise Complaint</Link>
      </div>

      <div className="stats-grid">
        {statCards.map(s => (
          <div className="stat-card" key={s.label}>
            <div className="stat-num" style={{ color: s.color }}>{s.val}</div>
            <div className="stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      {notices.length > 0 && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <div className="card-title">📌 Important Notices</div>
          {notices.map(n => (
            <div key={n.id} style={{ padding: '.75rem 0', borderBottom: '1px solid var(--border)' }}>
              <div style={{ fontWeight: 700 }}>{n.title}</div>
              <div style={{ fontSize: '.85rem', color: 'var(--text-muted)', marginTop: '.25rem' }}>{n.content}</div>
            </div>
          ))}
        </div>
      )}

      <div className="card">
        <div className="card-header">
          <div className="card-title">Recent Complaints</div>
          <Link to="/resident/complaints" className="btn btn-ghost btn-sm">View All</Link>
        </div>
        {complaints.length === 0 ? (
          <div className="empty"><div className="empty-icon">📋</div><p>No complaints yet.</p></div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>#</th><th>Title</th><th>Category</th><th>Status</th><th>Priority</th><th>Date</th></tr></thead>
              <tbody>
                {complaints.map(c => (
                  <tr key={c.id} className={c.isOverdue && c.status !== 'RESOLVED' ? 'row-overdue' : ''}>
                    <td style={{ color: 'var(--text-muted)', fontWeight: 600 }}>#{c.id}</td>
                    <td><Link to={`/resident/complaints/${c.id}`} style={{ color: 'var(--primary)', fontWeight: 500 }}>{c.title}</Link></td>
                    <td style={{ color: 'var(--text-muted)' }}>{c.category}</td>
                    <td><StatusBadge status={c.status} isOverdue={c.isOverdue} /></td>
                    <td><PriorityBadge priority={c.priority} /></td>
                    <td style={{ fontSize: '.8rem', color: 'var(--text-muted)' }}>{new Date(c.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  );
}
