package com.fielaan.macroline;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fielaan.macroline.adapter.MacrosAdapter;
import com.fielaan.macroline.model.Macros;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModulesFragment extends Fragment {

    View view;
    private String currentPath = "";
    private TextView pathText;
    private RecyclerView modulesRecycler;
    final String path = "/data/data/com.fielaan.macroline/plugins/";
    LuaValue currentLuaFile;
    Map<String,String> macroses = new HashMap<String,String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_modules, container, false);
        modulesRecycler = view.findViewById(R.id.app_modules_list);
        pathText = view.findViewById(R.id.app_current_path);
        view.findViewById(R.id.import_modules_butt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).replaceFragment(new ImportModulesFragment());
            }
        });

        showMacroses();

        return view;
    }
/*
    void showMacroses(){
        Log.d("PATH", currentPath);

        pathText.setText((currentPath.length() > 0? "" : "/") + currentPath);

        File dir = new File(this.path + currentPath);
        File[] files = dir.listFiles();


        List<Macros> macrosList = new ArrayList<>();

        if(currentPath.length() > 0) macrosList.add(new Macros(0, "Назад", Macros.macrosType.BACK_BUTTON, this));

        if(files == null || files.length == 0) {//Макросы не найдены
            view.findViewById(R.id.modules_not_found_text).setVisibility(View.VISIBLE);
        }else{
            view.findViewById(R.id.modules_not_found_text).setVisibility(View.GONE);
            for (int i = 0; i < files.length; i++) {
                String title = "";
                if(!files[i].isDirectory()) {


                    LuaValue resp = runLuaFunc(runLuaFile(files[i].toString()), "getInfo", null);

                    title = resp.checktable().get("title").toString();
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



 */

    void showMacroses(){

        Log.d("PATH", currentPath);

        pathText.setText((currentPath.length() > 0? "" : "/") + currentPath);

        File dir = new File(this.path + currentPath);
        File[] files = dir.listFiles();


        List<Macros> macrosList = new ArrayList<>();

        if(currentPath.length() > 0) macrosList.add(new Macros(0, "Назад", Macros.macrosType.BACK_BUTTON, this));

        if(files == null || files.length == 0) {//Макросы не найдены
            Log.d("", "MACROSES NOT FOUND");
            view.findViewById(R.id.modules_not_found_text).setVisibility(View.VISIBLE);
        }else{
            view.findViewById(R.id.modules_not_found_text).setVisibility(View.GONE);
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

//                    macroses.put(title, files[i].toString());
                }else{
                    title = files[i].getName();
                }
                macrosList.add(new Macros(i + 1, title, files[i].isDirectory()? Macros.macrosType.FOLDER: Macros.macrosType.MACROS, this));

            }

        }

        setMacrosRecycler(macrosList);


    }

    public void OnClick(String macros, Macros.macrosType type) {
        Log.d("", "CLICKED !!!");
        if (type == Macros.macrosType.FOLDER) {
            currentPath += "/" + macros;
            showMacroses();
        } else if(type == Macros.macrosType.MACROS)
        {
        }else if(type == Macros.macrosType.BACK_BUTTON){
            currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
            showMacroses();
        }else{
            Log.e("ERROR" , "ERR: unknown type of macros!");
        }
    }


    private void setMacrosRecycler(List<Macros> macrosList) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        modulesRecycler.setLayoutManager(layoutManager);
        modulesRecycler.setBackgroundColor(Color.parseColor("#ffffff"));
        MacrosAdapter macrosAdapter = new MacrosAdapter(view.getContext(), macrosList);

        modulesRecycler.setAdapter(macrosAdapter);
        modulesRecycler.setBackgroundColor(Color.parseColor("#ffffff"));
    }
}