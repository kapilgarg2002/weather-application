package com.frieght.weatherApp;

import com.frieght.weatherApp.dto.WeatherRequest;
import com.frieght.weatherApp.dto.WeatherResponse;
import com.frieght.weatherApp.entity.PincodeLocation;
import com.frieght.weatherApp.entity.WeatherData;
import com.frieght.weatherApp.repository.PincodeLocationRepository;
import com.frieght.weatherApp.repository.WeatherDataRepository;
import com.frieght.weatherApp.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeatherServiceTest {

	@Mock
	private PincodeLocationRepository pincodeLocationRepository;

	@Mock
	private WeatherDataRepository weatherDataRepository;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private WeatherService weatherService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetWeatherInfo_WithCachedData() {
		// Arrange
		String pincode = "411014";
		LocalDate forDate = LocalDate.of(2020, 10, 15);
		WeatherRequest request = new WeatherRequest(pincode, forDate);

		WeatherData cachedData = new WeatherData();
		cachedData.setPincode(pincode);
		cachedData.setForDate(forDate);
		cachedData.setTemperature(25.5);
		cachedData.setHumidity(70);
		cachedData.setWeatherMain("Clear");
		cachedData.setWeatherDescription("clear sky");

		when(weatherDataRepository.findByPincodeAndForDate(pincode, forDate))
				.thenReturn(Optional.of(cachedData));

		// Act
		WeatherResponse response = weatherService.getWeatherInfo(request);

		// Assert
		assertNotNull(response);
		assertEquals(pincode, response.getPincode());
		assertEquals(forDate, response.getForDate());
		assertEquals(25.5, response.getTemperature());
		assertTrue(response.isFromCache());

		verify(weatherDataRepository, times(1)).findByPincodeAndForDate(pincode, forDate);
		verify(pincodeLocationRepository, times(1)).findByPincode(any());
	}

	@Test
	void testGetWeatherInfo_WithoutCachedData() {
		// Arrange
		String pincode = "411014";
		LocalDate forDate = LocalDate.of(2020, 10, 15);
		WeatherRequest request = new WeatherRequest(pincode, forDate);

		when(weatherDataRepository.findByPincodeAndForDate(pincode, forDate))
				.thenReturn(Optional.empty());

		PincodeLocation location = new PincodeLocation(pincode, 18.5204, 73.8567, "Pune", "Maharashtra", "IN");
		when(pincodeLocationRepository.findByPincode(pincode))
				.thenReturn(Optional.of(location));

		// Mock API responses would go here for complete test

		// Act & Assert
		assertThrows(RuntimeException.class, () -> weatherService.getWeatherInfo(request));
	}
}

