import { Navigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const token = localStorage.getItem('token');

  // 1. No token? Kick to login.
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  try {
    const decodedToken = jwtDecode(token);

    // 2. NEW: Check if the token has expired
    // The 'exp' claim is in seconds, so we multiply by 1000 to compare with Date.now()
    const isExpired = decodedToken.exp * 1000 < Date.now();
    if (isExpired) {
      localStorage.removeItem('token'); // Clean up the dead token
      return <Navigate to="/login" replace />;
    }

    // 3. Admin Check
    if (requireAdmin && decodedToken.role !== 'ADMIN') {
      return <Navigate to="/dashboard" replace />;
    }
  } catch (error) {
    // If the token is mangled, clear it and kick to login
    localStorage.removeItem('token');
    return <Navigate to="/login" replace />;
  }

  // 4. All checks passed, render the page
  return children;
};

export default ProtectedRoute;