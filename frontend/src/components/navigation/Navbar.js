import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { userService } from '../../services/userService';
import './navigation.css';


function Navbar() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userRole, setUserRole] = useState(null);
    const location = useLocation();


    useEffect(() => {
        const checkAuth = async () => {
            const token = localStorage.getItem('authToken');
            setIsLoggedIn(!!token);

            if (token) {
                try {
                    const currentUser = await userService.getCurrentUser();
                    setUserRole(currentUser?.role);
                } catch (error) {
                    console.error('Failed to fetch user role:', error);
                    setUserRole(null);
                }

            } else {
                setUserRole(null);
            }
        };

        checkAuth();
    }, [location]);

    const shouldShowAdmin = isLoggedIn && userRole && userRole !== 'USER';

    return (
        <nav className="navbar">
            <Link to="/" className="logo">
                Hydro Cloud
            </Link>
            <div>
                <Link to="/" className="link">Home</Link>
                {shouldShowAdmin && <Link to="/admin" className="link">Admin</Link>}
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
