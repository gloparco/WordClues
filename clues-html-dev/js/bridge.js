/*global GAME, document */
GAME.bridge = (function()
	{
    'use strict';
	var callbacksCount = 1,
        earlyMessages = [],
        responseHandler,
		callbacks = {};

	function init(handler)
		{
        responseHandler = handler;
        if (responseHandler !== null && responseHandler !== undefined)
            {
            for (var i = 0; i < earlyMessages.length; i++)
                {
                responseHandler(earlyMessages[i]);
                }
            earlyMessages = [];
            }
		}

	// JavaScript -> Objective-C
	function request(topic, message, next)
		{
        callNative(topic, message, next !== undefined ?
            function(responseTopic, responseMessage)
                {
//                console.log('Response on local call: ' + responseMessage);
                if (next !== undefined)
                    {
                    var response = null;
                    try
                        {
                        response = JSON.parse(responseMessage);
                        }
                    catch (e)
                        {
                        response = responseMessage;
                        //log.info('Unexpected host response format: ' + responseMessage);
                        }
                    next(null, response && response.content ? response.content : response);
                    }
                else
                    {
  //                  log.info("undefined Response callback");
                    }
                }
            : response);
		}

	// Objective-C -> JavaScript
	function response(topic, message)
		{
        if (message === null || message === undefined)
            {
            return;
            }
        if (responseHandler !== null && responseHandler !== undefined)
            {
            var parsedMessage = JSON.parse(message);
            if (topic !== null)
                {
                parsedMessage.topic = topic;
                }
            responseHandler(parsedMessage);
            }
        else
            {
            earlyMessages.push(JSON.parse(message));
            }
		}

	// Automatically called by native layer when a result is available
	function resultForCallback(callbackId, resultArray)
		{
		try
			{
			var callback = callbacks[callbackId];
			if (!callback)
                {
                return;
                }
            delete callbacks[callbackId];
			callback.apply(null, resultArray);
			}
		catch (e)
            {
            console.log(e); // don't use logError() here due to possible endless cycle
            }
		}

	// Use this in javascript to request native objective-c code
	// topic : AKA functionName
	// args : array of arguments
	// callback : function with n-arguments that is going to be called when the native code returned
	function callNative(topic, args, callback)
		{
        var hasCallback = callback && typeof callback === 'function';
        var callbackId = hasCallback ? callbacksCount++ : 0;
        if (hasCallback)
            {
            callbacks[callbackId] = callback;
            }
        if (typeof NativeBridge !== 'undefined')
            {
                try {
                    var argsString = args !== null && args !== undefined ? JSON.stringify(args) : null;
                    //console.log("About to call NPObject with topic=" + topic + ", arg=" + argsString + ", callbackId=" + callbackId);
                    NativeBridge.handleJavascript(topic, callbackId, argsString);
                    //console.log("After calling NPObject with topic=" + topic);
                } catch (e) {
                    console.log("Error calling native code: " + e);
                }
            }
        else
            {
            var iframe = document.createElement('IFRAME');

            var argsComponent = args ? ':' + encodeURIComponent(JSON.stringify(args)) : ':';
            iframe.setAttribute('src', 'wmw:' + topic + ':' + callbackId + argsComponent);
            document.documentElement.appendChild(iframe);
            iframe.parentNode.removeChild(iframe);
            iframe = null;
            }
		}

    function logError(error)
        {
        //request('error', {'message': error});
        console.log('Error: ' + error);
        }

	return {
		init: init,
//		registerBroadcaster: registerBroadcaster,
		request: request,
        response: response,
        resultForCallback: resultForCallback,
        logError: logError
		};

	})();



