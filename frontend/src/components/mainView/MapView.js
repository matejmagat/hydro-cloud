import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents, Polygon, Polyline } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './mapView.css';
import { stationsService } from '../../services/stationsService';
import { useTab } from '../../context/TabContext';
import { useFilter } from '../../context/FilterContext';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
    iconUrl: require('leaflet/dist/images/marker-icon.png'),
    shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

const CROATIA_BOUNDS = [
    [42.0, 13.0],
    [49.0, 20.0],
];

function LocationPicker({ onLocationPick, enabled }) {
    useMapEvents({
        click: (e) => {
            if (enabled) {
                onLocationPick(e.latlng);
            }
        },
    });
    return null;
}

function PolygonSelector({ isSelecting, onPolygonComplete, onAddPoint }) {
    const [points, setPoints] = useState([]);

    useMapEvents({
        click: (e) => {
            if (isSelecting) {
                const newPoint = { lat: e.latlng.lat, lng: e.latlng.lng };
                const newPoints = [...points, newPoint];
                setPoints(newPoints);
                onAddPoint(newPoints);
            }
        },
    });

    useEffect(() => {
        if (!isSelecting) {
            setPoints([]);
        }
    }, [isSelecting]);

    const handleClosePolygon = () => {
        if (points.length >= 3) {
            onPolygonComplete([...points]);
            setPoints([]);
        }
    };

    return (
        <>
            {points.length > 0 && (
                <Polyline
                    positions={points.map(p => [p.lat, p.lng])}
                    pathOptions={{
                        color: '#ff9800',
                        weight: 3,
                        dashArray: '10, 10',
                    }}
                />
            )}

            {points.map((point, index) => (
                <Marker
                    key={index}
                    position={[point.lat, point.lng]}
                    icon={new L.Icon({
                        iconUrl: index === 0
                            ? 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png'
                            : 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-orange.png',
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowSize: [41, 41],
                    })}
                >
                    <Popup>
                        {index === 0 ? (
                            <div className="polygon-popup-content">
                                <strong>Start Point</strong><br />
                                <div className="polygon-point-count">
                                    {points.length} point{points.length !== 1 ? 's' : ''} selected
                                </div>
                                {points.length >= 3 && (
                                    <button
                                        onClick={handleClosePolygon}
                                        className="close-polygon-btn"
                                    >
                                        Close Polygon
                                    </button>
                                )}
                                {points.length < 3 && (
                                    <div className="polygon-help-text">
                                        Add {3 - points.length} more point{3 - points.length !== 1 ? 's' : ''}
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="polygon-popup-content">
                                <strong>Point {index + 1}</strong>
                            </div>
                        )}
                    </Popup>
                </Marker>
            ))}
        </>
    );
}

function MapView() {
    const [stations, setStations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [deleting, setDeleting] = useState(null);
    const [mapCenter] = useState([44.5, 16.5]);
    const isLoggedIn = !!localStorage.getItem('authToken');
    const { setActiveTab } = useTab();
    const {
        searchTerm,
        setSearchTerm,
        areaPolygon,
        setAreaPolygon,
        isSelectingArea,
        setIsSelectingArea,
    } = useFilter();

    const [showModal, setShowModal] = useState(false);
    const [newStation, setNewStation] = useState({
        stationName: '',
        latitude: '',
        longitude: '',
    });
    const [tempMarker, setTempMarker] = useState(null);
    const [creating, setCreating] = useState(false);
    const [currentPoints, setCurrentPoints] = useState(0);
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState(searchTerm);

    // Add debounce effect
    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedSearchTerm(searchTerm);
        }, 300); // 300ms delay

        return () => clearTimeout(timer);
    }, [searchTerm]);

    useEffect(() => {
        if (!isLoggedIn) {
            setLoading(false);
            setStations([]);
            return;
        }

        const fetchStations = async () => {
            try {
                setLoading(true);
                const data = debouncedSearchTerm
                    ? await stationsService.searchStations(debouncedSearchTerm)
                    : await stationsService.getAllStations();
                setStations(data);
            } catch (error) {
                console.error('Failed to fetch stations:', error);
                alert('Error loading stations');
            } finally {
                setLoading(false);
            }
        };

        fetchStations();
    }, [debouncedSearchTerm, isLoggedIn]);

    const handleDeleteStation = async (stationId, stationName) => {
        if (!window.confirm(`Are you sure you want to delete station "${stationName}"?`)) {
            return;
        }

        try {
            setDeleting(stationId);
            await stationsService.deleteStation(stationId);
            setStations(stations.filter(s => s.stationId !== stationId));
            alert('Station successfully deleted!');
        } catch (error) {
            console.error('Failed to delete station:', error);
            alert('Error deleting station');
        } finally {
            setDeleting(null);
        }
    };

    const handleViewMeasurements = (stationId, stationName) => {
        sessionStorage.setItem('selectedStationId', stationId);
        sessionStorage.setItem('selectedStationName', stationName);
        setSearchTerm(stationName);
        setActiveTab('table');
    };

    const handleOpenModal = () => {
        setShowModal(true);
        setNewStation({ stationName: '', latitude: '', longitude: '' });
        setTempMarker(null);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setTempMarker(null);
    };

    const handleMapClick = (latlng) => {
        setTempMarker(latlng);
        setNewStation({
            ...newStation,
            latitude: latlng.lat.toFixed(6),
            longitude: latlng.lng.toFixed(6),
        });
    };

    const handleCreateStation = async (e) => {
        e.preventDefault();

        if (!newStation.stationName || !newStation.latitude || !newStation.longitude) {
            alert('All fields are required!');
            return;
        }

        try {
            setCreating(true);
            const createdStation = await stationsService.createStation({
                stationName: newStation.stationName,
                latitude: parseFloat(newStation.latitude),
                longitude: parseFloat(newStation.longitude),
            });

            setStations([...stations, createdStation]);
            alert('Station successfully created!');
            handleCloseModal();
        } catch (error) {
            console.error('Failed to create station:', error);
            alert('Error creating station');
        } finally {
            setCreating(false);
        }
    };

    const handlePolygonComplete = (polygon) => {
        setAreaPolygon(polygon);
        setIsSelectingArea(false);
        setCurrentPoints(0);
    };

    const handleAddPoint = (points) => {
        setCurrentPoints(points.length);
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
        return isPointInPolygon(
            { lat: station.latitude, lng: station.longitude },
            areaPolygon,
        );
    });

    if (loading) {
        return (
            <div className="loading-container">
                Loading stations...
            </div>
        );
    }

    return (
        <div className="map-view-container">
            {isLoggedIn && !showModal && !isSelectingArea && (
                <button
                    onClick={handleOpenModal}
                    className="add-station-btn"
                >
                    <span className="add-icon">+</span>
                    <span>Add Station</span>
                </button>
            )}

            {isSelectingArea && (
                <div className="instruction-banner area-selection">
                    Click points on map to draw polygon ({currentPoints} point{currentPoints !== 1 ? 's' : ''})<br />
                    <span className="instruction-subtitle">
                        {currentPoints >= 3
                            ? 'Click red marker and press "Close Polygon" button'
                            : `Need ${3 - currentPoints} more point${3 - currentPoints !== 1 ? 's' : ''} minimum`
                        }
                    </span>
                </div>
            )}

            {showModal && (
                <>
                    <div className="instruction-banner location-selection">
                        Click on the map to select location
                    </div>

                    <div className="modal-overlay">
                        <h3 className="modal-title">New Station</h3>
                        <form onSubmit={handleCreateStation}>
                            <div className="form-group">
                                <label htmlFor="stationName" className="form-label">
                                    Station Name
                                </label>
                                <input
                                    id="stationName"
                                    type="text"
                                    className="form-input"
                                    value={newStation.stationName}
                                    onChange={(e) => setNewStation({ ...newStation, stationName: e.target.value })}
                                    placeholder="e.g. Zagreb - Sava"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="latitude" className="form-label">
                                    Latitude
                                </label>
                                <input
                                    id="latitude"
                                    type="number"
                                    step="any"
                                    className="form-input"
                                    value={newStation.latitude}
                                    onChange={(e) => {
                                        setNewStation({ ...newStation, latitude: e.target.value });
                                        if (newStation.longitude && e.target.value) {
                                            setTempMarker({
                                                lat: parseFloat(e.target.value),
                                                lng: parseFloat(newStation.longitude),
                                            });
                                        }
                                    }}
                                    placeholder="45.8150"
                                    required
                                />
                            </div>

                            <div className="form-group last">
                                <label htmlFor="longitude" className="form-label">
                                    Longitude
                                </label>
                                <input
                                    id="longitude"
                                    type="number"
                                    step="any"
                                    className="form-input"
                                    value={newStation.longitude}
                                    onChange={(e) => {
                                        setNewStation({ ...newStation, longitude: e.target.value });
                                        if (newStation.latitude && e.target.value) {
                                            setTempMarker({
                                                lat: parseFloat(newStation.latitude),
                                                lng: parseFloat(e.target.value),
                                            });
                                        }
                                    }}
                                    placeholder="15.9819"
                                    required
                                />
                            </div>

                            <div className="button-group">
                                <button
                                    type="button"
                                    onClick={handleCloseModal}
                                    disabled={creating}
                                    className="btn-cancel"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={creating}
                                    className="btn-submit"
                                >
                                    {creating ? 'Creating...' : 'Create'}
                                </button>
                            </div>
                        </form>
                    </div>
                </>
            )}

            <MapContainer
                center={mapCenter}
                zoom={7}
                minZoom={7}
                maxBounds={CROATIA_BOUNDS}
                maxBoundsViscosity={0.8}
                className="map-container"
                dragging={isLoggedIn}
                zoomControl={isLoggedIn}
                scrollWheelZoom={isLoggedIn}
                doubleClickZoom={isLoggedIn}
                touchZoom={isLoggedIn}
                boxZoom={isLoggedIn}
            >
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                />

                <LocationPicker
                    onLocationPick={handleMapClick}
                    enabled={showModal && !isSelectingArea}
                />
                <PolygonSelector
                    isSelecting={isSelectingArea}
                    onPolygonComplete={handlePolygonComplete}
                    onAddPoint={handleAddPoint}
                />

                {areaPolygon && (
                    <Polygon
                        positions={areaPolygon.map(p => [p.lat, p.lng])}
                        pathOptions={{
                            color: '#ff9800',
                            weight: 3,
                            fillOpacity: 0.15,
                        }}
                    />
                )}

                {tempMarker && showModal && (
                    <Marker
                        position={[tempMarker.lat, tempMarker.lng]}
                        icon={new L.Icon({
                            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
                            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                            iconSize: [25, 41],
                            iconAnchor: [12, 41],
                            popupAnchor: [1, -34],
                            shadowSize: [41, 41],
                        })}
                    >
                        <Popup>
                            <strong>New Station Location</strong>
                            <br />
                            {tempMarker.lat.toFixed(6)}, {tempMarker.lng.toFixed(6)}
                        </Popup>
                    </Marker>
                )}

                {filteredStations.map((station) => (
                    <Marker
                        key={station.stationId}
                        position={[station.latitude, station.longitude]}
                    >
                        <Popup>
                            <div className="popup-content">
                                <h3 className="popup-title">{station.stationName}</h3>
                                <p className="popup-info">
                                    <strong>ID:</strong> {station.stationId}
                                </p>
                                <p className="popup-info">
                                    <strong>Location:</strong> {station.latitude.toFixed(4)}, {station.longitude.toFixed(4)}
                                </p>
                                <p className="popup-info">
                                    <strong>Active from:</strong> {new Date(station.activeFrom).toLocaleDateString('hr-HR')}
                                </p>
                                <p className="popup-info">
                                    <strong>Active to:</strong> {new Date(station.activeTo).toLocaleDateString('hr-HR')}
                                </p>

                                <button
                                    onClick={() => handleViewMeasurements(station.stationId, station.stationName)}
                                    className="popup-btn view"
                                >
                                    View Measurements
                                </button>

                                <button
                                    onClick={() => handleDeleteStation(station.stationId, station.stationName)}
                                    disabled={deleting === station.stationId}
                                    className="popup-btn delete"
                                >
                                    {deleting === station.stationId ? 'Deleting...' : 'Delete Station'}
                                </button>
                            </div>
                        </Popup>
                    </Marker>
                ))}

                {isLoggedIn && !loading && filteredStations.length === 0 && !showModal && !isSelectingArea && (
                    <div className="empty-state">
                        {searchTerm || areaPolygon ? 'No stations matching filters' : 'No stations available'}
                    </div>
                )}
            </MapContainer>
        </div>
    );
}

export default MapView;
