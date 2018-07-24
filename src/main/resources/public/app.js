var viewer = null;
var container = null;

let restAccess = "api/";
let brokerLogs = {};

let topics = [];

let isConnected = false;
var config;

var selectedTopic;
var workflowDefinitions;
var selectedWorkflowDefinition;

var workflowInstances;
var selectedWorkflowInstance;
var currentPage;

let recordsPerPage = 20;
var recordPage = 0;
var recordCount = 0;

function cleanupData() {
			$.ajax({
		             type : 'POST',
		             url: restAccess + 'broker/cleanup',
		             success: function (result) {
		            	 window.location.replace('/');		
		             },
		             error: function (xhr, ajaxOptions, thrownError) {
		               	 showErrorResonse(xhr, ajaxOptions, thrownError);
	                },
		             crossDomain: true,
		    });				
}

function init(page) {	
	currentPage = page;
	refresh();
}

function refresh() {
	
	if (currentPage=='broker') {
		loadConfiguration();
		checkConnection();
		loadTopology();
	} else if (currentPage=='definition') {
		loadTopics();
		loadWorkflowDefinitions();		
	} else if (currentPage=="instance") {
		loadWorkflowInstances();		
	} else if (currentPage=="logs") {
		loadRecords();		
	} else if (currentPage=="setup" ) {
		renderSetup();
	}
}

function renderSetup() {
	
	loadConfiguration();
	
	if (config) {
		window.location.replace('/');
	}
}

// -------- config page

function loadConfiguration() {
	
	$.ajax({
        type : 'GET',
        url: restAccess + 'broker/config',
        contentType: 'application/text; charset=utf-8',
        success: function (cfg) {
       	 config = cfg
       	 renderConfiguration(config)
        },
        error: function (xhr, ajaxOptions, thrownError) {
       	 showErrorResonse(xhr, ajaxOptions, thrownError);
        },
        timeout: 3000,
        crossDomain: true,
	});	
}

function renderConfiguration(config) {	
	$("#connection-string").html(config.connectionString)	
}

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
			
			$('#topologyTable tbody').append("<tr><td>" + broker.address + "</td><td>" + partition.topicName + "</td><td>" + partition.partitionId + "</td><td>" + partition.role + "</td></tr>");
		}
	}
}	

function connectToBroker() {
	$.ajax({
             type : 'POST',
             url: restAccess + 'broker/connect',
             contentType: 'application/text; charset=utf-8',
             success: function (cfg) {
            	refresh();		
             },
             error: function (xhr, ajaxOptions, thrownError) {
            	 showErrorResonse(xhr, ajaxOptions, thrownError);
             },
             timeout: 5000,
             crossDomain: true,
    });				
}

function createTopic() {
	var topicName = $('#topicName').val()
	var partitionCount = $('#partitionCount').val()
	var replicationFactor = $('#replicationFactor').val()
	
	var command = '{"topicName":"' + topicName + '", "partitionCount":' + partitionCount + ', "replicationFactor":' + replicationFactor + '}';
	
	$.ajax({
        type : 'POST',
        url: restAccess + 'topics/',
        data: command,
        contentType: 'application/json; charset=utf-8',
        success: function (result) {
        	setTimeout(function() {
				refresh();
			}, 1000);
        },
        error: function (xhr, ajaxOptions, thrownError) {
       	 showErrorResonse(xhr, ajaxOptions, thrownError);
        },
        timeout: 20000,
        crossDomain: true,
});
}

function setup() {
	setupTo( $('#brokerConnection').val() );
}

function setupTo(connectionString) {
	$.ajax({
             type : 'POST',
             url: restAccess + 'broker/setup',
             data: connectionString,
             contentType: 'application/text; charset=utf-8',
             success: function (cfg) {
            	 config = cfg
            	 window.location.replace('/')	
             },
             error: function (xhr, ajaxOptions, thrownError) {
            	 showErrorResonse(xhr, ajaxOptions, thrownError);
             },
             timeout: 20000,
             crossDomain: true,
    });				
}

function checkConnection() {
	$.ajax({
	     type : 'GET',
	     url: restAccess + 'broker/check-connection',
	     contentType: 'application/text; charset=utf-8',
	     success: function (result) {
	    	 isConnected = result;
	    	 renderConnectionState(result);		    	 
	     },
	     error: function (xhr, textStatus, thrownError) {
	    	 isConnected = false;	 
	    	 renderConnectionState(false);   	 
	     },
	     timeout: 3000,
	     crossDomain: true,
	});				
}	

function connect() {
	$.ajax({
             type : 'POST',
             url: restAccess + 'broker/connect',
             contentType: 'application/json; charset=utf-8',
             success: function (cfg) {
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

function renderConnectionState(connected) {
	if (connected) {
	$('#connection-state').html('<span class="label label-success">connected</span>');
	}
	else {
	$('#connection-state').html('<span class="label label-warning">disconnected</span>' + '<button onclick="connect()" type="button" class="btn btn-success pull-right">Connect</button>');
	
	}
}


//-------- workflow page

function loadTopics() {
	$.ajax({
        type : 'GET',
        url: restAccess + 'topics/',
        contentType: 'application/json; charset=utf-8',
        success: function (result) {
        	topics = result;
        	renderTopicSelection();
        },
        error: function (xhr, ajaxOptions, thrownError) {
       	 showErrorResonse(xhr, ajaxOptions, thrownError);
        },
        timeout: 3000,
        crossDomain: true,
	});	
}

function renderTopicSelection() {
	
	$("#selectedTopicDropdown").empty();
	
	$("#selectedTopicDropdown").click(function(){ 
		selectedTopic = $("#selectedTopicDropdown").val();
	});
	
	for (index = 0; index < topics.length; index++) {
		var topic = topics[index];
		
		$('#selectedTopicDropdown').append('<option value="' + topic + '">' + topic + '</option>');
	}
	
	if (selectedTopic) {
		$("#selectedTopicDropdown").val(selectedTopic);
	} else {
		$("#selectedTopicDropdown").val($("#selectedTopicDropdown option:first").val());
	}
}	

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
				"<td "+selectedClass+">"+def.topic+"</td>"+
				"<td "+selectedClass+">"+def.countRunning+"</td>"+
				"<td "+selectedClass+">"+def.countEnded+"</td>"+
				"</tr>");
	}
}	

function selectWorkflowDefinition(index) {
	selectedWorkflowDefinition = workflowDefinitions[index];
	
	renderWorkflowDefinitionTable(); // set selected could be done with less overhead - but this is quick for now
	renderSelectedWorkflowDefinition();
}

function renderSelectedWorkflowDefinition() {
	if (selectedWorkflowDefinition) {
		$('#workflowKey').html(selectedWorkflowDefinition.workflowKey);
		$('#bpmnProcessId').html(selectedWorkflowDefinition.bpmnProcessId);
		$('#workflowVersion').text(selectedWorkflowDefinition.version);
		$('#topic').text(selectedWorkflowDefinition.topic);

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
		
		$('#workflowInstanceTable tbody').append("<tr>" + 
				"<td "+selectedClass+"><a onclick='selectWorkflowInstance("+index+")'>"+def.workflowInstanceKey+"</a></td>" + 
				"<td "+selectedClass+">"+def.bpmnProcessId+"</td>"+
				"<td "+selectedClass+">"+def.workflowVersion+"</td>"+
				"<td "+selectedClass+">"+def.workflowKey+"</td>"+
				"<td "+selectedClass+">"+def.topicName+"</td>"+
				"<td "+selectedClass+">"+(def.ended ? "Ended" : "Running")+"</td>"+
				"</tr>");
	}
}	

function selectWorkflowInstance(index) {
	selectedWorkflowInstance = workflowInstances[index];
	
	renderWorkflowInstanceTable(); // set selected could be done with less overhead - but this is quick for now
	renderSelectedWorkflowInstance();
}

function renderSelectedWorkflowInstance() {
	if (selectedWorkflowInstance) {
		
		$('#workflowInstanceKey').html(selectedWorkflowInstance.workflowInstanceKey);
		if (selectedWorkflowInstance.ended) {
			$('#workflowRunning').html("Ended");
		} else {
			$('#workflowRunning').html("Running");
		}

		$('#workflowKey').html(selectedWorkflowInstance.workflowKey);
		$('#bpmnProcessId').html(selectedWorkflowInstance.bpmnProcessId);
		
		$('#payload').val(
			JSON.stringify(
				JSON.parse(selectedWorkflowInstance.payload), undefined, 2
			));
		
		$("#update-payload").prop("disabled", selectedWorkflowInstance.ended);
		$("#cancel-workflow-instance").prop("disabled", selectedWorkflowInstance.ended);
		
		renderIncidentsTable();

		$('#workflowInstanceInfo').text('');
		$.get(restAccess + 'workflows/' + selectedWorkflowInstance.workflowKey, function(result) {
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

//-------- log page

function loadRecords() {
	
	countRecords();
	
	recordPage = 0;		
}

function loadRecordPage() {
	start = recordPage * recordsPerPage;

	if (recordPage == 0) {
		$('#recordPrevious').addClass("disabled")
	} else {
		$('#recordPrevious').removeClass("disabled")
	}

	if ((start + recordsPerPage) >= recordCount) {
		$('#recordNext').addClass("disabled")
	} else {
		$('#recordNext').removeClass("disabled")
	}		
	
	$.ajax({
        type : 'POST',
        url: restAccess + 'records/search?start=' + start + '&limit=' + recordsPerPage,
        data:  $('#recordQuery').val(),
        contentType: 'application/json; charset=utf-8',
        success: function (logs) {
        	brokerLogs = logs;
    		renderRecordsTable();
        },
        error: function (xhr, ajaxOptions, thrownError) {
       	 showErrorResonse(xhr, ajaxOptions, thrownError);
        },
   	 timeout: 5000,
        crossDomain: true,
	});
}

function loadNextRecords() {		
	if ((start + recordsPerPage) < recordCount) {
		recordPage = recordPage + 1;	
		loadRecordPage();
	}
}

function loadPreviousRecords() {		
	if (recordPage > 0) {	
		recordPage = recordPage - 1;	
		loadRecordPage();
	}
}

function countRecords() {
	$.ajax({
        type : 'POST',
        url: restAccess + 'records/count',
        data:  $('#recordQuery').val(),
        contentType: 'application/json; charset=utf-8',
        success: function (count) {
          recordCount = count;
        	$('#recordCount').html(count)
        	
        	loadRecordPage();
        },
        error: function (xhr, ajaxOptions, thrownError) {
       	 showErrorResonse(xhr, ajaxOptions, thrownError);
        },
   	 timeout: 5000,
        crossDomain: true,
	});
}


function renderRecordsTable() {
	$("#brokerLogsTable > tbody").html("");

			for (index = brokerLogs.length-1; index >= 0; --index) {

				var log = brokerLogs[index];
				var json = log.content
				var prettyJson = JSON.stringify(json, null, 4)
                				
				$('#brokerLogsTable tbody').append(
					"<tr><td><p style='white-space:pre'>"+prettyJson+"</p></td>"
					+"</td></tr>");
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
	             url: restAccess + 'instances/' + selectedWorkflowInstance.id + "/update-payload",
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
	             url: restAccess + 'instances/' + selectedWorkflowInstance.id + "/update-retries",
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
	             url: restAccess + 'instances/' + selectedWorkflowInstance.id,
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
