import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { jwtDecode } from 'jwt-decode';

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
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', backgroundColor: '#f4f4f9', fontFamily: 'sans-serif' }}>
      
      {/* HEADER */}
      <header style={{ backgroundColor: '#343a40', color: 'white', padding: '1rem 2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ margin: 0, fontSize: '1.5rem' }}>Admin Control Center</h1>
        <button onClick={() => navigate('/dashboard')} style={{ backgroundColor: '#0056b3', color: 'white', border: 'none', padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer' }}>
          Back to Dashboard
        </button>
      </header>

      <main style={{ padding: '2rem', display: 'flex', gap: '2rem', flex: 1, overflow: 'hidden' }}>
        
        {/* LEFT: Create User Form */}
        <div style={{ flex: '0 0 300px', backgroundColor: 'white', padding: '1.5rem', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', height: 'fit-content' }}>
          <h3 style={{ marginTop: 0 }}>Add Employee</h3>
          <form onSubmit={handleCreateUser} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <input type="email" placeholder="Employee Email" value={newEmail} onChange={e => setNewEmail(e.target.value)} required style={{ padding: '0.5rem' }} />
            <input type="password" placeholder="Temporary Password" value={newPassword} onChange={e => setNewPassword(e.target.value)} required style={{ padding: '0.5rem' }} />
            <select value={newRole} onChange={e => setNewRole(e.target.value)} style={{ padding: '0.5rem' }}>
              <option value="VIEWER">Viewer</option>
              <option value="ANALYST">Analyst</option>
              <option value="ADMIN">Admin</option>
            </select>
            <button type="submit" style={{ backgroundColor: '#28a745', color: 'white', padding: '0.75rem', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
              Create Account
            </button>
          </form>
        </div>

        {/* RIGHT: User Table */}
        <div style={{ flex: 1, backgroundColor: 'white', padding: '1.5rem', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', overflowY: 'auto' }}>
          <h3 style={{ marginTop: 0 }}>Company Directory</h3>
          {loading ? <p>Loading users...</p> : (
            <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: '#f8f9fa', borderBottom: '2px solid #dee2e6' }}>
                  <th style={{ padding: '12px' }}>ID</th>
                  <th style={{ padding: '12px' }}>Email</th>
                  <th style={{ padding: '12px' }}>Role</th>
                  <th style={{ padding: '12px' }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '12px', fontSize: '0.8rem', color: '#666' }}>{user.id.substring(0,8)}...</td>
                    <td style={{ padding: '12px', fontWeight: 'bold' }}>{user.email}</td>
                    <td style={{ padding: '12px' }}>
                      {/* Dropdown to instantly change roles! */}
                      <select 
                        value={user.role} 
                        onChange={(e) => handleRoleChange(user.id, e.target.value, user.isActive)}
                        style={{ padding: '4px', borderRadius: '4px' }}
                      >
                        <option value="VIEWER">Viewer</option>
                        <option value="ANALYST">Analyst</option>
                        <option value="ADMIN">Admin</option>
                      </select>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ color: user.isActive ? 'green' : 'red', fontWeight: 'bold' }}>
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