let restAccess = "api/";
let brokers = [
	{
		name: 'default',
		connectionString: '127.0.0.1:51015',
		state: 'disconnected'		
	}
];

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
		             	broker.state = 'connected';
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
		             	broker.state = 'disconnected';
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
		if (broker.state=='connected') {
			$('#brokerTable tbody').append("<tr><td>"+broker.name+"</td><td>"+broker.connectionString+"</td><td><span class='label label-success'>connected</span></td><td><a onclick='brokerDisconnect(\""+broker.connectionString+"\")'>disconnect</a></td></tr>");
		} else {
			$('#brokerTable tbody').append("<tr><td>"+broker.name+"</td><td>"+broker.connectionString+"</td><td>disconnected</td><td><a onclick='brokerConnect(\""+broker.connectionString+"\")'>connect</a></td></tr>");
		}
	}
}	

function init() {
	renderBrokerTable();
	
			var BpmnViewer = window.BpmnJS;
			var viewer = new BpmnViewer({container: '#diagramCanvas', width: '100%', height: '100%'});

			var container = $('#js-drop-zone');

			$.get(restAccess + 'workflow-definition/', function(workflowDefinitions) {
			//$.get('webjars/simple.bpmn', function(workflowDefinitions) {
				console.log(workflowDefinitions)
				for (let index = 0; index < workflowDefinitions.length; ++index) {
					if (index==0) {
						viewer.importXML(workflowDefinitions[index].resource, function(err) {
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

			});			
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