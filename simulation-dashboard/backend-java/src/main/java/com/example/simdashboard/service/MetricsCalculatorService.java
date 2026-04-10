package com.example.simdashboard.service;

import com.example.simdashboard.dto.ActivityStatsDto;
import com.example.simdashboard.dto.ChartPointDto;
import com.example.simdashboard.dto.ResourceStatsDto;
import com.example.simdashboard.model.Event;
import com.example.simdashboard.model.MetricsResult;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MetricsCalculatorService {

    public MetricsResult calculateMetrics(List<Event> events) {
        List<Event> orderedEvents = new ArrayList<>(events);
        orderedEvents.sort(Comparator.comparing(Event::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<String, LocalDateTime> processOpenTimes = new HashMap<>();
        List<Double> processDurations = new ArrayList<>();
        Set<String> completedProcesses = new HashSet<>();
        Set<String> failedProcesses = new HashSet<>();
        Set<String> seenProcesses = new HashSet<>();

        Map<String, LocalDateTime> activityOpenTimes = new HashMap<>();
        Map<String, List<Double>> activityDurations = new HashMap<>();
        Map<String, Integer> activityCounts = new HashMap<>();
        int activityStartedCount = 0;

        Map<String, Integer> resourceBlocks = new HashMap<>();
        Map<String, Integer> resourceAvailable = new HashMap<>();
        Map<String, Double> resourceBusyTime = new HashMap<>();
        Map<String, LocalDateTime> resourceOpenTimes = new HashMap<>();
        Map<String, String> resourceHints = new HashMap<>();

        List<LocalDateTime> timestamps = orderedEvents.stream()
                .map(Event::getTimestamp)
                .filter(value -> value != null)
                .toList();
        LocalDateTime startedAt = timestamps.isEmpty() ? null : timestamps.get(0);
        LocalDateTime finishedAt = timestamps.isEmpty() ? null : timestamps.get(timestamps.size() - 1);

        for (Event event : orderedEvents) {
            String eventType = upper(event.getEventType());
            String lifecycle = upper(event.getLifecycle());

            String processId = defaultString(event.getProcessId(), "unknown-process");
            String activityName = firstPresent(event.getActivityName(), event.getActivityId(), "Unknown Activity");
            String resourceName = firstPresent(event.getResourceName(), event.getResourceId(), "Unknown Resource");
            String activityKey = processId + "::" + activityName;

            if (Set.of("RESOURCE_BLOCK", "RESOURCE_AVAILABLE").contains(eventType) && event.getResourceName() != null) {
                resourceHints.put(activityKey, event.getResourceName());
            }

            String effectiveResourceName = firstPresent(event.getResourceName(), event.getResourceId(), resourceHints.get(activityKey));

            if (event.getProcessId() != null) {
                seenProcesses.add(event.getProcessId());
            }

            if (isProcessStart(eventType, lifecycle) && event.getTimestamp() != null && event.getProcessId() != null) {
                processOpenTimes.put(processId, event.getTimestamp());
            }

            if (isProcessEnd(eventType, lifecycle) && event.getTimestamp() != null && event.getProcessId() != null) {
                LocalDateTime openedAt = processOpenTimes.remove(processId);
                if (openedAt != null) {
                    processDurations.add((double) Duration.between(openedAt, event.getTimestamp()).toSeconds());
                }
                completedProcesses.add(processId);
            }

            if (isProcessFailure(eventType, lifecycle) && event.getProcessId() != null) {
                failedProcesses.add(event.getProcessId());
            }

            if (isActivityStart(eventType, lifecycle) && event.getTimestamp() != null) {
                activityStartedCount++;
                activityOpenTimes.put(activityKey, event.getTimestamp());
                if (effectiveResourceName != null) {
                    resourceOpenTimes.put(processId + "::" + effectiveResourceName, event.getTimestamp());
                }
            }

            if (isActivityEnd(eventType, lifecycle) && event.getTimestamp() != null) {
                LocalDateTime openedAt = activityOpenTimes.remove(activityKey);
                if (openedAt != null) {
                    double duration = Duration.between(openedAt, event.getTimestamp()).toSeconds();
                    activityDurations.computeIfAbsent(activityName, key -> new ArrayList<>()).add(duration);
                }
                activityCounts.merge(activityName, 1, Integer::sum);

                if (effectiveResourceName != null) {
                    String resourceKey = processId + "::" + effectiveResourceName;
                    LocalDateTime resourceOpenedAt = resourceOpenTimes.remove(resourceKey);
                    if (resourceOpenedAt != null) {
                        double duration = Duration.between(resourceOpenedAt, event.getTimestamp()).toSeconds();
                        resourceBusyTime.merge(effectiveResourceName, duration, Double::sum);
                    }
                }
            }

            if (isResourceBlock(eventType, lifecycle)) {
                resourceBlocks.merge(resourceName, 1, Integer::sum);
            }
            if ("RESOURCE_AVAILABLE".equals(eventType)) {
                resourceAvailable.merge(resourceName, 1, Integer::sum);
            }
        }

        Double totalSimulationTime = null;
        if (startedAt != null && finishedAt != null) {
            totalSimulationTime = (double) Duration.between(startedAt, finishedAt).toSeconds();
        }

        List<ActivityStatsDto> activities = buildActivityStats(activityCounts, activityDurations);
        List<ResourceStatsDto> resources = buildResourceStats(resourceBlocks, resourceAvailable, resourceBusyTime, totalSimulationTime);

        return new MetricsResult(
                startedAt != null ? startedAt.toString() : null,
                finishedAt != null ? finishedAt.toString() : null,
                new MetricsResult.Summary(
                        totalSimulationTime,
                        seenProcesses.isEmpty() ? null : seenProcesses.size(),
                        completedProcesses.size(),
                        failedProcesses.size(),
                        activityStartedCount,
                        activityCounts.values().stream().mapToInt(Integer::intValue).sum(),
                        resourceBlocks.values().stream().mapToInt(Integer::intValue).sum()
                ),
                new MetricsResult.ProcessStats(
                        processDurations.isEmpty() ? null : processDurations.stream().min(Double::compareTo).orElse(null),
                        average(processDurations),
                        processDurations.isEmpty() ? null : processDurations.stream().max(Double::compareTo).orElse(null),
                        processDurations
                ),
                activities,
                resources,
                activities.stream().map(item -> new ChartPointDto(item.name(), item.count(), null, null, null, null)).toList(),
                activities.stream().map(item -> new ChartPointDto(item.name(), item.avgDurationSec(), null, null, null, null)).toList(),
                resources.stream().map(item -> new ChartPointDto(item.name(), item.blockCount(), null, null, null, null)).toList(),
                buildHistogram(processDurations, 6)
        );
    }

    private List<ActivityStatsDto> buildActivityStats(Map<String, Integer> activityCounts, Map<String, List<Double>> activityDurations) {
        Set<String> activityNames = new HashSet<>(activityCounts.keySet());
        activityNames.addAll(activityDurations.keySet());

        List<ActivityStatsDto> activities = new ArrayList<>();
        activityNames.stream().sorted().forEach(name -> {
            List<Double> durations = activityDurations.getOrDefault(name, List.of());
            activities.add(new ActivityStatsDto(
                    name,
                    activityCounts.getOrDefault(name, durations.size()),
                    average(durations),
                    durations.isEmpty() ? null : durations.stream().min(Double::compareTo).orElse(null),
                    durations.isEmpty() ? null : durations.stream().max(Double::compareTo).orElse(null)
            ));
        });
        return activities;
    }

    private List<ResourceStatsDto> buildResourceStats(
            Map<String, Integer> resourceBlocks,
            Map<String, Integer> resourceAvailable,
            Map<String, Double> resourceBusyTime,
            Double totalSimulationTime
    ) {
        Set<String> resourceNames = new HashSet<>(resourceBlocks.keySet());
        resourceNames.addAll(resourceAvailable.keySet());
        resourceNames.addAll(resourceBusyTime.keySet());

        List<ResourceStatsDto> resources = new ArrayList<>();
        resourceNames.stream().sorted().forEach(name -> {
            Double observedWorkTime = resourceBusyTime.getOrDefault(name, 0.0);
            Double utilization = null;
            if (observedWorkTime > 0 && totalSimulationTime != null && totalSimulationTime > 0) {
                utilization = round((observedWorkTime / totalSimulationTime) * 100);
            }
            resources.add(new ResourceStatsDto(
                    name,
                    resourceBlocks.getOrDefault(name, 0),
                    resourceAvailable.getOrDefault(name, 0),
                    observedWorkTime > 0 ? observedWorkTime : null,
                    utilization
            ));
        });
        return resources;
    }

    private List<ChartPointDto> buildHistogram(List<Double> values, int bins) {
        if (values.isEmpty()) {
            return List.of();
        }
        double minValue = values.stream().min(Double::compareTo).orElse(0.0);
        double maxValue = values.stream().max(Double::compareTo).orElse(0.0);

        if (Double.compare(minValue, maxValue) == 0) {
            return List.of(new ChartPointDto(null, null, trim(minValue) + "-" + trim(maxValue), round(minValue), round(maxValue), values.size()));
        }

        double width = (maxValue - minValue) / bins;
        List<ChartPointDto> histogram = new ArrayList<>();
        for (int index = 0; index < bins; index++) {
            double from = minValue + index * width;
            double to = index < bins - 1 ? minValue + (index + 1) * width : maxValue;
            int count = 0;
            for (double value : values) {
                boolean inRange = index < bins - 1 ? value >= from && value < to : value >= from && value <= to;
                if (inRange) {
                    count++;
                }
            }
            histogram.add(new ChartPointDto(null, null, trim(from) + "-" + trim(to), round(from), round(to), count));
        }
        return histogram;
    }

    private Double average(List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        return round(values.stream().mapToDouble(Double::doubleValue).sum() / values.size());
    }

    private Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String trim(double value) {
        double rounded = round(value);
        return rounded == Math.rint(rounded) ? String.valueOf((long) rounded) : String.valueOf(rounded);
    }

    private String upper(String value) {
        return value == null ? "" : value.toUpperCase();
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String firstPresent(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }

    private boolean isProcessStart(String eventType, String lifecycle) {
        return Set.of("PROCESS_START", "CASE_START", "TRACE_START").contains(eventType) || "PROCESS_START".equals(lifecycle);
    }

    private boolean isProcessEnd(String eventType, String lifecycle) {
        return Set.of("PROCESS_END", "CASE_END", "TRACE_END").contains(eventType) || "PROCESS_END".equals(lifecycle);
    }

    private boolean isProcessFailure(String eventType, String lifecycle) {
        return Set.of("PROCESS_FAILED", "CASE_FAILED", "TRACE_FAILED").contains(eventType) || "FAILED".equals(lifecycle);
    }

    private boolean isActivityStart(String eventType, String lifecycle) {
        return Set.of("ACTIVITY_START", "TASK_START").contains(eventType) || Set.of("START", "BEGIN").contains(lifecycle);
    }

    private boolean isActivityEnd(String eventType, String lifecycle) {
        return Set.of("ACTIVITY_END", "TASK_END").contains(eventType) || Set.of("END", "COMPLETE", "COMPLETED", "FINISH").contains(lifecycle);
    }

    private boolean isResourceBlock(String eventType, String lifecycle) {
        return Set.of("RESOURCE_BLOCK", "RESOURCE_BLOCKED").contains(eventType) || "BLOCKED".equals(lifecycle);
    }
}
