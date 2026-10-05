import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import api from '../../api';
import Layout from '../../components/Layout';

const CATEGORIES = ['PLUMBING','ELECTRICAL','CLEANING','SECURITY','LIFT','WATER','PARKING','MAINTENANCE','OTHER'];

export default function NewComplaint() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ title: '', category: '', description: '' });
  const [photo, setPhoto] = useState(null);
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const handlePhoto = e => {
    const file = e.target.files[0];
    if (!file) return;
    setPhoto(file);
    setPreview(URL.createObjectURL(file));
  };

  const submit = async e => {
    e.preventDefault();
    setLoading(true);
    try {
      const fd = new FormData();
      fd.append('data', new Blob([JSON.stringify(form)], { type: 'application/json' }));
      if (photo) fd.append('photo', photo);
      await api.post('/complaints', fd);
      toast.success('Complaint submitted successfully!');
      navigate('/resident/complaints');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit complaint');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout title="Raise Complaint">
      <div className="page-header"><h1>Raise a Complaint</h1></div>
      <div className="card" style={{ maxWidth: 600 }}>
        <form onSubmit={submit}>
          <div className="form-group">
            <label>Title</label>
            <input name="title" value={form.title} onChange={handle} placeholder="Brief title of the issue" required />
          </div>
          <div className="form-group">
            <label>Category</label>
            <select name="category" value={form.category} onChange={handle} required>
              <option value="">Select a category…</option>
              {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea name="description" value={form.description} onChange={handle}
              placeholder="Describe the issue in detail…" required />
          </div>
          <div className="form-group">
            <label>Photo (optional — JPG, PNG, WEBP, max 5MB)</label>
            <input type="file" accept="image/jpeg,image/png,image/webp" onChange={handlePhoto} />
            {preview && <img src={preview} alt="Preview" className="photo-preview" />}
          </div>
          <div className="gap-2">
            <button className="btn btn-primary" disabled={loading}>
              {loading ? 'Submitting…' : 'Submit Complaint'}
            </button>
            <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>Cancel</button>
          </div>
        </form>
      </div>
    </Layout>
  );
}
