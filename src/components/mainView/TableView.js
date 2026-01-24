import React, { useState, useEffect } from 'react';
import { measurementsService } from '../../services/measurementsService';
import { stationsService } from '../../services/stationsService';
import { useFilter } from '../../context/FilterContext';
import './tableView.css';

function TableView() {
    const [measurements, setMeasurements] = useState([]);
    const [stations, setStations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedMeasurementType, setSelectedMeasurementType] = useState('all');
    
    const { searchTerm, areaPolygon } = useFilter();

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            
            const [measurementsData, stationsData] = await Promise.all([
                measurementsService.getAllMeasurements(null, null),
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

            const intersect = ((yi > point.lng) !== (yj > point.lng))
                && (point.lat < (xj - xi) * (point.lng - yi) / (yj - yi) + xi);
            
            if (intersect) inside = !inside;
        }

        return inside;
    };

    const filteredStations = stations.filter(station => {
        const matchesSearch = station.stationName.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesArea = isPointInPolygon(
            { lat: station.latitude, lng: station.longitude }, 
            areaPolygon
        );
        return matchesSearch && matchesArea;
    });

    const filteredStationNames = filteredStations.map(s => s.stationName);

    const filteredByStations = measurements.filter(measurement => 
        filteredStationNames.includes(measurement.station)
    );

    const filteredMeasurements = filteredByStations.filter(measurement => 
        selectedMeasurementType === 'all' || measurement.type === selectedMeasurementType
    );

    const availableMeasurementTypes = [...new Set(filteredByStations.map(m => m.type))].sort();

    if (loading) {
        return (
            <div className="table-view-loading">
                <h2>Loading measurements...</h2>
            </div>
        );
    }

    if (error) {
        return (
            <div className="table-view-error">
                <h2 className="table-view-error-title">{error}</h2>
                <button onClick={fetchData} className="retry-button">Try Again</button>
            </div>
        );
    }

    return (
        <div className="table-view-container">
            <h2 className="table-view-title">
                Measurements
                {(searchTerm || areaPolygon) && ` - Filtered (${filteredStations.length} stations)`}
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
                    {(searchTerm || areaPolygon) 
                        ? 'No measurements for selected stations.' 
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

            <button className="export-button">Export Data</button>
        </div>
    );
}
export default TableView;
