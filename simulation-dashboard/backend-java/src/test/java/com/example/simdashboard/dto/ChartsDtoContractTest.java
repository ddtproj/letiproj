package com.example.simdashboard.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChartsDtoContractTest {

    @Test
    void activityCountChartDtoContainsCategoryNamesAndNumericValues() {
        ChartsDto chartsDto = new ChartsDto(
                List.of(new ChartPointDto("Review application", 3, null, null, null, null)),
                List.of(),
                List.of(),
                List.of()
        );

        ChartPointDto point = chartsDto.activityCounts().get(0);
        assertThat(point.name()).isEqualTo("Review application");
        assertThat(point.value()).isEqualTo(3);
        assertThat(point.binLabel()).isNull();
        assertThat(point.count()).isNull();
    }

    @Test
    void activityAverageDurationChartDtoContainsCategoryNamesAndAverageValues() {
        ChartsDto chartsDto = new ChartsDto(
                List.of(),
                List.of(new ChartPointDto("Approve application", 42.5, null, null, null, null)),
                List.of(),
                List.of()
        );

        ChartPointDto point = chartsDto.activityAvgDurations().get(0);
        assertThat(point.name()).isEqualTo("Approve application");
        assertThat(point.value()).isEqualTo(42.5);
        assertThat(point.binLabel()).isNull();
        assertThat(point.count()).isNull();
    }

    @Test
    void resourceBlockChartDtoContainsResourceNamesAndBlockCounts() {
        ChartsDto chartsDto = new ChartsDto(
                List.of(),
                List.of(),
                List.of(new ChartPointDto("Analyst", 2, null, null, null, null)),
                List.of()
        );

        ChartPointDto point = chartsDto.resourceBlocks().get(0);
        assertThat(point.name()).isEqualTo("Analyst");
        assertThat(point.value()).isEqualTo(2);
        assertThat(point.binLabel()).isNull();
        assertThat(point.count()).isNull();
    }
}
