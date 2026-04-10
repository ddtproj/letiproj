package com.example.simdashboard.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

public class SimulationExecutionService {

    private final RunStorageService runStorageService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Path letitSimDir;
    private final String letitSimMainClass;
    private final String configuredJavaExecutable;

    public SimulationExecutionService(
            RunStorageService runStorageService,
            @Value("${app.letitsim-dir}") String letitSimDir,
            @Value("${app.letitsim-main-class}") String letitSimMainClass,
            @Value("${app.letitsim-java:}") String configuredJavaExecutable
    ) {
        this.runStorageService = runStorageService;
        this.letitSimDir = Paths.get(letitSimDir).toAbsolutePath().normalize();
        this.letitSimMainClass = letitSimMainClass;
        this.configuredJavaExecutable = configuredJavaExecutable == null ? "" : configuredJavaExecutable.trim();
    }

    public void startSimulation(String runId, String specificationPath) {
        executorService.submit(() -> executeSimulation(runId, specificationPath));
    }

    private void executeSimulation(String runId, String specificationPath) {
        try {
            if (specificationPath == null || specificationPath.isBlank()) {
                failRun(runId, "specification_path is required to start a simulation");
                return;
            }

            Path bpmnPath = Paths.get(specificationPath).toAbsolutePath().normalize();
            if (!Files.exists(bpmnPath)) {
                failRun(runId, "BPMN file not found: " + bpmnPath);
                return;
            }

            Path runDir = runStorageService.ensureRunDir(runId);
            Path logPath = runDir.resolve("simulation.log");
            Path jsonPath = runDir.resolve("parsed-log-dto.json");
            Path stdoutPath = runDir.resolve("stdout.log");
            Path stderrPath = runDir.resolve("stderr.log");

            runStorageService.updateRunMeta(runId, meta -> {
                meta.put("status", "running");
                meta.put("startedAt", utcNow());
            });

            ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(bpmnPath, logPath, jsonPath));
            processBuilder.directory(letitSimDir.toFile());
            processBuilder.redirectOutput(stdoutPath.toFile());
            processBuilder.redirectError(stderrPath.toFile());
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                failRun(runId, "Simulation process failed with exit code " + exitCode);
                runStorageService.updateRunMeta(runId, meta -> meta.put("exitCode", exitCode));
                return;
            }

            if (!Files.exists(logPath)) {
                failRun(runId, buildMissingArtifactMessage("simulation.log", stderrPath));
                runStorageService.updateRunMeta(runId, meta -> meta.put("exitCode", exitCode));
                return;
            }

            if (!Files.exists(jsonPath)) {
                failRun(runId, buildMissingArtifactMessage("parsed-log-dto.json", stderrPath));
                runStorageService.updateRunMeta(runId, meta -> meta.put("exitCode", exitCode));
                return;
            }

            runStorageService.updateRunMeta(runId, meta -> {
                meta.put("status", "completed");
                meta.put("finishedAt", utcNow());
                meta.put("exitCode", exitCode);
            });
        } catch (Exception exception) {
            try {
                failRun(runId, exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            } catch (IOException ignored) {
            }
        }
    }

    private List<String> buildCommand(Path bpmnPath, Path logPath, Path jsonPath) throws IOException {
        Path classesDir = letitSimDir.resolve("target").resolve("classes");
        if (!Files.exists(classesDir.resolve(letitSimMainClass + ".class"))) {
            throw new IOException("Compiled letitsim classes not found: " + classesDir);
        }

        List<String> classpathEntries = new ArrayList<>();
        classpathEntries.add(classesDir.toString());
        classpathEntries.add(resolveDependency("lib/jdom2-2.0.6.jar", ".m2/repository/org/jdom/jdom2/2.0.6/jdom2-2.0.6.jar"));
        classpathEntries.add(resolveDependency("lib/jaxb-api-2.3.1.jar", ".m2/repository/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.jar"));
        classpathEntries.add(resolveDependency("lib/colt-1.2.0.jar", ".m2/repository/colt/colt/1.2.0/colt-1.2.0.jar"));
        classpathEntries.add(resolveDependency("lib/concurrent-1.3.4.jar", ".m2/repository/concurrent/concurrent/1.3.4/concurrent-1.3.4.jar"));

        List<String> command = new ArrayList<>();
        command.add(resolveJavaExecutable());
        command.add("-cp");
        command.add(String.join(java.io.File.pathSeparator, classpathEntries));
        command.add(letitSimMainClass);
        command.add(bpmnPath.toString());
        command.add(logPath.toString());
        command.add(jsonPath.toString());
        return command;
    }

    private String resolveJavaExecutable() {
        if (!configuredJavaExecutable.isBlank()) {
            return configuredJavaExecutable;
        }
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null && !javaHome.isBlank()) {
            Path candidate = Paths.get(javaHome, "bin", isWindows() ? "java.exe" : "java");
            if (Files.exists(candidate)) {
                return candidate.toString();
            }
        }
        return isWindows() ? "java.exe" : "java";
    }

    private String resolveDependency(String localRelativePath, String m2RelativePath) throws IOException {
        Path localPath = letitSimDir.resolve(localRelativePath).normalize();
        if (Files.exists(localPath)) {
            return localPath.toString();
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            Path m2Path = Paths.get(userHome).resolve(m2RelativePath).normalize();
            if (Files.exists(m2Path)) {
                return m2Path.toString();
            }
        }

        throw new IOException("Dependency not found: " + localRelativePath);
    }

    private void failRun(String runId, String message) throws IOException {
        runStorageService.updateRunMeta(runId, meta -> {
            meta.put("status", "failed");
            meta.put("finishedAt", utcNow());
            meta.put("error", message);
        });
    }

    private String buildMissingArtifactMessage(String artifactName, Path stderrPath) throws IOException {
        StringBuilder message = new StringBuilder("Simulation finished without producing ").append(artifactName);
        if (Files.exists(stderrPath)) {
            String stderr = Files.readString(stderrPath).trim();
            if (!stderr.isEmpty()) {
                message.append(". stderr: ").append(stderr.replace(System.lineSeparator(), " | "));
            }
        }
        return message.toString();
    }

    private String utcNow() {
        return OffsetDateTime.now(ZoneOffset.UTC).toString();
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
