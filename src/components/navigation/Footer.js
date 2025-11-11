import React from 'react';
import './navigation.css'

function Footer() {
    return (
        <nav className="navbar">
            <div style={{fontWeight: 'bold', fontSize: '1.5em'}}>
                <a href="https://www.fer.unizg.hr/" target="_blank" rel="noopener noreferrer" style={{ color: 'inherit', textDecoration: 'none' }}>
                    FER
                </a>
            </div>
            <div>
                <a href="/public" className="link">contact</a>
                <a href="/public" className="link">info</a>
            </div>
        </nav>
    );
}
export default Footer;
