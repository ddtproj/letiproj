package com.example.simdashboard.config;

import com.example.simdashboard.service.RunStorageService;
import com.example.simdashboard.service.SimulationExecutionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutionConfig {

    @Bean
    public SimulationExecutionService simulationExecutionService(
            RunStorageService runStorageService,
            @Value("${app.letitsim-dir}") String letitSimDir,
            @Value("${app.letitsim-main-class}") String letitSimMainClass,
            @Value("${app.letitsim-java:}") String configuredJavaExecutable
    ) {
        return new SimulationExecutionService(
                runStorageService,
                letitSimDir,
                letitSimMainClass,
                configuredJavaExecutable
        );
    }
}
