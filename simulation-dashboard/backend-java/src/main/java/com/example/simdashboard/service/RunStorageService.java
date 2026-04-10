package com.example.simdashboard.service;

import com.example.simdashboard.dto.RunInfoDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class RunStorageService {

    private static final List<String> LOG_CANDIDATES = List.of(
            "simulation.log",
            "events.log",
            "simulation.csv",
            "events.csv",
            "simulation.jsonl",
            "events.jsonl"
    );

    private final Path storageDir;
    private final ObjectMapper objectMapper;

    public RunStorageService(@org.springframework.beans.factory.annotation.Value("${app.storage-dir}") String storageDir, ObjectMapper objectMapper) {
        this.storageDir = Paths.get(storageDir).toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
    }

    public Path ensureRunDir(String runId) throws IOException {
        Path runDir = getRunDir(runId);
        Files.createDirectories(runDir);
        return runDir;
    }

    public Path getRunDir(String runId) {
        return storageDir.resolve(runId);
    }

    public Optional<Path> getRunLogPath(String runId) {
        return resolveLogPath(getRunDir(runId));
    }

    public Optional<Map<String, Object>> readRunMeta(String runId) {
        return readMetaFile(getRunDir(runId).resolve("meta.json"));
    }

    public void updateRunMeta(String runId, Consumer<Map<String, Object>> updater) throws IOException {
        Map<String, Object> meta = new LinkedHashMap<>(readRunMeta(runId).orElseGet(HashMap::new));
        updater.accept(meta);
        writeRunMeta(runId, meta);
    }

    private Optional<Map<String, Object>> readMetaFile(Path metaPath) {
        if (!Files.exists(metaPath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(metaPath.toFile(), new TypeReference<>() {
            }));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    public void writeRunMeta(String runId, Map<String, Object> meta) throws IOException {
        ensureRunDir(runId);
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(getRunDir(runId).resolve("meta.json").toFile(), meta);
    }

    public List<RunInfoDto> listRuns() {
        Map<String, RunInfoDto> items = new LinkedHashMap<>();
        collectRuns(storageDir, items);

        List<RunInfoDto> results = new ArrayList<>(items.values());
        results.sort(Comparator.comparing(RunInfoDto::runId));
        return results;
    }

    private void collectRuns(Path baseDir, Map<String, RunInfoDto> items) {
        if (baseDir == null || !Files.exists(baseDir)) {
            return;
        }

        try (Stream<Path> stream = Files.list(baseDir)) {
            stream.filter(Files::isDirectory)
                    .forEach(path -> {
                        String runId = path.getFileName().toString();
                        Map<String, Object> meta = readMetaFile(path.resolve("meta.json")).orElseGet(HashMap::new);
                        items.putIfAbsent(runId, new RunInfoDto(
                                runId,
                                (String) meta.get("status"),
                                resolveLogPath(path).isPresent()
                        ));
                    });
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private Optional<Path> resolveLogPath(Path runDir) {
        if (!Files.exists(runDir)) {
            return Optional.empty();
        }

        return LOG_CANDIDATES.stream()
                .map(runDir::resolve)
                .filter(Files::exists)
                .findFirst();
    }
}
