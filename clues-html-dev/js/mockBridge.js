/*global document, log, console*/
GAME.bridge = (function()
	{
    "use strict";
	var callbacksCount = 1,
        responseHandler,
		callbacks = {};

	function init(handler)
		{
        responseHandler = handler;
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
        if (message != null && message !== '' && responseHandler != null)
            {
            var parsedMessage = JSON.parse(message);
            if (topic != null)
                {
                parsedMessage.topic = topic;
                }
            responseHandler(parsedMessage);
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
            alert('Error: ' + e);
            log.error(e);
            }
		}

	// Use this in javascript to request native objective-c code
	// topic : AKA functionName
	// args : array of arguments
	// callback : function with n-arguments that is going to be called when the native code returned
	function callNative(topic, args, callback)
		{
		if (!callback)
            {
            return;
            }
		callback(null, '');
		}

	return {
		init: init,
//		registerBroadcaster: registerBroadcaster,
		request: request,
        response: response,
        resultForCallback: resultForCallback
		};

	})();



