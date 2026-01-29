// TableView.js
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { measurementsService } from '../../services/measurementsService';
import { stationsService } from '../../services/stationsService';
import { useFilter } from '../../context/FilterContext';
import './tableView.css';

// OPTIMIZATION 1: Move static helpers outside component to prevent recreation on render
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

const downloadCSV = (data, fileName = 'export.csv') => {
    if (!data || data.length === 0) {
        alert("No data to export");
        return;
    }
    const headers = Object.keys(data[0]);
    const csvContent = [
        headers.join(','),
        ...data.map(row =>
            headers.map(header => {
                let value = row[header] === null || row[header] === undefined ? '' : row[header];
                const stringValue = value.toString().replace(/"/g, '""');
                return stringValue.search(/("|,|\n)/g) >= 0 ? `"${stringValue}"` : stringValue;
            }).join(',')
        )
    ].join('\n');

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
    // State
    const [measurements, setMeasurements] = useState([]);
    const [stations, setStations] = useState([]);
    const [loading, setLoading] = useState(true); // Global loading state
    const [stationsLoading, setStationsLoading] = useState(false); // Specific to station search
    const [error, setError] = useState(null);
    const [selectedMeasurementType, setSelectedMeasurementType] = useState({ "id": null, "name": "all" });
    const [numOfMeasurements, setNumOfMeasurements] = useState(0);
    const [typeData, setTypeData] = useState([]);
    const [numOfPages, setNumOfPages] = useState(1);
    const [currentPage, setCurrentPage] = useState(0);
    const [elementsInPage, setElementsInPage] = useState(20);
    const [isLastPage, setIsLastPage] = useState(false);

    // Refs for cancellation
    const abortControllerRef = useRef(null);

    // Context
    const { searchTerm, areaPolygon, startDate, endDate } = useFilter();

    // OPTIMIZATION 2: Separate Station Fetching
    // Only runs when search/area filters change. Does NOT run on pagination or date change.
    useEffect(() => {
        let active = true;
        const fetchStations = async () => {
            try {
                setStationsLoading(true);
                const searchedStations = await stationsService.searchStations(searchTerm);

                if (!active) return;

                const filteredStations = searchedStations.filter(station => {
                    return isPointInPolygon(
                        { lat: station.latitude, lng: station.longitude },
                        areaPolygon,
                    );
                });
                setStations(filteredStations);
            } catch (err) {
                console.error('Failed to fetch stations:', err);
                if (active) setError('Error loading stations');
            } finally {
                if (active) setStationsLoading(false);
            }
        };

        fetchStations();
        return () => { active = false; };
    }, [searchTerm, areaPolygon]);

    // OPTIMIZATION 3: Memoize station IDs to prevent unnecessary effect triggers
    const stationIdsString = React.useMemo(() =>
            stations.map(s => s.stationId).join(','),
        [stations]);

    // OPTIMIZATION 4: Optimized Measurement Fetcher
    const fetchMeasurements = useCallback(async (page, isNewFilter = false) => {
        // Cancel previous pending request
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }
        abortControllerRef.current = new AbortController();
        const signal = abortControllerRef.current.signal;

        // Don't fetch if no stations are found (unless we want to show empty state)
        if (stations.length === 0 && !stationsLoading) {
            setMeasurements([]);
            setNumOfMeasurements(0);
            return;
        }

        try {
            setLoading(true);

            // OPTIMIZATION 5: Parallel Requests with Promise.all
            const [measurementsData, typesData] = await Promise.all([
                measurementsService.getMeasurements(
                    stationIdsString,
                    selectedMeasurementType["id"],
                    startDate,
                    endDate,
                    page,
                    elementsInPage
                ),
                // We typically only need to re-fetch types if the filter context (stations/time) changes,
                // but fetching them in parallel is cheap enough here.
                measurementsService.getMeasurementsTypes(
                    stationIdsString,
                    null,
                    startDate,
                    endDate
                )
            ]);

            if (signal.aborted) return;

            // Update Types & Counts
            setTypeData(typesData);
            const totalMeasurements = typesData.reduce((sum, item) =>
                sum + item.n_occurrences_in_filtered_data, 0);
            setNumOfMeasurements(totalMeasurements);

            // Update Measurements
            if (isNewFilter || measurementsData["firstPage"]) {
                setMeasurements(measurementsData["data"]);
            } else {
                // OPTIMIZATION 6: Functional update for safety
                setMeasurements(prev => prev.concat(measurementsData["data"]));
            }

            setNumOfPages(measurementsData["numOfPages"]);
            setIsLastPage(measurementsData["lastPage"]);
            setCurrentPage(page);
            setError(null);

        } catch (err) {
            if (err.name !== 'AbortError') {
                console.error('Failed to fetch measurements:', err);
                setError('Error loading data');
            }
        } finally {
            if (!signal.aborted) {
                setLoading(false);
            }
        }
    }, [stationIdsString, selectedMeasurementType, startDate, endDate, elementsInPage, stationsLoading, stations.length]);


    // Trigger fetch when dependencies change (Reset to page 0)
    useEffect(() => {
        // Wait for stations to finish loading before fetching measurements
        if (!stationsLoading) {
            fetchMeasurements(0, true);
        }
    }, [fetchMeasurements, stationsLoading]);
    // fetchMeasurements depends on stationIdsString, so this runs when stations update


    const handleExport = () => {
        const dataToExport = measurements.map(m => ({
            ...m,
            measuredAt: new Date(m.measuredAt).toLocaleString('hr-HR')
        }));
        downloadCSV(dataToExport, 'measurements-export.csv');
    };

    // UI Loading state now checks both
    const isGlobalLoading = loading || stationsLoading;

    if (isGlobalLoading && measurements.length === 0) {
        return <div className="loading-container">Loading data...</div>;
    }

    if (error) {
        return <div className="error-state">{error}</div>;
    }

    return (
        <div className="table-view-container">
            <h2 className="table-view-title">
                Measurements Table
                {(searchTerm || areaPolygon || startDate || endDate) && ` - Filtered (${stations.length} stations)`}
            </h2>

            {stations.length > 0 && (
                <div className="measurement-type-filter">
                    <label htmlFor="measurementType" className="measurement-type-label">
                        Measurement Type
                    </label>
                    <div className="select-and-button">
                        <select
                            id="measurementType"
                            value={selectedMeasurementType.id || "all"}
                            onChange={(e) => {
                                const selectedId = e.target.value;
                                if (selectedId === "all") {
                                    setSelectedMeasurementType({ id: null, name: "All Types" });
                                } else {
                                    const type = typeData.find(t => String(t.measurement_type_id) === selectedId);
                                    if (type) {
                                        setSelectedMeasurementType({
                                            id: type.measurement_type_id,
                                            name: type.measurement_type_name
                                        });
                                    }
                                }
                            }}
                        >
                            <option value="all">All Types ({numOfMeasurements} measurements)</option>
                            {typeData.map(type => (
                                <option
                                    key={type.measurement_type_id}
                                    value={type.measurement_type_id}
                                >
                                    {type.measurement_type_name} ({type.n_occurrences_in_filtered_data} measurements)
                                </option>
                            ))}
                        </select>

                        <button
                            className="export-button"
                            onClick={handleExport}
                            disabled={measurements.length === 0}
                            style={measurements.length === 0 ? { opacity: 0.5, cursor: 'not-allowed' } : {}}
                        >
                            Export Data
                        </button>
                    </div>
                </div>
            )}

            {measurements.length === 0 && !isGlobalLoading ? (
                <p className="no-measurements-text">
                    {(searchTerm || areaPolygon || startDate || endDate)
                        ? 'No measurements match your filters.'
                        : 'No measurements available.'}
                </p>
            ) : (
                <>
                    <div className="table-view-info">
                        Showing page {currentPage + 1} of {numOfPages}
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
                        {measurements.map((measurement) => (
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

            {!isLastPage && (
                <button
                    onClick={() => fetchMeasurements(currentPage + 1, false)}
                    disabled={loading}
                    className="load-more-button"
                >
                    {loading ? 'Loading...' : 'Load More'}
                </button>
            )}
        </div>
    );
}

export default TableView;
