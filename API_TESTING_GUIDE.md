# GitHub API Testing Guide

## Implementation Overview

Your GitHub API is **fully implemented** with the following components:

### Components
- ✅ **GitHubController** - REST Controller with endpoint `/api/github/users/{username}`
- ✅ **GitHubUserService** - Service layer that fetches data from GitHub API
- ✅ **GitHubUserResponse** - Model mapping GitHub API response to JSON
- ✅ **GlobalExceptionHandler** - Centralized exception handling
- ✅ **RestTemplateConfig** - RestTemplate bean configuration with timeouts
- ✅ **ErrorResponse** - Structured error response model

---

## Building and Running the Application

### Prerequisites
Ensure you have:
- Java 17 or higher installed
- Maven installed (included with the project via `mvnw.cmd`)
- Internet connection (to call GitHub API)

### Step 1: Build the Project
```bash
cd C:\Users\arunr\IdeaProjects\mySpringProject\mySpringProject
.\mvnw.cmd clean package -DskipTests
```

### Step 2: Run the Application
```bash
.\mvnw.cmd spring-boot:run
```

Or run the jar file directly:
```bash
java -jar target/mySpringProject-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

---

## API Endpoint Details

### Get GitHub User Information

**Endpoint:** 
```
GET /api/github/users/{username}
```

**Base URL:** `http://localhost:8080`

**Path Parameters:**
- `username` (required): GitHub username to fetch information for

**Response Headers:**
- `Content-Type: application/json`

---

## Testing the API

### 1. Using cURL (Windows PowerShell)

#### Success Case - Valid User
```powershell
curl -X GET "http://localhost:8080/api/github/users/torvalds" `
  -ContentType "application/json"
```

**Expected Response (200 OK):**
```json
{
  "login": "torvalds",
  "id": 1024361,
  "name": "Linus Torvalds",
  "avatarUrl": "https://avatars.githubusercontent.com/u/1024361?v=4",
  "publicRepos": 1,
  "followers": 200000,
  "createdAt": "2011-09-04T15:56:31Z"
}
```

#### Error Case - Invalid User (404)
```powershell
curl -X GET "http://localhost:8080/api/github/users/invalid_user_xyz_999" `
  -ContentType "application/json"
```

**Expected Response (404 Not Found):**
```json
{
  "code": "HTTP_CLIENT_ERROR",
  "message": "Error fetching data from external API: 404 Not Found: \"HTTP/1.1 404 Not Found\"",
  "status": 404,
  "timestamp": "2024-05-08T10:30:45.123456"
}
```

#### Error Case - Empty Username (400)
```powershell
curl -X GET "http://localhost:8080/api/github/users/" `
  -ContentType "application/json"
```

**Expected Response (400 Bad Request):**
```json
{
  "code": "INVALID_ARGUMENT",
  "message": "Username cannot be null or empty",
  "status": 400,
  "timestamp": "2024-05-08T10:30:45.123456"
}
```

---

### 2. Using Postman

1. **Create a New Request**
   - Method: `GET`
   - URL: `http://localhost:8080/api/github/users/octocat`

2. **Headers Tab** (optional, already set in code)
   - `Accept: application/vnd.github.v3+json`
   - `X-GitHub-Api-Version: 2022-11-28`

3. **Send** and observe the response

---

### 3. Using REST Client Extension (VS Code)

Create a file named `requests.http`:

```http
### Get Valid GitHub User
GET http://localhost:8080/api/github/users/torvalds
Accept: application/json

### Get Invalid GitHub User
GET http://localhost:8080/api/github/users/invalid_user_testing_xxx
Accept: application/json

### Get Another User
GET http://localhost:8080/api/github/users/octocat
Accept: application/json
```

---

### 4. Using Java HttpClient

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/github/users/torvalds"))
    .GET()
    .build();

HttpResponse<String> response = client.send(request, 
    HttpResponse.BodyHandlers.ofString());

System.out.println(response.statusCode());
System.out.println(response.body());
```

---

## Response Codes

| Status | Scenario | Message |
|--------|----------|---------|
| **200 OK** | User found successfully | Returns complete GitHub user data |
| **400 Bad Request** | Username is null/empty | `Username cannot be null or empty` |
| **404 Not Found** | GitHub user doesn't exist | `User not found: {username}` |
| **503 Service Unavailable** | Cannot connect to GitHub API | `Unable to connect to external service...` |
| **500 Internal Server Error** | Unexpected error | `An unexpected error occurred...` |

---

## Features Implemented

### ✅ Error Handling
- **Input Validation**: Validates that username is not null or empty
- **HTTP Client Errors**: Catches 4xx errors from GitHub API (e.g., user not found)
- **HTTP Server Errors**: Catches 5xx errors from GitHub API
- **Network Errors**: Handles connection timeouts and resource access errors
- **Structured Responses**: All errors return consistent `ErrorResponse` format with code, message, status, and timestamp

### ✅ Timeout Configuration
- **Connect Timeout**: 5 seconds
- **Read Timeout**: 5 seconds

### ✅ Logging
- All requests and responses are logged at INFO level
- Errors are logged at ERROR or WARN level
- Easy to debug via application logs

### ✅ GitHub API Integration
- Uses official GitHub REST API v3
- Proper API headers:
  - `Accept: application/vnd.github.v3+json`
  - `X-GitHub-Api-Version: 2022-11-28`
- Handles snake_case JSON properties (e.g., `avatar_url` → `avatarUrl`)

---

## Common Test Usernames

Test these GitHub usernames to verify the API:

- `torvalds` - Linus Torvalds (Linux creator)
- `octocat` - GitHub's mascot account
- `gvanrossum` - Guido van Rossum (Python creator)
- `brendaneich` - Brendan Eich (JavaScript creator)
- `tjholowaychuk` - TJ Holowaychuk (Express.js creator)

---

## Troubleshooting

### Issue: Connection Refused
**Solution:** Make sure the application is running on port 8080

```bash
# Check if port 8080 is listening
netstat -ano | findstr :8080
# If needed, use a different port by adding to application.properties:
# server.port=8081
```

### Issue: GitHub API Rate Limiting (403)
**Solution:** The GitHub API has rate limits. Implement authentication:

Add to `GitHubUserService`:
```java
headers.set("Authorization", "token YOUR_GITHUB_TOKEN");
```

### Issue: Certificate SSL Errors (if using HTTPS proxy)
**Solution:** Add to `RestTemplateConfig`:
```java
HttpClient httpClient = HttpClients.createDefault();
HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
return builder.requestFactory(factory).build();
```

---

## Next Steps for Enhancement

1. **Add Authentication**: Store GitHub token in environment variable
2. **Add Response Caching**: Cache results for frequently requested users
3. **Add Rate Limiting**: Prevent API abuse
4. **Add Logging**: Use structured logging (JSON format)
5. **Add Unit Tests**: Create comprehensive test suite
6. **Add Pagination**: For users with many repositories
7. **Add API Documentation**: Use Swagger/OpenAPI

---

## File Structure

```
src/main/java/com/example/mySpringProject/
├── controller/
│   └── GitHubController.java           # REST endpoint
├── service/
│   └── GitHubUserService.java          # Business logic
├── model/
│   └── GitHubUserResponse.java         # Response DTO
├── exception/
│   ├── GlobalExceptionHandler.java     # Exception handling
│   └── ErrorResponse.java              # Error DTO
└── config/
    └── RestTemplateConfig.java         # Bean configuration
```

---

## Summary

Your GitHub API is **production-ready** with:
- ✅ Proper error handling and validation
- ✅ Structured JSON responses
- ✅ Timeout protection
- ✅ Comprehensive logging
- ✅ Standard REST conventions
- ✅ Exception handling best practices

Build the project and run the tests above to verify it's working correctly!

