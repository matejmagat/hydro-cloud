import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../common.css';

const API_URL = process.env.REACT_APP_API_URL;

function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [fieldErrors, setFieldErrors] = useState({});
    const navigate = useNavigate();

    const validateForm = () => {
        const errors = {};
        if (!username.trim()) {
            errors.username = 'Username is required';
        } else if (username.length < 4) {
            errors.username = 'Username must be at least 4 characters';
        }

        if (!password) {
            errors.password = 'Password is required';
        } else if (password.length < 8) {
            errors.password = 'Password must be at least 8 characters';
        }

        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);
        setFieldErrors({});

        if (!validateForm()) {
            return;
        }

        try {
            const response = await fetch(`${API_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    password: password,
                }),
            });

            const contentType = response.headers.get('content-type') || '';

            if (response.ok) {
                const data = await response.json();
                console.log('Logged in:', data["accessToken"]);

                if (data.is2FAEnabled && data.pendingToken) {
                    localStorage.setItem('pendingToken', data.pendingToken);
                    navigate('/verify-2fa');
                } else if (data.accessToken) {
                    localStorage.setItem('authToken', data.accessToken);
                    navigate('/');
                }
            } else {
                let errorMessage = 'Login failed';
                
                if (contentType?.includes('application/json')) {
                    const data = await response.json();
                    
                    if (response.status === 401) {
                    errorMessage = 'Invalid username or password';
                    } else if (data.detail) {
                    errorMessage = data.detail;
                    } else if (data.message) {
                    errorMessage = data.message;
                    } else if (typeof data === 'object') {
                    errorMessage = Object.values(data)[0] || errorMessage;
                    }
                } else if (response.status === 401) {
                    errorMessage = 'Invalid username or password';
                }
                
                setError(errorMessage);
            }


        } catch (err) {
            console.error(err);
            setError(err.message || 'Something went wrong');
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto', height: '100vh' }}>
            <h1>Login</h1>
            <form onSubmit={handleSubmit} noValidate>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="username">Username:</label>
                    <input
                        id="username"
                        type="text"
                        value={username}
                        onChange={(e) => {
                            setUsername(e.target.value);
                            if (fieldErrors.username) {
                            setFieldErrors({ ...fieldErrors, username: null });
                            }
                        }}
                        style={{
                            borderColor: fieldErrors.username ? 'red' : undefined,
                        }}
                    />
                    {fieldErrors.username && (
                        <div style={{ color: 'red', fontSize: '13px', marginTop: '4px' }}>
                            {fieldErrors.username}
                        </div>
                    )}
                </div>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="password">Password:</label>
                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={(e) => {
                            setPassword(e.target.value);
                            if (fieldErrors.password) {
                            setFieldErrors({ ...fieldErrors, password: null });
                            }
                        }}
                        style={{
                            borderColor: fieldErrors.password ? 'red' : undefined,
                        }}
                    />
                    {fieldErrors.password && (
                        <div style={{ color: 'red', fontSize: '13px', marginTop: '4px' }}>
                            {fieldErrors.password}
                        </div>
                    )}
                </div>

                {error && <div className="error-message">{error}</div>}

                <button type="submit">Login</button>
            </form>
            <p style={{ marginTop: '12px' }}>
                Don't have an account?{' '}
                <Link to="/register">Register</Link>
            </p>
        </div>
    );
}

export default LoginPage;
