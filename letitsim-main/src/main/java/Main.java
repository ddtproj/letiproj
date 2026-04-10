import engine.BPSimulator;
import logparser.JsonWriter;
import logparser.LogDtoMapper;
import logparser.LogParser;
import logparser.LogRecord;
import logparser.dto.ParsedLogDto;
import logger.ComplexLogger;
import logger.ConsoleLogger;
import logger.FileLogger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Path bpmnPath = args.length > 0 ? Paths.get(args[0]) : Paths.get("credit_card_application.bpmn");
        Path logPath = args.length > 1 ? Paths.get(args[1]) : Paths.get("log.txt");
        Path jsonPath = args.length > 2 ? Paths.get(args[2]) : Paths.get("parsed-log-dto.json");

        List<String> processFiles = new ArrayList<String>();
        processFiles.add(bpmnPath.toString());

        BPSimulator sim = new BPSimulator(processFiles);
        FileLogger fileLogger = new FileLogger(sim, logPath);

        try {
            ComplexLogger logger = sim.getLogger();
            logger.addLogger(new ConsoleLogger(sim));
            logger.addLogger(fileLogger);

            sim.run();

            LogParser parser = new LogParser();
            List<LogRecord> records = parser.parse(logPath);
            ParsedLogDto dto = new LogDtoMapper().map(logPath, records);
            String json = new JsonWriter().write(dto.toMap());
            Files.write(jsonPath, json.getBytes(StandardCharsets.UTF_8));

            System.out.println("Log file saved to:  " + logPath.toAbsolutePath());
            System.out.println("JSON file saved to: " + jsonPath.toAbsolutePath());

        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }
}
