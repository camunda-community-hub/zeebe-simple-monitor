var viewer = null;
var container = null;

let restAccess = "api/";
//let restAccess = "http://localhost:8080/camunda-tngp-monitor/api/";
let brokers = [];
let brokerLogs = {};

var workflowDefinitions;
var selectedWorkflowDefinition;

var workflowInstances;
var selectedWorkflowInstance;
var currentPage;

/*
{
		name: 'default',
		connectionString: '127.0.0.1:51015',
		state: 'disconnected'		
	}
	*/

function cleanupData() {
			$.ajax({
		             type : 'POST',
		             url: restAccess + 'broker/cleanup',
		             success: function (result) {
					    loadBrokers();
					    refresh();		
		             },
		             error: function (xhr, ajaxOptions, thrownError) {
		             	console.log(xhr);
		             	console.log(thrownError);
		             	showError(xhr.responseJSON.message);
		             },
		             crossDomain: true,
		    });				
}

function brokerConnect(connectionString) {
			$.ajax({
		             type : 'POST',
		             url: restAccess + 'broker/connect',
		             data: connectionString,
		             contentType: 'application/text; charset=utf-8',
		             //dataType: 'json',
		             success: function (result) {
					    loadBrokers();
					    refresh();		
		             },
		             error: function (xhr, ajaxOptions, thrownError) {
		            	 showErrorResonse(xhr, ajaxOptions, thrownError);
		             },
		             timeout: 5000,
		             crossDomain: true,
		    });				
}	
function brokerDisconnect(connectionString) {
			$.ajax({
		             type : 'POST',
		             url: restAccess + 'broker/disconnect',
		             data: connectionString,
		             contentType: 'application/text; charset=utf-8',
		             //dataType: 'json',
		             success: function (result) {
					    loadBrokers();
					    refresh();		
		             },
		             error: function (xhr, ajaxOptions, thrownError) {
		            	 showErrorResonse(xhr, ajaxOptions, thrownError);
		             },
		             timeout: 5000,
		             crossDomain: true,
		    });		
}	


function renderBrokerTable() {
	$("#brokerTable > tbody").html("");
	var atLeastOneBrokerConnected = false;
	for (index = brokerConnections.length-1; index >= 0; --index) {
		var brokerConnection = brokerConnections[index];
		if (brokerConnection.connected) {
			$('#brokerTable tbody').append("<tr><td>"+brokerConnection.broker.name+"</td><td>"+brokerConnection.broker.connectionString+"</td><td><span class='label label-success'>connected</span></td><td><a onclick='brokerDisconnect(\""+brokerConnection.broker.connectionString+"\")'>disconnect</a></td></tr>");
			atLeastOneBrokerConnected = true;
		} else {
			$('#brokerTable tbody').append("<tr><td>"+brokerConnection.broker.name+"</td><td>"+brokerConnection.broker.connectionString+"</td><td>disconnected</td><td><a onclick='brokerConnect(\""+brokerConnection.broker.connectionString+"\")'>connect</a></td></tr>");
		}
	}
	if (atLeastOneBrokerConnected) {
		$('#brokerGlobalInfo').html('<a><span class="label label-success">connected</span></a>');
	} else {
		$('#brokerGlobalInfo').html('<a onclick="brokerConnect(\'127.0.0.1:51015\')"><span class="label label-danger">Not connected. Click to connect default.</span></a>')	;	
	}
}

function renderBrokerDropdown() {
    // add brokers to selected broker dropdown
	$('#selectedBrokerDropdown').empty();
	for (index = brokerConnections.length-1; index >= 0; --index) {
		$("#selectedBrokerDropdown").append('<option>'+brokerConnections[index].broker.connectionString+'</option>');
	}	
}

function init(page) {	
	currentPage = page;
	refresh();
}

function refresh() {
	if (currentPage=='broker') {
		loadBrokers();
	} else if (currentPage=='definition') {
		loadBrokers();
		loadWorkflowDefinitions();		
	} else if (currentPage=="instance") {
		loadBrokers();
		loadWorkflowInstances();		
	} else if (currentPage=="logs") {
		loadBrokers();
		loadBrokerLogs();		
	}
}

function addBroker() {
	brokerConnect( $('#brokerConnection').val() );
	$('#brokerConnection').text('');
}

function loadBrokers() {
	$.get(restAccess + 'broker/', function(brokerList) {
		brokerConnections = brokerList;
		renderBrokerTable();
		renderBrokerDropdown();
	});
}

function loadBrokerLogs() {
	$.get(restAccess + 'broker/log', function(logs) {
		brokerLogs = logs;
		renderBrokerLogsTable();
	});
}


function loadWorkflowDefinitions() {
	$.get(restAccess + 'workflow-definition/', function(result) {
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
		if (selectedWorkflowDefinition && def.key==selectedWorkflowDefinition.key && def.version==selectedWorkflowDefinition.version) {
			selectedClass ='class="tngp-table-selected"';
		}
		$('#workflowDefinitionTable tbody').append("<tr><td "+selectedClass+"><a onclick='selectWorkflowDefinition("+index+")'>"+def.key + "(" + def.version + ")" +"</a></td><td "+selectedClass+">"+def.countRunning+"</td></tr>");
	}
}	

function selectWorkflowDefinition(index) {
	selectedWorkflowDefinition = workflowDefinitions[index];
	
	renderWorkflowDefinitionTable(); // set selected could be done with less overhead - but this is quick for now
	renderSelectedWorkflowDefinition();
}

function renderSelectedWorkflowDefinition() {
	if (selectedWorkflowDefinition) {
		$('#workflowDefinitionKey').html(selectedWorkflowDefinition.key);
		$('#workflowDefinitionName').html(selectedWorkflowDefinition.key);
		$('#workflowDefinitionVersion').text(selectedWorkflowDefinition.version);
		$('#workflowDefinitionBroker').text(selectedWorkflowDefinition.broker.connectionString);

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
	console.log(selectedWorkflowDefinition);
	if (selectedWorkflowDefinition) {
		console.log(JSON.stringify( $('#payload').val() ));
		$.ajax({
	             type : 'PUT',
	             url: restAccess + 'workflow-definition/' + selectedWorkflowDefinition.broker.connectionString + "/" + selectedWorkflowDefinition.key + "/" + selectedWorkflowDefinition.version,
	             data:  $('#payload').val(),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	console.log("STARTED");
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

function loadWorkflowInstances() {
	$.get(restAccess + 'workflow-instance/', function(result) {
	    workflowInstances = result;
	    if (workflowInstances && workflowInstances.length>0) {
	    	
	      var index = -1
	      if (selectedWorkflowInstance) {
	    	index = workflowInstances.findIndex(function(wf) { return wf.id == selectedWorkflowInstance.id});  
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
		if (selectedWorkflowInstance && def.id==selectedWorkflowInstance.id) {
			selectedClass ='class="tngp-table-selected"';
		}
		$('#workflowInstanceTable tbody').append(
			"<tr><td "+selectedClass+"><a onclick='selectWorkflowInstance("+index+")'>"+def.id +"</a></td><td "+selectedClass+">"+def.workflowDefinitionKey+"</td></tr>");
	}
}	

function selectWorkflowInstance(index) {
	selectedWorkflowInstance = workflowInstances[index];
	
	renderWorkflowInstanceTable(); // set selected could be done with less overhead - but this is quick for now
	renderSelectedWorkflowInstance();
}

function renderSelectedWorkflowInstance() {
	if (selectedWorkflowInstance) {
		
		$('#workflowInstanceId').html(selectedWorkflowInstance.id);
		if (selectedWorkflowInstance.ended) {
			$('#workflowRunning').html("Ended");
		} else {
			$('#workflowRunning').html("Running");
		}

		$('#workflowDefinitionUuid').html(selectedWorkflowInstance.workflowDefinitionUuid);
		$('#workflowDefinitionKey').html(selectedWorkflowInstance.workflowDefinitionKey);
		$('#payload').val(
			JSON.stringify(
				JSON.parse(selectedWorkflowInstance.payload), undefined, 2
			));
		
		renderIncidentsTable();

		$('#workflowInstanceInfo').text('');
		$.get(restAccess + 'workflow-definition/' + selectedWorkflowInstance.broker.connectionString + '/' + selectedWorkflowInstance.workflowDefinitionKey + '/' + selectedWorkflowInstance.workflowDefinitionVersion, function(result) {
			viewer.importXML(result.resource, function(err) {
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

								addBpmnOverlays(canvas, overlays, selectedWorkflowInstance);
								markSequenceFlows(injector, selectedWorkflowInstance);
							}
			});
		});
    }
}

function renderIncidentsTable() {
	$("#incidentsTable > tbody").html("");
	for (index = 0; index < selectedWorkflowInstance.incidents.length; ++index) {
		var incident = selectedWorkflowInstance.incidents[index];
		$('#incidentsTable tbody').append("<tr><td>"+incident.errorType+"</td><td>"+incident.errorMessage+"</td></tr>");
	}
}

function renderBrokerLogsTable() {
	$("#brokerLogsTable > tbody").html("");

			for (index = brokerLogs.length-1; index >= 0; --index) {

				var loggedEvent = brokerLogs[index];
                var payload = JSON.parse(loggedEvent.payload);
                
				$('#brokerLogsTable tbody').append(
					"<tr><td>"+loggedEvent.broker.connectionString+"</td>"
					+"<td>"+loggedEvent.eventType+"</td>"
					+"<td>"+loggedEvent.state+"</td>"
					+"<td>"+loggedEvent.partitionId+"</td>"
					+"<td>"+loggedEvent.position+"</td>"
					+"<td>"+loggedEvent.key+"</td>"
					+"<td>"
			        + '<a label="Details" data-toggle="collapse" data-target="#payload'+index+'" class="btn btn-default table-row-btn"><span class="glyphicon glyphicon-eye-open"></span></a>'
			        +"<div class=\"collapse\" id=\"payload"+ index + "\"><pre>"+JSON.stringify(payload, null, 2)+"</pre></div></td></tr>");

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


function uploadModels() {
  	
	var fileUpload = $('#documentToUpload').get(0);

	var filesToUpload = {
		broker: $('#selectedBrokerDropdown').val(), 
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
	             url: restAccess + 'workflow-definition/',
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
	             url: restAccess + 'workflow-instance/' + selectedWorkflowInstance.id + "/update-payload",
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

function cancelWorkflowInstance() {
	if (selectedWorkflowInstance) {
		$.ajax({
	             type : 'DELETE',
	             url: restAccess + 'workflow-instance/' + selectedWorkflowInstance.id,
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
