package io.zeebe.monitor.rest;

import io.camunda.zeebe.client.api.response.BrokerInfo;
import io.camunda.zeebe.client.api.response.PartitionInfo;
import io.camunda.zeebe.client.api.response.Topology;
import io.zeebe.monitor.rest.dto.BrokerDto;
import io.zeebe.monitor.rest.dto.PartitionInfoDto;
import io.zeebe.monitor.rest.dto.StatusDto;
import io.zeebe.monitor.zeebe.ZeebeStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class ServiceStatusViewController extends AbstractViewController {

  @Autowired private ZeebeStatusService zeebeStatusService;
  @Autowired BuildProperties buildProperties;

  @GetMapping("/views/service-status")
  public String index(final Map<String, Object> model, final Pageable pageable) {
    addDefaultAttributesToModel(model);

    final Topology topology = zeebeStatusService.getTopology();
    model.put("status", toDto(topology, buildProperties));

    return "service-status-view";
  }

  private StatusDto toDto(Topology topology, BuildProperties buildProperties) {
    final StatusDto statusDto = new StatusDto();
    statusDto.setClusterSize(topology.getClusterSize());
    statusDto.setGatewayVersion(topology.getGatewayVersion());
    statusDto.setPartitionsCount(topology.getPartitionsCount());
    statusDto.setReplicationFactor(topology.getReplicationFactor());
    statusDto.setSimpleMonitorVersion(buildProperties.getVersion());
    statusDto.setSimpleMonitorBuildTime(buildProperties.getTime().toString());
    for (BrokerInfo broker : topology.getBrokers()) {
      final BrokerDto brokerDto = new BrokerDto();
      brokerDto.setAddress(broker.getAddress());
      brokerDto.setHost(broker.getHost());
      brokerDto.setNodeId(broker.getNodeId());
      brokerDto.setPort(broker.getPort());
      brokerDto.setVersion(broker.getVersion());
      for (PartitionInfo partition : broker.getPartitions()) {
        final PartitionInfoDto partitionInfoDto = new PartitionInfoDto();
        partitionInfoDto.setPartitionId(partition.getPartitionId());
        partitionInfoDto.setRole(String.valueOf(partition.getRole()));
        partitionInfoDto.setHealth(String.valueOf(partition.getHealth()));
        partitionInfoDto.setLeader(partition.isLeader());
        brokerDto.addPartitionInfo(partitionInfoDto);
      }
      statusDto.addBroker(brokerDto);
    }
    return statusDto;
  }

}
