import { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api';
import toast from 'react-hot-toast';
import Layout from '../../components/Layout';
import { StatusBadge, PriorityBadge } from '../../components/Badges';

const CATEGORIES = ['','PLUMBING','ELECTRICAL','CLEANING','SECURITY','LIFT','WATER','PARKING','MAINTENANCE','OTHER'];
const STATUSES   = ['','OPEN','IN_PROGRESS','RESOLVED'];
const PRIORITIES = ['','LOW','MEDIUM','HIGH'];

export default function AdminComplaints() {
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [filters, setFilters] = useState({ status:'', category:'', priority:'', overdue:'', from:'', to:'' });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [update, setUpdate] = useState({ status:'', priority:'', note:'' });
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    const params = new URLSearchParams({ page, size: 20, sort: 'isOverdue,desc' });
    Object.entries(filters).forEach(([k, v]) => { if (v) params.append(k, v); });
    api.get(`/admin/complaints?${params}`).then(r => setData(r.data)).finally(() => setLoading(false));
  }, [filters, page]);

  useEffect(() => { load(); }, [load]);

  const openModal = c => { setModal(c); setUpdate({ status: c.status, priority: c.priority, note: '' }); };

  const saveUpdate = async () => {
    setSaving(true);
    try {
      if (update.status !== modal.status)
        await api.patch(`/admin/complaints/${modal.id}/status`, { status: update.status, note: update.note });
      if (update.priority !== modal.priority)
        await api.patch(`/admin/complaints/${modal.id}/priority`, { priority: update.priority, note: update.note });
      toast.success('Complaint updated successfully');
      setModal(null);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed');
    } finally {
      setSaving(false);
    }
  };

  const setFilter = (k, v) => { setFilters(f => ({ ...f, [k]: v })); setPage(0); };

  return (
    <Layout title="Complaints">
      <div className="page-header"><h1>All Complaints</h1></div>

      <div className="filters">
        <select value={filters.status}   onChange={e => setFilter('status', e.target.value)}>
          {STATUSES.map(s => <option key={s} value={s}>{s || 'All Statuses'}</option>)}
        </select>
        <select value={filters.category} onChange={e => setFilter('category', e.target.value)}>
          {CATEGORIES.map(c => <option key={c} value={c}>{c || 'All Categories'}</option>)}
        </select>
        <select value={filters.priority} onChange={e => setFilter('priority', e.target.value)}>
          {PRIORITIES.map(p => <option key={p} value={p}>{p || 'All Priorities'}</option>)}
        </select>
        <select value={filters.overdue}  onChange={e => setFilter('overdue', e.target.value)}>
          <option value="">All</option>
          <option value="true">Overdue Only</option>
        </select>
        <input type="date" value={filters.from} onChange={e => setFilter('from', e.target.value)} />
        <input type="date" value={filters.to}   onChange={e => setFilter('to',   e.target.value)} />
      </div>

      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrap">
          {loading ? <div className="loading">Loading complaints…</div> : (
            <table>
              <thead>
                <tr><th>#</th><th>Resident</th><th>Flat</th><th>Title</th><th>Category</th>
                    <th>Status</th><th>Priority</th><th>Date</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {data.content.length === 0 && (
                  <tr><td colSpan={9} style={{ textAlign:'center', padding:'3rem', color:'var(--text-subtle)' }}>No complaints found</td></tr>
                )}
                {data.content.map(c => (
                  <tr key={c.id}
                    className={[c.isOverdue && c.status !== 'RESOLVED' ? 'row-overdue' : '',
                                c.priority === 'HIGH' && c.status !== 'RESOLVED' ? 'row-high' : ''].join(' ')}>
                    <td style={{ color:'var(--text-muted)', fontWeight:600 }}>#{c.id}</td>
                    <td style={{ fontWeight:500 }}>{c.resident?.name}</td>
                    <td style={{ color:'var(--text-muted)' }}>{c.resident?.apartmentNumber}</td>
                    <td style={{ maxWidth:180 }} title={c.title}>{c.title.length > 40 ? c.title.slice(0,40)+'…' : c.title}</td>
                    <td style={{ color:'var(--text-muted)' }}>{c.category}</td>
                    <td><StatusBadge status={c.status} isOverdue={c.isOverdue} /></td>
                    <td><PriorityBadge priority={c.priority} /></td>
                    <td style={{ fontSize:'.8rem', color:'var(--text-muted)', whiteSpace:'nowrap' }}>
                      {new Date(c.createdAt).toLocaleDateString()}
                    </td>
                    <td>
                      <div className="gap-2">
                        {c.status !== 'RESOLVED' && (
                          <button className="btn btn-primary btn-sm" onClick={() => openModal(c)}>Update</button>
                        )}
                        <Link to={`/admin/complaints/${c.id}`} className="btn btn-ghost btn-sm">View</Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        {data.totalPages > 1 && (
          <div style={{ display:'flex', justifyContent:'center', gap:'.5rem', padding:'1rem' }}>
            <button className="btn btn-ghost btn-sm" disabled={page===0} onClick={() => setPage(p=>p-1)}>← Prev</button>
            <span style={{ padding:'.35rem .75rem', fontSize:'.875rem', color:'var(--text-muted)' }}>
              Page {page+1} of {data.totalPages}
            </span>
            <button className="btn btn-ghost btn-sm" disabled={page>=data.totalPages-1} onClick={() => setPage(p=>p+1)}>Next →</button>
          </div>
        )}
      </div>

      {modal && (
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">Update Complaint #{modal.id}</div>
            <p style={{ fontSize:'.85rem', color:'var(--text-muted)', marginBottom:'1.25rem' }}>
              {modal.title} · {modal.resident?.name} · Flat {modal.resident?.apartmentNumber}
            </p>
            <div className="form-group">
              <label>Status</label>
              <select value={update.status} onChange={e => setUpdate(u => ({ ...u, status: e.target.value }))}>
                {['OPEN','IN_PROGRESS','RESOLVED'].map(s => <option key={s}>{s}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Priority</label>
              <select value={update.priority} onChange={e => setUpdate(u => ({ ...u, priority: e.target.value }))}>
                {['LOW','MEDIUM','HIGH'].map(p => <option key={p}>{p}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Note (optional)</label>
              <textarea value={update.note} onChange={e => setUpdate(u => ({ ...u, note: e.target.value }))}
                placeholder="Add a note for the resident…" />
            </div>
            <div className="modal-actions">
              <button className="btn btn-ghost" onClick={() => setModal(null)}>Cancel</button>
              <button className="btn btn-primary" onClick={saveUpdate} disabled={saving}>
                {saving ? 'Saving…' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
