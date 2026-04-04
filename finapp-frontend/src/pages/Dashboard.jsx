import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { jwtDecode } from 'jwt-decode';
import '../styles/Dashboard.css'; // <-- IMPORTING OUR NEW CSS HERE!

function Dashboard() {
  const navigate = useNavigate();
  
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [myUserId, setMyUserId] = useState(null);
  const [myRole, setMyRole] = useState('');

  const [amount, setAmount] = useState('');
  const [type, setType] = useState('INCOME');
  const [category, setCategory] = useState('');
  const [date, setDate] = useState('');
  const [notes, setNotes] = useState('');

  // FILTER STATE
  const [filterType, setFilterType] = useState('');
  const [filterCategory, setFilterCategory] = useState('');

  const [editingRecord, setEditingRecord] = useState(null);

  const fetchMyRecords = useCallback(async (userId, currentType = '', currentCategory = '') => {
    try {
      // Build the URL with the filters attached!
      let url = `/api/records/user/${userId}?`;
      if (currentType) url += `type=${currentType}&`;
      if (currentCategory) url += `category=${currentCategory}`;

      const response = await api.get(url);
      setRecords(response.data);
    } catch (error) {
      console.error("Failed to fetch records:", error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    const decodedToken = jwtDecode(token);
    setMyUserId(decodedToken.userId);
    setMyRole(decodedToken.role);
    fetchMyRecords(decodedToken.userId);
  }, [navigate, fetchMyRecords]);

  const handleAddRecord = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/records', { amount: parseFloat(amount), type, category, date, notes, user: { id: myUserId } });
      setAmount(''); setCategory(''); setDate(''); setNotes('');
      fetchMyRecords(myUserId);
    } catch (error) {
      alert("Error saving record.");
    }
  };

  const handleDelete = async (recordId) => {
    if (!window.confirm("Are you sure you want to delete this record?")) return;
    try {
      await api.delete(`/api/records/${recordId}`);
      fetchMyRecords(myUserId); 
    } catch (error) {
      alert("Error deleting record.");
    }
  };

  const handleUpdateRecord = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/api/records/${editingRecord.id}`, {
        amount: parseFloat(editingRecord.amount), type: editingRecord.type, category: editingRecord.category, date: editingRecord.date, notes: editingRecord.notes, user: { id: myUserId } 
      });
      setEditingRecord(null); 
      fetchMyRecords(myUserId); 
    } catch (error) {
      alert("Error updating record.");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="dashboard-wrapper">
      
      {/* HEADER */}
      <header className="dashboard-header">
        <div>
          <h1 className="header-title">FinApp Dashboard</h1>
          <span className="header-role">Role: <strong>{myRole}</strong></span>
        </div>
        <div className="header-actions">
          {myRole === 'ADMIN' && (
            <button onClick={() => navigate('/users')} className="btn btn-primary">
              Manage Users
            </button>
          )}
          <button onClick={handleLogout} className="btn btn-header-outline">
            Log Out
          </button>
        </div>
      </header>

      {/* MAIN CONTENT */}
      <main className="dashboard-main">
        
        {/* ADD DATA FORM */}
        {myRole !== 'ANALYST' && (
          <div className="card form-card">
            <h3 className="card-title">Add New Record</h3>
            <form onSubmit={handleAddRecord} className="styled-form">
              <input type="number" step="0.01" placeholder="Amount (e.g. 50.00)" value={amount} onChange={e => setAmount(e.target.value)} required className="form-input" />
              <select value={type} onChange={e => setType(e.target.value)} className="form-input">
                <option value="INCOME">Income</option>
                <option value="EXPENSE">Expense</option>
              </select>
              <input type="text" placeholder="Category (e.g. Groceries)" value={category} onChange={e => setCategory(e.target.value)} required className="form-input" />
              <input type="date" value={date} onChange={e => setDate(e.target.value)} required className="form-input" />
              <textarea placeholder="Notes (Optional)" value={notes} onChange={e => setNotes(e.target.value)} className="form-input" style={{ minHeight: '80px' }} />
              <button type="submit" className="btn btn-success">Save Record</button>
            </form>
          </div>
        )}

        {/* DATA TABLE */}
        <div className="card table-card">
          <h3 className="card-title">Your Transaction History</h3>
          {/* THE NEW FILTER BAR */}
          <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', paddingBottom: '1.5rem', borderBottom: '1px solid #e2e8f0' }}>
            <select value={filterType} onChange={(e) => { setFilterType(e.target.value); fetchMyRecords(myUserId, e.target.value, filterCategory); }} className="form-input" style={{ width: '150px' }}>
              <option value="">All Types</option>
              <option value="INCOME">Income Only</option>
              <option value="EXPENSE">Expense Only</option>
            </select>
            
            <input 
              type="text" 
              placeholder="Search by category..." 
              value={filterCategory} 
              onChange={(e) => { setFilterCategory(e.target.value); fetchMyRecords(myUserId, filterType, e.target.value); }} 
              className="form-input" 
              style={{ flex: 1 }} 
            />
            
            <button onClick={() => { setFilterType(''); setFilterCategory(''); fetchMyRecords(myUserId, '', ''); }} className="btn btn-outline">
              Clear Filters
            </button>
          </div>
          {loading ? <p>Loading your data...</p> : records.length === 0 ? <p style={{ color: '#94a3b8' }}>No records found.</p> : (
            <table className="styled-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Category</th>
                  <th>Amount</th>
                  {myRole === 'ADMIN' && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {records.map((record) => (
                  <tr key={record.id}>
                    <td>{record.date}</td>
                    <td>
                      <span className={`badge ${record.type === 'INCOME' ? 'badge-income' : 'badge-expense'}`}>
                        {record.type}
                      </span>
                    </td>
                    <td>{record.category}</td>
                    <td style={{ fontWeight: 'bold' }}>${record.amount.toFixed(2)}</td>
                    
                    {myRole === 'ADMIN' && (
                      <td style={{ display: 'flex', gap: '0.5rem' }}>
                        <button onClick={() => setEditingRecord(record)} className="btn btn-warning">Edit</button>
                        <button onClick={() => handleDelete(record.id)} className="btn btn-danger">Delete</button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* EDIT MODAL */}
        {editingRecord && (
          <div className="modal-overlay">
            <div className="modal-content">
              <h3 className="card-title">Edit Record</h3>
              <form onSubmit={handleUpdateRecord} className="styled-form">
                <input type="number" step="0.01" value={editingRecord.amount} onChange={e => setEditingRecord({...editingRecord, amount: e.target.value})} required className="form-input" />
                <select value={editingRecord.type} onChange={e => setEditingRecord({...editingRecord, type: e.target.value})} className="form-input">
                  <option value="INCOME">Income</option>
                  <option value="EXPENSE">Expense</option>
                </select>
                <input type="text" value={editingRecord.category} onChange={e => setEditingRecord({...editingRecord, category: e.target.value})} required className="form-input" />
                <input type="date" value={editingRecord.date} onChange={e => setEditingRecord({...editingRecord, date: e.target.value})} required className="form-input" />
                <textarea value={editingRecord.notes || ''} onChange={e => setEditingRecord({...editingRecord, notes: e.target.value})} className="form-input" style={{ minHeight: '80px' }} />
                
                <div className="modal-actions">
                  <button type="submit" className="btn btn-primary">Save Changes</button>
                  <button type="button" onClick={() => setEditingRecord(null)} className="btn btn-outline">Cancel</button>
                </div>
              </form>
            </div>
          </div>
        )}

      </main>
    </div>
  );
}

export default Dashboard;