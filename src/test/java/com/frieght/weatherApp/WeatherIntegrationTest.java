package com.frieght.weatherApp;


import com.frieght.weatherApp.dto.WeatherRequest;
import com.frieght.weatherApp.entity.PincodeLocation;
import com.frieght.weatherApp.entity.WeatherData;
import com.frieght.weatherApp.repository.PincodeLocationRepository;
import com.frieght.weatherApp.repository.WeatherDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class WeatherApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PincodeLocationRepository pincodeLocationRepository;

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Clean up database
        weatherDataRepository.deleteAll();
        pincodeLocationRepository.deleteAll();

        // Setup test data
        PincodeLocation location = new PincodeLocation("411014", 18.5204, 73.8567, "Pune", "Maharashtra", "IN");
        pincodeLocationRepository.save(location);

        WeatherData weatherData = new WeatherData();
        weatherData.setPincode("411014");
        weatherData.setForDate(LocalDate.of(2020, 10, 15));
        weatherData.setTemperature(25.5);
        weatherData.setFeelsLike(27.0);
        weatherData.setHumidity(70);
        weatherData.setWeatherMain("Clear");
        weatherData.setWeatherDescription("clear sky");
        weatherData.setWindSpeed(5.2);
        weatherData.setWindDirection(180);
        weatherDataRepository.save(weatherData);
    }

    @Test
    void testGetWeatherInfo_WithCachedData() throws Exception {
        // Arrange
        WeatherRequest request = new WeatherRequest("411014", LocalDate.of(2020, 10, 15));

        // Act & Assert
        mockMvc.perform(post("/api/weather/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pincode").value("411014"))
                .andExpect(jsonPath("$.temperature").value(25.5))
                .andExpect(jsonPath("$.city").value("Pune"))
                .andExpect(jsonPath("$.fromCache").value(true));
    }
}

