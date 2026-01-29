// Sidebar.js
import React from 'react';
import './sidebar.css';
import '../common.css';
import { useFilter } from '../../context/FilterContext';
import Tabs from "../navigation/Tabs";

function Sidebar({ tabs, activeTab, setActiveTab }) {
    const {
        searchTerm,
        setSearchTerm,
        isSelectingArea,
        setIsSelectingArea,
        areaPolygon,
        setAreaPolygon,
        resetFilters,
        // Destructure new values
        startDate,
        setStartDate,
        endDate,
        setEndDate
    } = useFilter();

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
                        {/* Connected Start Date Input */}
                        <input
                            type="date"
                            className="time-period-input"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                    </div>
                    <div>
                        <div className="time-period-label">To</div>
                        {/* Connected End Date Input */}
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
