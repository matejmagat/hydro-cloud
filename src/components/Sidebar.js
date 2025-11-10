import React from 'react';

function Sidebar() {
    return (
        <aside
            style={{
                width: '250px',
                minWidth: '170px',
                borderRight: '2px solid #222',
                padding: '28px 22px',
                boxSizing: 'border-box',
                fontWeight: 'bold'
            }}
        >
            <div style={{ marginBottom: '24px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', fontSize: '1.1em' }}>
                    Station name
                </label>
                <input
                    type="text"
                    placeholder="Search"
                    style={{
                        width: '100%',
                        padding: '10px',
                        fontWeight: 'bold',
                        fontSize: '1em',
                        border: '4px solid #222',
                        borderRadius: 0,
                        outline: 'none',
                        boxSizing: 'border-box'
                    }}
                />
            </div>
            <div style={{ marginBottom: '30px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', fontSize: '1.1em' }}>
                    Country
                </label>
                <select
                    style={{
                        width: '100%',
                        padding: '10px',
                        fontWeight: 'bold',
                        fontSize: '1em',
                        border: '4px solid #222',
                        borderRadius: 0,
                        outline: 'none',
                        boxSizing: 'border-box'
                    }}
                    defaultValue=""
                >
                    <option value="" disabled>
                        Select
                    </option>
                    {/* Example options */}
                    <option value="country1">Country 1</option>
                    <option value="country2">Country 2</option>
                </select>
            </div>
            <div>
                <div style={{ fontWeight: 'bold', fontSize: '1.1em' }}>Time Period</div>
                <div style={{ display: 'flex', justifyContent: 'start', gap: '18px', margin: '12px 0 0 0' }}>
                    <div>
                        <div style={{ marginBottom: '5px', fontWeight: 'bold' }}>From</div>
                        <input
                            type="text"
                            style={{
                                width: '48px',
                                padding: '10px',
                                fontWeight: 'bold',
                                textAlign: 'center',
                                border: '4px solid #222',
                                borderRadius: 0,
                                fontSize: '1em'
                            }}
                        />
                    </div>
                    <div>
                        <div style={{ marginBottom: '5px', fontWeight: 'bold' }}>To</div>
                        <input
                            type="text"
                            style={{
                                width: '48px',
                                padding: '10px',
                                fontWeight: 'bold',
                                textAlign: 'center',
                                border: '4px solid #222',
                                borderRadius: 0,
                                fontSize: '1em'
                            }}
                        />
                    </div>
                </div>
            </div>
            <div>
                <button
                    style={{
                        marginTop: '12px',
                        width: '100%',
                        padding: '10px 0',
                        background: '#fff',
                        fontWeight: 'bold',
                        fontSize: '1em',
                        border: '4px solid #222',
                        borderRadius: 0,
                        cursor: 'pointer'
                    }}
                >
                    Search
                </button>
            </div>
        </aside>
    );
}

export default Sidebar;
