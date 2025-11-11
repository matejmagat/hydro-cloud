import React from 'react';
import './app.css'
import '../common.css'

function Sidebar() {
    return (
        <aside className="sidebar">
            <div style={{ marginBottom: '24px' }}>
                <label> Station name </label>
                <input type="text" placeholder="Search" />
            </div>
            <div style={{ marginBottom: '30px' }}>
                <label>
                    Country
                </label>
                <select defaultValue="">
                    <option value="" disabled>
                        Select
                    </option>
                    <option value="country1">Country 1</option>
                    <option value="country2">Country 2</option>
                </select>
            </div>
            <div>
                <div style={{ fontWeight: 'bold', fontSize: '1.1em' }}>Time Period</div>
                <div style={{ display: 'flex', justifyContent: 'start', gap: '18px', margin: '12px 0 0 0' }}>
                    <div>
                        <div style={{ marginBottom: '5px', fontWeight: 'bold' }}>From</div>
                        <input type="text" />
                    </div>
                    <div>
                        <div style={{ marginBottom: '5px', fontWeight: 'bold' }}>To</div>
                        <input type="text"/>
                    </div>
                </div>
            </div>
            <div>
                <button> Search </button>
            </div>
        </aside>
    );
}

export default Sidebar;
