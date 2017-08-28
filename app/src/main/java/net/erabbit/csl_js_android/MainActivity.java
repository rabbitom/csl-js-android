package net.erabbit.csl_js_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.webkit.javascriptcore.JSBaseArray;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSON;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    JSContext jsContext = new JSContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String script = getStrFromAssets("csl.js");
        jsContext.evaluateScript(script);
        String pattern = getStrFromAssets("csl-message.json");
        pattern = pattern.replaceAll(" ", "");
        pattern = pattern.replaceAll("\n", "");
        jsContext.evaluateScript("var pattern = JSON.parse('" + pattern + "');");
        jsContext.evaluateScript("var csl = new CSLMessage(pattern);");
    }

    public void onClick(View v) {
        //battery-get
        encode(null, "battery-get");
        //battery-data
        JSONObject data = new JSONObject();
        try {
            data.put("battery-level", 0);
        }
        catch(JSONException e) {
            e.printStackTrace();
            return;
        }
        Byte[] array = encode(data.toString(), "battery-data");
        JSONObject object = decode(array, "battery-data");
        int batteryLevel = -1;
        try {
            batteryLevel = object.getInt("battery-level");
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
        if(batteryLevel >= 0)
            ((TextView)v).setText(String.format(Locale.getDefault(), "batteryLevel: %d", batteryLevel));
    }

    private Byte[] encode(String object, String fieldId) {
        jsContext.evaluateScript("var array = csl.encode(" + object + ", '" + fieldId + "');");
        JSValue jsValue = jsContext.property("array");
        JSBaseArray jsArray = jsValue.toJSArray();
        Byte[] array = (Byte[])jsArray.toArray();
        Log.d("csl", "object = " + object + "; fieldId = " + fieldId + "; array = " + hexString(array));
        return array;
    }

    private JSONObject decode(Byte[] array, String fieldId) {
        StringBuilder builder = new StringBuilder();
        for(Byte b : array) {
            builder.append(b & 0xff);
            builder.append(",");
        }
        builder.insert(0, "[");
        builder.replace(builder.length()-1, builder.length(), "]");
        String arrayString = builder.toString();
        jsContext.evaluateScript("var a = csl.decode(" + arrayString + ", undefined, undefined, '" + fieldId + "');");
        JSValue jsValue = jsContext.property("a");
        String json = jsValue.toJSON();
        Log.d("csl", "array = " + hexString(array) + "; fieldId = " + fieldId + "; object = " + json);
        JSONObject object = null;
        try {
            object = new JSONObject(json);
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private String hexString(Byte[] array) {
        StringBuilder hex = new StringBuilder();
        for(Byte b : array)
            hex.append(String.format("%02X ", b & 0xff));
        return hex.toString();
    }

    public String getStrFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
