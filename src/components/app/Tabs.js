import React from 'react';
import './app.css'
import '../common.css'

function Tabs({activeTab, setActiveTab}) {
    return (
        <div className='tab-selector'>
            <button
                className={`tab ${activeTab === 'map' ? 'active' : ''}`}
                onClick={() => setActiveTab('map')}
            >MAP</button>
            <button
                className={`tab ${activeTab === 'table' ? 'active' : ''}`}
                onClick={() => setActiveTab('table')}
            >TABLES</button>
            <button
                className={`tab ${activeTab === 'charts' ? 'active' : ''}`}
                onClick={() => setActiveTab('charts')}
            >CHARTS</button>
        </div>
    );
}
export default Tabs;
