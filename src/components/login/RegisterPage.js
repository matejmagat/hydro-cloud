import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import '../common.css';

const API_URL = process.env.REACT_APP_API_URL;

function RegisterPage() {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);

        try {
            const response = await fetch(`${API_URL}/auth/sign-up`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    firstName: firstName,
                    lastName: lastName,
                    username: username,
                    email: email,
                    password: password,
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Registration failed');
            }

            console.log('Registered successfully:', data);
            localStorage.setItem('authToken', data["accessToken"]);
        } catch (err) {
            console.error(err);
            setError(err.message || 'Something went wrong');
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto', height: '100vh' }}>
            <h1>Register</h1>
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="firstName">First name:</label>
                    <input
                        id="firstName"
                        type="text"
                        value={firstName}
                        onChange={e => setFirstName(e.target.value)}
                        required
                    />
                </div>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="lastName">Last name:</label>
                    <input
                        id="lastName"
                        type="text"
                        value={lastName}
                        onChange={e => setLastName(e.target.value)}
                        required
                    />
                </div>
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
                    <label htmlFor="email">Email:</label>
                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
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

                <button type="submit">Sign up</button>
            </form>
            <p style={{ marginTop: '12px' }}>
                Already have an account?{' '}
                <Link to="/login">Login</Link>
            </p>
        </div>
    );
}

export default RegisterPage;
