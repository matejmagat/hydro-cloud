import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

import Navbar from './components/navigation/Navbar';
import Footer from './components/navigation/Footer';
import Tabs from './components/navigation/Tabs';
import Sidebar from './components/mainView/Sidebar';
import MapView from './components/mainView/MapView';
import TableView from './components/mainView/TableView';
import ChartView from "./components/mainView/ChartView";
import AdminPage from './components/admin/AdminPage';
import LoginPage from './components/login/LoginPage';
import RegisterPage from "./components/login/RegisterPage";
import ProfilePage from "./components/login/ProfilePage";
import Verify2FAPage from "./components/login/Verify2FAPage";


function App() {
    const [activeTab, setActiveTab] = useState('map');
    const tabNames = ['map', 'table', 'charts'];
    const views = {
        map: <MapView />,
        table: <TableView />,
        charts: <ChartView />
    };

    return (
        <Router>
            <Navbar />
            <Routes>
                <Route path="/admin" element={<AdminPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/verify-2fa" element={<Verify2FAPage />} />
                <Route path="/" element={
                    <div className="app-container">
                        <Tabs tabs={tabNames} activeTab={activeTab} setActiveTab={setActiveTab} />
                        <div className="main-layout" style={{ display: 'flex', height: 'calc(100vh - 100px)' }}>
                            <Sidebar />
                            {views[activeTab] || null}
                        </div>
                    </div>
                } />
            </Routes>
            <Footer />
        </Router>
    );
}
export default App;
