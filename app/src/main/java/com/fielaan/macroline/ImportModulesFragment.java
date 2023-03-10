package com.fielaan.macroline;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.luaj.vm2.ast.Str;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class ImportModulesFragment extends Fragment {

    EditText importModulePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_import_modules, container, false);


        getWebModules();
        importModulePath = view.findViewById(R.id.import_module_path);

        view.findViewById(R.id.import_selected_module_butt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String myData = "";
                String originalPath = "";
                try {

                     originalPath = importModulePath.getText().toString();

                    FileInputStream fis = new FileInputStream(originalPath);
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
                    showError("?????????????????? ?????????????????????? ????????????. ??????????????????, ?????? ???????????????????? ?????????????????????????? ?????? ??????????, ?? ?????????? ?????????????????? ???????? ????????????????????.");
                    return;
                }
                int i;
                try{
                   i = myData.indexOf("--IMPORT_INTO{");
                }catch (Exception e)
                {
                    showError("???????????? ???????????? ?? ?????????? ?????????? ?????? ????????????????????");
                    return;
                }

                if(i == -1)
                {
                    showError("????????????: ?? ?????????? ???????????? ???? ???????????? ???????? ?????? ????????????????????");
                    return;
                }

                String importPath = "";
                importPath = myData.substring(i + 14, myData.indexOf("}", i));
                Log.d("PATH", importPath);


                OutputStream outputStream = null;
                try {
                    //File path = new File(importPath.substring(0, importPath.lastIndexOf("/")));
                    //path.mkdirs();
                    //create(importPath.substring(0, importPath.lastIndexOf("/")));
                    /*FileOutputStream fos = new FileOutputStream("plugins/" + importPath);
                    fos.write(myData.toString().getBytes());
                    fos.close();

                     */


                    //writeFileOnInternalStorage(getContext(), importPath, myData);
                    copyFile(originalPath.substring(0, originalPath.lastIndexOf("/") + 1),
                            originalPath.substring(originalPath.lastIndexOf("/") + 1),
                            "/data/data/com.fielaan.macroline/plugins/" + importPath.substring(0, importPath.lastIndexOf("/") + 1),
                            importPath.substring(importPath.lastIndexOf("/") + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("???????????? ???????????? ?? ???????? ????????????????????");
                }


            }
        });

        return view;
    }


    private void showError(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("?????????????????? ????????????")
                .setMessage(Html.fromHtml(text))
                .setCancelable(true)
                .setNegativeButton("??????????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void copyFile(String inputPath, String inputFile, String outputPath, String outputFile) {
        Log.d("", "inputPath " + inputPath);
        Log.d("", "inputFile " + inputFile);
        Log.d("", "outputPath " + outputPath);

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new  FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

            Toast toast = Toast.makeText(getContext(),
                    inputFile + " ???????????????????????? ?????? " + outputFile + "!", Toast.LENGTH_SHORT);
            toast.show();

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }


    void getWebModules(){
        try {
            URL url = new URL("http://fielaan.ihostfull.com/modules-list.txt");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d("DATA", connection.getContent().toString());
            } else {
                Log.e("IMF", "response code != 200");
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}