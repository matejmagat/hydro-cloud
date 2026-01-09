import React from 'react';
import './navigation.css';
import '../common.css';

function Tabs({ tabs, activeTab, setActiveTab }) {
    return (
        <div className='tab-selector'>
            {tabs.map((tab) => (
                <button
                    key={tab}
                    className={`tab ${activeTab === tab ? 'active' : ''}`}
                    onClick={() => setActiveTab(tab)}
                >
                    {tab.toUpperCase()}
                </button>
            ))}
        </div>
    );
}

export default Tabs;
