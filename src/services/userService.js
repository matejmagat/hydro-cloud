const API_URL = process.env.REACT_APP_API_URL;

export const userService = {
    async getCurrentUser() {
        const token = localStorage.getItem('authToken');
        
        if (!token) {
            return null;
        }
        
        const response = await fetch(`${API_URL}/user/me`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch current user');
        }

        return await response.json();
    }
};