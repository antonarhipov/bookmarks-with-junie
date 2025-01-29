# Bookmarks Manager

> **Note**: This project is entirely generated using Junie, with no manual code writing involved. All features, implementations, and documentation are auto-generated to ensure consistency and maintainability.

A web application for managing bookmarks with folder organization, lazy loading, and advanced search capabilities.

## Features

- Folder organization for bookmarks
- Lazy loading for better performance
- Search functionality
- Sort bookmarks by various attributes
- Multi-select for bulk operations
- Responsive design

## Technology Stack

### Backend
- Spring Boot
- Java 17
- Spring Data JDBC
- H2 Database

### Frontend
- TypeScript
- Tailwind CSS
- Modern web components

## Setup Instructions

### Prerequisites
- JDK 17 or higher
- Node.js and npm
- Gradle 

### Backend Setup
1. Clone the repository
2. Navigate to the project root
3. Run the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd src/main/resources/static
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Build TypeScript files:
   ```bash
   npm run build
   ```
   Or for development with watch mode:
   ```bash
   npm run watch
   ```

## API Endpoints

### Bookmarks

- `GET /api/bookmarks` - Get all bookmarks
- `GET /api/bookmarks/{id}` - Get bookmark by ID
- `GET /api/bookmarks/folder/{folderId}` - Get bookmarks in folder (paginated)
  - Parameters:
    - `page` (default: 0)
    - `size` (default: 20)
    - `sortBy` (default: "title")
    - `sortDir` (default: "asc")
- `POST /api/bookmarks` - Create new bookmark
- `PUT /api/bookmarks/{id}` - Update bookmark
- `DELETE /api/bookmarks/{id}` - Delete bookmark
- `DELETE /api/bookmarks/bulk` - Delete multiple bookmarks
- `GET /api/bookmarks/search` - Search bookmarks (paginated)
  - Parameters:
    - `query`
    - `page` (default: 0)
    - `size` (default: 20)
    - `sortBy` (default: "title")
    - `sortDir` (default: "asc")

### Folders

- `GET /api/folders` - Get all folders
- `GET /api/folders/{id}` - Get folder by ID
- `POST /api/folders` - Create new folder
- `PUT /api/folders/{id}` - Update folder
- `DELETE /api/folders/{id}` - Delete folder
- `GET /api/folders/search` - Search folders
- `GET /api/folders/with-count` - Get folders with bookmark counts

## Development

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── org/crud/bookmarks/
│   │       ├── controller/
│   │       ├── service/
│   │       └── repository/
│   └── resources/
│       └── static/
│           ├── js/
│           │   ├── components/
│           │   ├── types.ts
│           │   ├── api.ts
│           │   └── main.ts
│           ├── css/
│           └── index.html
```

### Building for Production
1. Build the frontend:
   ```bash
   cd src/main/resources/static
   npm run build
   ```
2. Build the application:
   ```bash
   ./gradlew build
   ```
3. The final JAR will be in `build/libs/`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
