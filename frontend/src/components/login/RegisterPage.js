import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../common.css';

const API_URL = process.env.REACT_APP_API_URL;

function RegisterPage() {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [fieldErrors, setFieldErrors] = useState({});
    const navigate = useNavigate();
    
    const validateForm = () => {
        const errors = {};
        
        if (!firstName.trim()) {
            errors.firstName = "First name is required";
        } else if (firstName.length < 1 || firstName.length > 50) {
            errors.firstName = "First name must be between 1 and 50 characters";
        }
        
        if (!lastName.trim()) {
            errors.lastName = "Last name is required";
        } else if (lastName.length < 1 || lastName.length > 50) {
            errors.lastName = "Last name must be between 1 and 50 characters";
        }
        
        if (!username.trim()) {
            errors.username = "Username is required";
        } else if (username.length < 4 || username.length > 30) {
            errors.username = "Username must be between 4 and 30 characters";
        }
        
        if (!email.trim()) {
            errors.email = "Email is required";
        } else if (!/\S+@\S+\.\S+/.test(email)) {
            errors.email = "Email must be valid";
        }
        
        if (!password) {
            errors.password = "Password is required";
        } else if (password.length < 8) {
            errors.password = "Password must be at least 8 characters long";
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

            const contentType = response.headers.get('content-type') || '';

            if (response.ok) {
                const data = await response.json();
                console.log('Registered successfully:', data);
                localStorage.setItem('authToken', data["accessToken"]);
                navigate('/');
            } else {
            let errorMessage = "Registration failed";
            
            try {
                const data = await response.json();
                console.log("Backend error:", JSON.stringify(data, null, 2));
                
                if (data.detail) {
                    const match = data.detail.match(/"([^"]+)"/);
                    errorMessage = match ? match[1] : data.detail;
                }
                else if (data.errors && typeof data.errors === 'object') {
                    const firstError = Object.values(data.errors)[0];
                    errorMessage = firstError || "Validation failed";
                    setFieldErrors(data.errors);
                } 
                else if (data.message) {
                    errorMessage = data.message;
                }
                else if (data.title) {
                    errorMessage = data.title;
                }
            } catch (parseError) {
                console.error("Could not parse error response:", parseError);
                errorMessage = `Registration failed (${response.status})`;
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
            <h1>Register</h1>
            <form onSubmit={handleSubmit} noValidate>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="firstName">First name:</label>
                    <input
                        id="firstName"
                        type="text"
                        value={firstName}
                        onChange={(e) => {
                            setFirstName(e.target.value);
                            if (fieldErrors.firstName) {
                            setFieldErrors({ ...fieldErrors, firstName: null });
                            }
                        }}
                        style={{ 
                            borderColor: fieldErrors.firstName ? "red" : undefined 
                        }}
                    />
                    {fieldErrors.firstName && (
                        <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                            {fieldErrors.firstName}
                        </div>
                    )}
                </div>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="lastName">Last name:</label>
                    <input
                        id="lastName"
                        type="text"
                        value={lastName}
                        onChange={(e) => {
                            setLastName(e.target.value);
                            if (fieldErrors.lastName) {
                            setFieldErrors({ ...fieldErrors, lastName: null });
                            }
                        }}
                        style={{ 
                            borderColor: fieldErrors.lastName ? "red" : undefined 
                        }}
                    />
                    {fieldErrors.lastName && (
                        <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                            {fieldErrors.lastName}
                        </div>
                    )}
                </div>
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
                            borderColor: fieldErrors.username ? "red" : undefined 
                        }}
                    />
                    {fieldErrors.username && (
                        <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                            {fieldErrors.username}
                        </div>
                    )}
                </div>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="email">Email:</label>
                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            if (fieldErrors.email) {
                            setFieldErrors({ ...fieldErrors, email: null });
                            }
                        }}
                        style={{ 
                            borderColor: fieldErrors.email ? "red" : undefined 
                        }}
                    />
                    {fieldErrors.email && (
                        <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                            {fieldErrors.email}
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
                            borderColor: fieldErrors.password ? "red" : undefined 
                        }}
                    />
                </div>
                {fieldErrors.password && (
                    <div style={{ color: "red", fontSize: "13px", marginTop: "4px" }}>
                        {fieldErrors.password}
                    </div>
                )}

                {error && <div className="error-message">{error}</div>}

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
