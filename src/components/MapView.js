import { MapContainer, TileLayer } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const position = [45.8150, 15.9819]; // Example coordinates

function MapView() {
    return (
        <MapContainer
            center={position}
            zoom={13}
            style={{ height: "100%", width: "100%" }}
        >
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {/* Add markers, popups, etc if needed */}
        </MapContainer>
    );
}

export default MapView;
