import React, { useState, useEffect, useMemo, useRef, useCallback } from 'react';

import { Line, Bar } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import { measurementsService } from '../../services/measurementsService';
import { stationsService } from '../../services/stationsService';
import { useFilter } from '../../context/FilterContext';
// We can reuse the table view styles for the filter controls if applicable
import './tableView.css';

// Register chart components
ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    Title,
    Tooltip,
    Legend
);

function ChartView() {
    // 1. Shared State & Logic (Same as TableView)
    const [measurements, setMeasurements] = useState([]);
    const [stations, setStations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedMeasurementType, setSelectedMeasurementType] = useState('all');

    // 2. New State for Chart Type
    const [chartType, setChartType] = useState('line');

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

    // Polygon filtering logic
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

    // Filter Stations
    const filteredStations = stations.filter(station => {
        const matchesSearch = station.stationName.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesArea = isPointInPolygon(
            { lat: station.latitude, lng: station.longitude },
            areaPolygon
        );
        return matchesSearch && matchesArea;
    });

    const filteredStationNames = filteredStations.map(s => s.stationName);

    // Filter Measurements by Station
    const filteredByStations = measurements.filter(measurement =>
        filteredStationNames.includes(measurement.station)
    );

    // Filter Measurements by Type
    const filteredMeasurements = filteredByStations.filter(measurement =>
        selectedMeasurementType === 'all' || measurement.type === selectedMeasurementType
    );

    const availableMeasurementTypes = [...new Set(filteredByStations.map(m => m.type))].sort();

    // 3. Prepare Chart Data
    const chartData = useMemo(() => {
        if (filteredMeasurements.length === 0) return null;

        // Extract unique sorted timestamps for X-axis labels
        const labels = [...new Set(filteredMeasurements.map(m => m.measuredAt))]
            .sort()
            .map(dateStr => new Date(dateStr).toLocaleString('hr-HR'));

        // Group data by Station (and Type if 'all' is selected to avoid confusion)
        // We create a unique key for each dataset: "StationName - Type"
        const groupedData = {};

        filteredMeasurements.forEach(m => {
            const labelKey = selectedMeasurementType === 'all'
                ? `${m.station} (${m.type})`
                : m.station;

            if (!groupedData[labelKey]) {
                groupedData[labelKey] = [];
            }

            // Map the value to the correct index in labels
            // (This ensures values align with the correct time on X-axis)
            const dateLabel = new Date(m.measuredAt).toLocaleString('hr-HR');
            groupedData[labelKey].push({ x: dateLabel, y: m.value });
        });

        // specific colors for differentiation
        const colors = [
            'rgba(75,192,192,1)', 'rgba(255,99,132,1)', 'rgba(54,162,235,1)',
            'rgba(255,206,86,1)', 'rgba(153,102,255,1)', 'rgba(255,159,64,1)'
        ];

        const datasets = Object.keys(groupedData).map((key, index) => {
            // Create a sparse array matching the labels length
            const dataPoints = labels.map(label => {
                const found = groupedData[key].find(item => item.x === label);
                return found ? found.y : null; // null for gaps
            });

            const color = colors[index % colors.length];

            return {
                label: key,
                data: dataPoints,
                borderColor: color,
                backgroundColor: color.replace('1)', '0.5)'),
                tension: 0.3, // slight curve for lines
            };
        });

        return { labels, datasets };
    }, [filteredMeasurements, selectedMeasurementType]);


    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false, // Allows height control via CSS
        plugins: {
            legend: { position: 'top' },
            title: {
                display: true,
                text: `Measurements Chart (${selectedMeasurementType === 'all' ? 'Mixed Types' : selectedMeasurementType})`,
            },
            tooltip: {
                mode: 'index',
                intersect: false,
            },
        },
        scales: {
            y: {
                beginAtZero: false, // Often better for measurements like temp
                title: {
                    display: true,
                    text: selectedMeasurementType === 'all' ? 'Value' : filteredMeasurements[0]?.unit || 'Value'
                }
            }
        }
    };

    const chartRef = useRef(null);

    const handleExport = useCallback(() => {
        if (chartRef.current) {
            const link = document.createElement('a');
            link.download = `chart-${selectedMeasurementType}-${new Date().toISOString().slice(0, 10)}.png`;
            link.href = chartRef.current.toBase64Image();
            link.click();
        }
    }, [selectedMeasurementType]);


    if (loading) return <div className="table-view-loading"><h2>Loading charts...</h2></div>;
    if (error) return <div className="table-view-error"><h2>{error}</h2></div>;

    return (
        <div className="table-view-container"> {/* Reuse container class */}
            <h2 className="table-view-title">
                Measurements Chart
                {(searchTerm || areaPolygon) && ` - Filtered (${filteredStations.length} stations)`}
            </h2>

            {/* Controls Section */}
            <div style={{ display: 'flex', gap: '20px', marginBottom: '20px', flexWrap: 'wrap' }}>

                {/* 1. Measurement Type Picker (Reused Logic) */}
                {filteredByStations.length > 0 && (
                    <div className="measurement-type-filter" style={{margin: 0}}>
                        <label htmlFor="measurementType" className="measurement-type-label">
                            Measurement Type
                        </label>
                        <select
                            id="measurementType"
                            value={selectedMeasurementType}
                            onChange={(e) => setSelectedMeasurementType(e.target.value)}
                            className="measurement-type-select"
                        >
                            <option value="all">All Types ({filteredByStations.length})</option>
                            {availableMeasurementTypes.map(type => {
                                const count = filteredByStations.filter(m => m.type === type).length;
                                return (
                                    <option key={type} value={type}>
                                        {type} ({count})
                                    </option>
                                );
                            })}
                        </select>
                    </div>
                )}

                {/* 2. Chart Type Picker (New) */}
                <div className="measurement-type-filter" style={{margin: 0}}>
                    <label htmlFor="chartType" className="measurement-type-label">
                        Chart Type
                    </label>
                    <select
                        id="chartType"
                        value={chartType}
                        onChange={(e) => setChartType(e.target.value)}
                        className="measurement-type-select"
                    >
                        <option value="line">Line Chart</option>
                        <option value="bar">Bar Chart</option>
                    </select>
                </div>
            </div>

            {/* Chart Rendering */}
            <div style={{ height: '400px', width: '100%', backgroundColor: '#fff', padding: '10px', borderRadius: '4px' }}>
                {!chartData || chartData.datasets.length === 0 ? (
                    <p className="no-measurements-text" style={{ textAlign: 'center', marginTop: '150px' }}>
                        No data available for the selected filters.
                    </p>
                ) : (
                    chartType === 'line'
                        ? <Line ref={chartRef} data={chartData} options={chartOptions} />
                        : <Bar ref={chartRef} data={chartData} options={chartOptions} />
                )}
            </div>

            <button
                className="export-button"
                style={{marginTop: '20px'}}
                onClick={handleExport}
            >
                Export Chart
            </button>

        </div>
    );
}

export default ChartView;
