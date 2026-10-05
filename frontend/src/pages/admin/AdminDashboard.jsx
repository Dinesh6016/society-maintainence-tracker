import { useEffect, useState, useCallback } from 'react';
import api from '../../api';
import Layout from '../../components/Layout';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';

const STATUS_COLORS = { OPEN: '#2563eb', IN_PROGRESS: '#d97706', RESOLVED: '#16a34a' };
const PIE_COLORS = ['#4f46e5','#0284c7','#16a34a','#d97706','#dc2626','#7c3aed','#0891b2','#65a30d','#ea580c'];

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    setLoading(true);
    api.get('/admin/dashboard').then(r => setStats(r.data)).finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  if (loading) return <Layout title="Dashboard"><div className="loading">Loading dashboard…</div></Layout>;

  const statCards = [
    { label: 'Total',        val: stats.totalComplaints, color: '#4f46e5' },
    { label: 'Open',         val: stats.open,            color: '#2563eb' },
    { label: 'In Progress',  val: stats.inProgress,      color: '#d97706' },
    { label: 'Resolved',     val: stats.resolved,        color: '#16a34a' },
    { label: 'Overdue',      val: stats.overdue,         color: '#dc2626' },
    { label: 'High Priority',val: stats.highPriority,    color: '#9f1239' },
  ];

  const statusData = Object.entries(stats.byStatus || {}).map(([name, value]) => ({ name, value: Number(value) }));
  const categoryData = Object.entries(stats.byCategory || {}).map(([name, value]) => ({ name, value: Number(value) }));
  const maxCat = Math.max(...categoryData.map(d => d.value), 1);

  return (
    <Layout title="Admin Dashboard">
      <div className="page-header"><h1>Dashboard</h1></div>

      <div className="stats-grid">
        {statCards.map(s => (
          <div className="stat-card" key={s.label}>
            <div className="stat-num" style={{ color: s.color }}>{s.val}</div>
            <div className="stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div className="card">
          <div className="card-title">Complaints by Status</div>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={statusData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="value" radius={[4,4,0,0]}>
                {statusData.map((entry, i) => (
                  <Cell key={i} fill={STATUS_COLORS[entry.name] || '#4f46e5'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <div className="card-title">Complaints by Category</div>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={categoryData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label={({ name, percent }) => `${name} ${(percent*100).toFixed(0)}%`}>
                {categoryData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card">
        <div className="card-title">Category Breakdown</div>
        {categoryData.map(r => (
          <div className="bar-row" key={r.name}>
            <span className="bar-label">{r.name}</span>
            <div className="bar-track">
              <div className="bar-fill" style={{ width: `${(r.value / maxCat) * 100}%` }} />
            </div>
            <span className="bar-count">{r.value}</span>
          </div>
        ))}
      </div>
    </Layout>
  );
}
