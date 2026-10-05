import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../../api';
import Layout from '../../components/Layout';
import { StatusBadge, PriorityBadge } from '../../components/Badges';

export default function ComplaintDetail() {
  const { id } = useParams();
  const [complaint, setComplaint] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get(`/complaints/${id}`),
      api.get(`/complaints/${id}/history`),
    ]).then(([c, h]) => {
      setComplaint(c.data);
      setHistory(h.data);
    }).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Layout title="Complaint Detail"><div className="loading">Loading…</div></Layout>;
  if (!complaint) return <Layout title="Complaint Detail"><div className="empty"><p>Complaint not found.</p></div></Layout>;

  const dotClass = s => s === 'RESOLVED' ? 'resolved' : s === 'IN_PROGRESS' ? 'in_progress' : '';

  return (
    <Layout title={`Complaint #${complaint.id}`}>
      <div className="page-header">
        <h1>Complaint #{complaint.id}</h1>
        <Link to="/resident/complaints" className="btn btn-ghost btn-sm">← Back</Link>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '1.5rem', alignItems: 'start' }}>
        <div>
          <div className="card">
            <div style={{ display: 'flex', gap: '.5rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
              <StatusBadge status={complaint.status} isOverdue={complaint.isOverdue} />
              <PriorityBadge priority={complaint.priority} />
              <span className="badge" style={{ background: '#f1f5f9', color: '#475569' }}>{complaint.category}</span>
            </div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '.75rem' }}>{complaint.title}</h2>
            <p style={{ color: 'var(--text-muted)', lineHeight: 1.7 }}>{complaint.description}</p>
            {complaint.photoUrl && (
              <img src={complaint.photoUrl} alt="Complaint" className="photo-preview" style={{ marginTop: '1rem' }} />
            )}
          </div>

          <div className="card">
            <div className="card-title">Status Timeline</div>
            {history.length === 0 ? (
              <p style={{ color: 'var(--text-subtle)', fontSize: '.9rem' }}>No history available.</p>
            ) : (
              <div className="timeline">
                {history.map(h => (
                  <div className="timeline-item" key={h.id}>
                    <div className={`timeline-dot ${dotClass(h.newStatus)}`} />
                    <div className="timeline-content">
                      <div className="timeline-status">
                        {h.oldStatus ? `${h.oldStatus.replace('_',' ')} → ${h.newStatus.replace('_',' ')}` : h.newStatus.replace('_',' ')}
                      </div>
                      <div className="timeline-meta">
                        {h.changedBy} · {new Date(h.changedAt).toLocaleString()}
                      </div>
                      {h.note && <div className="timeline-note">"{h.note}"</div>}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="card-title">Details</div>
          {[
            ['Status',   <StatusBadge status={complaint.status} isOverdue={complaint.isOverdue} />],
            ['Priority', <PriorityBadge priority={complaint.priority} />],
            ['Category', complaint.category],
            ['Created',  new Date(complaint.createdAt).toLocaleDateString()],
            ['Updated',  new Date(complaint.updatedAt).toLocaleDateString()],
            complaint.resolvedAt && ['Resolved', new Date(complaint.resolvedAt).toLocaleDateString()],
          ].filter(Boolean).map(([label, val]) => (
            <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '.6rem 0', borderBottom: '1px solid var(--border)', fontSize: '.875rem' }}>
              <span style={{ color: 'var(--text-muted)', fontWeight: 600 }}>{label}</span>
              <span>{val}</span>
            </div>
          ))}
        </div>
      </div>
    </Layout>
  );
}
