import React from 'react';

function Navbar() {
    return (
        <nav style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', borderBottom: '2px solid #222'}}>
            <div style={{fontWeight: 'bold', fontSize: '1.5em'}}>
                <a href="/" style={{ color: 'inherit', textDecoration: 'none' }}>
                    Hydrological Information WebApp
                </a>
            </div>
            <div>
                <a href="/" style={{fontWeight: 'bold', marginRight: '20px'}}>home</a>
                <a href="/admin" style={{fontWeight: 'bold', marginRight: '20px'}}>admin</a>
                <a href="/login" style={{fontWeight: 'bold'}}>login</a>
            </div>
        </nav>
    );
}
export default Navbar;
