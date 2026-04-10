package logger;

import engine.BPSimulator;
import interfaces.IProcessLogger;
import model.Collaboration;
import model.ProcessActivity;
import model.ProcessInstance;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileLogger implements IProcessLogger, Closeable {
    private final BPSimulator simInstance;
    private final Path filePath;
    private BufferedWriter writer;

    public FileLogger(BPSimulator simInstance, Path filePath) {
        this.simInstance = simInstance;
        this.filePath = filePath;
    }

    public void logElementCompletion(ProcessActivity activity) {
        writeLine("Process: " + activity.getProcessInstance().getId() + " Completed: " + activity.toString() +
                " at time " + this.simInstance.getClock().getFormattedTime() + ", idle for: " +
                (activity.getWorkingIdleTime() + activity.getEnabledIdleTime()) + "s");
    }

    public void logElementEnabled(ProcessActivity activity) {
        writeLine("Process: " + activity.getProcessInstance().getId() + " Enabled: " + activity.toString() +
                " at time " + this.simInstance.getClock().getFormattedTime() + " completion time: " +
                this.simInstance.getClock().timeToString(activity.getCompletionTime()));
    }

    public void logProcessEnd(ProcessInstance process) {
        writeLine("Process: " + process.getId() + " completed at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logProcessEnabled(ProcessInstance process) {
        writeLine("Process: " + process.getId() + " started at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logElementWithdrawn(ProcessActivity activity) {
        writeLine("Process: " + activity.getProcessInstance().getId() + " NOT processed: " +
                activity.getActivity().toString() + "at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logCollaboration(ProcessActivity activity, Collaboration collaboration) {
        writeLine("Process: " + activity.getProcessInstance().getId() + " collaboration '" + collaboration.getName() +
                "' from " + activity.getActivity().toString() + " " + collaboration.getTargetActivity().toString() +
                "at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logEnabledPending(ProcessActivity activity) {
        writeLine("Process: " + activity.getProcessInstance().getId() + " Pending: " +
                activity.getActivity().toString() + "at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logMessageRegistered(ProcessActivity activity) {
        writeLine("Process: " + activity.getProcessInstance().getId() + " Message received: " +
                activity.getActivity().toString() + "at time " + this.simInstance.getClock().getFormattedTime());
    }

    public void logResourceAvailable(ProcessActivity activity, int totalAvailable) {
        writeLine(totalAvailable + " resources (" + activity.getActivity().getResource() + ") available for " + activity);
    }

    public void logResourceUnavailable(ProcessActivity activity) {
        writeLine("NO resources (" + activity.getActivity().getResource() + ") available for " + activity);
    }

    public void finish() {
        writeLine("Simulation finished");
        flushQuietly();
        closeQuietly();
    }

    public void init() {
        try {
            Path parent = filePath.toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            writer = Files.newBufferedWriter(
                    filePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            throw new RuntimeException("Unable to initialize file logger for " + filePath, exception);
        }
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    private void writeLine(String line) {
        try {
            if (writer == null) {
                throw new IllegalStateException("File logger is not initialized");
            }

            writer.write(line);
            writer.newLine();
        } catch (IOException exception) {
            throw new RuntimeException("Unable to write simulation log to " + filePath, exception);
        }
    }

    private void flushQuietly() {
        if (writer != null) {
            try {
                writer.flush();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeQuietly() {
        try {
            close();
        } catch (IOException ignored) {
        }
    }
}
