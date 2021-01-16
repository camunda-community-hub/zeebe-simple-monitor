
function showError(message) {
	document.getElementById("errorText").innerHTML = message;
	$('#errorPanel').show();
}

function showSuccess(message) {
	document.getElementById("successText").innerHTML = message;
	$('#successPanel').show();
}

function showInfo(message) {
	document.getElementById("infoText").innerHTML = message;
	$('#infoPanel').show();
}

function showErrorResonse(xhr, ajaxOptions, thrownError) {
	if (xhr.responseJSON) {
		showError(xhr.responseJSON.message);
	}
	else {
		showError(thrownError);
	}
}

function buildPath(resource) {
	return base_path + resource;
}

// --------------------------------------------------------------------

function reload() {
	history.go(0)
}

// --------------------------------------------------------------------

            var stompClient = null;

            var subscribedWorkflowInstanceKeys = [];
            var subscribedWorkflowKeys = [];
             
            function connect() {
                var socket = new SockJS(buildPath('notifications'));
                stompClient = Stomp.over(socket); 
                stompClient.connect({}, function(frame) {
                    stompClient.subscribe(buildPath('notifications/workflow-instance'), function(message) {
                      handleMessage(JSON.parse(message.body));
                    });
                });
            }
             
            function disconnect() {
                if(stompClient != null) {
                    stompClient.disconnect();
                }
            }
             
            function sendMessage(msg) {
                stompClient.send(buildPath('notifications'), {},
                  JSON.stringify(msg));
            }
             
            function handleMessage(notification) {

                if (subscribedWorkflowInstanceKeys.includes(notification.workflowInstanceKey)) {
                    showInfo('Workflow instance has changed.');
                }

                if (subscribedWorkflowKeys.includes(notification.workflowKey)) {
                    showInfo('Instance(s) of this workflow have changed.');
                }
            }

            function subscribeForWorkflowInstance(key) {
                subscribedWorkflowInstanceKeys.push(key);
            }

            function subscribeForWorkflow(key) {
                subscribedWorkflowKeys.push(key);
            }

// --------------------------------------------------------------------

function uploadModels() {

	var fileUpload = document.getElementById('documentToUpload');

	var filesToUpload = {
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
	    var index;
		for (index = 0; index < fileUpload.files.length; ++index) {

		    var reader = new FileReader();
		    reader.onloadend = processUploadedFile(fileUpload, index);
            reader.readAsArrayBuffer(fileUpload.files[index]);
        }
    }

	var uploadFiles = function() {
	    $.ajax({
           type : 'POST',
           url: buildPath('api/workflows/'),
           data:  JSON.stringify(filesToUpload),
           contentType: 'application/json; charset=utf-8',
           success: function (result) {
           	showSuccess("New deployment created.");
           },
           error: function (xhr, ajaxOptions, thrownError) {
          	 showErrorResonse(xhr, ajaxOptions, thrownError);
           },
        	 timeout: 5000,
           crossDomain: true,
	    });
	};
}

// --------------------------------------------------------------------

function createInstance(key) {
	$.ajax({
       type : 'POST',
       url: buildPath('api/workflows/' + key),
       data:  getVariablesDocument(),
       contentType: 'application/json; charset=utf-8',
       success: function (result) {
       	showSuccess("New instance created.");
       },
       error: function (xhr, ajaxOptions, thrownError) {
      	 showErrorResonse(xhr, ajaxOptions, thrownError);
       },
    	 timeout: 5000,
       crossDomain: true,
    });
}

// --------------------------------------------------------------------

function updateVariable(scopeKey, name) {

		var newValue = document.getElementById("variable-new-value-" + scopeKey + "-" + name).value;

		var data = '{"' + name + '":' + newValue + '}';

		$.ajax({
	       type : 'PUT',
	       url: buildPath('api/instances/' + scopeKey + '/set-variables'),
	       data:  data,
	       contentType: 'application/json; charset=utf-8',
	       success: function (result) {
	       	showSuccess("Variable updated.");
	       },
	       error: function (xhr, ajaxOptions, thrownError) {
	      	 showErrorResonse(xhr, ajaxOptions, thrownError);
	       },
	    	 timeout: 5000,
	       crossDomain: true,
	    });
}

function setVariable() {

		var scopeKeyElement = document.getElementById("variable-scopeKey");
		var scopeKey = scopeKeyElement.options[scopeKeyElement.selectedIndex].value;

		var name = document.getElementById("variable-name").value;

		var newValue = document.getElementById("variable-value").value;

		var data = '{"' + name + '":' + newValue + '}';

		var local = document.getElementById("variable-local").checked;

		var url = buildPath('api/instances/' + scopeKey + '/set-variables');

		if (local) {
			url = url + "-local";
		}

		$.ajax({
	       type : 'PUT',
	       url:  url,
	       data:  data,
	       contentType: 'application/json; charset=utf-8',
	       success: function (result) {
	       	showSuccess("Variable set.");
	       },
	       error: function (xhr, ajaxOptions, thrownError) {
	      	 showErrorResonse(xhr, ajaxOptions, thrownError);
	       },
	    	 timeout: 5000,
	       crossDomain: true,
	    });
}

// --------------------------------------------------------------------

function updateRetries(jobKey) {
		$.ajax({
	             type : 'PUT',
	             url: buildPath('api/instances/' + jobKey + '/update-retries'),
	             data:  document.getElementById("remaining-retries-" + jobKey).value,
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	showSuccess("Retries updated.");
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
}

// --------------------------------------------------------------------

function resolveJobIncident(incidentKey, jobKey) {

		var remainingRetries = document.getElementById("remaining-retries-" + incidentKey).value;

		resolveIncident(incidentKey, jobKey, remainingRetries);
}

function resolveWorkflowInstanceIncident(incidentKey) {
		resolveIncident(incidentKey, null, null);
}

function resolveIncident(incidentKey, jobKey, remainingRetries) {

		var data = {
			jobKey: jobKey,
			remainingRetries: remainingRetries
		};

		$.ajax({
	             type : 'PUT',
	             url: buildPath('api/instances/' + incidentKey + '/resolve-incident'),
	             data:  JSON.stringify(data),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	showSuccess("Incident resolved.");
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
}

// --------------------------------------------------------------------

function cancelInstance(key) {
		$.ajax({
	             type : 'DELETE',
	             url: buildPath('api/instances/' + key),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	showSuccess("Instance canceled.");
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
}

// --------------------------------------------------------------------

function completeJob(jobKey) {

		$.ajax({
	             type : 'PUT',
	             url: buildPath('api/jobs/' + jobKey + '/complete'),
	             data:  getVariablesDocumentFrom(jobKey),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	showSuccess("Job completed.");
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
}

// --------------------------------------------------------------------

function failJob(jobKey) {

		$.ajax({
	             type : 'PUT',
	             url: buildPath('api/jobs/' + jobKey + '/fail'),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	showSuccess("Job failed.");
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
}

// --------------------------------------------------------------------

function throwError(jobKey) {

	var data = {
		errorCode: document.getElementById("error-code-" + jobKey).value
	};

	$.ajax({
		type : 'PUT',
		url: buildPath('api/jobs/' + jobKey + '/throw-error'),
		data:  JSON.stringify(data),
		contentType: 'application/json; charset=utf-8',
		success: function (result) {
			showSuccess("Error thrown.");
		},
		error: function (xhr, ajaxOptions, thrownError) {
			showErrorResonse(xhr, ajaxOptions, thrownError);
		},
		timeout: 5000,
		crossDomain: true,
	});
}

// --------------------------------------------------------------------

function publishMessage() {

	var data = {
		name: document.getElementById("message-name").value,
		correlationKey: document.getElementById("message-correlation-key").value,
		payload: getVariablesDocument(),
		timeToLive: document.getElementById("message-ttl").value
	};

	publishMessageWithPayload(data);
}

function publishMessageSubscription(key) {

	var data = {
		name: document.getElementById("message-name-" + key).value,
		correlationKey: document.getElementById("message-correlation-key-" + key).value,
		payload: getVariablesDocumentFrom(key),
		timeToLive: document.getElementById("message-ttl-" + key).value
	};

	publishMessageWithPayload(data);
}

function publishMessageWithPayload(data) {

		$.ajax({
	             type : 'POST',
	             url: buildPath('api/messages/'),
	             data:  JSON.stringify(data),
	             contentType: 'application/json; charset=utf-8',
	             success: function (result) {
	             	showSuccess("Message published.");
	             },
	             error: function (xhr, ajaxOptions, thrownError) {
	            	 showErrorResonse(xhr, ajaxOptions, thrownError);
	             },
            	 timeout: 5000,
	             crossDomain: true,
	    });
}

// --------------------------------------------------------------------

function getVariablesDocumentFrom(key) {

	var formCount = 10;
	var variableCount = 0;
	var variableDocument = '{';

	var i;
	for (i = 1; i <= formCount; i++) {
		var varName = document.getElementById('variable-form-' + i + '-name_' + key).value;
		var varValue = document.getElementById('variable-form-' + i + '-value_' + key).value;

		if (varValue.length == 0) {
			varValue = null;
		}

		if (varName.length > 0) {
			if (variableCount > 0) {
				variableDocument += ',';
			}
			variableDocument += '"' + varName + '":' + varValue;
			variableCount += 1;
		}
	}

	variableDocument += '}';

	return variableDocument;
}

function getVariablesDocument() {

	var formCount = 10;
	var variableCount = 0;
	var variableDocument = '{';

	var i;
	for (i = 1; i <= formCount; i++) {
		var varName = document.getElementById('variable-form-' + i + '-name').value;
		var varValue = document.getElementById('variable-form-' + i + '-value').value;

		if (varValue.length == 0) {
			varValue = null;
		}

		if (varName.length > 0) {
			if (variableCount > 0) {
				variableDocument += ',';
			}
			variableDocument += '"' + varName + '":' + varValue;
			variableCount += 1;
		}
	}

	variableDocument += '}';

	return variableDocument;
}

// --------------------------------------------------------------------

function loadDiagram(resource) {
	viewer.importXML(resource, function(err) {
							if (err) {
								console.log('error rendering', err);
				             	showError(err);
							} else {
								var canvas = viewer.get('canvas');

								container.removeClass('with-error')
										 .addClass('with-diagram');

								// zoom to fit full viewport
								canvas.zoom('fit-viewport');
							}
						});
}

function addElementInstanceActiveMarker(canvas, elementId) {
	canvas.addMarker(elementId, 'bpmn-element-active');
}

function addElementInstanceIncidentMarker(canvas, elementId) {
	canvas.addMarker(elementId, 'bpmn-element-incident');
}

function addElementSelectedMarker(elementId) {
	canvas.addMarker(elementId, 'bpmn-element-selected');
}

function removeElementSelectedMarker(elementId) {
	canvas.removeMarker(elementId, 'bpmn-element-selected');
}

function addElementInstanceCounter(overlays, elemenId, active, ended) {

		var style = ((active > 0) ? "bpmn-badge-active" : "bpmn-badge-inactive");

		overlays.add(elemenId, {
		  position: {
		    top: -25,
  			left: 0
		  },
		  html: '<span class="' + style + '" data-toggle="tooltip" data-placement="bottom" title="active | ended">'
		  				+ active + ' | ' + ended
		  				+ '</span>'
		});
}

function addIncidentMarker(overlays, elemenId) {
		overlays.add(elemenId, {
		  position: {
		    top: -25,
  			right: 10
		  },
		  html: '<span class="bpmn-badge-incident" data-toggle="tooltip" data-placement="bottom" title="incident">'
		  				+ "⚡"
		  				+ '</span>'
		});
}

function markSequenceFlow(elementRegistry, graphicsFactory, flow) {
	var element = elementRegistry.get(flow);
	var gfx = elementRegistry.getGraphics(element);

	colorSequenceFlow(graphicsFactory, element, gfx, '#52b415');
}

function colorSequenceFlow(graphicsFactory, sequenceFlow, gfx, color) {
	var businessObject = sequenceFlow.businessObject,
		di = businessObject.di;

	di.set('stroke', color);
	di.set('fill', color);

	graphicsFactory.update('connection', sequenceFlow, gfx);
}

// --------------------------------------------------------------------
