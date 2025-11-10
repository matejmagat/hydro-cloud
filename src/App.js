import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

import Navbar from './components/Navbar';
import Tabs from './components/Tabs';
import Sidebar from './components/Sidebar';
import MapView from './components/MapView';
import TableView from './components/TableView';
import AdminPage from './components/AdminPage';
import LoginPage from './components/LoginPage';
import Footer from './components/Footer';




function App() {
    const [activeTab, setActiveTab] = useState('map');
    return (
        <Router>
            <Navbar />
            <Routes>
                <Route path="/admin" element={<AdminPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/" element={
                    <div className="app-container">
                        <Tabs activeTab={activeTab} setActiveTab={setActiveTab} />
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
