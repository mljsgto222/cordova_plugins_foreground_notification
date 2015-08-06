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
        urlPath = url;
        getURL();
    }

    private Intent serviceIntent;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        boolean result = false;
        if(action.equals(ACTION_START)){
            start(args.getJSONObject(0));
            result = true;
        }else if(action.equals(ACTION_CANCEL)){
            cancel();
            result = true;
        }else if(action.equals(ACTION_GETURL)){
            ForegroundNotification.callbackContext = callbackContext;
            getURL();
            result = true;
        }

        return result;
    }

    private void start(JSONObject jsonObject){
        cancel();
        serviceIntent = new Intent(this.cordova.getActivity(), ForegroundService.class);
        try{
            serviceIntent.putExtra("message", jsonObject.getString("message"));
            serviceIntent.putExtra("type", jsonObject.getString("type"));
            if(jsonObject.has("url")){
                serviceIntent.putExtra("url", jsonObject.getString("url"));
            }
            if(jsonObject.has("showTime")){
                serviceIntent.putExtra("showTime", jsonObject.getString("showTime"));
            }
            this.cordova.getActivity().startService(serviceIntent);
        }catch (JSONException ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    private void cancel(){
        if(serviceIntent != null){
            this.cordova.getActivity().stopService(serviceIntent);
            serviceIntent = null;
        }
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
