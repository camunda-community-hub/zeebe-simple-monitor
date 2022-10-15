const IMAGE_ARROW_DOWN = '<svg class="bi" width="12" height="12" fill="white"><use xlink:href="/img/bootstrap-icons.svg#caret-down-fill"/></svg>';
const IMAGE_ARROW_UP = '<svg class="bi" width="12" height="12" fill="white"><use xlink:href="/img/bootstrap-icons.svg#caret-up-fill"/></svg>';
const IMAGE_LIGHTNING = '<svg class="bi" width="12" height="12" fill="white"><use xlink:href="/img/bootstrap-icons.svg#lightning-fill"/></svg>';

/**
 * @typedef ErrorMessage
 * @type {object}
 * @property {string} message
 */

/**
 * @typedef ProcessInstanceNotification
 * @type {object}
 * @property {number} processInstanceKey
 * @property {number} processDefinitionKey
 * @property {string} type
 */

/**
 * @typedef ZeebeClusterNotification
 * @type {object}
 * @property {string} message
 * @property {string} type
 */

/**
 * @param {string} elementId - the HTML element ID to attach the text to
 * @param {string} title - the title for the message
 * @param {string} message - the actual text message
 */
function appendAndSowMessageToElement(elementId, title, message) {
    var dataTexts = jQuery("#" + elementId + " [data-text]");
    var length = dataTexts.length;
    // skip duplicates
    if (length > 0) {
        if (jQuery(dataTexts[length - 1]).text().endsWith(message)) {
            return;
        }
    }
    // drop the top of the stack == the oldest entries
    for (var i = 0; i < Math.max(0, length - 3); i++) {
        jQuery(dataTexts[i]).fadeOut(dataTexts[i].remove);
    }
    var newTextElement = jQuery("<div/>");
    newTextElement.hide();
    newTextElement.attr("data-text", "");
    newTextElement.append(jQuery("<strong>" + title + "</strong>"));
    var textSpanElement = jQuery("<span/>");
    textSpanElement.text(message);
    newTextElement.append(textSpanElement);
    var panelElement = jQuery("#" + elementId);
    if (length === 0) {
        panelElement.prepend(newTextElement);
    } else {
        jQuery(dataTexts[dataTexts.length - 1]).after(newTextElement);
    }
    panelElement.show();
    newTextElement.fadeIn();
}

/**
 * @param {string} message - the actual text message
 */
function showError(message) {
    appendAndSowMessageToElement("errorPanel", "Error: ", message);
}

/**
 * @param {string} message - the actual text message
 */
function showSuccess(message) {
    appendAndSowMessageToElement("successPanel", "Success: ", message);
}

/**
 * @param {string} message - the actual text message
 */
function showInfo(message) {
    appendAndSowMessageToElement("infoPanel", "Info: ", message);
}

function showErrorResonse(xhr, ajaxOptions, thrownError) {
    if (xhr.responseJSON) {
        /** @type {ErrorMessage} */
        let errorMessage = xhr.responseJSON;
        showError(errorMessage.message);
    } else {
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

var subscribedProcessInstanceKeys = [];
var subscribedProcessDefinitionKeys = [];

function connect() {
    var socket = new SockJS(buildPath('notifications'));
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        stompClient.subscribe(buildPath('notifications/process-instance'), function (message) {
            handleProcessInstanceNotification(JSON.parse(message.body));
        });
        stompClient.subscribe(buildPath('notifications/zeebe-cluster'), function (message) {
            handleZeebeClusterNotification(JSON.parse(message.body));
        });
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
}

function sendMessage(msg) {
    stompClient.send(buildPath('notifications'), {},
        JSON.stringify(msg));
}

/**
 * @param notification {ProcessInstanceNotification}
 */
function handleProcessInstanceNotification(notification) {

    if (subscribedProcessInstanceKeys.includes(notification.processInstanceKey)) {
        showInfo('Process instance has changed.');
    }

    if (subscribedProcessDefinitionKeys.includes(notification.processDefinitionKey)) {
        showInfo('Instance(s) of this process have changed.');
    }
}

/**
 * @param notification {ZeebeClusterNotification}
 */
function handleZeebeClusterNotification(notification) {
    showError(notification.message)
}

function subscribeForProcessInstance(key) {
    subscribedProcessInstanceKeys.push(key);
}

function subscribeForProcessDefinition(key) {
    subscribedProcessDefinitionKeys.push(key);
}

// --------------------------------------------------------------------

function uploadModels() {

    var fileUpload = document.getElementById('documentToUpload');

    var filesToUpload = {
        files: []
    }

    var processUploadedFile = function (fileUpload, index) {
        return function (e) {
            var binary = '';
            var bytes = new Uint8Array(e.target.result);
            var len = bytes.byteLength;
            for (var j = 0; j < len; j++) {
                binary += String.fromCharCode(bytes[j]);
            }

            var currentFile = {
                filename: fileUpload.files[index].name,
                mimeType: fileUpload.files[index].type,
                content: btoa(binary)
            }

            filesToUpload.files.push(currentFile);

            // if all files are processed - do the upload
            if (filesToUpload.files.length == fileUpload.files.length) {
                uploadFiles();
            }
        };
    }

    // read all selected files
    if (typeof FileReader === 'function' && fileUpload.files.length > 0) {
        var index;
        for (index = 0; index < fileUpload.files.length; ++index) {

            var reader = new FileReader();
            reader.onloadend = processUploadedFile(fileUpload, index);
            reader.readAsArrayBuffer(fileUpload.files[index]);
        }
    }

    var uploadFiles = function () {
        $.ajax({
            type: 'POST',
            url: buildPath('api/processes/'),
            data: JSON.stringify(filesToUpload),
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
        type: 'POST',
        url: buildPath('api/processes/' + key),
        data: getVariablesDocument(),
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
        type: 'PUT',
        url: buildPath('api/instances/' + scopeKey + '/set-variables'),
        data: data,
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
        type: 'PUT',
        url: url,
        data: data,
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
        type: 'PUT',
        url: buildPath('api/instances/' + jobKey + '/update-retries'),
        data: document.getElementById("remaining-retries-" + jobKey).value,
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

function resolveProcessInstanceIncident(incidentKey) {
    resolveIncident(incidentKey, null, null);
}

function resolveIncident(incidentKey, jobKey, remainingRetries) {

    var data = {
        jobKey: jobKey,
        remainingRetries: remainingRetries
    };

    $.ajax({
        type: 'PUT',
        url: buildPath('api/instances/' + incidentKey + '/resolve-incident'),
        data: JSON.stringify(data),
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
        type: 'DELETE',
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
        type: 'PUT',
        url: buildPath('api/jobs/' + jobKey + '/complete'),
        data: getVariablesDocumentFrom(jobKey),
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
        type: 'PUT',
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
        type: 'PUT',
        url: buildPath('api/jobs/' + jobKey + '/throw-error'),
        data: JSON.stringify(data),
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
        type: 'POST',
        url: buildPath('api/messages/'),
        data: JSON.stringify(data),
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
    viewer.importXML(resource, function (err) {
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
            + IMAGE_LIGHTNING
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

/**
 *
 * @param pageElement mandatory, the element's ID
 * @param page mandatory, the page number
 * @param size optional, the size value to be added to the URL
 */
function listPage(pageElement, page, size) {
    let pageParam = "page=" + page;
    let search = window.location.search;

    if (typeof size === "number" && size > 0) {
        if (search.includes("size")) {
            pageParam = pageParam.replace(/size=\d+/, ("size=" + size));
        } else {
            pageParam = pageParam + "&size=" + size;
        }
    }

    if(search.includes("page")) {
        let withNewPage = search.replace(/page=\d+/, pageParam);
        document.getElementById(pageElement).href = withNewPage;
    } else if(search) {
        document.getElementById(pageElement).href = search + "&" + pageParam;
    } else {
        document.getElementById(pageElement).href = "?" + pageParam;
    }
}

function listSort(sortProperty, elementId) {
    let sortAsc = "sort=" + sortProperty + ",asc";
    let sortDesc = "sort=" + sortProperty + ",desc";
    let elementNode = document.getElementById(elementId);
    let search = window.location.search
    let sortParamRegEx = /sort=\w+,\w+/;

    if(search.includes(sortAsc)) {
        let withReverseOrder = search.replace(sortAsc, sortDesc);
        elementNode.href = withReverseOrder;
        elementNode.innerHTML = IMAGE_ARROW_DOWN;
    } else if(search.includes(sortDesc)) {
        let withReverseOrder = search.replace(sortDesc, sortAsc);
        elementNode.href = withReverseOrder;
        elementNode.innerHTML = IMAGE_ARROW_UP;
    } else if(search) {
        if (sortParamRegEx.test(window.location.search)) {
            // sort param exists already and will be replaced
            elementNode.href = search.replace(sortParamRegEx, sortDesc)
        } else {
            // no sort param exists and will be appended
            elementNode.href = search + "&" + sortDesc;
        }
        elementNode.innerHTML = IMAGE_ARROW_DOWN;
    } else {
        elementNode.href = "?" + sortDesc;
        elementNode.innerHTML = IMAGE_ARROW_UP;
    }
}

// --------------------------------------------------------------------

function openSaveFileDialog (data, filename, mimetype) {
    'use strict';

    if (!data) return;

    var blob = data.constructor !== Blob
        ? new Blob([data], {type: mimetype || 'application/octet-stream'})
        : data ;

    if (navigator.msSaveBlob) {
        navigator.msSaveBlob(blob, filename);
        return;
    }

    var lnk = document.createElement('a'),
        url = window.URL,
        objectURL;

    if (mimetype) {
        lnk.type = mimetype;
    }

    lnk.download = filename || 'untitled';
    lnk.href = objectURL = url.createObjectURL(blob);
    lnk.dispatchEvent(new MouseEvent('click'));
    setTimeout(url.revokeObjectURL.bind(url, objectURL));

}

function onBpmnDownloadClicked(){
    'use strict';
    var filename = $('#bpmnDownloadLink').attr('download');
    openSaveFileDialog(RAW_BPMN_RESOURCE, filename, 'application/bpmn+xml');
}

(function checkIfBpmnExistsOrDisableDownloadButton() {
    'use strict';
    if (typeof RAW_BPMN_RESOURCE !== 'undefined' && RAW_BPMN_RESOURCE === 'WARNING-NO-XML-RESOURCE-FOUND') {
        var link = $('#bpmnDownloadLink');
        link.attr('title', 'BPMN definition not found or broken :-( ');
        link.removeAttr('href');
        link.removeAttr('onclick');
        link.css('color', 'grey');
        link.css('cursor', 'not-allowed');
    }
})();

// --------------------------------------------------------------------
