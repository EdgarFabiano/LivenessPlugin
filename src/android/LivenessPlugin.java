package ar.com.nec.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import ar.com.nec.liveness.NecFlow;
import ar.com.nec.liveness.NecFlowData;
import ar.com.nec.liveness.NecFlowHandler;
import ar.com.nec.liveness.face.charlie.model.NecCaptureMode;
import ar.com.nec.liveness.face.core.model.NecFaceLivenessError;
import ar.com.nec.liveness.face.core.model.NecFaceLivenessThreshold;
import org.json.JSONArray;
import org.json.JSONObject; 
import org.json.JSONException; 
import java.lang.reflect.*;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;

public class LivenessPlugin extends CordovaPlugin implements NecFlowHandler {
    private static final int CAMERA_CODE = 1;
    private NecFlow flow = new NecFlow();
    private CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;
        if (action.equals("capture")) {
            setAsync(callbackContext);
            capture();
            return true;
        } else if (action.equals("initialize")) {
            setAsync(callbackContext);
            initialize();
            return true;
        } else if (action.equals("setThreshold")) {
            String param = (String) args.get(0);
            setThreshold(param);
            return true;
        } else if (action.equals("setCamera")) {
            String param = (String) args.get(0);
            setCamera(param);
            return true;
        }

        return false;
    }

    private void setAsync(CallbackContext callback) {
        PluginResult pluginresult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginresult.setKeepCallback(true);
        callback.sendPluginResult(pluginresult);
    }

    private void capture() {
        try {
            flow.capture(cordova.getActivity());
        } catch (Exception e) {}
    }

    private void initialize() {
        if (!cordova.hasPermission(Manifest.permission.CAMERA)) {
            cordova.requestPermission(this, CAMERA_CODE, Manifest.permission.CAMERA);
            return;
        }

        flow.setHandler(this);
        flow.setEndpoint("https://onboarding-demo.neclatam-cloud.com:9084");
        flow.setTokenPath("https://onboarding-demo.neclatam-cloud.com:8070/auth/realms/obiqid-devel/protocol/openid-connect/token");
        flow.setClientId("mobile-app", "e5263862-639c-4a12-91ca-be26af8e1c0e");
        flow.setGroup("Cordova-Android");
        flow.initialize(cordova.getActivity(), "1377br.gov.meugovbr.hom.lic", (flow) -> {
            cordova.getActivity().runOnUiThread(() -> {
                callback.success();
                callback = null;
            });
            return null;
        });
    }

    private void setThreshold(String param) {
        try {
            NecFaceLivenessThreshold threshold = NecFaceLivenessThreshold.valueOf(param.toUpperCase());
            flow.setThreshold(threshold);
        } catch (Exception e) {
            flow.setThreshold(NecFaceLivenessThreshold.BALANCED_VERY_HIGH);
        } finally {
            callback.success();
            callback = null;
        }
    }

    private void setCamera(String param) {
        try {
            NecCaptureMode threshold = NecCaptureMode.valueOf(param.toUpperCase());
            flow.setCaptureMode(threshold);
        } catch (Exception e) {
            flow.setCaptureMode(NecCaptureMode.FRONT);
        } finally {
            callback.success();
            callback = null;
        }
    }

    @Override
    public void onError(NecFaceLivenessError error, NecFlowData data) {
        if (callback == null) return;

        JSONObject dataMap = null;
        if (data != null) dataMap = toMap(data);

        cordova.getActivity().runOnUiThread(() -> {
            callback.error(String.valueOf(error));
            callback = null;
        });
    }

    @Override
    public void onResponse(NecFlowData data) {
        if (callback == null) return;

        cordova.getActivity().runOnUiThread(() -> {
            JSONObject dataMap = null;
            if (data != null) dataMap = toMap(data);

            callback.success(dataMap);
            callback = null;
        });
    }

    private JSONObject toMap(NecFlowData data) {
        JSONObject map = new JSONObject();
        Field fields[] = NecFlowData.class.getDeclaredFields();

        for (Field field : fields) {
            try {
                Log.println(Log.ASSERT, "TAG", field.getName());
                field.setAccessible(true);

                map.put(field.getName(), field.get(data));
            } catch (JSONException | IllegalAccessException e) {
                Log.println(Log.ASSERT, "TAG", e.getMessage());
            }
        }

        return map;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int r : grantResults) {
            if(r == PackageManager.PERMISSION_DENIED) {
                callback.error("permission_denied");
                return;
            }
        }

        initialize();
    }
}