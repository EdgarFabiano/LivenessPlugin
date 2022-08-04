var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'LivenessPlugin', 'coolMethod', [arg0]);
};

exports.capture = function (success, error) {
    console.log('capturing');
    exec(success, error, 'LivenessPlugin', 'capture');
};

exports.initialize = function () {
    exec(
        () => {},
        () => console.log('init error'),
        "LivenessPlugin",
        "initialize",
        []
      );
};

exports.setThreshold = function (thresholdOption) {
    if(thresholdOption === null || thresholdOption === undefined) {
        thresholdOption = "balanced_very_high";
    }
    exec(
        () => {},
        () => {},
        "LivenessPlugin",
        "setThreshold",
        [thresholdOption]
      );
};

exports.setCamera = function (cameraOption) {
    if(cameraOption === null || cameraOption === undefined) {
        cameraOption = "front";
    }
    exec(
        () => {},
        () => {},
        "LivenessPlugin",
        "setCamera",
        [cameraOption]
      );
};