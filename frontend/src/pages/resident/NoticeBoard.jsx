import { useEffect, useState } from 'react';
import api from '../../api';
import Layout from '../../components/Layout';

export default function NoticeBoard() {
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/notices').then(r => setNotices(r.data)).finally(() => setLoading(false));
  }, []);

  if (loading) return <Layout title="Notice Board"><div className="loading">Loading notices…</div></Layout>;

  return (
    <Layout title="Notice Board">
      <div className="page-header"><h1>Notice Board</h1></div>
      {notices.length === 0 ? (
        <div className="empty"><div className="empty-icon">📋</div><p>No notices posted yet.</p></div>
      ) : (
        notices.map(n => (
          <div className={`card notice-card ${n.important ? 'important' : ''}`} key={n.id}>
            {n.important && <span className="notice-pin">📌 Important</span>}
            <div className="notice-title">{n.title}</div>
            <p style={{ color: 'var(--text-muted)', margin: '.5rem 0', lineHeight: 1.6 }}>{n.content}</p>
            <span className="notice-meta">
              Posted by {n.createdBy} · {new Date(n.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}
            </span>
          </div>
        ))
      )}
    </Layout>
  );
}
