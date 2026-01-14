import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../common.css';

const API_URL = process.env.REACT_APP_API_URL;

function Verify2FAPage() {
    const [code, setCode] = useState('');
    const [error, setError] = useState(null);
    const [scratchCodes, setScratchCodes] = useState([]);
    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);

        const pendingToken = localStorage.getItem('pendingToken');
        if (!pendingToken) {
            setError('No pending authentication found');
            return;
        }

        try {
            const response = await fetch(`${API_URL}/google-2fa/login/verify`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${pendingToken}`
                },
                body: JSON.stringify({ code: parseInt(code) }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '2FA verification failed');
            }

            if (data.accessToken) {
                localStorage.setItem('authToken', data.accessToken);
                localStorage.removeItem('pendingToken');
                
                if (data.scratchCodes && data.scratchCodes.length > 0) {
                    setScratchCodes(data.scratchCodes);
                } else {
                    navigate('/');
                }
            } else {
                setError('Invalid 2FA code');
            }
        } catch (err) {
            console.error(err);
            setError(err.message || 'Something went wrong');
        }
    };

    if (scratchCodes.length > 0) {
        return (
            <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto', height: '100vh' }}>
                <h1>2FA Setup Complete</h1>
                <p>Save these backup codes in a safe place:</p>
                <div style={{ background: '#f0f0f0', padding: '20px', margin: '20px 0', border: '2px solid #222' }}>
                    {scratchCodes.map((code, idx) => (
                        <div key={idx} style={{ fontFamily: 'monospace', fontSize: '16px', margin: '5px 0' }}>
                            {code}
                        </div>
                    ))}
                </div>
                <button onClick={() => navigate('/')}>Continue</button>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto', height: '100vh' }}>
            <h1>Two-Factor Authentication</h1>
            <p>Enter the 6-digit code from your authenticator app</p>
            
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="code">Authentication Code:</label>
                    <input
                        id="code"
                        type="text"
                        value={code}
                        onChange={e => setCode(e.target.value)}
                        placeholder="000000"
                        maxLength="6"
                        required
                    />
                </div>
                
                {error && (
                    <div style={{ color: 'red', marginBottom: '12px' }}>
                        {error}
                    </div>
                )}
                
                <button type="submit">Verify</button>
            </form>
        </div>
    );
}

export default Verify2FAPage;
