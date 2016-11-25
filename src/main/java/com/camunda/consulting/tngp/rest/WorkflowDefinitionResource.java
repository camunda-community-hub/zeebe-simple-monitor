package com.camunda.consulting.tngp.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.camunda.consulting.tngp.dto.WorkflowDefinitionDto;

@Path("workflow-definition")
@Produces("application/json")
public class WorkflowDefinitionResource {
  
  public static List<WorkflowDefinitionDto> definitions = new ArrayList<>();
  
  @GET
  public List<WorkflowDefinitionDto> getWorkflowDefinitions() {
    return definitions;
  }

}
