import React, { useState } from 'react';

function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (event) => {
        event.preventDefault();
        alert(`Logging in with\nUsername: ${username}\nPassword: ${password}`);
        // Add real login logic here
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto', height: '100vh' }}>
            <h1>Login</h1>
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="username" style={{ display: 'block', marginBottom: '4px' }}>Username:</label>
                    <input
                        id="username"
                        type="text"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                        style={{ width: '100%', padding: '8px' }}
                        required
                    />
                </div>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="password" style={{ display: 'block', marginBottom: '4px' }}>Password:</label>
                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        style={{ width: '100%', padding: '8px' }}
                        required
                    />
                </div>
                <button
                    style={{
                        marginTop: '12px',
                        width: '100%',
                        padding: '10px 0',
                        background: '#fff',
                        fontWeight: 'bold',
                        fontSize: '1em',
                        border: '4px solid #222',
                        borderRadius: 0,
                        cursor: 'pointer'
                    }}
                >
                    Login
                </button>
            </form>
        </div>
    );
}

export default LoginPage;
