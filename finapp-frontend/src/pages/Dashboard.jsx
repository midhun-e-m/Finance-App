import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { jwtDecode } from 'jwt-decode';
import '../styles/Dashboard.css';

function Dashboard() {
  const navigate = useNavigate();
  
  // -- APP STATE --
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [myUserId, setMyUserId] = useState(null);
  const [myRole, setMyRole] = useState('');

  // -- SUMMARY STATE --
  const [summary, setSummary] = useState({ totalIncome: 0, totalExpense: 0, netBalance: 0 });

  // -- FORM STATE --
  const [amount, setAmount] = useState('');
  const [type, setType] = useState('INCOME');
  const [category, setCategory] = useState('');
  const [date, setDate] = useState('');
  const [notes, setNotes] = useState('');

  // -- FILTER & EDIT STATE --
  const [filterType, setFilterType] = useState('');
  const [filterCategory, setFilterCategory] = useState('');
  const [editingRecord, setEditingRecord] = useState(null);

  // 1. Fetch Summary Data (For the Visual Cards)
  const fetchSummary = useCallback(async (userId, role) => {
    try {
      const url = (role === 'ADMIN' || role === 'ANALYST') 
        ? `/api/records/all/summary` 
        : `/api/records/user/${userId}/summary`;
      
      const response = await api.get(url);
      setSummary(response.data);
    } catch (error) {
      console.error("Failed to fetch summary:", error);
    }
  }, []);

  // 2. Fetch Table Records
  const fetchMyRecords = useCallback(async (userId, role, currentType = '', currentCategory = '') => {
    try {
      let url = '';
      if (role === 'ADMIN' || role === 'ANALYST') {
        url = `/api/records/all?`;
      } else {
        url = `/api/records/user/${userId}?`;
      }

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

  // 3. Page Load Initialization
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    const decodedToken = jwtDecode(token);
    setMyUserId(decodedToken.userId);
    setMyRole(decodedToken.role);
    
    fetchMyRecords(decodedToken.userId, decodedToken.role);
    fetchSummary(decodedToken.userId, decodedToken.role);
  }, [navigate, fetchMyRecords, fetchSummary]);

  // 4. CRUD Operations
  const handleAddRecord = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/records', { amount: parseFloat(amount), type, category, date, notes, user: { id: myUserId } });
      setAmount(''); setCategory(''); setDate(''); setNotes('');
      fetchMyRecords(myUserId, myRole, filterType, filterCategory);
      fetchSummary(myUserId, myRole); // Refresh visual cards
    } catch (error) {
      alert("Error saving record.");
    }
  };

  const handleDelete = async (recordId) => {
    if (!window.confirm("Are you sure you want to delete this record?")) return;
    try {
      await api.delete(`/api/records/${recordId}`);
      fetchMyRecords(myUserId, myRole, filterType, filterCategory); 
      fetchSummary(myUserId, myRole); // Refresh visual cards
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
      fetchMyRecords(myUserId, myRole, filterType, filterCategory); 
      fetchSummary(myUserId, myRole); // Refresh visual cards
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
      
      {/* --- HEADER --- */}
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

      {/* --- MAIN CONTENT --- */}
      <main className="dashboard-main">
        
        {/* ADD DATA FORM (Hidden for Analysts) */}
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

        {/* RIGHT COLUMN AREA */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '1.5rem', overflow: 'hidden' }}>
          
          {/* VISUAL STAT CARDS */}
          <div style={{ display: 'flex', gap: '1.5rem', width: '100%' }}>
            <div className="card" style={{ flex: 1, borderLeft: '4px solid #10b981' }}>
              <p style={{ margin: 0, color: '#64748b', fontSize: '0.85rem', fontWeight: 'bold', textTransform: 'uppercase' }}>Total Income</p>
              <h2 style={{ margin: '0.5rem 0 0 0', color: '#10b981' }}>${summary.totalIncome.toFixed(2)}</h2>
            </div>
            <div className="card" style={{ flex: 1, borderLeft: '4px solid #ef4444' }}>
              <p style={{ margin: 0, color: '#64748b', fontSize: '0.85rem', fontWeight: 'bold', textTransform: 'uppercase' }}>Total Expenses</p>
              <h2 style={{ margin: '0.5rem 0 0 0', color: '#ef4444' }}>${summary.totalExpense.toFixed(2)}</h2>
            </div>
            <div className="card" style={{ flex: 1, borderLeft: '4px solid #3b82f6' }}>
              <p style={{ margin: 0, color: '#64748b', fontSize: '0.85rem', fontWeight: 'bold', textTransform: 'uppercase' }}>Net Balance</p>
              <h2 style={{ margin: '0.5rem 0 0 0', color: '#0f172a' }}>${summary.netBalance.toFixed(2)}</h2>
            </div>
          </div>

          {/* DATA TABLE SECTION */}
          <div className="card table-card">
            <h3 className="card-title">Transaction History</h3>
            
            {/* FILTER BAR */}
            <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', paddingBottom: '1.5rem', borderBottom: '1px solid #e2e8f0' }}>
              <select 
                value={filterType} 
                onChange={(e) => { 
                  setFilterType(e.target.value); 
                  fetchMyRecords(myUserId, myRole, e.target.value, filterCategory); 
                }} 
                className="form-input" style={{ width: '150px' }}
              >
                <option value="">All Types</option>
                <option value="INCOME">Income Only</option>
                <option value="EXPENSE">Expense Only</option>
              </select>
              
              <input 
                type="text" 
                placeholder="Search by category..." 
                value={filterCategory} 
                onChange={(e) => { 
                  setFilterCategory(e.target.value); 
                  fetchMyRecords(myUserId, myRole, filterType, e.target.value); 
                }} 
                className="form-input" 
                style={{ flex: 1 }} 
              />
              
              <button 
                onClick={() => { 
                  setFilterType(''); 
                  setFilterCategory(''); 
                  fetchMyRecords(myUserId, myRole, '', ''); 
                }} 
                className="btn btn-outline"
              >
                Clear Filters
              </button>
            </div>

            {/* TABLE */}
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
        </div>

        {/* --- EDIT MODAL --- */}
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