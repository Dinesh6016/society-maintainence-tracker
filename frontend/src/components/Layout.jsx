import Sidebar from './Sidebar';

export default function Layout({ title, children }) {
  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <header className="topbar">
          <span className="topbar-title">{title}</span>
        </header>
        <main className="page-content">{children}</main>
      </div>
    </div>
  );
}
