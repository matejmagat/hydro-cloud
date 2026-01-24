const API_URL = process.env.REACT_APP_API_URL;

export const stationsService = {
    async getAllStations() {
        const token = localStorage.getItem('authToken');
        
        const response = await fetch(`${API_URL}/stations`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch stations');
        }

        return await response.json();
    },

    async getStationById(stationId) {
        const token = localStorage.getItem('authToken');
        
        const response = await fetch(`${API_URL}/stations/${stationId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch station ${stationId}`);
        }

        return await response.json();
    },

    async createStation(stationData) {
        const token = localStorage.getItem('authToken');
        
        const response = await fetch(`${API_URL}/stations`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(stationData)
        });

        if (!response.ok) {
            throw new Error('Failed to create station');
        }

        return await response.json();
    },

    async deleteStation(stationId) {
        const token = localStorage.getItem('authToken');
        
        const response = await fetch(`${API_URL}/stations/${stationId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to delete station ${stationId}`);
        }
    }
};