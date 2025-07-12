package com.frieght.weatherApp.service;


import com.frieght.weatherApp.dto.WeatherRequest;
import com.frieght.weatherApp.dto.WeatherResponse;
import com.frieght.weatherApp.entity.PincodeLocation;
import com.frieght.weatherApp.entity.WeatherData;
import com.frieght.weatherApp.repository.PincodeLocationRepository;
import com.frieght.weatherApp.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;


@Service
public class WeatherService {

    @Autowired
    private PincodeLocationRepository pincodeLocationRepository;

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.geocoding.url}")
    private String geocodingUrl;

    @Value("${openweather.weather.url}")
    private String weatherUrl;

    public WeatherResponse getWeatherInfo(WeatherRequest request) {
        // Check if we have cached weather data
        Optional<WeatherData> cachedWeatherData = weatherDataRepository
                .findByPincodeAndForDate(request.getPincode(), request.getForDate());

        if (cachedWeatherData.isPresent()) {
            return createResponseFromCachedData(cachedWeatherData.get());
        }

        // Get or fetch pincode location
        PincodeLocation location = getOrFetchPincodeLocation(request.getPincode());

        // Fetch weather data from API
        WeatherData weatherData = fetchWeatherFromAPI(location, request.getForDate());

        // Save weather data to database
        weatherDataRepository.save(weatherData);

        return createResponseFromWeatherData(weatherData, location);
    }

    private PincodeLocation getOrFetchPincodeLocation(String pincode) {
        Optional<PincodeLocation> existingLocation = pincodeLocationRepository.findByPincode(pincode);

        if (existingLocation.isPresent()) {
            return existingLocation.get();
        }

        // Fetch from geocoding API
        PincodeLocation location = fetchLocationFromGeocoding(pincode);
        return pincodeLocationRepository.save(location);
    }

    @Cacheable(value = "geocoding", key = "#pincode")
    private PincodeLocation fetchLocationFromGeocoding(String pincode) {
        String url = String.format("%s?zip=%s,IN&appid=%s", geocodingUrl, pincode, apiKey);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                return new PincodeLocation(
                        pincode,
                        (Double) response.get("lat"),
                        (Double) response.get("lon"),
                        (String) response.get("name"),
                        null, // State not provided in zip geocoding
                        (String) response.get("country")
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch location for pincode: " + pincode, e);
        }

        throw new RuntimeException("Location not found for pincode: " + pincode);
    }

    private WeatherData fetchWeatherFromAPI(PincodeLocation location, LocalDate forDate) {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric",
                weatherUrl, location.getLatitude(), location.getLongitude(), apiKey);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                WeatherData weatherData = new WeatherData();
                weatherData.setPincode(location.getPincode());
                weatherData.setForDate(forDate);

                Map<String, Object> main = (Map<String, Object>) response.get("main");
                weatherData.setTemperature((Double) main.get("temp"));
                weatherData.setFeelsLike((Double) main.get("feels_like"));
                weatherData.setHumidity((Integer) main.get("humidity"));

                Map<String, Object> weather = ((java.util.List<Map<String, Object>>) response.get("weather")).get(0);
                weatherData.setWeatherMain((String) weather.get("main"));
                weatherData.setWeatherDescription((String) weather.get("description"));

                Map<String, Object> wind = (Map<String, Object>) response.get("wind");
                weatherData.setWindSpeed((Double) wind.get("speed"));
                weatherData.setWindDirection((Integer) wind.get("deg"));

                return weatherData;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data", e);
        }

        throw new RuntimeException("Weather data not available");
    }

    private WeatherResponse createResponseFromCachedData(WeatherData weatherData) {
        Optional<PincodeLocation> location = pincodeLocationRepository.findByPincode(weatherData.getPincode());
        WeatherResponse response = createResponseFromWeatherData(weatherData, location.orElse(null));
        response.setFromCache(true);
        return response;
    }

    private WeatherResponse createResponseFromWeatherData(WeatherData weatherData, PincodeLocation location) {
        WeatherResponse response = new WeatherResponse();
        response.setPincode(weatherData.getPincode());
        response.setForDate(weatherData.getForDate());
        response.setTemperature(weatherData.getTemperature());
        response.setFeelsLike(weatherData.getFeelsLike());
        response.setHumidity(weatherData.getHumidity());
        response.setWeatherMain(weatherData.getWeatherMain());
        response.setWeatherDescription(weatherData.getWeatherDescription());
        response.setWindSpeed(weatherData.getWindSpeed());
        response.setWindDirection(weatherData.getWindDirection());
        response.setFromCache(false);

        if (location != null) {
            response.setCity(location.getCity());
            response.setState(location.getState());
            response.setCountry(location.getCountry());
        }

        return response;
    }
}
