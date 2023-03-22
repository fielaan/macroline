package com.fielaan.macroline;

import android.accessibilityservice.AccessibilityService;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fielaan.macroline.adapter.MacrosAdapter;
import com.fielaan.macroline.model.Macros;

import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MacrolineAccessibilityService extends AccessibilityService {

    int height = 0;
    int width = 0;
    String lastText = "";
    ConstraintLayout rootView;
    RecyclerView macrosRecycler;
    MacrosAdapter macrosAdapter;
    TextView pathText;
    TextView argumentTitle;
    EditText customArgumentInput;

    String currentPath = "";

    String textToReplace = ""; //Сюда указывается текст, который будет заменён при вызове setText()

    String activeModule;

    WindowManager windowManager;
    AccessibilityNodeInfo source;

    Globals currentLuaFile;

    Map<String,String> macroses = new HashMap<String,String>();
    SharedPreferences sharedPreferences;
    AccessibilityNodeInfo accessibilityNodeInfo;


    String windowId = "";
    List<AccessibilityNodeInfo> nodeList;

    final String path = "/data/data/com.fielaan.macroline/plugins/";
    final int[] screens = {R.id.macros_select_window, R.id.enterArgumentWindow};

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if(sharedPreferences == null) sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        accessibilityNodeInfo = accessibilityEvent.getSource();

        if(accessibilityNodeInfo == null)
            return;

        if(accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED){
            String text = accessibilityNodeInfo.getText() == null ? "" : accessibilityNodeInfo.getText().toString();

            if( text.endsWith(sharedPreferences.getString("activation_text", "ml")) ||
                    (!sharedPreferences.getBoolean("consider_activation_text_register", false) &&
                            text.toLowerCase().endsWith(sharedPreferences.getString("activation_text", "ml").toLowerCase())))
            {


                source = accessibilityEvent.getSource();

                if (source != null) {
                    //source.refresh(); // to fix issue with viewIdResName = null on Android 6+
                }

                source.refresh();
                windowId = source.getViewIdResourceName();

                String packageName = accessibilityNodeInfo.getPackageName().toString();


                //nodeList = source.findAccessibilityNodeInfosByViewId(windowId);


                //if(lastText.contains(text) && lastText.length() > text.length())
                   //return;

                //if(width == 0 || height == 0) //Вызывается только один раз
                if(true)
                {

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager == null) {
                        Log.e("ERROR", "windowManager is NULL");
                        return;
                    }

                    windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                    height = displayMetrics.heightPixels;
                    width = displayMetrics.widthPixels;

                }

                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        width - 100, // Ширина экрана
                        height - 100, // Высота экрана
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        //WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                        PixelFormat.TRANSLUCENT); // Само окно прозрачное

                // Задаем позиции для нашего Layout
                params.gravity = Gravity.TOP | Gravity.RIGHT;
                params.x = 50;
                params.y = 50;


            MacrolineAccessibilityService t = this;


            // Отображаем наш Layout

            rootView = (ConstraintLayout) LayoutInflater.from(t).inflate(R.layout.overlay_layout, null);
            pathText = rootView.findViewById(R.id.pathText);
            //accessibilityNodeInfo.performAction(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
            windowManager.addView(rootView, params);

            Button backButt = rootView.findViewById(R.id.backButton);
            backButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Back", "Back");
                    try {
                        windowManager.removeView(rootView);
                    }catch (Exception e){}
                }
            });


            showMacroses();



            }
            lastText = text;
        }else{
            //WTF??
        }

    }

    private void setMacrosRecycler(List<Macros> macrosList) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        macrosRecycler = rootView.findViewById(R.id.macrosRecycler);
        macrosRecycler.setLayoutManager(layoutManager);
        macrosRecycler.setBackgroundColor(Color.parseColor("#ffffff"));

        macrosAdapter = new MacrosAdapter(rootView.getContext(), macrosList);
        macrosRecycler.setAdapter(macrosAdapter);
        macrosRecycler.setBackgroundColor(Color.parseColor("#ffffff"));


    }


    public void OnClick(String macros, Macros.macrosType type) {



        showScreen(0);

        if (type == Macros.macrosType.FOLDER) {
            currentPath += "/" + macros;
            showMacroses();
        } else if(type == Macros.macrosType.MACROS)
        {
            LuaValue[] args = {CoerceJavaToLua.coerce(new Query(accessibilityNodeInfo, this))};

            currentLuaFile = runLuaFile(macroses.get(macros));
            if(currentLuaFile == null) return;

            LuaValue resp = runLuaFunc(currentLuaFile,  "onInvoked", args);


            activeModule = macroses.get(macros); //Путь к макросу (/data/data/fielaan.ml.../plugins/...)

        }else if(type == Macros.macrosType.BACK_BUTTON){
            currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
            showMacroses();
        }else{
            Log.e("ERROR" , "ERR: unknown type of macros!");
        }

    }

    void showMacroses(){
        showScreen(0);
        Log.d("PATH", currentPath);
        macroses.clear();
        pathText.setText((currentPath.length() > 0? "" : "/") + currentPath);

        File dir = new File(this.path + currentPath);
        File[] files = dir.listFiles();


        List<Macros> macrosList = new ArrayList<>();

        if(currentPath.length() > 0) macrosList.add(new Macros(0, "Назад", Macros.macrosType.BACK_BUTTON, this));

        if(files == null || files.length == 0) {//Макросы не найдены
            Log.d("", "MACROSES NOT FOUND");
            rootView.findViewById(R.id.macroses_not_found).setVisibility(View.VISIBLE);
        }else{
            rootView.findViewById(R.id.macroses_not_found).setVisibility(View.GONE);
            for (int i = 0; i < files.length; i++) {
                String title = "";
                if(!files[i].isDirectory()) {


                    //LuaValue resp = runLuaFunc(runLuaFile(files[i].toString()), "getInfo", null);

                    //title = resp.checktable().get("title").toString();

                    String myData = "";

                    try {


                        FileInputStream fis = new FileInputStream(files[i].toString());
                        DataInputStream in = new DataInputStream(fis);
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(in));
                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            myData = myData + strLine;
                        }
                        in.close();

                        Log.d("IN", "IMPORT: " + myData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    int index;
                    try{
                        index = myData.indexOf("--MODULE_TITLE{");
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        return;
                    }

                    if(index == -1)
                    {
                        Log.e("MAS ERR", "MODULE_TITLE not found!");
                        return;
                    }
                    index += 1;
                    title = myData.substring(index  + 14, myData.indexOf("}", index));

                    Log.d("RECIVED:", title);

                    macroses.put(title, files[i].toString());
                }else{
                    title = files[i].getName();
                }
                macrosList.add(new Macros(i + 1, title, files[i].isDirectory()? Macros.macrosType.FOLDER: Macros.macrosType.MACROS, this));

            }

        }

        setMacrosRecycler(macrosList);


    }


    @Override
    public void onInterrupt(){

    }


    void showScreen(int index){


        for (int i = 0; i < screens.length; i++){
            //Log.d("INDEX", Integer.toString(i));
            rootView.findViewById(screens[i]).setVisibility(i == index ? View.VISIBLE : View.GONE);
        }


    }


    public void requestArgument(String title, int inputType, String callback){
        showScreen(1);

        customArgumentInput = rootView.findViewById(R.id.custom_argument_input);
        argumentTitle = rootView.findViewById(R.id.argument_title);

        customArgumentInput.setText("");
        customArgumentInput.setInputType(inputType);

        MacrolineAccessibilityService th = this;

        rootView.findViewById(R.id.enter_argument).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(activeModule == null) { //TODO active module is need?
                    Log.e("ERROR", "active module is NULL");
                    return;
                }
                boolean useCustomArgument = ((RadioButton)rootView.findViewById(R.id.radio_enter_custom_argument)).isChecked();


                if(!useCustomArgument)
                    textToReplace = source.getText().toString().substring(0, source.getText().toString().length() - sharedPreferences.getString("activation_text", "ml").length());

                LuaValue[] args = {LuaString.valueOf(
                        useCustomArgument?
                            customArgumentInput.getText().toString() : textToReplace),
                        CoerceJavaToLua.coerce(new Query(accessibilityNodeInfo, th))};



                //LuaValue[] args = {LuaString.valueOf(customArgumentInput.getText().toString()),
                //        CoerceJavaToLua.coerce(new Query(accessibilityNodeInfo, th))};
                runLuaFunc(currentLuaFile, callback, args);
            }
        });

        argumentTitle.setText(title == null? "Введите аргумент" : title);
    }


    public void closeWindow(){
        windowManager.removeView(rootView);
    }


    Globals runLuaFile(String filename)
    {
        try {
            Globals globals = JsePlatform.standardGlobals();
            globals.get("dofile").call(LuaValue.valueOf(filename));
            return globals;
        }catch (Exception e)
        {
            closeWindow();
            Toast toast = Toast.makeText(this,
                    "При выполнении модуля произошла ошибка: " + e.getMessage() , Toast.LENGTH_LONG);
            toast.show();
            return null;
        }
    }



    LuaValue runLuaFunc(Globals file, String func, LuaValue[] arguments) {
        //Globals globals = JsePlatform.standardGlobals();
        //globals.get("dofile").call(LuaValue.valueOf(filename));
        // chunk.call();

        //LuaValue f = globals.get(func);
        try {
            LuaValue f = file.get(func);
            LuaValue response = null;


            if (arguments == null || arguments.length == 0)
                response = f.call();
            else if (arguments.length == 1)
                response = f.call(arguments[0]);
            else if (arguments.length == 2)
                response = f.call(arguments[0], arguments[1]);
            else if (arguments.length == 3)
                response = f.call(arguments[0], arguments[1], arguments[2]);
            else {
                Log.e("ERROR", "Unknown length of arguments array!");
            }
            return response;
        }catch (Exception e)
        {
            closeWindow();
            Toast toast = Toast.makeText(this,
                    "При выполнении модуля произошла ошибка: " + e.getMessage() , Toast.LENGTH_LONG);
            toast.show();
            return null;
        }


    }


    public void write(String msg){

        String text = source.getText() == null ? "" : source.getText().toString();

        String activation_text = sharedPreferences.getString("activation_text", "ml");

        if(text.toLowerCase().endsWith(activation_text.toLowerCase())) {
            text = text.substring(0, text.length() - activation_text.length());
        }
        if(textToReplace != null && text.contains(textToReplace))
        {
            text = text.substring(0, text.length() - textToReplace.length());
        }
        text += msg;
        setText(text);

    }

    private void setText(String text){

        closeWindow();
/*
        AccessibilityNodeInfo targetNode = null;
        AccessibilityNodeInfo rootNode = source;

        closeWindow();

        if (rootNode != null) {

            List<AccessibilityNodeInfo> nodeList = source.findAccessibilityNodeInfosByViewId(windowId);

            if (nodeList != null && !nodeList.isEmpty()) {
                targetNode = nodeList.get(0);
            }
        }

        if (targetNode != null) {
            targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            targetNode.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
        }
*/

        Bundle bundle = new Bundle();

        bundle.putCharSequence(accessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);

       source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle);

    }

    public void setSelection(int start, int end){

        Bundle bundle = new Bundle();
        bundle.putInt(accessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT , start);
        bundle.putInt(accessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, end);

        AccessibilityNodeInfo targetNode = null;
        AccessibilityNodeInfo rootNode = accessibilityNodeInfo;

        if (rootNode != null) {


            Log.d("window", windowId);

            if (nodeList != null && !nodeList.isEmpty()) {
                targetNode = nodeList.get(0);
            }else{
                Log.e("ERROR", "");
            }
        }else{
            Log.e("ERROR", "rootNode is NULL");
        }

        if (targetNode != null) {
            targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            targetNode.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
        }else{
            Log.e("ERROR", "target node is NULL");
        }

         source.performAction(accessibilityNodeInfo.ACTION_SET_SELECTION, bundle);

    }


}
