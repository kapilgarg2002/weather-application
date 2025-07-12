package com.frieght.weatherApp.controller;


import com.frieght.weatherApp.dto.WeatherRequest;
import com.frieght.weatherApp.dto.WeatherResponse;
import com.frieght.weatherApp.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather API", description = "Weather information API for pincode")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @PostMapping("/info")
    @Operation(summary = "Get weather information for a pincode and date")
    public ResponseEntity<WeatherResponse> getWeatherInfo(@Valid @RequestBody WeatherRequest request) {
        WeatherResponse response = weatherService.getWeatherInfo(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Get weather information using query parameters")
    public ResponseEntity<WeatherResponse> getWeatherInfoByParams(
            @RequestParam String pincode,
            @RequestParam String forDate) {
        WeatherRequest request = new WeatherRequest(pincode, java.time.LocalDate.parse(forDate));
        WeatherResponse response = weatherService.getWeatherInfo(request);
        return ResponseEntity.ok(response);
    }
}
