import React, {useState} from 'react';
import Tabs from "../navigation/Tabs";
import DataManager from "./DataManager";
import UserManager from "./UserManager";


function AdminPage() {
    const [activeTab, setActiveTab] = useState('data');
    const tabNames = ['data', 'users'];

    return (

        <div>
            <Tabs tabs={tabNames} activeTab={activeTab} setActiveTab={setActiveTab} />
            {activeTab === 'data' ? <DataManager /> : <UserManager />}
        </div>
    );
}

export default AdminPage;
