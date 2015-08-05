var exec = require('cordova/exec'),
    channel = require('cordova/channel'),
    cordova = require('cordova');

var noop = function(){};  
function ForegroundNotification(){
    
};

ForegroundNotification.prototype.getURL = function(successCallback, errorCallback){
    successCallback = successCallback || noop;
    errorCallback = errorCallback || noop;
    exec(successCallback, errorCallback, 'ForegroundNotification', 'getURL', []);  
};

ForegroundNotification.prototype.start = function(options, successCallback, errorCallback){
    successCallback = successCallback || noop;
    errorCallback = errorCallback || noop;
    exec(successCallback, errorCallback, 'ForegroundNotification', 'start', [options]);
};

ForegroundNotification.prototype.cancel = function(successCallback, errorCallback){
    successCallback = successCallback || noop;
    errorCallback = errorCallback || noop;
    exec(successCallback, errorCallback, 'ForegroundNotification', 'cancel', []);
};

var foregroundNotification = new ForegroundNotification();

channel.onCordovaReady.subscribe(function(){
    foregroundNotification.getURL(function(data){
        if(data && data !== 'OK'){
            var json = JSON.parse(data);
            if(json){
                cordova.fireDocumentEvent('appOpenWithURL', json);
            }
        }
    }, function(error){
        console.log('get url data error');
    });
});

module.exports = foregroundNotification;