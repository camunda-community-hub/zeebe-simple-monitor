package io.zeebe.monitor.zeebe.clean;

import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.zeebe.ZeebeHazelcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class CompletedProcessInstanceCleanupTask {
    private static final Logger LOG = LoggerFactory.getLogger(CompletedProcessInstanceCleanupTask.class);
    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    private final long expirationIntervalMillis;

    public CompletedProcessInstanceCleanupTask(@Value("${expiration.processInstances.completed}") final long cleanupIntervalInDays) {
        this.expirationIntervalMillis = TimeUnit.DAYS.toMillis(cleanupIntervalInDays);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 10_000)
    public void cleanupExpiredCompletedProcesses() {
        var processes = processInstanceRepository.findAll();
        Set<Long> idsToClean = new HashSet<>();

        long currentTime = System.currentTimeMillis();
        for (ProcessInstanceEntity process : processes) {
            if (process.getState().equals("Completed")
                    && process.getEnd() != null
                    && currentTime - process.getEnd() > expirationIntervalMillis) {
                idsToClean.add(process.getKey());
            }
        }

        LOG.info("Cleaning expired completed process instances with ids: " + idsToClean);
        processInstanceRepository.deleteAllById(idsToClean);
    }
}