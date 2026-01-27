// FilterContext.js
import React, { createContext, useContext, useState } from 'react';

const FilterContext = createContext();

export const useFilter = () => {
    const context = useContext(FilterContext);
    if (!context) {
        throw new Error('useFilter must be used within FilterProvider');
    }
    return context;
};

export const FilterProvider = ({ children }) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [areaPolygon, setAreaPolygon] = useState(null);
    const [isSelectingArea, setIsSelectingArea] = useState(false);

    // New state for date filtering
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    const resetFilters = () => {
        setSearchTerm('');
        setAreaPolygon(null);
        setIsSelectingArea(false);
        // Reset date filters
        setStartDate('');
        setEndDate('');
        sessionStorage.removeItem('selectedStationId');
        sessionStorage.removeItem('selectedStationName');
    };

    return (
        <FilterContext.Provider value={{
            searchTerm,
            setSearchTerm,
            areaPolygon,
            setAreaPolygon,
            isSelectingArea,
            setIsSelectingArea,
            startDate,       // Exported
            setStartDate,    // Exported
            endDate,         // Exported
            setEndDate,      // Exported
            resetFilters
        }}>
            {children}
        </FilterContext.Provider>
    );
};
