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
                <Link to="/" className="link">home</Link>
                <Link to="/admin" className="link">admin</Link>
                {isLoggedIn ? (
                    <Link to="/profile" className="link">profile</Link>
                ) : (
                    <Link to="/login" className="link">login</Link>
                )}
            </div>
        </nav>
    );
}
export default Navbar;
