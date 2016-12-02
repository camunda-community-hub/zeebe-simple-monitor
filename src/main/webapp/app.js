var viewer = null;
var container = null;

//let restAccess = "api/";
let restAccess = "http://localhost:8080/camunda-tngp-monitor/api/";
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

function startEmbeddedBroker() {
			$.ajax({
		             type : 'POST',
		             url: restAccess + 'broker/embedded/start',
		             //data: connectionString,
		             //contentType: 'application/text; charset=utf-8',
		             //dataType: 'json',
		             success: function (result) {
		             	$('#btnBrokerStart').hide(); $('#btnBrokerStop').show();
		             	brokerConnect('127.0.0.1:51015');
		             },
		             error: function (xhr, ajaxOptions, thrownError) {
		             	console.log(thrownError);
		             	showError(xhr.responseJSON.message);
		             },
		             crossDomain: true,
		    });				
}	
function stopEmbeddedBroker() {
			$.ajax({
		             type : 'POST',
		             url: restAccess + 'broker/embedded/stop',
		             //data: connectionString,
		             //contentType: 'application/text; charset=utf-8',
		             //dataType: 'json',
		             success: function (result) {
		             	brokerDisconnect('127.0.0.1:51015');
		             	$('#btnBrokerStart').show(); $('#btnBrokerStop').hide();
		             },
		             error: function (xhr, ajaxOptions, thrownError) {
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
		             	console.log(xhr);
		             	console.log(thrownError);
		             	showError(xhr.responseJSON.message);
		             },
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
		             	console.log(thrownError);
		             	showError(xhr.responseJSON.message);
		             },
		             crossDomain: true,
		    });		
}	


function renderBrokerTable() {
	$("#brokerTable > tbody").html("");
	for (index = brokers.length-1; index >= 0; --index) {
		var broker = brokers[index];
		if (broker.connected) {
			$('#brokerTable tbody').append("<tr><td>"+broker.name+"</td><td>"+broker.connectionString+"</td><td><span class='label label-success'>connected</span></td><td><a onclick='brokerDisconnect(\""+broker.connectionString+"\")'>disconnect</a></td></tr>");
		} else {
			$('#brokerTable tbody').append("<tr><td>"+broker.name+"</td><td>"+broker.connectionString+"</td><td>disconnected</td><td><a onclick='brokerConnect(\""+broker.connectionString+"\")'>connect</a></td></tr>");
		}
	}
	if (brokers && brokers.length > 0 && brokers[0].connected) {
		$('#brokerGlobalInfo').html('<a><span class="label label-success">connected</span></a>');
	} else {
		$('#brokerGlobalInfo').html('<a onclick="brokerConnect(\'127.0.0.1:51015\')"><span class="label label-danger">Not connected. Klick to connect default.</span></a>')	;	
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
		brokers = brokerList;
		renderBrokerTable();
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
	    	selectedWorkflowDefinition = workflowDefinitions[0];
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
		if (selectedWorkflowDefinition && def.id==selectedWorkflowDefinition.id) {
			selectedClass ='class="tngp-table-selected"';
		}
		$('#workflowDefinitionTable tbody').append("<tr><td "+selectedClass+"><a onclick='selectWorkflowDefinition("+index+")'>"+def.key + "(" + def.id + ")" +"</a></td><td "+selectedClass+">"+def.countRunning+"</td></tr>");
	}

    // add brokers to selected broker dropdown
	$('#selectedBrokerDropdown').empty();
	for (index = brokers.length-1; index >= 0; --index) {
		console.log(brokers[index].connectionString);
		$("#selectedBrokerDropdown").append('<option>'+brokers[index].connectionString+'</option>');
	}	
}	

function selectWorkflowDefinition(index) {
	selectedWorkflowDefinition = workflowDefinitions[index];
	renderSelectedWorkflowDefinition();
}

function renderSelectedWorkflowDefinition() {
	if (selectedWorkflowDefinition) {
		$('#workflowDefinitionId').html(selectedWorkflowDefinition.id);
		$('#workflowDefinitionName').html(selectedWorkflowDefinition.key);
		$('#workflowDefinitionVersion').text('');
		$('#workflowDefinitionBroker').text(selectedWorkflowDefinition.broker);

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
		$.ajax({
	             type : 'PUT',
	             url: restAccess + 'workflow-definition/' + selectedWorkflowDefinition.broker + "/" + selectedWorkflowDefinition.id,
	             data:  JSON.stringify( $('#payload').val() ),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	console.log("STARTED");
	             	setTimeout(function() {
    					refresh();
					}, 1000);
	             },
	             crossDomain: true,
	    });
	}
}

function loadWorkflowInstances() {
	$.get(restAccess + 'workflow-instance/', function(result) {
	    workflowInstances = result;
	    if (!selectedWorkflowInstance && workflowInstances && workflowInstances.length>0) {
	    	selectedWorkflowInstance = workflowInstances[0];
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

		$('#workflowDefinitionId').html(selectedWorkflowInstance.workflowDefinitionId);
		$('#workflowDefinitionKey').html(selectedWorkflowInstance.workflowDefinitionKey);
		$('#payload').html(
			JSON.stringify(
				JSON.parse(selectedWorkflowInstance.payload), undefined, 2
			));

		$('#workflowInstanceInfo').text('');
		$.get(restAccess + 'workflow-definition/' + selectedWorkflowInstance.workflowDefinitionId, function(result) {
			viewer.importXML(result.resource, function(err) {
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

								addBpmnOverlays(canvas, overlays, selectedWorkflowInstance);
							}
			});
		});
    }
}



function renderBrokerLogsTable() {
	$("#brokerLogsTable > tbody").html("");
	for (var broker in brokerLogs) {
	    var topics = brokerLogs[broker];
		for (var topic in topics) {
		    var logs = topics[topic];
			for (index = logs.length-1; index >= 0; --index) {
				var log = logs[index];
				$('#brokerLogsTable tbody').append("<tr><td>"+broker+"</td><td>"+topic+"</td><td>"+log+"</td></tr>");
			}
		}
	}
}





function showError(errorText) {
	$("#errorText").html(errorText);
	$("#errorPanel").show();
}
function ackError() {
	$("#errorPanel").hide();
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
    					refresh();
					}, 1000);
	             },
	             crossDomain: true,
	    });
	}; 
      
    
}
	
function addBpmnOverlays(canvas, overlays, workflowInstance) {
	
        for (index = 0; index < workflowInstance.endedActivities.length; ++index) {
				overlays.add(workflowInstance.endedActivities[index], {
				  position: {
				    top: 0,
				    left: 0
				  },
				  html: '<div class="bpmn-badge"><span class="glyphicon glyphicon-ok"></span></div>'
				});
		}
        for (index = 0; index < workflowInstance.runningActivities.length; ++index) {
        	canvas.addMarker(workflowInstance.runningActivities[index], 'highlight');
		}
}	
