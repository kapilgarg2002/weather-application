package com.frieght.weatherApp.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public class WeatherRequest {
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pincode;

    @NotNull(message = "Date is required")
    private LocalDate forDate;

    public WeatherRequest() {}

    public WeatherRequest(String pincode, LocalDate forDate) {
        this.pincode = pincode;
        this.forDate = forDate;
    }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public LocalDate getForDate() { return forDate; }
    public void setForDate(LocalDate forDate) { this.forDate = forDate; }
}