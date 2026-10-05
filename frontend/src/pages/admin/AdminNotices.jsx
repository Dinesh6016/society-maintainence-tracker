import { useEffect, useState, useCallback } from 'react';
import api from '../../api';
import toast from 'react-hot-toast';
import Layout from '../../components/Layout';

export default function AdminNotices() {
  const [notices, setNotices] = useState([]);
  const [form, setForm] = useState({ title: '', content: '', important: false });
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState(null);

  const load = useCallback(() => {
    api.get('/notices').then(r => setNotices(r.data));
  }, []);

  useEffect(() => { load(); }, [load]);

  const submit = async e => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/notices', form);
      toast.success(form.important ? 'Important notice published — residents emailed!' : 'Notice posted successfully');
      setForm({ title: '', content: '', important: false });
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to post notice');
    } finally {
      setLoading(false);
    }
  };

  const deleteNotice = async id => {
    setDeleting(id);
    try {
      await api.delete(`/notices/${id}`);
      toast.success('Notice deleted');
      load();
    } catch {
      toast.error('Failed to delete');
    } finally {
      setDeleting(null);
    }
  };

  return (
    <Layout title="Notices">
      <div className="page-header"><h1>Notice Board</h1></div>

      <div className="card" style={{ maxWidth: 580, marginBottom: '2rem' }}>
        <div className="card-title">Post New Notice</div>
        <form onSubmit={submit}>
          <div className="form-group">
            <label>Title</label>
            <input value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} required />
          </div>
          <div className="form-group">
            <label>Content</label>
            <textarea value={form.content} onChange={e => setForm(f => ({ ...f, content: e.target.value }))} required />
          </div>
          <label className="checkbox-row">
            <input type="checkbox" checked={form.important} onChange={e => setForm(f => ({ ...f, important: e.target.checked }))} />
            <span>Mark as Important — pins to top &amp; emails all residents</span>
          </label>
          <button className="btn btn-primary" disabled={loading}>
            {loading ? 'Posting…' : 'Post Notice'}
          </button>
        </form>
      </div>

      {notices.length === 0 ? (
        <div className="empty"><div className="empty-icon">📋</div><p>No notices yet.</p></div>
      ) : (
        notices.map(n => (
          <div className={`card notice-card ${n.important ? 'important' : ''}`} key={n.id}>
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', gap:'1rem' }}>
              <div style={{ flex:1 }}>
                {n.important && <span className="notice-pin">📌 Important</span>}
                <div className="notice-title">{n.title}</div>
                <p style={{ color:'var(--text-muted)', margin:'.5rem 0', lineHeight:1.6 }}>{n.content}</p>
                <span className="notice-meta">By {n.createdBy} · {new Date(n.createdAt).toLocaleDateString()}</span>
              </div>
              <button className="btn btn-danger btn-sm" onClick={() => deleteNotice(n.id)}
                disabled={deleting === n.id} style={{ flexShrink:0 }}>
                {deleting === n.id ? '…' : 'Delete'}
              </button>
            </div>
          </div>
        ))
      )}
    </Layout>
  );
}
