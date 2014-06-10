/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.holo.cts;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.holo.cts.LayoutAdapter.LayoutInfo;
import android.holo.cts.ThemeAdapter.ThemeInfo;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Collection of utility methods to handle creation and deletion of assets.
 *
 * It creates a directory called "cts-holo-assets" on the external storage.
 * Below that it creates a directory called "reference" to store images
 * generated by the generator and "failure" to store images that failed to
 * match when running the test.
 */
class BitmapAssets {

    private static final String TAG = BitmapAssets.class.getSimpleName();

    static final int TYPE_REFERENCE = 0;

    static final int TYPE_FAILED = 1;

    static final int TYPE_DIFF = 2;

    public static boolean clearDirectory(int type) {
        if (!isExternalStorageReady()) {
            return false;
        }

        File dir = getBitmapDir(type);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            int numFiles = files.length;
            for (int i = 0; i < numFiles; i++) {
                files[i].delete();
            }
        }
        return true;
    }

    private static boolean isExternalStorageReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getBitmapDir(int type) {
        String subDir;
        switch (type) {
            case TYPE_REFERENCE:
                subDir = "reference";
                break;
            case TYPE_FAILED:
                subDir = "failed";
                break;
            case TYPE_DIFF:
                subDir = "diff";
                break;
            default:
                throw new IllegalArgumentException("Bad type: " + type);
        }

        File file = new File(Environment.getExternalStorageDirectory(), "cts-holo-assets");
        return new File(file, subDir);
    }

    public static String getBitmapName(ThemeInfo themeInfo, LayoutInfo layoutInfo) {
        return themeInfo.getBitmapName() + "_" + layoutInfo.getBitmapName();
    }

    public static Bitmap getBitmap(Context context, String bitmapName) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier(bitmapName, "drawable", context.getPackageName());
        return ((BitmapDrawable) resources.getDrawable(resourceId)).getBitmap();
    }

    public static File getBitmapPath(String bitmapName, int type) {
        String prefix;
        switch (type) {
            case TYPE_REFERENCE:
                prefix = "";
                break;
            case TYPE_FAILED:
                prefix = "failed_";
                break;
            case TYPE_DIFF:
                prefix = "diff_";
                break;
            default:
                throw new IllegalArgumentException("Bad type: " + type);
        }

        return new File(getBitmapDir(type), prefix + bitmapName + ".png");
    }

    public static String saveBitmap(Bitmap bitmap, String bitmapName, int type) throws IOException {
        if (!isExternalStorageReady()) {
            Log.i(TAG, "External storage for saving bitmaps is not mounted");
            return null;
        }

        File file = getBitmapPath(bitmapName, type);
        File dir = file.getParentFile();
        dir.mkdirs();

        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(CompressFormat.PNG, 100, stream);
        stream.close();
        return file.getAbsolutePath();
    }
}
