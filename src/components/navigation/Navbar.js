import React from 'react';
import './navigation.css'

function Navbar() {
    return (
        <nav className="navbar">
            <a href="/" className="logo">
                Hydrological Information WebApp
            </a>
            <div>
                <a href="/" className="link">home</a>
                <a href="/admin" className="link">admin</a>
                <a href="/login" className="link">login</a>
            </div>
        </nav>
    );
}
export default Navbar;
