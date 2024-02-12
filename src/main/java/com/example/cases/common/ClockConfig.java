package com.example.cases.common;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    public static final ZoneId AMSTERDAM = ZoneId.of("Europe/Amsterdam");

    @Bean
    Clock clock(@Value("${app.today:}") String today) {
        if (today == null || today.isBlank()) {
            return Clock.system(AMSTERDAM);
        }
        return Clock.fixed(LocalDate.parse(today).atTime(LocalTime.NOON).atZone(AMSTERDAM).toInstant(), AMSTERDAM);
    }
}
