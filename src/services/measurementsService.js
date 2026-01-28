const API_URL = process.env.REACT_APP_API_URL;

export const measurementsService = {
    async getAllMeasurements(stationId = null, typeId = null) {
        const token = localStorage.getItem('authToken');
        
        const params = new URLSearchParams();
        if (stationId) params.append('stationId', stationId);
        if (typeId) params.append('typeId', typeId);
        
        const queryString = params.toString();
        const url = `${API_URL}/measurements${queryString ? '?' + queryString : ''}`;
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch measurements');
        }

        return await response.json();
    },

    async getMeasurements(stationId = null, typeId = null,
                          startDate = null, endDate = null,
                          page = 0, size = null) {
        const token = localStorage.getItem('authToken');

        const params = new URLSearchParams();
        if (stationId) params.append('stationId', stationId);
        if (typeId) params.append('typeId', typeId);
        if (startDate) params.append('fromDate', new Date(startDate).toISOString());
        if (endDate) params.append('toDate', new Date(endDate).toISOString());
        if (page) params.append('page', page.toString());
        if (size) params.append('size', size.toString());

        const queryString = params.toString();
        const url = `${API_URL}/measurements${queryString ? '?' + queryString : ''}`;

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch measurements');
        }

        return await response.json();
    },

    async getMeasurementsTypes(stationId = null, typeId = null,
                          startDate = null, endDate = null) {
        const token = localStorage.getItem('authToken');

        const params = new URLSearchParams();
        if (stationId) params.append('stationId', stationId);
        if (typeId) params.append('typeId', typeId);
        if (startDate) params.append('fromDate', new Date(startDate).toISOString());
        if (endDate) params.append('toDate', new Date(endDate).toISOString());

        const queryString = params.toString();
        const url = `${API_URL}/measurements/types${queryString ? '?' + queryString : ''}`;

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch measurements');
        }

        return await response.json();
    },

    async createMeasurement(measurementData) {
        const token = localStorage.getItem('authToken');
        
        const response = await fetch(`${API_URL}/measurements`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(measurementData)
        });

        if (!response.ok) {
            throw new Error('Failed to create measurement');
        }

        return await response.json();
    },

    async getMeasurementById(measurementId) {
        const token = localStorage.getItem('authToken');
        
        const response = await fetch(`${API_URL}/measurements/${measurementId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch measurement ${measurementId}`);
        }

        return await response.json();
    },

    async deleteMeasurement(measurementId) {
        const token = localStorage.getItem('authToken');
        
        const response = await fetch(`${API_URL}/measurements/${measurementId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to delete measurement ${measurementId}`);
        }
    }
};