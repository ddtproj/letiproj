package com.example.simdashboard.dto;

import java.util.List;

public record ProcessStatsDto(
        Double minDurationSec,
        Double avgDurationSec,
        Double maxDurationSec,
        List<Double> durations
) {
}
