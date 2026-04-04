import axios from 'axios';

// Create a custom axios instance pointing to your Spring Boot server
const api = axios.create({
    baseURL: 'http://localhost:8080', 
});

// The "Bouncer Protocol"
// This intercepts EVERY request before it leaves React and attaches the JWT wristband if we have one.
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token'); // We will save the token here later
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

export default api;