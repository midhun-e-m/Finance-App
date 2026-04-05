import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import '../styles/Login.css'; // <-- Bringing in the beautiful CSS

function Login() {
  // --- 1. THE LOGIC (100% Untouched) ---
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault(); 
    setError(''); 

    try {
      // Send credentials to Spring Boot
      const response = await api.post('/api/auth/login', { email, password });
      
      // Grab and save the VIP wristband
      const token = response.data.token;
      localStorage.setItem('token', token);
      
      // Send user to the dashboard
      navigate('/dashboard');
      
    } catch (err) {
      console.error(err);
      setError('Invalid email or password. Please try again.');
    }
  };

  // --- 2. THE VISUALS (Upgraded with classNames) ---
  return (
    <div className="login-container">
      <div className="login-card">
        
        <div className="login-header">
          <h2 className="login-title">FinApp</h2>
          <p className="login-subtitle">Sign in to your dashboard</p>
        </div>
        
        {error && <div className="error-message">{error}</div>}
        
        <form onSubmit={handleLogin} className="login-form">
          <div className="input-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              placeholder="example@gmail.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="login-input"
            />
          </div>
          
          <div className="input-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="login-input"
            />
          </div>
          
          <button type="submit" className="login-btn">
            Sign In
          </button>
        </form>

      </div>
    </div>
  );
}

export default Login;