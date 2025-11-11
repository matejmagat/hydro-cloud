import React from 'react';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';

// Register chart components
ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

function ChartView() {
    // Dummy data representing hydrological measurements over months
    const data = {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'],
        datasets: [
            {
                label: 'Water Flow (m³/s)',
                data: [120, 130, 98, 150, 170, 160, 140],
                borderColor: 'rgba(75,192,192,1)',
                fill: false,
                tension: 0.4,
            },
        ],
    };

    const options = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            },
            title: {
                display: true,
                text: 'Hydrological Data - Monthly Water Flow',
            },
        },
        scales: {
            y: {
                beginAtZero: true,
            },
        },
    };

    return (
        <div style={{ width: '90%', maxWidth: 700, margin: '20px auto' }}>
            <div style={{ marginBottom: '24px' }}>
                <label> Chart type </label>
                <select defaultValue="">
                    <option value="" disabled>
                        Select
                    </option>
                    <option value="line1">Line</option>
                </select>
            </div>
            <Line data={data} options={options} />
            <button>Export Chart</button>
        </div>
    );
}

export default ChartView;
