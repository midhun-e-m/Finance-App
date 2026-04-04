import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard'; 
import UserManagement from './pages/UserManagement';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        
        {/* Replace the old div with our new official Dashboard component */}
        <Route path="/dashboard" element={<Dashboard />} />

        <Route path="/users" element={<UserManagement />} />
        
      </Routes>
    </BrowserRouter>
  );
}

export default App;