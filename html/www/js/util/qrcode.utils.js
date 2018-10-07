/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2018 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of this software, including this file, may be copied, modified,    *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

var NRS = (function (NRS) {

    NRS.scanQRCode = function(readerId, callback) {
        if (!NRS.isScanningAllowed()) {
            $.growl($.t("scanning_not_allowed"));
            return;
        }
        if (NRS.isCordovaScanningEnabled()) {
            if (NRS.isCameraPermissionRequired()) {
                NRS.logConsole("request camera permission");
                cordova.plugins.permissions.hasPermission(cordova.plugins.permissions.CAMERA, function(status) {
                    cordovaCheckCameraPermission(status, callback)
                }, null);
            } else {
                NRS.logConsole("scan without requesting camera permission");
                cordovaScan(callback);
            }
        } else {
            NRS.logConsole("scan using desktop browser");
            html5Scan(readerId, callback);
        }
    };

    function cordovaCheckCameraPermission(status, callback) {
        if(!status.hasPermission) {
            var errorCallback = function() {
                NRS.logConsole('Camera permission not granted');
            };

            NRS.logConsole('Request camera permission');
            cordova.plugins.permissions.requestPermission(cordova.plugins.permissions.CAMERA, function(status) {
                if(!status.hasPermission) {
                    NRS.logConsole('Camera status has no permission');
                    errorCallback();
                    return;
                }
                cordovaScan(callback);
            }, errorCallback);
            return;
        }
        NRS.logConsole('Camera already has permission');
        cordovaScan(callback);
    }

    function cordovaScan(callback) {
        try {
            NRS.logConsole("before scan");
            cordova.plugins.barcodeScanner.scan(function(result) {
                cordovaScanQRDone(result, callback)
            }, function (error) {
                NRS.logConsole(error);
            });
        } catch (e) {
            NRS.logConsole(e.message);
        }
    }

    function cordovaScanQRDone(result, callback) {
        NRS.logConsole("Scan result format: " + result.format);
        if (!result.cancelled && result.format == "QR_CODE") {
            NRS.logConsole("Scan complete, send result to callback");
            callback(result.text);
        } else {
            NRS.logConsole("Scan cancelled");
        }
    }

    function html5Scan(readerId, callback) {
        var reader = $("#" + readerId);
        if (reader.is(':visible')) {
            reader.fadeOut();
            NRS.stopScanQRCode();
            return;
        }
        reader.empty();
        reader.fadeIn();
        html5_qrcode(reader,
            function (data) {
                NRS.logConsole(data);
                callback(data);
                reader.hide();
                NRS.stopScanQRCode();
            },
            function (error) {
                NRS.logConsole(error.message);
                reader.hide();
                $.growl($.t("video_error"));
                NRS.stopScanQRCode();
            }
        );
    }

    var scanner;

    function html5_qrcode(currentElem, qrcodeSuccess, qrcodeError) {
        var vidElem = $('<video></video>').appendTo(currentElem);
        var video = vidElem[0];
        scanner = new Instascan.Scanner({ video: video });

        scanner.addListener('scan', function (content) {
            qrcodeSuccess(content);
            scanner.stop();
        });

        Instascan.Camera.getCameras().then(function (cameras) {
            if (cameras.length > 0) {
                scanner.start(cameras[0]);
            } else {
                qrcodeError(e);
                NRS.stopScanQRCode();
            }
        }).catch(function(e) {
            qrcodeError(e);
            NRS.stopScanQRCode();
        });
    }

    NRS.stopScanQRCode = function() {
        if (scanner) {
            scanner.stop();
        }
    };

    return NRS;
}(NRS || {}));
