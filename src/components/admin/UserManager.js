import React, { useEffect, useState } from 'react'

function UserManager() {
    // 1. Define API URL and State
    const API_URL = process.env.REACT_APP_API_URL;
    const [users, setUsers] = useState([]);
    const [error, setError] = useState(null);

    // 2. Fetch users on first load [web:10][web:12]
    useEffect(() => {
        const fetchUsers = async () => {
            const token = localStorage.getItem('authToken');

            try {
                const response = await fetch(`${API_URL}/user/all-users`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`, // [web:3][web:7]
                        'Content-Type': 'application/json'
                    }
                });

                if (!response.ok) {
                    throw new Error(`Error: ${response.status}`);
                }

                const data = await response.json();
                setUsers(data);
            } catch (err) {
                console.error("Failed to fetch users:", err);
                setError(err.message);
            }
        };

        fetchUsers();
    }, [API_URL]);

    // 3. Delete user handler [web:1][web:5]
    const handleDelete = async (userId) => {
        const token = localStorage.getItem('authToken');

        try {
            const response = await fetch(`${API_URL}/user/${userId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                // Update UI by removing the deleted user from state
                setUsers(prevUsers => prevUsers.filter(user => user.id !== userId));
            } else {
                console.error("Failed to delete user");
            }
        } catch (err) {
            console.error("Error deleting user:", err);
        }
    };

    return (
        <div style={{padding: '24px'}}>
            <h2>User Data</h2>
            {error && <p style={{color: 'red'}}>Failed to load users: {error}</p>}

            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                <tr>
                    <th style={{border: '1px solid #222', padding: '8px'}}>Username</th>
                    {/* API does not return Admin status, so we display 2FA status instead */}
                    <th style={{border: '1px solid #222', padding: '8px'}}>2FA Enabled</th>
                    <th style={{border: '1px solid #222', padding: '8px'}}>Actions</th>
                </tr>
                </thead>
                <tbody>
                {users.map(user => (
                    <tr key={user.id}>
                        <td style={{border: '1px solid #222', padding: '8px'}}>
                            {user.username}
                        </td>
                        <td style={{border: '1px solid #222', padding: '8px'}}>
                            {/* Display visual indicator for boolean field */}
                            <button>{user['2FAEnabled'] ? 'Yes' : 'No'}</button>
                        </td>
                        <td style={{border: '1px solid #222', padding: '8px'}}>
                            <button onClick={() => handleDelete(user.id)}>
                                Delete
                            </button>
                        </td>
                    </tr>
                ))}

                {/* Fallback if list is empty */}
                {users.length === 0 && !error && (
                    <tr>
                        <td colSpan="3" style={{border: '1px solid #222', padding: '8px', textAlign: 'center'}}>
                            No users found
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

export default UserManager;
