import React from 'react';

function Tabs({activeTab, setActiveTab}) {
    return (
        <div style={{display: 'flex', gap: '12px', padding: '16px', borderBottom: '2px solid #222'}}>
            <button
                style={{
                    fontWeight: 'bold',
                    border: activeTab === 'map' ? '3px solid #222' : '2px solid #ccc',
                    background: activeTab === 'map' ? '#eee' : '#fff',
                    padding: '8px 24px'
                }}
                onClick={() => setActiveTab('map')}
            >MAP</button>
            <button
                style={{
                    fontWeight: 'bold',
                    border: activeTab === 'table' ? '3px solid #222' : '2px solid #ccc',
                    background: activeTab === 'table' ? '#eee' : '#fff',
                    padding: '8px 24px'
                }}
                onClick={() => setActiveTab('table')}
            >TABLE</button>
        </div>
    );
}
export default Tabs;
