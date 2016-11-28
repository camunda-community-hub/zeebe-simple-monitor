var viewer = null;
var container = null;

//let restAccess = "api/";
let restAccess = "http://localhost:8080/camunda-tngp-monitor/api/";
let brokers = [];

var workflowDefinitions;
var selectedWorkflowDefinition;

var workflowInstances;
var selectedWorkflowInstance;


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
		             	var broker = brokers.filter(function( broker ) { return broker.connectionString == connectionString; })[0];
		             	broker.connected = true;
					    renderBrokerTable();		
		             },
		             error: function (xhr, ajaxOptions, thrownError) {
		             	console.log(thrownError);
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
		             	var broker = brokers.filter(function( broker ) { return broker.connectionString == connectionString; })[0];
		             	broker.connected = false;
					    renderBrokerTable();		
		             },
		             error: function (xhr, ajaxOptions, thrownError) {
		             	console.log(thrownError);
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

function initBroker() {
	loadBrokers();
}

function loadBrokers() {
	$.get(restAccess + 'broker/', function(brokerList) {
		brokers = brokerList;
		renderBrokerTable();
	});
}


function initDefinitions() {	
	loadBrokers();
	loadWorkflowDefinitions();
}

function loadWorkflowDefinitions() {
	$.get(restAccess + 'workflow-definition/', function(result) {
	    workflowDefinitions = result;
	    renderWorkflowDefinitionTable();
	    if (workflowDefinitions && workflowDefinitions.length>0) {
	    	selectedWorkflowDefinition = workflowDefinitions[0];
	    } else {
	    	selectedWorkflowDefinition = null;
	    }
	    renderSelectedWorkflowDefinition();
	});			
}

function renderWorkflowDefinitionTable() {
	$("#workflowDefinitionTable > tbody").html("");
	for (index = workflowDefinitions.length-1; index >= 0; --index) {
		var def = workflowDefinitions[index];
		$('#workflowDefinitionTable tbody').append("<tr><td><a onclick='selectWorkflowDefinition("+index+")'>"+def.key + "(" + def.id + ")" +"</a></td></tr>");
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
		$('#workflowDefinitionInfo').text('');
						viewer.importXML(selectedWorkflowDefinition.resource, function(err) {
							if (err) {
								console.log('error rendering', err);
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

function initWorkflowInstances() {	
	loadBrokers();
	loadWorkflowInstances();
}

function loadWorkflowInstances() {
	$.get(restAccess + 'workflow-instance/', function(result) {
	    workflowInstances = result;
	    renderWorkflowInstanceTable();

	    if (workflowInstances && workflowInstances.length>0) {
	    	selectedWorkflowInstance = workflowInstances[0];
	    } else {
	    	selectedWorkflowInstance = null;
	    }
	    renderSelectedWorkflowInstance();
	});			
}

function renderWorkflowInstanceTable() {
	$("#workflowInstanceTable > tbody").html("");
	for (index = workflowInstances.length-1; index >= 0; --index) {
		var def = workflowInstances[index];
		$('#workflowInstanceTable tbody').append(
			"<tr><td><a onclick='selectWorkflowInstance("+index+")'>"+def.id +"</a></td><td>"+def.workflowDefinitionKey+"</td></tr>");
	}
}	

function selectWorkflowInstance(index) {
	selectedWorkflowInstance = workflowInstances[index];
	renderSelectedWorkflowInstance();
}

function renderSelectedWorkflowInstance() {
	if (selectedWorkflowInstance) {
		console.log(selectedWorkflowInstance);
		$('#workflowInstanceId').html(selectedWorkflowInstance.workflowInstanceId);
		$('#workflowDefinitionId').html(selectedWorkflowInstance.workflowDefinitionId);
		$('#workflowDefinitionKey').html(selectedWorkflowInstance.workflowDefinitionKey);
		$('#payload').text(selectedWorkflowInstance.payload);
		$('#workflowInstanceInfo').text('');
		$.get(restAccess + 'workflow-definition/' + selectedWorkflowInstance.workflowDefinitionId, function(result) {
			viewer.importXML(result.resource, function(err) {
							if (err) {
								console.log('error rendering', err);
							} else {
								var canvas = viewer.get('canvas');
								var overlays = viewer.get('overlays');

								container.removeClass('with-error')
										 .addClass('with-diagram');

								// zoom to fit full viewport
								canvas.zoom('fit-viewport');
							}
			});
		});
    }
}
	
	function addMarkerForActivities(canvas, actInstTree) {
		if (actInstTree.childTransitionInstances.length==0 && actInstTree.childActivityInstances.length==0) {
			canvas.addMarker(actInstTree.activityId, 'highlight');	
		}
		else {
			for (index=0; index < actInstTree.childTransitionInstances.length; ++index) {
				canvas.addMarker(actInstTree.childTransitionInstances[index].activityId, 'highlight');	
			}
			for (index=0; index < actInstTree.childActivityInstances.length; ++index) {
			    // add recursively
				addMarkerForActivities(canvas, actInstTree.childActivityInstances[index]);	
			}
		}
	}
	
	function addHistoryInfoOverlay(overlays, actInstList) {
	
       for (index = 0; index < actInstList.length; ++index) {
			var calledPiLink = '';
			var finished = '';
			if (actInstList[index].endTime) {
				finished = '<i class="icon-ok icon-white"></i>';
			}
			if (actInstList[index].calledCaseInstanceId) {
				calledPiLink = '<a href="cmmn.html?caseInstanceId=' + actInstList[index].calledCaseInstanceId + '"><i class="icon-circle-arrow-right icon-white"></i></a>';
			}		
			if (actInstList[index].calledProcessInstanceId) {
				calledPiLink = '<a href="bpmn.html?processInstanceId=' + actInstList[index].calledProcessInstanceId + '"><i class="icon-circle-arrow-right icon-white"></i></a>';
			}
			if (finished || calledPiLink) {
				overlays.add(actInstList[index].activityId, {
				  position: {
				    top: 0,
				    right: 0
				  },
				  html: '<div class="bpmn-badge">'+ finished + calledPiLink+'</div>'
				});
			}					        
       }
	}