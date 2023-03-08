package com.fielaan.macroline.model;

import android.content.Context;

import com.fielaan.macroline.MacrolineAccessibilityService;
import com.fielaan.macroline.ModulesFragment;
import com.fielaan.macroline.adapter.MacrosAdapter;

//P.s. под макросом здесь подразумевается любой пункт в меню выбора макроса
//     так например макросом может быть папка, кнопка и прочее
public class Macros {

    public enum macrosType{
        MACROS,
        FOLDER,
        BACK_BUTTON
    }


    MacrolineAccessibilityService macrolineAccessibilityService;
    ModulesFragment modulesFragment;
    int id;
    String title;
    macrosType type;

    public Macros(int id, String title, macrosType type, MacrolineAccessibilityService macrolineAccessibilityService) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.macrolineAccessibilityService = macrolineAccessibilityService;
        this.modulesFragment = null;
    }

    public Macros(int id, String title, macrosType type, ModulesFragment modulesFragment) {
        this.macrolineAccessibilityService = null;
        this.modulesFragment = modulesFragment;
        this.id = id;
        this.title = title;
        this.type = type;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public macrosType getType() {
        return type;
    }
    public void setType(macrosType type) {
        this.type = type;
    }

    public MacrolineAccessibilityService getMacrolineAccessibilityService() {
        return macrolineAccessibilityService;
    }
    public void setMacrolineAccessibilityService(MacrolineAccessibilityService macrolineAccessibilityService) {
        this.macrolineAccessibilityService = macrolineAccessibilityService;
    }

    public ModulesFragment getModulesFragment() {
        return modulesFragment;
    }


}
