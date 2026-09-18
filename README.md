# HydroCloud

**HydroCloud** is a full-stack web application for managing, exploring, and visualizing hydrological measurement data. It provides authenticated users and administrators with tools to browse monitoring stations, filter time-series measurements, view charts and maps, export data, and manage user access.

> **Project context:** This was a university team project developed collaboratively at FER, University of Zagreb. It is included in my portfolio as an example of collaborative full-stack development, backend API integration, and data-driven application design.

## My Contribution

My primary work focused on the **React frontend** and its integration with backend APIs, alongside several backend-facing data-query features.

- Built and refined dashboard views for hydrological data: table, chart, map, admin, profile, authentication, and registration flows.
- Implemented backend-driven filtering for measurement data, including date filtering and querying measurements across multiple station IDs.
- Added and integrated station search and measurement-type statistics endpoints.
- Moved client-side filtering logic toward backend API queries to improve scalability and reduce unnecessary client-side processing.
- Implemented CSV export for filtered measurement data and PNG export for charts.
- Added UI support for bulk measurement import, user-role updates, user management, profile updates, and authentication error handling.
- Added local-development CORS support for the React application.
- Organized the project into a monorepo structure and maintained project documentation.

The Git history is intentionally preserved to show the collaborative development process and the scope of individual contributions.

## Features

### Data Exploration

- Browse hydrological monitoring stations on an interactive map.
- Search and filter stations by name and geographic area.
- View measurement data in paginated tables.
- Filter measurements by station, date range, and measurement type.
- Query measurements for multiple stations in one request.
- Visualize time-series data in charts.
- Export filtered data to CSV.
- Download chart visualizations as PNG images.

### User and Administration

- User registration and login.
- JWT-based authentication and role-based access control.
- Two-factor authentication support.
- User profile viewing and updates.
- Administrative user listing, deletion, and role management.
- Bulk measurement import from JSON, CSV, and Excel files.

## Screenshots

The interface provides separate views for exploring hydrological data, visualizing time-series measurements, locating stations on a map, and managing users and data imports.

> Screenshots below illustrate the React frontend developed as part of the HydroCloud team project.
![](frontend/hydro_cloud.png) 

## Tech Stack

| Area | Technologies |
|---|---|
| Backend | Java, Spring Boot, Spring Security |
| API | REST, OpenAPI / Swagger |
| Authentication | JWT, two-factor authentication |
| Database | PostgreSQL, JPA / Hibernate |
| Frontend | React, JavaScript, CSS |
| Data visualization | Charts, map-based station exploration |
| Testing | Integration testing, Testcontainers |
| Development and deployment | Docker Compose, Maven, Git |

## Architecture

HydroCloud is organized as a monorepo with separate frontend and backend applications.

```text
hydro-cloud/
├── backend/        # Spring Boot REST API and PostgreSQL integration
├── frontend/       # React web application
└── README.md
```

The backend exposes REST endpoints for authentication, users, stations, measurements, filtering, statistics, and bulk data import. The frontend consumes these APIs to provide interactive data exploration and administrative workflows.

For backend-specific setup and API documentation, see [`backend/README.md`](backend/README.md).

## Local Setup

### Prerequisites

- Java and Maven
- Node.js and npm
- Docker and Docker Compose
- PostgreSQL, or Docker Compose for the local database environment

### Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

Consult the backend README for database configuration, Swagger/OpenAPI access, and other backend-specific instructions.

### Run the frontend

```bash
cd frontend
npm install
npm start
```

The frontend development server is configured to communicate with the backend during local development.

## API Highlights

Examples of backend capabilities available to the frontend include:

- Authentication and registration endpoints.
- User profile and administration endpoints.
- Station retrieval and station search.
- Measurement retrieval with date and station filters.
- Measurement-type statistics.
- Paginated measurement responses.
- Bulk measurement upload from JSON, CSV, and Excel files.

## Team

HydroCloud was developed as a collaborative university project.

- [Matej Magat](https://github.com/matejmagat) — React frontend, dashboard and data visualization workflows, frontend-backend integration, measurement filtering features, exports, selected API-related features, documentation, and monorepo organization.
- [Dean Trkulja](https://github.com/Trki137) — backend security, JWT authentication, two-factor authentication, user-management functionality, domain-model work, API documentation, testing, and Docker-related setup.
- [Ana Vuksanović](https://github.com/ana-vuksanovic) — station and measurement CRUD functionality, database connectivity configuration, and initial domain model work.
- [Dario Tomšić](https://github.com/datom18) — bulk measurement import and persistence support.
- Additional contributions are visible in the repository commit history.
- [Tara Baće](https://github.com/bacetara)
- [Martin Bugarin](https://github.com/martinbugarin/)
- [Fran Horvat](https://github.com/fhorvat23)
- [Viktor Kapelina](Viktor.Kapelina@fer.hr)
- [Matej Marić](https://github.com/mm55104)
- [Nikola Trebus](Nikola.Trebus@fer.hr)
- [Anton Vivoda](https://github.com/antonVivoda)

## Portfolio Notes

This repository demonstrates experience with:

- Building and consuming REST APIs.
- Spring Boot backend development and layered application design.
- Relational database integration with PostgreSQL and JPA.
- JWT authentication, role-based authorization, and two-factor authentication.
- React frontend development and API integration.
- Server-side filtering, pagination, export workflows, and bulk data ingestion.
- Docker-based local development and integration testing.
- Collaborative Git workflows, feature branches, merges, and shared ownership of a codebase.

## License

This project was created for academic purposes. Please contact the repository owner or project contributors before reusing substantial parts of the code.
