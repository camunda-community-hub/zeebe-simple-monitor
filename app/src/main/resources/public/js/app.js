
function showError(message) {
	document.getElementById("errorText").innerHTML = message;
	$('#errorPanel').show();
}

function showSuccess(message) {
	document.getElementById("successText").innerHTML = message;
	$('#successPanel').show();
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

function reload() {
	history.go(0)
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
		for (index = 0; index < fileUpload.files.length; ++index) {	  

		    var reader = new FileReader();
		    reader.onloadend = processUploadedFile(fileUpload, index);
            reader.readAsArrayBuffer(fileUpload.files[index]);
        }
    }
	    
	var uploadFiles = function() {
	    $.ajax({
           type : 'POST',
           url: '/api/workflows/',
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
       url: '/api/workflows/' + key,
       data:  document.getElementById("payload").value,
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

function updatePayload(key) {
		$.ajax({
	       type : 'PUT',
	       url:  '/api/instances/' + key + "/update-payload",
	       data:  document.getElementById("new-payload").value,
	       contentType: 'application/json; charset=utf-8',
	       success: function (result) {
	       	showSuccess("Payload updated.");	
	       },
	       error: function (xhr, ajaxOptions, thrownError) {
	      	 showErrorResonse(xhr, ajaxOptions, thrownError);
	       },
	    	 timeout: 5000,
	       crossDomain: true,
	    });
}

// --------------------------------------------------------------------

function updateRetries(key) {
		$.ajax({
	             type : 'PUT',
	             url: '/api/instances/' + key + "/update-retries",
	             data:  document.getElementById("remaining-retries").value,
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

function resolveJobIncident(incidentKey, elementInstanceKey, jobKey) {

		var remainingRetries = document.getElementById("remaining-retries-" + incidentKey).value;
	
		resolveIncident(incidentKey, elementInstanceKey, jobKey, remainingRetries);
}

function resolveWorkflowInstanceIncident(incidentKey, elementInstanceKey) {
		resolveIncident(incidentKey, elementInstanceKey, null, null);
}

function resolveIncident(incidentKey, elementInstanceKey, jobKey, remainingRetries) {

		var data = {
			elementInstanceKey: elementInstanceKey,
			payload: document.getElementById("new-payload-" + incidentKey).value,
			jobKey: jobKey,
			remainingRetries: remainingRetries
		};
		
		$.ajax({
	             type : 'PUT',
	             url: '/api/instances/' + incidentKey + "/resolve-incident",
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
	             url: '/api/instances/' + key,
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

function loadDiagram(resource) {
	viewer.importXML(resource, function(err) {
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

function addElementInstanceActiveMarker(canvas, elementId) {	
	canvas.addMarker(elementId, 'bpmn-element-active');
}

function addElementInstanceIncidentMarker(canvas, elementId) {	
	canvas.addMarker(elementId, 'bpmn-element-incident');
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
