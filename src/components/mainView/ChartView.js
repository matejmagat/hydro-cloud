// ChartView.js
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
    const [typeData, setTypeData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [numOfMeasurements, setNumOfMeasurements] = useState(0);

    // Match TableView state structure for type selection
    const [selectedMeasurementType, setSelectedMeasurementType] = useState({ id: null, name: 'all' });
    const [chartType, setChartType] = useState('line');

    const { searchTerm, areaPolygon, startDate, endDate } = useFilter();

    const chartRef = useRef(null);

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

    useEffect(() => {
        fetchData();
    }, [searchTerm, areaPolygon, startDate, endDate, selectedMeasurementType.id]);

    const fetchData = async () => {
        try {
            setLoading(true);

            // 1. Search and Filter Stations (Backend Search + Frontend Polygon)
            const searchedStations = await stationsService.searchStations(searchTerm);
            const filteredStations = searchedStations.filter(station => {
                return isPointInPolygon(
                    { lat: station.latitude, lng: station.longitude },
                    areaPolygon,
                );
            });

            const stationIdsString = filteredStations.map(station => station.stationId).join(',');

            // If no stations match, clear data and return early
            if (!stationIdsString && filteredStations.length === 0) {
                setMeasurements([]);
                setStations([]);
                setTypeData([]);
                setNumOfMeasurements(0);
                setLoading(false);
                return;
            }

            // 2. Fetch Measurements and Types (Backend Filter)
            // We request a large page size (1000) to get enough data for the chart
            // since charts don't typically use pagination like tables.
            const [measurementsResponse, typesResponse] = await Promise.all([
                measurementsService.getMeasurements(
                    stationIdsString,
                    selectedMeasurementType.id,
                    startDate,
                    endDate,
                    0,    // Page 0
                    1000  // Limit 1000 items for chart visualization
                ),
                measurementsService.getMeasurementsTypes(
                    stationIdsString,
                    null,
                    startDate,
                    endDate
                )
            ]);

            const totalMeasurements = typesResponse.reduce((sum, item) =>
                sum + item.n_occurrences_in_filtered_data, 0);

            setMeasurements(measurementsResponse.data);
            setStations(filteredStations);
            setTypeData(typesResponse);
            setNumOfMeasurements(totalMeasurements);
            setError(null);

        } catch (err) {
            console.error('Failed to fetch data:', err);
            setError('Error loading data');
        } finally {
            setLoading(false);
        }
    };

    // Prepare Chart Data (Using the already filtered 'measurements' from state)
    const chartData = useMemo(() => {
        if (measurements.length === 0) return null;

        // Extract unique sorted timestamps for X-axis labels
        const labels = [...new Set(measurements.map(m => m.measuredAt))]
            .sort()
            .map(dateStr => new Date(dateStr).toLocaleString('hr-HR'));

        const groupedData = {};

        measurements.forEach(m => {
            // Group by "Station (Type)" if looking at all types, or just "Station" if specific type selected
            const labelKey = selectedMeasurementType.id === null
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
    }, [measurements, selectedMeasurementType]);

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { position: 'top' },
            title: {
                display: true,
                text: `Measurements Chart (${selectedMeasurementType.name})`,
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
                    text: selectedMeasurementType.id === null ? 'Value' : measurements[0]?.unit || 'Value'
                }
            }
        }
    };

    const handleExport = useCallback(() => {
        if (chartRef.current) {
            const link = document.createElement('a');
            link.download = `chart-${selectedMeasurementType.name}-${new Date().toISOString().slice(0, 10)}.png`;
            link.href = chartRef.current.toBase64Image();
            link.click();
        }
    }, [selectedMeasurementType]);

    if (loading) return <div className="loading-container">Loading charts...</div>;
    if (error) return <div className="error-state"><h2>{error}</h2></div>;

    return (
        <div className="table-view-container">
            <h2 className="table-view-title">
                Measurements Chart
                {(searchTerm || areaPolygon || startDate || endDate) && ` - Filtered (${stations.length} stations)`}
            </h2>

            {/* Controls Section */}
            <div style={{ display: 'flex', gap: '20px', marginBottom: '20px', flexWrap: 'wrap' }}>

                {stations.length > 0 && (
                    <div className="measurement-type-filter" style={{ margin: 0 }}>
                        <label htmlFor="measurementType" className="measurement-type-label">
                            Measurement Type
                        </label>
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
                            className="measurement-type-select"
                        >
                            <option value="all">All Types ({numOfMeasurements})</option>
                            {typeData.map(type => (
                                <option
                                    key={type.measurement_type_id}
                                    value={type.measurement_type_id}
                                >
                                    {type.measurement_type_name} ({type.n_occurrences_in_filtered_data})
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                <div className="measurement-type-filter" style={{ margin: 0 }}>
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
                style={{ marginTop: '20px' }}
                onClick={handleExport}
                disabled={!chartData || chartData.datasets.length === 0}
            >
                Export Chart
            </button>

        </div>
    );
}

export default ChartView;
