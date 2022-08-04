var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'LivenessPlugin', 'coolMethod', [arg0]);
};

exports.capture = function (success, error) {
    console.log('capturing');
    exec(success, error, 'LivenessPlugin', 'capture');
};