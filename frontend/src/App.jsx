import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

import Login from './pages/Login';
import Register from './pages/Register';
import ResidentDashboard from './pages/resident/ResidentDashboard';
import MyComplaints from './pages/resident/MyComplaints';
import NewComplaint from './pages/resident/NewComplaint';
import ComplaintDetail from './pages/resident/ComplaintDetail';
import NoticeBoard from './pages/resident/NoticeBoard';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminComplaints from './pages/admin/AdminComplaints';
import AdminComplaintDetail from './pages/admin/AdminComplaintDetail';
import AdminNotices from './pages/admin/AdminNotices';

function RootRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'ADMIN' ? '/admin/dashboard' : '/resident/dashboard'} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Toaster position="top-right" toastOptions={{ duration: 4000 }} />
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Resident */}
          <Route path="/resident/dashboard" element={<ProtectedRoute role="RESIDENT"><ResidentDashboard /></ProtectedRoute>} />
          <Route path="/resident/complaints" element={<ProtectedRoute role="RESIDENT"><MyComplaints /></ProtectedRoute>} />
          <Route path="/resident/complaints/new" element={<ProtectedRoute role="RESIDENT"><NewComplaint /></ProtectedRoute>} />
          <Route path="/resident/complaints/:id" element={<ProtectedRoute role="RESIDENT"><ComplaintDetail /></ProtectedRoute>} />
          <Route path="/notices" element={<ProtectedRoute><NoticeBoard /></ProtectedRoute>} />

          {/* Admin */}
          <Route path="/admin/dashboard" element={<ProtectedRoute role="ADMIN"><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/complaints" element={<ProtectedRoute role="ADMIN"><AdminComplaints /></ProtectedRoute>} />
          <Route path="/admin/complaints/:id" element={<ProtectedRoute role="ADMIN"><AdminComplaintDetail /></ProtectedRoute>} />
          <Route path="/admin/notices" element={<ProtectedRoute role="ADMIN"><AdminNotices /></ProtectedRoute>} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
