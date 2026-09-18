import React from 'react';
import { useNavigate } from 'react-router-dom';
import './sidebar.css';
import '../common.css';
import { useFilter } from '../../context/FilterContext';
import Tabs from "../navigation/Tabs";

function Sidebar({ tabs, activeTab, setActiveTab }) {
    const navigate = useNavigate();
    const isLoggedIn = !!localStorage.getItem('authToken');
    
    const {
        searchTerm,
        setSearchTerm,
        isSelectingArea,
        setIsSelectingArea,
        areaPolygon,
        setAreaPolygon,
        resetFilters,
        startDate,
        setStartDate,
        endDate,
        setEndDate
    } = useFilter();

    if (!isLoggedIn) {
        return (
            <aside className="sidebar">
                <div className="welcome-section">
                    <h2 className="welcome-title">Welcome to Hydro Cloud</h2>
                    <p className="welcome-text">
                        Please log in or register to access stations and measurements.
                    </p>
                    
                    <button 
                        onClick={() => navigate('/login')}
                        className="auth-button login-button"
                    >
                        Login
                    </button>
                    
                    <button 
                        onClick={() => navigate('/register')}
                        className="auth-button register-button"
                    >
                        Register
                    </button>
                </div>
            </aside>
        );
    }

    const handleAreaSelection = () => {
        if (areaPolygon) {
            setAreaPolygon(null);
        } else if (isSelectingArea) {
            setIsSelectingArea(false);
        } else {
            setIsSelectingArea(true);
        }
    };

    const getAreaButtonText = () => {
        if (areaPolygon) {
            return 'Deselect Area';
        } else if (isSelectingArea) {
            return 'Cancel Selection';
        } else {
            return 'Select Area';
        }
    };

    const getAreaButtonClass = () => {
        if (areaPolygon || isSelectingArea) {
            return 'area-button area-button-orange';
        }
        return 'area-button area-button-blue';
    };

    return (
        <aside className="sidebar">
            <Tabs tabs={tabs} activeTab={activeTab} setActiveTab={setActiveTab} />
            <div className="sidebar-section">
                <label className="sidebar-label">Station name</label>
                <input
                    type="text"
                    placeholder="Search"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="sidebar-input"
                />
            </div>

            <div className="sidebar-section">
                <label className="sidebar-label">Area Selection</label>
                <button
                    onClick={handleAreaSelection}
                    className={getAreaButtonClass()}
                >
                    {getAreaButtonText()}
                </button>
                {areaPolygon && !isSelectingArea && (
                    <div className="area-active-info">
                        ✓ Area active ({areaPolygon.length} points)
                    </div>
                )}
            </div>

            <div className="sidebar-section">
                <div className="time-period-title">Time Period</div>
                <div className="time-period-row">
                    <div>
                        <div className="time-period-label">From</div>
                        <input
                            type="date"
                            className="time-period-input"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                    </div>
                    <div>
                        <div className="time-period-label">To</div>
                        <input
                            type="date"
                            className="time-period-input"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                        />
                    </div>
                </div>
            </div>

            <div className="sidebar-section">
                <button
                    onClick={resetFilters}
                    className="reset-button"
                >
                    Reset Filters
                </button>
            </div>
        </aside>
    );
}

export default Sidebar;
