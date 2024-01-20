package io.zeebe.monitor.zeebe.clean;

import com.google.common.collect.Lists;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class CompletedProcessInstanceCleanupTask {
    private static final Logger LOG = LoggerFactory.getLogger(CompletedProcessInstanceCleanupTask.class);

    private final ProcessInstanceRepository processInstanceRepository;
    private final ElementInstanceRepository elementInstanceRepository;
    private final ErrorRepository errorRepository;
    private final IncidentRepository incidentRepository;
    private final JobRepository jobRepository;
    private final MessageSubscriptionRepository messageSubscriptionRepository;
    private final TimerRepository timerRepository;
    private final VariableRepository variableRepository;

    private final PlatformTransactionManager transactionManager;

    private final long expirationIntervalMillis;
    private final int partitionSize;

    public CompletedProcessInstanceCleanupTask(
            @Value("${cleanup.processInstances.completed.expiration-days}") final long expirationIntervalInDays,
            @Value("${cleanup.processInstances.completed.partition-size}") final int partitionSize,
            PlatformTransactionManager transactionManager,
            ProcessInstanceRepository processInstanceRepository,
            ElementInstanceRepository elementInstanceRepository,
            ErrorRepository errorRepository,
            IncidentRepository incidentRepository,
            JobRepository jobRepository,
            MessageSubscriptionRepository messageSubscriptionRepository,
            TimerRepository timerRepository,
            VariableRepository variableRepository) {
        this.expirationIntervalMillis = TimeUnit.DAYS.toMillis(expirationIntervalInDays);
        this.partitionSize = partitionSize;
        this.processInstanceRepository = processInstanceRepository;
        this.elementInstanceRepository = elementInstanceRepository;
        this.errorRepository = errorRepository;
        this.incidentRepository = incidentRepository;
        this.jobRepository = jobRepository;
        this.messageSubscriptionRepository = messageSubscriptionRepository;
        this.timerRepository = timerRepository;
        this.variableRepository = variableRepository;
        this.transactionManager = transactionManager;
    }

    @Scheduled(fixedRateString = "${cleanup.processInstances.completed.frequency-days}", timeUnit = TimeUnit.DAYS)
    public void cleanupExpiredData() {
        var idsToClean = processInstanceRepository.findExpiredIdsByStateAndTime(
                ProcessInstanceEntity.State.COMPLETED.getTitle(),
                System.currentTimeMillis(),
                expirationIntervalMillis
        );

        int groupSize = Math.max(1, partitionSize);
        List<List<Long>> idGroups = Lists.partition(new ArrayList<>(idsToClean), groupSize);

        idGroups.forEach(this::cleanupData);
    }

    @Transactional
    public void cleanupData(Collection<Long> processInstanceIdsToClean) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            List<CompletableFuture<?>> futures = List.of(
                    processInstanceRepository.deleteByKeyIn(processInstanceIdsToClean),
                    errorRepository.deleteByProcessInstanceKeyIn(processInstanceIdsToClean),
                    incidentRepository.deleteByProcessInstanceKeyIn(processInstanceIdsToClean),
                    jobRepository.deleteByProcessInstanceKeyIn(processInstanceIdsToClean),
                    messageSubscriptionRepository.deleteByProcessInstanceKeyIn(processInstanceIdsToClean),
                    timerRepository.deleteByProcessInstanceKeyIn(processInstanceIdsToClean),
                    variableRepository.deleteByProcessInstanceKeyIn(processInstanceIdsToClean),
                    elementInstanceRepository.deleteByProcessInstanceKeyIn(processInstanceIdsToClean)
            );

            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            combinedFuture.join();

            transactionManager.commit(status);
            LOG.info("Deletion completed for group: {}", processInstanceIdsToClean);
        } catch (Exception e) {
            transactionManager.rollback(status);
            LOG.error("Error occurred during deletion for group: " + processInstanceIdsToClean, e);
        }
    }
}