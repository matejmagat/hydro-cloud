import React, { useEffect, useState } from 'react'

function UserManager() {
    // 1. Define API URL and State
    const API_URL = process.env.REACT_APP_API_URL;
    const [users, setUsers] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);

    // Defined roles constant for easy mapping
    const AVAILABLE_ROLES = [
        "ADMIN",
        "USER_MANAGER",
        "DATA_MANAGER",
        "USER"
    ];

    // 2. Fetch users AND their individual roles on load
    useEffect(() => {
        const fetchUsersAndRoles = async () => {
            const token = localStorage.getItem('authToken');
            setLoading(true);

            try {
                // Step A: Get the list of all users
                const listResponse = await fetch(`${API_URL}/user/all-users`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (!listResponse.ok) {
                    throw new Error(`Error fetching list: ${listResponse.status}`);
                }

                const basicUsersList = await listResponse.json();

                // Step B: For each user, fetch their specific profile to get the correct ROLE
                // We map the list of users to a list of Promises
                const detailedUserPromises = basicUsersList.map(async (user) => {
                    try {
                        const detailResponse = await fetch(`${API_URL}/user/${user.id}`, {
                            method: 'GET',
                            headers: {
                                'Authorization': `Bearer ${token}`,
                                'Content-Type': 'application/json'
                            }
                        });

                        // If successful, use the detailed user object (which has the correct role)
                        if (detailResponse.ok) {
                            return await detailResponse.json();
                        }
                        // If specific fetch fails, fall back to the basic info we already have
                        return user;
                    } catch (err) {
                        console.error(`Failed to fetch details for user ${user.id}`, err);
                        return user;
                    }
                });

                // Wait for all individual requests to finish
                const detailedUsers = await Promise.all(detailedUserPromises);

                setUsers(detailedUsers);
            } catch (err) {
                console.error("Failed to fetch users:", err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchUsersAndRoles();
    }, [API_URL]);

    // 3. Handle Role Update
    const handleRoleUpdate = async (userId, newRole) => {
        const token = localStorage.getItem('authToken');

        // Optimistic UI update: update the dropdown immediately
        const previousUsers = [...users];
        setUsers(prevUsers => prevUsers.map(user =>
            user.id === userId ? { ...user, role: newRole } : user
        ));

        try {
            const response = await fetch(`${API_URL}/user/update-role`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    role: newRole,
                    userId: userId
                })
            });

            if (!response.ok) {
                // Revert changes if API fails
                setUsers(previousUsers);
                console.error("Failed to update role");
                alert("Failed to update role. Please try again.");
            }
        } catch (err) {
            // Revert changes on error
            setUsers(previousUsers);
            console.error("Error updating role:", err);
            alert("Error connecting to server.");
        }
    };

    // 4. Delete user handler
    const handleDelete = async (userId) => {
        const token = localStorage.getItem('authToken');

        if (!window.confirm("Are you sure you want to delete this user?")) return;

        try {
            const response = await fetch(`${API_URL}/user/${userId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                setUsers(prevUsers => prevUsers.filter(user => user.id !== userId));
            } else {
                console.error("Failed to delete user");
                alert("Failed to delete user.");
            }
        } catch (err) {
            console.error("Error deleting user:", err);
        }
    };

    if (loading) return <div style={{padding: '24px'}}>Loading users...</div>;

    return (
        <div style={{padding: '24px'}}>
            <h2>User Data</h2>
            {error && <p style={{color: 'red'}}>Failed to load users: {error}</p>}

            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                <tr>
                    <th style={{border: '1px solid #222', padding: '8px'}}>Username</th>
                    <th style={{border: '1px solid #222', padding: '8px'}}>Role</th>
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
                            <select
                                value={user.role || 'USER'}
                                onChange={(e) => handleRoleUpdate(user.id, e.target.value)}
                                style={{ padding: '4px', width: '100%' }}
                            >
                                {AVAILABLE_ROLES.map(role => (
                                    <option key={role} value={role}>
                                        {role}
                                    </option>
                                ))}
                            </select>
                        </td>
                        <td style={{border: '1px solid #222', padding: '8px', textAlign: 'center'}}>
                            <button
                                onClick={() => handleDelete(user.id)}
                                style={{ backgroundColor: '#ff4d4f', color: 'white', border: 'none', padding: '5px 10px', cursor: 'pointer' }}
                            >
                                Delete
                            </button>
                        </td>
                    </tr>
                ))}

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
