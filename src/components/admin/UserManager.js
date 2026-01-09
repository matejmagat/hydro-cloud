import React from 'react'

function UserManager() {

    return (
        <div style={{padding: '24px'}}>
            <h2>User Data</h2>
            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                <tr>
                    <th style={{border: '1px solid #222', padding: '8px'}}>Username</th>
                    <th style={{border: '1px solid #222', padding: '8px'}}>Admin</th>
                    <th style={{border: '1px solid #222', padding: '8px'}}></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td style={{border: '1px solid #222', padding: '8px'}}>Ivan</td>
                    <td style={{border: '1px solid #222', padding: '8px'}}><button>No</button></td>
                    <td style={{border: '1px solid #222', padding: '8px'}}><button>Delete</button></td>
                </tr>
                <tr>
                    <td style={{border: '1px solid #222', padding: '8px'}}>Marko</td>
                    <td style={{border: '1px solid #222', padding: '8px'}}><button>Yes</button></td>
                    <td style={{border: '1px solid #222', padding: '8px'}}><button>Delete</button></td>
                </tr>
                </tbody>
            </table>
        </div>
    );
}

export default UserManager;