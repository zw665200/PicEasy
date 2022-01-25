package com.picfix.tools.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/1/24 19:25
 */
public class ABC extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public void permissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 检查该权限是否已经获取
            int firstResult = ContextCompat.checkSelfPermission(this, permissions[0]);
            int secondResult = ContextCompat.checkSelfPermission(this, permissions[1]);

            if (firstResult != PackageManager.PERMISSION_GRANTED || secondResult != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 0x100);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //都同意的情况下
            }
        }

    }
}
