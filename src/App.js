import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

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
import { TabProvider, useTab } from './context/TabContext';
import { FilterProvider } from './context/FilterContext';


function MainView() {
    const { activeTab, setActiveTab } = useTab();
    const tabNames = ['map', 'table', 'charts'];
    const views = {
        map: <MapView />,
        table: <TableView />,
        charts: <ChartView />
    };

    return (
        <div className="app-container">
            <div className="main-layout" style={{ display: 'flex', height: 'calc(100vh - 160px)', overflow: 'hidden' }}>
                <Sidebar tabs={tabNames} activeTab={activeTab} setActiveTab={setActiveTab}/>
                {views[activeTab] || null}
            </div>
        </div>
    );
}

function App() {
    return (
        <FilterProvider>
            <TabProvider>
                <Router>
                    <Navbar />
                    <Routes>
                        <Route path="/admin" element={<AdminPage />} />
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/profile" element={<ProfilePage />} />
                        <Route path="/verify-2fa" element={<Verify2FAPage />} />
                        <Route path="/" element={<MainView />} />
                    </Routes>
                    <Footer />
                </Router>
            </TabProvider>
        </FilterProvider>
    );
}

export default App;
