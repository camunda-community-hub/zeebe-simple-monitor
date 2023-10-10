package io.zeebe.monitor.service;

import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "retention", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseRetentionService {

    ProcessInstanceRepository processInstanceRepository;
    JobRepository jobRepository;
    ElementInstanceRepository elementInstanceRepository;
    MessageRepository messageRepository;
    MessageSubscriptionRepository messageSubscriptionRepository;
    ErrorRepository errorRepository;
    TimerRepository timerRepository;
    VariableRepository variableRepository;
    IncidentRepository incidentRepository;

    @Value("${retention.age}")
    Duration oldestProcess;

    @Autowired
    public DatabaseRetentionService(ProcessInstanceRepository processInstanceRepository,
                                    JobRepository jobRepository,
                                    ElementInstanceRepository elementInstanceRepository,
                                    MessageRepository messageRepository,
                                    MessageSubscriptionRepository messageSubscriptionRepository,
                                    ErrorRepository errorRepository,
                                    TimerRepository timerRepository,
                                    VariableRepository variableRepository,
                                    IncidentRepository incidentRepository) {
        this.processInstanceRepository = processInstanceRepository;
        this.jobRepository = jobRepository;
        this.elementInstanceRepository = elementInstanceRepository;
        this.messageRepository = messageRepository;
        this.messageSubscriptionRepository = messageSubscriptionRepository;
        this.errorRepository = errorRepository;
        this.timerRepository = timerRepository;
        this.variableRepository = variableRepository;
        this.incidentRepository = incidentRepository;
    }

    private ArrayList<Long> findAllChildProcesses(long processInstanceId) {
        ArrayList<Long> processKeys = new ArrayList<Long>();
        processKeys.add(processInstanceId);
        List<ProcessInstanceEntity> childProcesses = processInstanceRepository.findByParentProcessInstanceKey(processInstanceId, null).getContent();
        for (ProcessInstanceEntity process : childProcesses) {
            processKeys.addAll(findAllChildProcesses(process.getKey()));
        }
        return processKeys;
    }

    @Scheduled(fixedRateString = "${retention.interval}")
    @Transactional
    public void retention() {
        long oldestProcessTime = Instant.now().minus(oldestProcess).toEpochMilli();

        // find all Completed and Terminated (i.e. those without an `end_` set) root processes older than X
        ArrayList<ProcessInstanceEntity> topLevelProcessInstances =
                processInstanceRepository.findProcessInstanceEntitiesByStartBeforeAndParentElementInstanceKeyIsAndEndIsNotNull(oldestProcessTime, -1);
        // find all their children processes and build all keys into a list
        ArrayList<Long> allRemovableProcessInstanceKeys = new ArrayList<Long>();
        for (ProcessInstanceEntity processInstance : topLevelProcessInstances) {
            allRemovableProcessInstanceKeys.addAll(findAllChildProcesses(processInstance.getKey()));
        }

        // with the combined list of keys, remove all related variables and finally the process
        for (long key : allRemovableProcessInstanceKeys) {
            jobRepository.deleteAllByProcessInstanceKey(key);
            elementInstanceRepository.deleteAllByProcessInstanceKey(key);
            errorRepository.deleteAllByProcessInstanceKey(key);
            incidentRepository.deleteAllByProcessInstanceKey(key);
            variableRepository.deleteAllByProcessInstanceKey(key);
            timerRepository.deleteAllByProcessInstanceKey(key);
            messageSubscriptionRepository.deleteAllByProcessInstanceKey(key);
            processInstanceRepository.deleteAllByKey(key);
        }

        // messages have to be removed separately
        // this is annoying and it should be fixed
        messageRepository.deleteAllByTimestampLessThanAndState(oldestProcessTime, "expired");
    }
}
