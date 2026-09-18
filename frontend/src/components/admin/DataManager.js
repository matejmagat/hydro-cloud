import React, { useState, useRef } from 'react';
import './dataManager.css';

const API_URL = process.env.REACT_APP_API_URL;

function DataManager() {
    const [dragActive, setDragActive] = useState(false);
    const [error, setError] = useState(null);
    const [fileName, setFileName] = useState(null);
    const [message, setMessage] = useState(null); // For server response
    const [isUploading, setIsUploading] = useState(false);

    // Reference to the hidden file input
    const inputRef = useRef(null);

    const handleDrag = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === "dragenter" || e.type === "dragover") {
            setDragActive(true);
        } else if (e.type === "dragleave") {
            setDragActive(false);
        }
    };

    const handleDrop = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);

        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            validateAndUpload(e.dataTransfer.files[0]);
        }
    };

    const handleChange = (e) => {
        e.preventDefault();
        if (e.target.files && e.target.files[0]) {
            validateAndUpload(e.target.files[0]);
        }
    };

    const validateAndUpload = (file) => {
        setError(null);
        setMessage(null);

        // Validate JSON extension
        if (file.type !== "application/json" && !file.name.endsWith('.json')) {
            setError("Invalid file format. Please upload a .json file.");
            setFileName(null);
            return;
        }

        setFileName(file.name);
        uploadFile(file);
    };

    const uploadFile = (file) => {
        setIsUploading(true);
        const reader = new FileReader();

        reader.onload = async (e) => {
            try {
                // 1. Parse the file content
                const jsonData = JSON.parse(e.target.result);
                const token = localStorage.getItem('authToken');

                // 2. Send POST request
                const response = await fetch(`${API_URL}/measurements/bulk`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(jsonData)
                });

                if (!response.ok) {
                    throw new Error('Upload failed');
                }

                // 3. Set success message
                setMessage('Data imported successfully!');

            } catch (err) {
                console.error("Upload error:", err);
                setError(err.message || 'Error processing file upload.');
            } finally {
                setIsUploading(false);
            }
        };

        reader.onerror = () => {
            setError("Failed to read the file.");
            setIsUploading(false);
        };

        reader.readAsText(file);
    };

    const onButtonClick = () => {
        inputRef.current.click();
    };

    return (
        <div className="dm-container">
            <div className="dm-header">
                <div className="dm-title">
                    <span className="dm-icon">↑</span> IMPORT DATA
                </div>
            </div>

            <div
                className={`dm-drop-zone ${dragActive ? "active" : ""}`}
                onDragEnter={handleDrag}
                onDragLeave={handleDrag}
                onDragOver={handleDrag}
                onDrop={handleDrop}
            >
                <div className="dm-drop-content">
                    <div className="dm-cloud-icon">
                        {isUploading ? '⏳' : '☁'}
                    </div>

                    <h3>
                        {isUploading
                            ? 'Uploading...'
                            : 'Drag & Drop a backup to import it'}
                    </h3>

                    <button
                        className="dm-import-btn"
                        onClick={onButtonClick}
                        disabled={isUploading}
                    >
                        {isUploading ? 'PLEASE WAIT' : 'IMPORT FROM'}
                    </button>

                    {/* Hidden Input */}
                    <input
                        ref={inputRef}
                        type="file"
                        accept=".json"
                        onChange={handleChange}
                        style={{ display: 'none' }}
                    />

                    {fileName && (
                        <div className="dm-file-selected">
                            Selected: <strong>{fileName}</strong>
                        </div>
                    )}
                </div>
            </div>

            <div className="dm-footer-info">
                Maximum upload file size: <strong>Unlimited</strong>.
            </div>

            {/* Error & Success Message Space */}
            <div className="dm-error-container">
                {error && <div className="dm-error-message" style={{ color: 'red' }}>{error}</div>}
                {message && <div className="dm-success-message" style={{ color: 'green', marginTop: '10px' }}>{message}</div>}
            </div>
        </div>
    );
}

export default DataManager;
