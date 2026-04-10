package com.example.simdashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.simdashboard.dto.ActivityStatsDto;
import com.example.simdashboard.dto.ChartPointDto;
import com.example.simdashboard.dto.ResourceStatsDto;
import com.example.simdashboard.model.Event;
import com.example.simdashboard.model.MetricsResult;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetricsCalculatorServiceTest {

    private final MetricsCalculatorService metricsCalculatorService = new MetricsCalculatorService();

    @Test
    void calculatesActivityCountsDurationsAndChartDtos() {
        MetricsResult result = metricsCalculatorService.calculateMetrics(List.of(
                activityEvent("p1", "Review application", "ACTIVITY_START", "START", at(10, 0, 0)),
                activityEvent("p1", "Review application", "ACTIVITY_END", "COMPLETE", at(10, 0, 30)),
                activityEvent("p2", "Review application", "ACTIVITY_START", "START", at(10, 1, 0)),
                activityEvent("p2", "Review application", "ACTIVITY_END", "COMPLETE", at(10, 2, 30)),
                activityEvent("p3", "Approve application", "ACTIVITY_START", "START", at(10, 3, 0)),
                activityEvent("p3", "Approve application", "ACTIVITY_END", "COMPLETE", at(10, 4, 0))
        ));

        assertThat(result.activities()).hasSize(2);

        ActivityStatsDto review = result.activities().stream()
                .filter(item -> item.name().equals("Review application"))
                .findFirst()
                .orElseThrow();
        assertThat(review.count()).isEqualTo(2);
        assertThat(review.avgDurationSec()).isEqualTo(60.0);
        assertThat(review.minDurationSec()).isEqualTo(30.0);
        assertThat(review.maxDurationSec()).isEqualTo(90.0);

        ActivityStatsDto approve = result.activities().stream()
                .filter(item -> item.name().equals("Approve application"))
                .findFirst()
                .orElseThrow();
        assertThat(approve.count()).isEqualTo(1);
        assertThat(approve.avgDurationSec()).isEqualTo(60.0);
        assertThat(approve.minDurationSec()).isEqualTo(60.0);
        assertThat(approve.maxDurationSec()).isEqualTo(60.0);

        assertThat(result.activityCounts())
                .extracting(ChartPointDto::name, point -> point.value().intValue())
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Review application", 2),
                        org.assertj.core.groups.Tuple.tuple("Approve application", 1)
                );

        assertThat(result.activityAvgDurations())
                .extracting(ChartPointDto::name, point -> point.value().doubleValue())
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Review application", 60.0),
                        org.assertj.core.groups.Tuple.tuple("Approve application", 60.0)
                );
    }

    @Test
    void calculatesResourceBlockAvailabilityAndChartDtos() {
        MetricsResult result = metricsCalculatorService.calculateMetrics(List.of(
                resourceEvent("p1", "Analyst", "RESOURCE_BLOCK", "BLOCKED", null),
                resourceEvent("p2", "Analyst", "RESOURCE_BLOCK", "BLOCKED", null),
                resourceEvent("p3", "Analyst", "RESOURCE_AVAILABLE", "AVAILABLE", null),
                resourceEvent("p4", "Reviewer", "RESOURCE_AVAILABLE", "AVAILABLE", null),
                activityWithResourceEvent("p5", "Approve application", "Analyst", "ACTIVITY_START", "START", at(11, 0, 0)),
                activityWithResourceEvent("p5", "Approve application", "Analyst", "ACTIVITY_END", "COMPLETE", at(11, 2, 0))
        ));

        assertThat(result.resources()).hasSize(2);

        ResourceStatsDto analyst = result.resources().stream()
                .filter(item -> item.name().equals("Analyst"))
                .findFirst()
                .orElseThrow();
        assertThat(analyst.blockCount()).isEqualTo(2);
        assertThat(analyst.availableCount()).isEqualTo(1);
        assertThat(analyst.observedWorkTimeSec()).isEqualTo(120.0);
        assertThat(analyst.utilizationPercent()).isEqualTo(100.0);

        ResourceStatsDto reviewer = result.resources().stream()
                .filter(item -> item.name().equals("Reviewer"))
                .findFirst()
                .orElseThrow();
        assertThat(reviewer.blockCount()).isEqualTo(0);
        assertThat(reviewer.availableCount()).isEqualTo(1);
        assertThat(reviewer.observedWorkTimeSec()).isNull();
        assertThat(reviewer.utilizationPercent()).isNull();

        assertThat(result.resourceBlocks())
                .extracting(ChartPointDto::name, point -> point.value().intValue())
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Analyst", 2),
                        org.assertj.core.groups.Tuple.tuple("Reviewer", 0)
                );
    }

    @Test
    void calculatesCompletedProcessDurationsAndHistogramChartDtos() {
        MetricsResult result = metricsCalculatorService.calculateMetrics(List.of(
                processEvent("p1", "PROCESS_START", "PROCESS_START", at(12, 0, 0)),
                processEvent("p1", "PROCESS_END", "PROCESS_END", at(12, 0, 30)),
                processEvent("p2", "PROCESS_START", "PROCESS_START", at(12, 1, 0)),
                processEvent("p2", "PROCESS_END", "PROCESS_END", at(12, 2, 30)),
                processEvent("p3", "PROCESS_START", "PROCESS_START", at(12, 3, 0)),
                processEvent("p3", "PROCESS_END", "PROCESS_END", at(12, 5, 0)),
                processEvent("p4", "PROCESS_START", "PROCESS_START", at(12, 6, 0))
        ));

        assertThat(result.processStats().durations())
                .containsExactlyInAnyOrder(30.0, 90.0, 120.0);
        assertThat(result.processStats().minDurationSec()).isEqualTo(30.0);
        assertThat(result.processStats().avgDurationSec()).isEqualTo(80.0);
        assertThat(result.processStats().maxDurationSec()).isEqualTo(120.0);

        assertThat(result.processDurationHistogram()).hasSize(6);
        assertThat(result.processDurationHistogram())
                .extracting(ChartPointDto::binLabel, ChartPointDto::from, ChartPointDto::to, ChartPointDto::count)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("30-45", 30.0, 45.0, 1),
                        org.assertj.core.groups.Tuple.tuple("45-60", 45.0, 60.0, 0),
                        org.assertj.core.groups.Tuple.tuple("60-75", 60.0, 75.0, 0),
                        org.assertj.core.groups.Tuple.tuple("75-90", 75.0, 90.0, 0),
                        org.assertj.core.groups.Tuple.tuple("90-105", 90.0, 105.0, 1),
                        org.assertj.core.groups.Tuple.tuple("105-120", 105.0, 120.0, 1)
                );
    }

    private static Event activityEvent(
            String processId,
            String activityName,
            String eventType,
            String lifecycle,
            LocalDateTime timestamp
    ) {
        Event event = new Event();
        event.setProcessId(processId);
        event.setActivityName(activityName);
        event.setEventType(eventType);
        event.setLifecycle(lifecycle);
        event.setTimestamp(timestamp);
        return event;
    }

    private static Event resourceEvent(
            String processId,
            String resourceName,
            String eventType,
            String lifecycle,
            LocalDateTime timestamp
    ) {
        Event event = new Event();
        event.setProcessId(processId);
        event.setResourceName(resourceName);
        event.setEventType(eventType);
        event.setLifecycle(lifecycle);
        event.setTimestamp(timestamp);
        return event;
    }

    private static Event activityWithResourceEvent(
            String processId,
            String activityName,
            String resourceName,
            String eventType,
            String lifecycle,
            LocalDateTime timestamp
    ) {
        Event event = activityEvent(processId, activityName, eventType, lifecycle, timestamp);
        event.setResourceName(resourceName);
        return event;
    }

    private static Event processEvent(
            String processId,
            String eventType,
            String lifecycle,
            LocalDateTime timestamp
    ) {
        Event event = new Event();
        event.setProcessId(processId);
        event.setEventType(eventType);
        event.setLifecycle(lifecycle);
        event.setTimestamp(timestamp);
        return event;
    }

    private static LocalDateTime at(int hour, int minute, int second) {
        return LocalDateTime.of(2026, 4, 10, hour, minute, second);
    }
}
