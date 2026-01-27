// TableView.js
import React, { useState, useEffect } from 'react';
import { measurementsService } from '../../services/measurementsService';
import { stationsService } from '../../services/stationsService';
import { useFilter } from '../../context/FilterContext';
import './tableView.css';

// Helper function to handle CSV generation and download
const downloadCSV = (data, fileName = 'export.csv') => {
    if (!data || data.length === 0) {
        alert("No data to export");
        return;
    }

    // 1. Extract headers from the first object
    const headers = Object.keys(data[0]);

    // 2. Convert data to CSV string
    const csvContent = [
        headers.join(','), // Header row
        ...data.map(row =>
            headers.map(header => {
                let value = row[header] === null || row[header] === undefined ? '' : row[header];

                // Escape quotes and wrap in quotes if the value contains a comma, newline, or quote
                const stringValue = value.toString().replace(/"/g, '""');
                if (stringValue.search(/("|,|\n)/g) >= 0) {
                    return `"${stringValue}"`;
                }
                return stringValue;
            }).join(',')
        )
    ].join('\n');

    // 3. Create a Blob and trigger download
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};

function TableView() {
    const [measurements, setMeasurements] = useState([]);
    const [stations, setStations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedMeasurementType, setSelectedMeasurementType] = useState('all');

    // Destructure all filters including new date filters
    const { searchTerm, areaPolygon, startDate, endDate } = useFilter();

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            // Fetch ALL data and filter on client side as requested
            const [measurementsData, stationsData] = await Promise.all([
                measurementsService.getAllMeasurements(),
                stationsService.getAllStations()
            ]);
            setMeasurements(measurementsData);
            setStations(stationsData);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch data:', err);
            setError('Error loading data');
        } finally {
            setLoading(false);
        }
    };

    const isPointInPolygon = (point, polygon) => {
        if (!polygon || polygon.length < 3) return true;
        let inside = false;
        for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            const xi = polygon[i].lat, yi = polygon[i].lng;
            const xj = polygon[j].lat, yj = polygon[j].lng;
            const intersect = ((yi > point.lng) !== (yj > point.lng)) &&
                (point.lat < (xj - xi) * (point.lng - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    };

    // 1. Filter Stations based on Search and Area
    const filteredStations = stations.filter(station => {
        const matchesSearch = station.stationName.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesArea = isPointInPolygon(
            { lat: station.latitude, lng: station.longitude },
            areaPolygon
        );
        return matchesSearch && matchesArea;
    });

    const filteredStationNames = filteredStations.map(s => s.stationName);

    // 2. Create 'filteredByStations' which includes Station AND Date filters
    const filteredByStations = measurements.filter(measurement => {
        const matchesStation = filteredStationNames.includes(measurement.station);

        let matchesDate = true;
        if (startDate || endDate) {
            const mDate = new Date(measurement.measuredAt);
            if (startDate) {
                const start = new Date(startDate);
                start.setHours(0, 0, 0, 0);
                if (mDate < start) matchesDate = false;
            }
            if (endDate && matchesDate) {
                const end = new Date(endDate);
                end.setHours(23, 59, 59, 999);
                if (mDate > end) matchesDate = false;
            }
        }

        return matchesStation && matchesDate;
    });

    // 3. Final list filtered by Type
    const filteredMeasurements = filteredByStations.filter(measurement =>
        selectedMeasurementType === 'all' || measurement.type === selectedMeasurementType
    );

    const availableMeasurementTypes = [...new Set(filteredByStations.map(m => m.type))].sort();

    // Handler for the Export button
    const handleExport = () => {
        // Optional: formatting data before export (e.g., formatting dates)
        // If you want raw data, pass filteredMeasurements directly.
        // Here is a version that formats the date to be readable in Excel:
        const dataToExport = filteredMeasurements.map(m => ({
            ...m,
            measuredAt: new Date(m.measuredAt).toLocaleString('hr-HR')
        }));

        downloadCSV(dataToExport, 'measurements-export.csv');
    };

    if (loading) {
        return <div className="loading-state">Loading data...</div>;
    }

    if (error) {
        return <div className="error-state">{error}</div>;
    }

    return (
        <div className="table-view-container">
            <h2 className="table-view-title">
                Measurements
                {(searchTerm || areaPolygon || startDate || endDate) && ` - Filtered (${filteredStations.length} stations)`}
            </h2>

            {filteredByStations.length > 0 && (
                <div className="measurement-type-filter">
                    <label
                        htmlFor="measurementType"
                        className="measurement-type-label"
                    >
                        Measurement Type
                    </label>
                    <select
                        id="measurementType"
                        value={selectedMeasurementType}
                        onChange={(e) => setSelectedMeasurementType(e.target.value)}
                        className="measurement-type-select"
                    >
                        <option value="all">All Types ({filteredByStations.length} measurements)</option>
                        {availableMeasurementTypes.map(type => {
                            const count = filteredByStations.filter(m => m.type === type).length;
                            return (
                                <option key={type} value={type}>
                                    {type} ({count} measurements)
                                </option>
                            );
                        })}
                    </select>
                </div>
            )}

            {filteredMeasurements.length === 0 ? (
                <p className="no-measurements-text">
                    {(searchTerm || areaPolygon || startDate || endDate)
                        ? 'No measurements match your filters.'
                        : 'No measurements available.'}
                </p>
            ) : (
                <>
                    <div className="table-view-info">
                        Showing {filteredMeasurements.length} of {measurements.length} measurements
                    </div>
                    <table className="measurement-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Station</th>
                            <th>Measurement Type</th>
                            <th>Value</th>
                            <th>Unit</th>
                            <th>Date &amp; Time</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredMeasurements.map((measurement) => (
                            <tr key={measurement.measurementId}>
                                <td>{measurement.measurementId}</td>
                                <td>{measurement.station}</td>
                                <td>{measurement.type}</td>
                                <td>{measurement.value}</td>
                                <td>{measurement.unit}</td>
                                <td>
                                    {new Date(measurement.measuredAt).toLocaleString('hr-HR')}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </>
            )}

            {/* UPDATED EXPORT BUTTON */}
            <button
                className="export-button"
                onClick={handleExport}
                disabled={filteredMeasurements.length === 0}
                style={filteredMeasurements.length === 0 ? { opacity: 0.5, cursor: 'not-allowed' } : {}}
            >
                Export Data
            </button>
        </div>
    );
}

export default TableView;
