package com.frieght.weatherApp;


import com.frieght.weatherApp.controller.WeatherController;
import com.frieght.weatherApp.dto.WeatherRequest;
import com.frieght.weatherApp.dto.WeatherResponse;
import com.frieght.weatherApp.service.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetWeatherInfo_Success() throws Exception {
        // Arrange
        WeatherRequest request = new WeatherRequest("411014", LocalDate.of(2020, 10, 15));
        WeatherResponse response = new WeatherResponse();
        response.setPincode("411014");
        response.setForDate(LocalDate.of(2020, 10, 15));
        response.setTemperature(25.5);
        response.setWeatherMain("Clear");

        when(weatherService.getWeatherInfo(any(WeatherRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/weather/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pincode").value("411014"))
                .andExpect(jsonPath("$.temperature").value(25.5))
                .andExpect(jsonPath("$.weatherMain").value("Clear"));
    }

    @Test
    void testGetWeatherInfo_InvalidPincode() throws Exception {
        // Arrange
        WeatherRequest request = new WeatherRequest("12345", LocalDate.of(2020, 10, 15)); // Invalid pincode

        // Act & Assert
        mockMvc.perform(post("/api/weather/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pincode").value("Pincode must be 6 digits"));
    }
}
