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
// Reuse table styles
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
    const [measurements, setMeasurements] = useState([]);
    const [stations, setStations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedMeasurementType, setSelectedMeasurementType] = useState('all');
    const [chartType, setChartType] = useState('line');

    // Get all filters including startDate and endDate
    const { searchTerm, areaPolygon, startDate, endDate } = useFilter();

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
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
            const intersect = ((yi > point.lng) !== (yj > point.lng))
                && (point.lat < (xj - xi) * (point.lng - yi) / (yj - yi) + xi);
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

    // 2. Filter Measurements by Station AND Date
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

    // 3. Filter Measurements by Type
    const filteredMeasurements = filteredByStations.filter(measurement =>
        selectedMeasurementType === 'all' || measurement.type === selectedMeasurementType
    );

    const availableMeasurementTypes = [...new Set(filteredByStations.map(m => m.type))].sort();

    // 4. Prepare Chart Data
    const chartData = useMemo(() => {
        if (filteredMeasurements.length === 0) return null;

        // Extract unique sorted timestamps for X-axis labels
        const labels = [...new Set(filteredMeasurements.map(m => m.measuredAt))]
            .sort()
            .map(dateStr => new Date(dateStr).toLocaleString('hr-HR'));

        const groupedData = {};

        filteredMeasurements.forEach(m => {
            const labelKey = selectedMeasurementType === 'all'
                ? `${m.station} (${m.type})`
                : m.station;

            if (!groupedData[labelKey]) {
                groupedData[labelKey] = [];
            }

            const dateLabel = new Date(m.measuredAt).toLocaleString('hr-HR');
            groupedData[labelKey].push({ x: dateLabel, y: m.value });
        });

        const colors = [
            'rgba(75,192,192,1)', 'rgba(255,99,132,1)', 'rgba(54,162,235,1)',
            'rgba(255,206,86,1)', 'rgba(153,102,255,1)', 'rgba(255,159,64,1)'
        ];

        const datasets = Object.keys(groupedData).map((key, index) => {
            const dataPoints = labels.map(label => {
                const found = groupedData[key].find(item => item.x === label);
                return found ? found.y : null;
            });

            const color = colors[index % colors.length];

            return {
                label: key,
                data: dataPoints,
                borderColor: color,
                backgroundColor: color.replace('1)', '0.5)'),
                tension: 0.3,
            };
        });

        return { labels, datasets };
    }, [filteredMeasurements, selectedMeasurementType]);


    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
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
                beginAtZero: false,
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


    if (loading) return <div className="loading-state"><h2>Loading charts...</h2></div>;
    if (error) return <div className="error-state"><h2>{error}</h2></div>;

    return (
        <div className="table-view-container">
            <h2 className="table-view-title">
                Measurements Chart
                {(searchTerm || areaPolygon || startDate || endDate) && ` - Filtered (${filteredStations.length} stations)`}
            </h2>

            {/* Controls Section */}
            <div style={{ display: 'flex', gap: '20px', marginBottom: '20px', flexWrap: 'wrap' }}>

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

            <div style={{ height: '400px', width: '100%', backgroundColor: '#fff', padding: '10px', borderRadius: '4px' }}>
                {!chartData || chartData.datasets.length === 0 ? (
                    <p className="no-measurements-text" style={{ textAlign: 'center', marginTop: '150px' }}>
                        {(searchTerm || areaPolygon || startDate || endDate)
                            ? 'No measurements match your filters.'
                            : 'No data available.'}
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
