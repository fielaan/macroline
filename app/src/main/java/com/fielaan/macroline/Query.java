package com.fielaan.macroline;

import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;


public class Query {

    AccessibilityNodeInfo accessibilityNodeInfo;
    MacrolineAccessibilityService macrolineAccessibilityService;

    public Query(AccessibilityNodeInfo accessibilityNodeInfo, MacrolineAccessibilityService macrolineAccessibilityService) {
        this.accessibilityNodeInfo = accessibilityNodeInfo;
        this.macrolineAccessibilityService = macrolineAccessibilityService;

    }

    public void closeWindow(){
        macrolineAccessibilityService.closeWindow();
    }

    public void write(String msg){ //

        macrolineAccessibilityService.write(msg);

    }

    public void requestArgument(String title, int inputType, String callback){
        macrolineAccessibilityService.requestArgument(title, inputType, callback);
    }

    private void setSelection(int start, int end){ //TODO check that working good

        Bundle bundle = new Bundle();
        bundle.putInt(accessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT , start);
        bundle.putInt(accessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, end);
        accessibilityNodeInfo.performAction(accessibilityNodeInfo.ACTION_SET_SELECTION, bundle);

    }

    public void showMessage(String text, boolean isShortLength)
    {
        Toast toast = Toast.makeText(macrolineAccessibilityService,
                text, isShortLength? Toast.LENGTH_SHORT:Toast.LENGTH_LONG);
        toast.show();
    }



}
