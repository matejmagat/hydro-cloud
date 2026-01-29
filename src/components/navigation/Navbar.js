import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import './navigation.css';

function Navbar() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const location = useLocation();

    useEffect(() => {
        const token = localStorage.getItem('authToken');
        setIsLoggedIn(!!token);
    }, [location]);

    return (
        <nav className="navbar">
            <Link to="/" className="logo">
                Hydro Cloud
            </Link>
            <div>
                <Link to="/" className="link">Home</Link>
                <Link to="/admin" className="link">Admin</Link>
                {isLoggedIn ? (
                    <Link to="/profile" className="link">Profile</Link>
                ) : (
                    <Link to="/login" className="link">Login</Link>
                )}
            </div>
        </nav>
    );
}
export default Navbar;
