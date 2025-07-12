# Weather API for Pincode

A Spring Boot REST API that provides weather information for Indian pincodes using OpenWeatherMap API.

(only gives the weather data for the current day as API for the previous/future days was paid)
## Features

- **Single REST API endpoint** for weather information by pincode and date
- **Database caching** for both pincode locations and weather data
- **Optimized API calls** - reuses cached location data
- **Comprehensive validation** and error handling
- **Test coverage** with unit and integration tests
- **Java 21 support** with modern language features

## Tech Stack

- **Spring Boot 3.3.0** (Java 21)
- **Spring Data JPA** for database operations
- **H2 Database** for development (easily switchable to MySQL/PostgreSQL)
- **Spring Cache** for optimization
- **JUnit 5** and **Mockito** for testing

## Java 21 Features Used

- **String Templates** - STR."..." for cleaner string interpolation
- **var keyword** - Type inference for cleaner code
- **Modern exception handling** with improved readability
- **Enhanced pattern matching** in switch expressions

## API Endpoints

### POST /api/weather/info
Get weather information for a pincode and date.

**Request Body:**
```json
{
  "pincode": "411014",
  "for_date": "2020-10-15"
}
```

**Response:**
```json
{
  "pincode": "411014",
  "forDate": "2020-10-15",
  "city": "Pune",
  "state": "Maharashtra",
  "country": "IN",
  "temperature": 25.5,
  "feelsLike": 27.0,
  "humidity": 70,
  "pressure": 1013.25,
  "weatherMain": "Clear",
  "weatherDescription": "clear sky",
  "windSpeed": 5.2,
  "windDirection": 180,
  "fromCache": true
}
```

### GET /api/weather/info
Alternative endpoint using query parameters.

**Query Parameters:**
- `pincode`: 6-digit Indian pincode
- `forDate`: Date in YYYY-MM-DD format

## Database Schema

### pincode_locations
- `id` (Primary Key)
- `pincode` (Unique)
- `latitude`, `longitude`
- `city`, `state`, `country`
- `created_at`

### weather_data
- `id` (Primary Key)
- `pincode`
- `for_date`
- `temperature`, `feels_like`, `humidity`, `pressure`
- `weather_main`, `weather_description`
- `wind_speed`, `wind_direction`
- `created_at`

## Setup Instructions

1. **Clone the repository**
2. **Update API key** in `application.properties`:
   ```properties
   openweather.api.key=YOUR_API_KEY_HERE
   ```
3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

## Testing

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Run tests**: `mvn test`

## API Optimization

The API optimizes calls by:
1. **Caching pincode locations** - fetches lat/long once per pincode
2. **Caching weather data** - avoids duplicate API calls for same date/pincode
3. **Using Spring Cache** for in-memory caching of API responses

## Sample Requests

### Using cURL:
```bash
curl -X POST "http://localhost:8080/api/weather/info" \
  -H "Content-Type: application/json" \
  -d '{"pincode": "411014", "forDate": "2020-10-15"}'
```

### Using Postman:
- Method: POST
- URL: http://localhost:8080/api/weather/info
- Body: Raw JSON with the request payload

## Production Considerations

1. **Database**: Switch to PostgreSQL/MySQL for production
2. **API Rate Limiting**: Implement rate limiting for external API calls
3. **Monitoring**: Add application monitoring and logging
4. **Security**: Add authentication/authorization if needed
5. **Docker**: Containerize the application for deployment

## Error Handling

The API provides comprehensive error handling for:
- Invalid pincode format
- Missing required fields
- API failures
- Database errors

All errors return appropriate HTTP status codes and descriptive messages.
