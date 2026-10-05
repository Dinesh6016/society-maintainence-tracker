import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FiHome, FiList, FiPlusCircle, FiBell, FiLogOut, FiUsers, FiBarChart2 } from 'react-icons/fi';

const adminLinks = [
  { to: '/admin/dashboard',   label: 'Dashboard',   icon: <FiBarChart2 /> },
  { to: '/admin/complaints',  label: 'Complaints',  icon: <FiList /> },
  { to: '/admin/notices',     label: 'Notices',     icon: <FiBell /> },
];

const residentLinks = [
  { to: '/resident/dashboard',      label: 'Dashboard',       icon: <FiHome /> },
  { to: '/resident/complaints',     label: 'My Complaints',   icon: <FiList /> },
  { to: '/resident/complaints/new', label: 'Raise Complaint', icon: <FiPlusCircle /> },
  { to: '/notices',                 label: 'Notice Board',    icon: <FiBell /> },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const { pathname } = useLocation();
  const navigate = useNavigate();

  const links = user?.role === 'ADMIN' ? adminLinks : residentLinks;

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">🏢 Society Tracker</div>
      <nav className="sidebar-nav">
        <div className="sidebar-section">{user?.role === 'ADMIN' ? 'Admin' : 'Resident'}</div>
        {links.map(({ to, label, icon }) => (
          <Link key={to} to={to} className={`sidebar-link ${pathname === to ? 'active' : ''}`}>
            {icon} {label}
          </Link>
        ))}
      </nav>
      <div className="sidebar-footer">
        <div className="sidebar-user">
          <strong>{user?.name}</strong>
          {user?.apartmentNumber && <span>Flat {user.apartmentNumber}</span>}
        </div>
        <button className="btn btn-ghost btn-sm btn-full" onClick={handleLogout}>
          <FiLogOut /> Logout
        </button>
      </div>
    </aside>
  );
}
