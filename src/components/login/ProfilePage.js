import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../common.css';

const API_URL = process.env.REACT_APP_API_URL;

function ProfilePage() {
    // --- New State for User Profile ---
    const [userInfo, setUserInfo] = useState({
        id: 0,
        firstName: '',
        lastName: '',
        username: '',
        email: ''
    });
    const [profileMessage, setProfileMessage] = useState(null);
    const [profileError, setProfileError] = useState(null);

    // --- Existing 2FA State ---
    const [qrCode, setQrCode] = useState(null);
    const [setupCode, setSetupCode] = useState('');
    const [error, setError] = useState(null);
    const [fieldErrors, setFieldErrors] = useState({});
    const [message, setMessage] = useState(null);
    const [is2FAEnabled, setIs2FAEnabled] = useState(false);
    const [showSetup, setShowSetup] = useState(false);

    const navigate = useNavigate();

    // --- New Effect: Fetch User Info ---
    useEffect(() => {
        const token = localStorage.getItem('authToken');
        if (!token) return; // Navigation is handled by the other useEffect

        const fetchUserInfo = async () => {
            try {
                const response = await fetch(`${API_URL}/user/me`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    // We only extract the fields we need for editing, ignoring is2FAEnabled here
                    setUserInfo({
                        id: data.id,
                        firstName: data.firstName || '',
                        lastName: data.lastName || '',
                        username: data.username || '',
                        email: data.email || ''
                    });
                }
            } catch (err) {
                console.error('Failed to fetch user info', err);
                setProfileError('Failed to load user information.');
            }
        };

        fetchUserInfo();
    }, []);

    // --- Existing Effect: Fetch 2FA Status ---
    useEffect(() => {
        const token = localStorage.getItem('authToken');
        if (!token) {
            navigate('/login');
            return;
        }

        const fetchStatus = async () => {
            try {
                const response = await fetch(`${API_URL}/google-2fa/status`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    setIs2FAEnabled(data.enabled);
                }
            } catch (err) {
                console.error('Failed to fetch 2FA status', err);
            }
        };

        fetchStatus();
    }, [navigate]);

    const validateForm = () => {
        const errors = {};
        
        if (!userInfo.firstName.trim()) {
            errors.firstName = "First name is required";
        } else if (userInfo.firstName.length < 1 || userInfo.firstName.length > 50) {
            errors.firstName = "First name must be between 1 and 50 characters";
        }
        
        if (!userInfo.lastName.trim()) {
            errors.lastName = "Last name is required";
        } else if (userInfo.lastName.length < 1 || userInfo.lastName.length > 50) {
            errors.lastName = "Last name must be between 1 and 50 characters";
        }
        
        if (!userInfo.username.trim()) {
            errors.username = "Username is required";
        } else if (userInfo.username.length < 4 || userInfo.username.length > 30) {
            errors.username = "Username must be between 4 and 30 characters";
        }
        
        if (!userInfo.email.trim()) {
            errors.email = "Email is required";
        } else if (!/\S+@\S+\.\S+/.test(userInfo.email)) {
            errors.email = "Email must be valid";
        }
        
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // --- New Handler: Update User Info ---
    const handleUpdateProfile = async (e) => {
        e.preventDefault();
        setProfileMessage(null);
        setProfileError(null);
        setFieldErrors({});

        if (!validateForm()) {
            return;
        }

        const token = localStorage.getItem('authToken');

        try {
            const response = await fetch(`${API_URL}/user`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(userInfo),
            });

            if (response.ok) {
                setProfileMessage('Profile updated successfully!');
            } else {
                let errorMessage = "Failed to update profile";
                
                try {
                    const data = await response.json();
                    console.log("Backend error:", JSON.stringify(data, null, 2));
                    
                    if (data.detail) {
                        const match = data.detail.match(/"([^"]+)"/);
                        errorMessage = match ? match[1] : data.detail;
                    } else if (data.errors && typeof data.errors === 'object') {
                        const firstError = Object.values(data.errors)[0];
                        errorMessage = firstError || "Validation failed";
                        setFieldErrors(data.errors);
                    } else if (data.message) {
                        errorMessage = data.message;
                    } else if (data.title) {
                        errorMessage = data.title;
                    }
                } catch (parseError) {
                    console.error("Could not parse error response:", parseError);
                }
                
                setProfileError(errorMessage);
            }
        } catch (err) {
            console.error(err);
            setProfileError('Could not update profile. Please try again.');
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setUserInfo(prev => ({
            ...prev,
            [name]: value
        }));

        if (fieldErrors[name]) {
            setFieldErrors(prev => ({
                ...prev,
                [name]: null
        }));
    }
    };

    // --- Existing Handlers ---
    const handleLogout = () => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('pendingToken');
        navigate('/login');
    };

    const handleActivate2FA = async () => {
        const token = localStorage.getItem('authToken');
        setError(null);

        try {
            const response = await fetch(`${API_URL}/google-2fa/activate`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to activate 2FA');
            }

            setQrCode(data.qrCodeBase64);
            setShowSetup(true);
        } catch (err) {
            console.error(err);
            setError(err.message || 'Something went wrong');
        }
    };

    const handleVerifySetup = async (event) => {
        event.preventDefault();
        setError(null);
        const token = localStorage.getItem('authToken');

        try {
            const response = await fetch(`${API_URL}/google-2fa/verify`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ code: parseInt(setupCode) }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '2FA verification failed');
            }

            if (data.scratchCodes && data.scratchCodes.length > 0) {
                setMessage('2FA enabled successfully! Backup codes: ' + data.scratchCodes.join(', '));
                setIs2FAEnabled(true);
                setShowSetup(false);
                setError(null);
                setSetupCode('');
            } else {
                setError('Invalid verification code. Please try again.');
            }
        } catch (err) {
            console.error(err);
            setError(err.message || 'Invalid code');
        }
    };

    const handleDisable2FA = async () => {
        const token = localStorage.getItem('authToken');

        try {
            const response = await fetch(`${API_URL}/google-2fa/disable`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
            });

            if (!response.ok) {
                throw new Error('Failed to disable 2FA');
            }

            setMessage('2FA disabled successfully');
            setIs2FAEnabled(false);
            setQrCode(null);
            setShowSetup(false);
        } catch (err) {
            console.error(err);
            setError(err.message || 'Something went wrong');
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto' }}>
            <h1>Profile Settings</h1>

            {/* --- New Profile Form Section --- */}
            <section style={{ marginBottom: '40px' }}>
                <h2>User Information</h2>
                {profileMessage && (
                    <div style={{ color: 'green', marginBottom: '15px' }}>{profileMessage}</div>
                )}

                <form onSubmit={handleUpdateProfile} noValidate>
                    <div style={{ marginBottom: '12px' }}>
                        <label htmlFor="firstName" style={{ display: 'block', marginBottom: '5px' }}>First Name</label>
                        <input
                            id="firstName"
                            name="firstName"
                            type="text"
                            value={userInfo.firstName}
                            onChange={handleInputChange}
                            style={{ borderColor: fieldErrors.firstName ? "red" : undefined }}
                        />
                        {fieldErrors.firstName && (
                            <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                                {fieldErrors.firstName}
                            </div>
                        )}
                    </div>
                    <div style={{ marginBottom: '12px' }}>
                        <label htmlFor="lastName" style={{ display: 'block', marginBottom: '5px' }}>Last Name</label>
                        <input
                            id="lastName"
                            name="lastName"
                            type="text"
                            value={userInfo.lastName}
                            onChange={handleInputChange}
                            style={{ borderColor: fieldErrors.lastName ? "red" : undefined }}
                        />
                        {fieldErrors.lastName && (
                            <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                                {fieldErrors.lastName}
                            </div>
                        )}
                    </div>
                    <div style={{ marginBottom: '12px' }}>
                        <label htmlFor="username" style={{ display: 'block', marginBottom: '5px' }}>Username</label>
                        <input
                            id="username"
                            name="username"
                            type="text"
                            value={userInfo.username}
                            onChange={handleInputChange}
                            style={{ borderColor: fieldErrors.username ? "red" : undefined }}
                        />
                        {fieldErrors.username && (
                            <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                                {fieldErrors.username}
                            </div>
                        )}
                    </div>
                    <div style={{ marginBottom: '12px' }}>
                        <label htmlFor="email" style={{ display: 'block', marginBottom: '5px' }}>Email</label>
                        <input
                            id="email"
                            name="email"
                            type="email"
                            value={userInfo.email}
                            onChange={handleInputChange}
                            style={{ borderColor: fieldErrors.email ? "red" : undefined }}
                        />
                        {fieldErrors.email && (
                            <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                                {fieldErrors.email}
                            </div>
                        )}
                    </div>

                    {profileError && (
                        <div className="error-message">{profileError}</div>
                    )}

                    <button type="submit">Update Profile</button>
                </form>
            </section>

            <hr style={{ margin: '40px 0', border: '1px solid #ccc' }} />

            {/* --- Existing 2FA Section --- */}

            {message && (
                <div style={{ color: 'green', marginBottom: '15px' }}>
                    {message}
                </div>
            )}

            <h2>Two-Factor Authentication</h2>
            <p>Status: {is2FAEnabled ? 'Enabled ✓' : 'Disabled'}</p>

            {!showSetup && !is2FAEnabled && (
                <button
                    onClick={handleActivate2FA}
                    className="submit-button"
                >
                    Enable 2FA
                </button>
            )}

            {!showSetup && is2FAEnabled && (
                <button
                    onClick={handleDisable2FA}
                    className="submit-button"
                >
                    Disable 2FA
                </button>
            )}

            {showSetup && qrCode && (
                <div style={{ marginTop: '20px' }}>
                    <h3>Scan QR Code</h3>
                    <p>Use Google Authenticator app:</p>
                    <img
                        src={`data:image/png;base64,${qrCode}`}
                        alt="QR Code"
                        style={{ maxWidth: '300px', margin: '20px 0', border: '2px solid #222' }}
                    />

                    <form onSubmit={handleVerifySetup}>
                        <div style={{ marginBottom: '12px' }}>
                            <label htmlFor="setupCode">Verification Code:</label>
                            <input
                                id="setupCode"
                                type="text"
                                value={setupCode}
                                onChange={e => setSetupCode(e.target.value)}
                                placeholder="000000"
                                maxLength="6"
                                required
                            />
                        </div>

                        {error && (
                            <div className="error-message">{error}</div>
                        )}

                        <button type="submit">Verify & Enable</button>
                        <button
                            type="button"
                            onClick={() => {
                                setShowSetup(false);
                                setError(null);
                                setSetupCode('');
                            }}
                            className="submit-button"
                        >
                            Cancel
                        </button>
                    </form>
                </div>
            )}

            <hr style={{ margin: '40px 0', border: '2px solid #222' }} />

            <button onClick={handleLogout}>Logout</button>
        </div>
    );
}

export default ProfilePage;
