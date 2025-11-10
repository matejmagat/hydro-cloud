import React from 'react';

function Footer() {
    return (
        <nav style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', borderTop: '2px solid #222'}}>
            <div style={{fontWeight: 'bold', fontSize: '1.5em'}}>
                <a href="https://www.fer.unizg.hr/" target="_blank" rel="noopener noreferrer" style={{ color: 'inherit', textDecoration: 'none' }}>
                    FER
                </a>

            </div>
            <div>
                <a href="/" style={{fontWeight: 'bold', marginRight: '20px'}}>contact</a>
                <a href="/" style={{fontWeight: 'bold'}}>info</a>
            </div>
        </nav>
    );
}
export default Footer;
