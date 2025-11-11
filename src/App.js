import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

import Navbar from './components/navigation/Navbar';
import Footer from './components/navigation/Footer';
import Tabs from './components/navigation/Tabs';
import Sidebar from './components/mainView/Sidebar';
import MapView from './components/mainView/MapView';
import TableView from './components/mainView/TableView';
import AdminPage from './components/admin/AdminPage';
import LoginPage from './components/login/LoginPage';


function App() {
    const [activeTab, setActiveTab] = useState('map');
    const tabNames = ['map', 'table', 'charts'];

    return (
        <Router>
            <Navbar />
            <Routes>
                <Route path="/admin" element={<AdminPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/" element={
                    <div className="app-container">
                        <Tabs tabs={tabNames} activeTab={activeTab} setActiveTab={setActiveTab} />
                        <div className="main-layout" style={{ display: 'flex', height: 'calc(100vh - 100px)' }}>
                            <Sidebar />
                            {activeTab === 'map' ? <MapView /> : <TableView />}
                        </div>
                    </div>
                } />
            </Routes>
            <Footer />
        </Router>
    );
}
export default App;
