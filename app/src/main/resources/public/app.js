var viewer = null;
var container = null;

var currentPage;

var workflowDefinitions;
var selectedWorkflowDefinition;

var workflowInstances;
var selectedWorkflowInstance;

var restAccess = "api/";
var isConnected = false;

// --------------------------------------------------------------------

function init(page) {	
	currentPage = page;
	refresh();
}

function refresh() {

	if (currentPage=='definition') {
		loadWorkflowDefinitions();		
		
	} else if (currentPage=="instance") {
		loadWorkflowInstances();		
	} 
}

function showError(errorText) {
	$("#errorText").html(errorText);
	$("#errorPanel").show();
}
function ackError() {
	$("#errorPanel").hide();
}

function showErrorResonse(xhr, ajaxOptions, thrownError) {
	if (xhr.responseJSON) {
		showError(xhr.responseJSON.message);
	}
	else {
		showError(thrownError);
	}	
}

// --------------------------------------------------------------------

function loadTopology() {
	
	$.ajax({
        type : 'GET',
        url: restAccess + 'broker/topology',
        contentType: 'application/json; charset=utf-8',
        success: function (topology) {
       	 renderTopology(topology)
        },
        error: function (xhr, ajaxOptions, thrownError) {
        	if (isConnected) {
        		showErrorResonse(xhr, ajaxOptions, thrownError);
        	}
        },
        timeout: 3000,
        crossDomain: true,
	});	
}

function renderTopology(topology) {
	$("#topologyTable > tbody").html("");
	for (index = 0; index < topology.length; index++) {
		var broker = topology[index];
		
		for (p = 0; p < broker.partitions.length; p++) {
			var partition = broker.partitions[p];
			
			$('#topologyTable tbody').append("<tr><td>" + broker.address + "</td><td>" + partition.partitionId + "</td><td>" + partition.role + "</td></tr>");
		}
	}
}	

function checkConnection() {
	$.ajax({
	     type : 'GET',
	     url: restAccess + 'broker/check-connection',
	     contentType: 'application/text; charset=utf-8',
	     success: function (result) {
	    	 isConnected = result;    	 
	     },
	     error: function (xhr, textStatus, thrownError) {
	    	 isConnected = false;	 	 
	     },
	     timeout: 3000,
	     crossDomain: true,
	});				
}	

//-------- workflow page

function loadWorkflowDefinitions() {
	$.get(restAccess + 'workflows/', function(result) {
	    workflowDefinitions = result;
	    if (!selectedWorkflowDefinition && workflowDefinitions && workflowDefinitions.length>0) {
	    	selectedWorkflowDefinition = workflowDefinitions[workflowDefinitions.length - 1];
	    }
	    renderWorkflowDefinitionTable();
	    renderSelectedWorkflowDefinition();
	});			
}

function renderWorkflowDefinitionTable() {
	$("#workflowDefinitionTable > tbody").html("");
	for (index = workflowDefinitions.length-1; index >= 0; --index) {
		var def = workflowDefinitions[index];
		var selectedClass = '';
		if (selectedWorkflowDefinition && def.workflowKey==selectedWorkflowDefinition.workflowKey) {
			selectedClass ='class="tngp-table-selected"';
		}
		$('#workflowDefinitionTable tbody').append("<tr>" + 
				"<td "+selectedClass+"><a onclick='selectWorkflowDefinition("+index+")'>"+def.workflowKey+"</a></td>" + 
				"<td "+selectedClass+"><a onclick='selectWorkflowDefinition("+index+")'>"+def.bpmnProcessId+"</a></td>"+
				"<td "+selectedClass+">"+def.version+"</td>"+
				"<td "+selectedClass+">"+def.countRunning+"</td>"+
				"<td "+selectedClass+">"+def.countEnded+"</td>"+
				"</tr>");
	}
}	

function selectWorkflowDefinition(index) {
	selectedWorkflowDefinition = workflowDefinitions[index];
	
	renderWorkflowDefinitionTable(); 
	renderSelectedWorkflowDefinition();
}

function renderSelectedWorkflowDefinition() {
	if (selectedWorkflowDefinition) {
		$('#workflowKey').html(selectedWorkflowDefinition.workflowKey);
		$('#bpmnProcessId').html(selectedWorkflowDefinition.bpmnProcessId);
		$('#workflowVersion').text(selectedWorkflowDefinition.version);
		
		$('#countRunning').text(selectedWorkflowDefinition.countRunning);
		$('#countEnded').text(selectedWorkflowDefinition.countEnded);

						viewer.importXML(selectedWorkflowDefinition.resource, function(err) {
							if (err) {
								console.log('error rendering', err);
				             	showError(err);
							} else {
								var canvas = viewer.get('canvas');
								var overlays = viewer.get('overlays');

								container.removeClass('with-error')
										 .addClass('with-diagram');

								// zoom to fit full viewport
								canvas.zoom('fit-viewport');
							}
						});
    }
}

function startWorkflowInstance() {
	
	if (selectedWorkflowDefinition) {
		
		$.ajax({
	             type : 'POST',
	             url: restAccess + 'workflows/' + selectedWorkflowDefinition.workflowKey,
	             data:  $('#payload').val(),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	setTimeout(function() {
    					refresh();
					}, 1000);
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
	}
}

//-------- instance page

function loadWorkflowInstances() {
	$.get(restAccess + 'instances/', function(result) {
	    workflowInstances = result;
	    if (workflowInstances && workflowInstances.length>0) {
	    	
	      var index = -1
	      if (selectedWorkflowInstance) {
	    	index = workflowInstances.findIndex(function(wf) { return wf.key == selectedWorkflowInstance.key});  
	      }	      
	      if (index < 0) {
	        index = workflowInstances.length - 1
	      }
	      selectedWorkflowInstance = workflowInstances[index]
	    }
	    else {
	      selectedWorkflowInstance = null
	    }	    	

	    renderWorkflowInstanceTable();
	    renderSelectedWorkflowInstance();
	});			
}

function renderWorkflowInstanceTable() {
	$("#workflowInstanceTable > tbody").html("");
	for (index = workflowInstances.length-1; index >= 0; --index) {
		var def = workflowInstances[index];
		var selectedClass = '';
		if (selectedWorkflowInstance && def.key==selectedWorkflowInstance.key) {
			selectedClass ='class="tngp-table-selected"';
		}
		
		$('#workflowInstanceTable tbody').append("<tr>" + 
				"<td "+selectedClass+"><a onclick='selectWorkflowInstance("+index+")'>"+def.key+"</a></td>" + 
				"<td "+selectedClass+">"+def.bpmnProcessId+"</td>"+
				"<td "+selectedClass+">"+def.version+"</td>"+
				"<td "+selectedClass+">"+def.workflowKey+"</td>"+
				"<td "+selectedClass+">"+(def.end ? "Ended" : "Running")+"</td>"+
				"</tr>");
	}
}	

function selectWorkflowInstance(index) {
	selectedWorkflowInstance = workflowInstances[index];
	
	renderWorkflowInstanceTable(); 
	renderSelectedWorkflowInstance();
}

function renderSelectedWorkflowInstance() {
	if (selectedWorkflowInstance) {
		
		$.get(restAccess + 'instances/' + selectedWorkflowInstance.key, function(result) {
		
			$('#workflowInstanceKey').html(selectedWorkflowInstance.key);
			if (result.ended) {
				$('#workflowRunning').html("Ended");
			} else {
				$('#workflowRunning').html("Running");
			}
	
			$('#workflowKey').html(selectedWorkflowInstance.workflowKey);
			$('#bpmnProcessId').html(selectedWorkflowInstance.bpmnProcessId);
			
			$('#payload').val(
				JSON.stringify(
					JSON.parse(result.payload), undefined, 2
				));
			
			$("#update-payload").prop("disabled", result.ended);
			$("#cancel-workflow-instance").prop("disabled", result.ended);
			$("#update-retries").prop("disabled", result.ended || result.incidents.filter(i => i.errorType == "JOB_NO_RETRIES").length == 0);
			
			renderIncidentsTable(result);
	
			$('#workflowInstanceInfo').text('');
		
			viewer.importXML(result.workflowResource, function(err) {
							if (err) {
								console.log('error rendering', err);
				             	showError(err);
							} else {
								var canvas = viewer.get('canvas');
								var overlays = viewer.get('overlays');
								var injector = viewer.get('injector');

								container.removeClass('with-error')
										 .addClass('with-diagram');

								// zoom to fit full viewport
								canvas.zoom('fit-viewport');

								addBpmnOverlays(canvas, overlays, result);
								markSequenceFlows(injector, result);
							}
			});
		});
    }
}

function renderIncidentsTable(instance) {
	$("#incidentsTable > tbody").html("");
	for (index = 0; index < instance.incidents.length; ++index) {
		var incident = instance.incidents[index];
		$('#incidentsTable tbody').append("<tr><td>"+incident.errorType+"</td><td>"+incident.errorMessage+"</td></tr>");
	}
}

function uploadModels() {
  	
	var fileUpload = $('#documentToUpload').get(0);

	var filesToUpload = {
		topic: $('#selectedTopicDropdown').val(), 
		files: []
	} 

	var processUploadedFile = function(fileUpload, index) {
		return function(e) {
		            var binary = '';
		            var bytes = new Uint8Array( e.target.result );
		            var len = bytes.byteLength;
		            for (var j = 0; j < len; j++) {
		                binary += String.fromCharCode( bytes[ j ] );
		            }

		            var currentFile = {
		            	filename: fileUpload.files[index].name,
		            	mimeType: fileUpload.files[index].type,
		            	content:  btoa(binary)
		            }

		            filesToUpload.files.push(currentFile);

		            // if all files are processed - do the upload
		            if (filesToUpload.files.length == fileUpload.files.length) {
		            	uploadFiles();
		            }
		};
	}

    // read all selected files
	if(typeof FileReader === 'function' && fileUpload.files.length > 0) {
		for (index = 0; index < fileUpload.files.length; ++index) {	  

		    var reader = new FileReader();
		    reader.onloadend = processUploadedFile(fileUpload, index);
            reader.readAsArrayBuffer(fileUpload.files[index]);
        }
    }
	    
	var uploadFiles = function() {
	    $.ajax({
	             type : 'POST',
	             url: restAccess + 'workflows/',
	             data:  JSON.stringify(filesToUpload),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	setTimeout(function() {
	             		selectedWorkflowDefinition = null;
    					refresh();
					}, 1000);
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
	}; 
      
    
}
	
function addBpmnOverlays(canvas, overlays, workflowInstance) {
	
		for (index = 0; index < workflowInstance.runningActivities.length; ++index) {
	    	canvas.addMarker(workflowInstance.runningActivities[index], 'highlight');
		}
	
        for (index = 0; index < workflowInstance.endedActivities.length; ++index) {
				overlays.add(workflowInstance.endedActivities[index], {
				  position: {
				    top: 0,
				    left: 0
				  },
				  html: '<div class="bpmn-badge"><span class="glyphicon glyphicon-ok"></span></div>'
				});
		}
        
        for (index = 0; index < workflowInstance.incidents.length; ++index) {
			overlays.add(workflowInstance.incidents[index].activityId, {
			  position: {
			    top: 0,
			    left: 0
			  },
			  html: '<div class="bpmn-badge error"><span class="glyphicon glyphicon glyphicon-flash"></span></div>'
			});			
        }
}	

function markSequenceFlows(injector, workflowInstance) {
	
	var elementRegistry = injector.get('elementRegistry'),
		graphicsFactory = injector.get('graphicsFactory');
	
	var takenSequenceFlows = workflowInstance.takenSequenceFlows.map(function(id) {
		return elementRegistry.get(id);
	});
	
	takenSequenceFlows.forEach(function(sequenceFlow) {
		var gfx = elementRegistry.getGraphics(sequenceFlow);
		
		colorSequenceFlow(graphicsFactory, sequenceFlow, gfx, '#52b415');
	});
}

function colorSequenceFlow(graphicsFactory, sequenceFlow, gfx, color) {
	var businessObject = sequenceFlow.businessObject,
		di = businessObject.di;
	
	di.set('stroke', color);
	di.set('fill', color);
	
	graphicsFactory.update('connection', sequenceFlow, gfx);
}

function updatePayload() {
	if (selectedWorkflowInstance) {
		$.ajax({
	             type : 'PUT',
	             url: restAccess + 'instances/' + selectedWorkflowInstance.key + "/update-payload",
	             data:  $('#payload').val(),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	setTimeout(function() {
    					refresh();
					}, 1000);
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
	}
}

function updateRetries() {
	if (selectedWorkflowInstance) {
		$.ajax({
	             type : 'PUT',
	             url: restAccess + 'instances/' + selectedWorkflowInstance.key + "/update-retries",
	             data:  '{"retries": "2"}', // TODO
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	setTimeout(function() {
    					refresh();
					}, 1000);
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
	}
}

function cancelWorkflowInstance() {
	if (selectedWorkflowInstance) {
		$.ajax({
	             type : 'DELETE',
	             url: restAccess + 'instances/' + selectedWorkflowInstance.key,
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	setTimeout(function() {
    					refresh();
					}, 1000);
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
	}
}
