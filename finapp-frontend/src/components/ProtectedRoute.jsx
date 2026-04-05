import { Navigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const token = localStorage.getItem('token');

  // 1. If there's no token at all, kick them instantly to login
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // 2. If this specific route requires Admin privileges, check their role
  if (requireAdmin) {
    try {
      const decodedToken = jwtDecode(token);
      if (decodedToken.role !== 'ADMIN') {
        // Not an admin? Send them back to their standard dashboard
        return <Navigate to="/dashboard" replace />;
      }
    } catch (error) {
      // If the token is mangled or invalid, kick to login
      return <Navigate to="/login" replace />;
    }
  }

  // 3. If they pass the checks, render the page!
  return children;
};

export default ProtectedRoute;