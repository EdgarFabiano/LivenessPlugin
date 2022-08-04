
import NECFaceLivenessCore
import NECFaceLivenessCharlie
import NecLiveness
import AVKit

@objc(LivenessPlugin) class LivenessPlugin : CDVPlugin, NecFlowDelegate {
    private var callbackId: String!
    private var flow: NecFlow!

    func flowDidFinishWith(data: NecFlowData?) {
        guard let data = data else {
            return
        }
        
        // Se pueden agregar mas campos
        let dataDictionary: [String : Any?] = [
            "livenessScore": data.livenessScore,
            "processingTime": data.processingTime,
            "captureTime": data.captureTime,
            "quality": data.quality,
            "eyesClosed": data.eyesClosed,
        ]
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: dataDictionary)
        commandDelegate?.send(pluginResult, callbackId: callbackId)
        callbackId = nil
    }
    
    func flowDidFinishWith(error: NecFaceLivenessError, data: NecFlowData?) {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: String(describing: error))
        commandDelegate?.send(pluginResult, callbackId: callbackId)
        callbackId = nil
    }
    
    func flowDidCancel() {
        let error = NecFaceLivenessError.activityPaused
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: String(describing: error))
        commandDelegate?.send(pluginResult, callbackId: callbackId)
        callbackId = nil
    }
        
    @objc(capture:)
    func capture(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        flow.capture(from: viewController, for: "12345")
    }

    @objc(initialize:)
    func initialize(_ command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId

        if AVCaptureDevice.authorizationStatus(for: .video) != .authorized {
            AVCaptureDevice.requestAccess(for: .video) {
                if $0 { self.initialize(command) }
                else {
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
                    self.commandDelegate?.send(pluginResult, callbackId: self.callbackId)
                    self.callbackId = nil
                }
            }
        }

        flow = NecFlow()
        flow.group = "Cordova-iOS"
        flow.delegate = self
        flow.endpoint = "https://hom.liveness.serpro.gov.br:7044"
        flow.tokenPath = "https://hom.liveness.serpro.gov.br:7080/auth/realms/serpro_qa/protocol/openid-connect/token"
        flow.setClientId("mobile-app", secret: "e5263862-639c-4a12-91ca-be26af8e1c0e")
        
        let license = Bundle.main.path(forResource: "1999_ar.com.nec.face.liveness.offline.demo", ofType: "lic")
        flow.initialize(withLicense: license) { flow in
            DispatchQueue.main.async {
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
                self.commandDelegate?.send(pluginResult, callbackId: self.callbackId)
                self.callbackId = nil
            }
        }
    }

    @objc(setThreshold:)
    func setThreshold(_ command: CDVInvokedUrlCommand) {
        let s: String = command.argument(at: 0) as! String
        flow.threshold = NecFaceLivenessThreshold(rawValue: s) ?? .balancedVeryHigh

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        commandDelegate?.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(setCamera:)
    func setCamera(_ command: CDVInvokedUrlCommand) {
        let s: String = command.argument(at: 0) as! String
        flow.assistedMode = s == "front" 

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        commandDelegate?.send(pluginResult, callbackId: command.callbackId)
    }
}
