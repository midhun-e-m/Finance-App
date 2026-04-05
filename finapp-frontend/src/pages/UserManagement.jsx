import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { jwtDecode } from 'jwt-decode';
import '../styles/UserManagement.css'; // <-- Import our new CSS!

function UserManagement() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  // Form State for new users
  const [newEmail, setNewEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newRole, setNewRole] = useState('VIEWER');

  // 1. Security Check & Fetch Data
  const fetchUsers = useCallback(async () => {
    try {
      const response = await api.get('/api/users');
      setUsers(response.data);
    } catch (error) {
      console.error("Failed to fetch users:", error);
      if (error.response?.status === 403) {
        alert("You do not have permission to view this page.");
        navigate('/dashboard');
      }
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    const decodedToken = jwtDecode(token);
    if (decodedToken.role !== 'ADMIN') {
      navigate('/dashboard'); // Kick out non-admins instantly
      return;
    }
    fetchUsers();
  }, [navigate, fetchUsers]);

  // 2. Create a New Employee Account
  const handleCreateUser = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/users', {
        email: newEmail,
        password: newPassword,
        role: newRole,
        isActive: true
      });
      setNewEmail(''); setNewPassword(''); setNewRole('VIEWER');
      fetchUsers(); // Refresh the table
    } catch (error) {
      console.error("Failed to create user:", error);
      alert("Error creating user.");
    }
  };

  // 3. Update an Employee's Role
  const handleRoleChange = async (userId, newRoleValue, currentIsActive) => {
    try {
      await api.put(`/api/users/${userId}`, {
        role: newRoleValue,
        isActive: currentIsActive
      });
      fetchUsers(); // Refresh to show the new role
    } catch (error) {
      console.error("Failed to update role:", error);
      alert("Error updating user.");
    }
  };

  return (
    <div className="um-wrapper">
      
      {/* HEADER */}
      <header className="um-header">
        <h1>Admin Control Center</h1>
        <button onClick={() => navigate('/dashboard')} className="btn btn-header-outline">
          Back to Dashboard
        </button>
      </header>

      <main className="um-main">
        
        {/* LEFT: Create User Form */}
        <div className="card form-card">
          <h3 className="card-title">Add Employee</h3>
          <form onSubmit={handleCreateUser} className="styled-form">
            <input 
              type="email" 
              placeholder="Employee Email" 
              value={newEmail} 
              onChange={e => setNewEmail(e.target.value)} 
              required 
              className="form-input" 
            />
            <input 
              type="password" 
              placeholder="Temporary Password" 
              value={newPassword} 
              onChange={e => setNewPassword(e.target.value)} 
              required 
              className="form-input" 
            />
            <select 
              value={newRole} 
              onChange={e => setNewRole(e.target.value)} 
              className="form-input"
            >
              <option value="VIEWER">Viewer</option>
              <option value="ANALYST">Analyst</option>
              <option value="ADMIN">Admin</option>
            </select>
            <button type="submit" className="btn btn-success">
              Create Account
            </button>
          </form>
        </div>

        {/* RIGHT: User Table */}
        <div className="card table-card">
          <h3 className="card-title">Company Directory</h3>
          {loading ? <p>Loading users...</p> : (
            <table className="styled-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td style={{ fontSize: '0.8rem', color: '#94a3b8', fontFamily: 'monospace' }}>
                      {user.id.substring(0,8)}...
                    </td>
                    <td style={{ fontWeight: 'bold' }}>{user.email}</td>
                    <td>
                      <select 
                        value={user.role} 
                        onChange={(e) => handleRoleChange(user.id, e.target.value, user.isActive)}
                        className="table-select"
                      >
                        <option value="VIEWER">Viewer</option>
                        <option value="ANALYST">Analyst</option>
                        <option value="ADMIN">Admin</option>
                      </select>
                    </td>
                    <td>
                      <span className={`status-badge ${user.isActive ? 'status-active' : 'status-disabled'}`}>
                        {user.isActive ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

      </main>
    </div>
  );
}

export default UserManagement;