-- Society Maintenance Tracker - Sample Data
-- Passwords are BCrypt hashed. Plain text: Admin@123 / Resident@123
USE society_tracker;

INSERT INTO users (name, email, password, phone, apartment_number, role) VALUES
('Admin User',    'admin@society.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y', '9000000001', 'A0',   'ADMIN'),
('Ravi Kumar',    'ravi@resident.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y', '9000000002', 'B101', 'RESIDENT'),
('Priya Sharma',  'priya@resident.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y', '9000000003', 'B102', 'RESIDENT'),
('Arjun Mehta',   'arjun@resident.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7y', '9000000004', 'C201', 'RESIDENT');

INSERT INTO complaints (resident_id, category, title, description, status, priority, is_overdue) VALUES
(2, 'PLUMBING',    'Water leakage in bathroom',      'Pipe under sink is leaking continuously.',          'OPEN',        'HIGH',   true),
(2, 'ELECTRICAL',  'Power outage in Block B',        'No electricity since yesterday evening.',           'IN_PROGRESS', 'HIGH',   false),
(3, 'LIFT',        'Lift not working',               'Lift on floor 3 is stuck and not responding.',     'OPEN',        'MEDIUM', false),
(3, 'CLEANING',    'Common area not cleaned',        'Lobby has not been cleaned for 3 days.',           'RESOLVED',    'LOW',    false),
(4, 'SECURITY',    'Main gate lock broken',          'The main gate lock is broken since last week.',    'OPEN',        'HIGH',   true),
(4, 'WATER',       'No water supply in morning',     'Water supply cuts off every morning at 7 AM.',     'IN_PROGRESS', 'MEDIUM', false);

INSERT INTO complaint_status_history (complaint_id, old_status, new_status, changed_by, note) VALUES
(1, NULL,          'OPEN',        2, 'Complaint submitted'),
(2, NULL,          'OPEN',        2, 'Complaint submitted'),
(2, 'OPEN',        'IN_PROGRESS', 1, 'Electrician assigned, will visit tomorrow'),
(3, NULL,          'OPEN',        3, 'Complaint submitted'),
(4, NULL,          'OPEN',        3, 'Complaint submitted'),
(4, 'OPEN',        'IN_PROGRESS', 1, 'Cleaning crew notified'),
(4, 'IN_PROGRESS', 'RESOLVED',   1, 'Area cleaned and sanitized'),
(5, NULL,          'OPEN',        4, 'Complaint submitted'),
(6, NULL,          'OPEN',        4, 'Complaint submitted'),
(6, 'OPEN',        'IN_PROGRESS', 1, 'Plumber scheduled for tomorrow morning');

INSERT INTO notices (title, content, important, created_by) VALUES
('Water Supply Interruption',  'Water supply will be interrupted on Sunday 6 AM - 10 AM for tank cleaning. Please store water in advance.', true,  1),
('Society Meeting',            'Monthly society meeting scheduled for 15th at 6 PM in the community hall. All residents are requested to attend.', false, 1),
('New Security Guard',         'We have appointed a new security guard. Please cooperate and show your ID cards at the gate.', false, 1);
