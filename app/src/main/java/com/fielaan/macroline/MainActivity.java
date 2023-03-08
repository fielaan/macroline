package com.fielaan.macroline;

import static android.provider.Settings.canDrawOverlays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.view.accessibility.AccessibilityManager;

import com.fielaan.macroline.databinding.ActivityMainBinding;
import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.appBottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.modules:
                    replaceFragment(new ModulesFragment());
                    break;
                case R.id.settings:
                    replaceFragment(new SettingsFragment());
                    break;
            }
            return true;
        });



        create("plugins");


        boolean enabled = isAccessibilityServiceEnabled(this, MacrolineAccessibilityService.class);
        if(!enabled){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Служба специальных возможностей выключена")
                    .setMessage(Html.fromHtml("Для работы приложения неоходимо включить сервис macroline в настройках."))
                    .setCancelable(true)
                    .setPositiveButton("Перейти в настройки", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });


            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if(!Settings.canDrawOverlays(this))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Разрешение не предоставллено")
                    .setMessage(Html.fromHtml("Для работы приложения неоходимо предоставить разрешение показывать окна поверх других приложений."))
                    .setCancelable(true)
                    .setPositiveButton("Перейти в настройки", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });


            AlertDialog dialog = builder.create();
            dialog.show();
        }


    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.app_frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public boolean isOverlayEnabled(){
        int accessibilityEnabledCode = 0;
        try {
            accessibilityEnabledCode = Settings.Secure.getInt(this.getContentResolver(), Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        } catch (Exception e) {
            return false;
        }
        return accessibilityEnabledCode == 1;



    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(service.getName()))
                return true;
        }

        return false;
    }






    private File create(String name) {
        File baseDir;

        baseDir = new File("/data/data/com.fielaan.macroline");

        if (baseDir == null)
            return Environment.getExternalStorageDirectory();

        File folder = new File(baseDir, name);

        if (folder.exists()) {
            return folder;
        }
        if (folder.isFile()) {
            folder.delete();
        }
        if (folder.mkdirs()) {
            return folder;
        }

        return Environment.getExternalStorageDirectory();
    }


    @Override
    public void onBackPressed() {

    }


}
