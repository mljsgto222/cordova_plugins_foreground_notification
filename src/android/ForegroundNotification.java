package com.mobishift.plugins.foregroundnotification;

import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class ForegroundNotification extends CordovaPlugin {
    private static final String TAG = "ForegroundNotification";
    private static final String ACTION_START = "start";
    private static final String ACTION_CANCEL = "cancel";
    private static final String ACTION_GETURL = "getURL";

    private static String urlPath = null;
    private static CallbackContext callbackContext = null;
    public static void setUrlPath(String url){

    }

    private Intent serviceIntent;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        boolean result = false;
        if(action.equals(ACTION_START)){

        }else if(action.equals(ACTION_CANCEL)){

        }else if(action.equals(ACTION_GETURL)){
            ForegroundNotification.callbackContext = callbackContext;
        }

        return result;
    }

    private void start(){

    }

    private void cancel(){

    }

    private static void getURL(){
        if(callbackContext != null && urlPath != null){
            JSONObject jsonObject = new JSONObject();
            try{
                jsonObject.put("urlPath", urlPath);
            }catch (JSONException ex){
                Log.e(TAG, ex.getMessage());
            }
            urlPath = null;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonObject);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);

        }
    }
}
