import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import '../common.css'

const API_URL = process.env.REACT_APP_API_URL;

function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);

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

            // If your API returns JSON:
            const data = await response.json();

            if (!response.ok) {
                // Adjust according to your API error shape
                throw new Error(data.message || 'Login failed');
                // alert(data.message || 'Login failed')
            }

            // Handle successful login here
            console.log('Logged in:', data["accessToken"]);
            localStorage.setItem('authToken', data["accessToken"]);
        } catch (err) {
            console.error(err);
            setError(err.message || 'Something went wrong');
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto', height: '100vh' }}>
            <h1>Login</h1>
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="username">Username:</label>
                    <input
                        id="username"
                        type="text"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="password">Password:</label>
                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        required
                    />
                </div>

                {error && (
                    <div style={{ color: 'red', marginBottom: '12px' }}>
                        {error}
                    </div>
                )}

                <button type="submit">Login</button>
            </form>
            <p style={{ marginTop: '12px' }}>
                Don&apos;t have an account?{' '}
                <Link to="/register">Register</Link>
            </p>
        </div>
    );
}

export default LoginPage;
