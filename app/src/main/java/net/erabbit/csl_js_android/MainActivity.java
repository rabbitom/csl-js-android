package net.erabbit.csl_js_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;
import org.liquidplayer.webkit.javascriptcore.JSBaseArray;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSON;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
        jsContext.evaluateScript("var a = csl.encode(null, 'battery-get');");
        JSValue jsValue = jsContext.property("a");
        JSBaseArray jsArray = jsValue.toJSArray();
        Object[] array = jsArray.toArray();
        Log.d("csl", jsValue.toJSON());
        ((TextView)v).setText(jsValue.toJSON());
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
