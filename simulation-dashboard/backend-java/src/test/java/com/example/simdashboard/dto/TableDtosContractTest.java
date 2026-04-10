package com.example.simdashboard.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class TableDtosContractTest {

    @Test
    void activityTableDtoContainsExpectedColumns() {
        assertThat(componentNames(ActivityStatsDto.class.getRecordComponents())).containsExactly(
                "name",
                "count",
                "avgDurationSec",
                "minDurationSec",
                "maxDurationSec"
        );
    }

    @Test
    void resourceTableDtoContainsExpectedColumns() {
        assertThat(componentNames(ResourceStatsDto.class.getRecordComponents())).containsExactly(
                "name",
                "blockCount",
                "availableCount",
                "observedWorkTimeSec",
                "utilizationPercent"
        );
    }

    @Test
    void simulationResultDtoExposesTableRowsDirectlyFromServerPayload() {
        RecordComponent[] components = SimulationResultDto.class.getRecordComponents();

        assertThat(componentNames(components)).contains("activities", "resources");
        assertThat(Arrays.stream(components)
                .filter(component -> component.getName().equals("activities"))
                .findFirst()
                .orElseThrow()
                .getGenericType()
                .getTypeName()).contains("ActivityStatsDto");
        assertThat(Arrays.stream(components)
                .filter(component -> component.getName().equals("resources"))
                .findFirst()
                .orElseThrow()
                .getGenericType()
                .getTypeName()).contains("ResourceStatsDto");
    }

    private String[] componentNames(RecordComponent[] components) {
        return Arrays.stream(components)
                .map(RecordComponent::getName)
                .toArray(String[]::new);
    }
}
