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
    const [selectedMeasurementType,
        setSelectedMeasurementType] = useState({"id":null, "name":"all"});
    const [numOfMeasurements, setNumOfMeasurements] = useState(0);
    const [typeData, setTypeData] = useState([]);
    const [numOfPages, setNumOfPages] = useState(1);
    const [currentPage, setCurrentPage] = useState(0);
    const [elementsInPage, setElementsInPage] = useState(20);
    const [isLastPage, setIsLastPage] = useState(false);

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


    // Destructure all filters including new date filters
    const { searchTerm, areaPolygon, startDate, endDate } = useFilter();

    useEffect(() => {
        fetchData(0);
    }, [searchTerm, areaPolygon, startDate, endDate, selectedMeasurementType]);

    const fetchData = async (page) => {
        try {
            setLoading(true);
            const searchedStations = await stationsService.searchStations(searchTerm);
            const filteredStations = searchedStations.filter(station => {
                return isPointInPolygon(
                    { lat: station.latitude, lng: station.longitude },
                    areaPolygon,
                );
            });
            const stationIdsString = filteredStations.map(station => station.stationId).join(',');
            const measurementsData = await measurementsService.getMeasurements(stationIdsString,
                selectedMeasurementType["id"], startDate, endDate, page, elementsInPage);

            const typeData = await measurementsService.getMeasurementsTypes(stationIdsString, null,
                startDate, endDate);

            const totalMeasurements = typeData.reduce((sum, item) =>
                sum + item.n_occurrences_in_filtered_data, 0);

            const firstPage = measurementsData["firstPage"];

            if (firstPage) {
                setMeasurements(measurementsData["data"]);
            } else {
                setMeasurements(measurements.concat(measurementsData["data"]));
            }
            setCurrentPage(page);
            setNumOfPages(measurementsData["numOfPages"]);
            setTypeData(typeData);
            setStations(filteredStations);
            setNumOfMeasurements(totalMeasurements);
            setIsLastPage(measurementsData["lastPage"]);


            // setElementsInPage(measurementsData["elementsInPage"]);

            setError(null);
        } catch (err) {
            console.error('Failed to fetch data:', err);
            setError('Error loading data');
        } finally {
            setLoading(false);
        }
    };




    // const availableMeasurementTypes = [...new Set(filteredByStations.map(m => m.type))].sort();

    // Handler for the Export button
    const handleExport = () => {
       const dataToExport = measurements.map(m => ({
         ...m,
           measuredAt: new Date(m.measuredAt).toLocaleString('hr-HR')
        }));

        downloadCSV(dataToExport, 'measurements-export.csv');
    };

    if (loading) {
        return <div className="loading-container">Loading data...</div>;
    }

    if (error) {
        return <div className="error-state">{error}</div>;
    }

    return (
        <div className="table-view-container">
            <h2 className="table-view-title">
                Measurements
                {(searchTerm || areaPolygon || startDate || endDate) && ` - Filtered (${stations.length} stations)`}
            </h2>

            {stations.length > 0 && (
                <div className="measurement-type-filter">
                    <label
                        htmlFor="measurementType"
                        className="measurement-type-label"
                    >
                        Measurement Type
                    </label>
                    <div className="select-and-button">
                        <select
                            id="measurementType"
                            // Bind value to the ID, not the name (it's safer/unique)
                            value={selectedMeasurementType.id || "all"}
                            onChange={(e) => {
                                const selectedId = e.target.value;

                                if (selectedId === "all") {
                                    setSelectedMeasurementType({ id: null, name: "All Types" });
                                } else {
                                    // Find the specific type object to get the name back
                                    const type = typeData.find(t => String(t.measurement_type_id) === selectedId);
                                    if (type) {
                                        setSelectedMeasurementType({
                                            id: type.measurement_type_id,
                                            name: type.measurement_type_name
                                        });
                                    }
                                }
                            }}
                            className="measurement-type-select"
                        >
                            {/* Ideally calculate the total count sum for 'All' */}
                            <option value="all">All Types ({numOfMeasurements} measurements)</option>

                            {typeData.map(type => {
                                const count = type["n_occurrences_in_filtered_data"];
                                const id = type["measurement_type_id"];
                                const name = type["measurement_type_name"];

                                return (
                                    <option
                                        key={id}       // Unique key is required for React lists
                                        value={id}     // Set the value to the ID
                                    >
                                        {name} ({count} measurements)
                                    </option>
                                );
                            })}
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

            {measurements.length === 0 ? (
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

            {isLastPage ? null :
                <button onClick={() => {
                    if (currentPage < numOfPages - 1) {
                        // setCurrentPage(currentPage + 1);
                        fetchData(currentPage + 1);
                    }
                }}>
                    Load More
                </button>
            }


        </div>
    );
}

export default TableView;
