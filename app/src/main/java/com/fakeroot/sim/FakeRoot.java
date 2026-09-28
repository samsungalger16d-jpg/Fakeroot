package com.fakeroot.sim;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.*;

public class FakeRoot extends Activity {
    private static final String TAG = "FakeRoot";
    private static final String[] FAKE_BINARIES = {
        "su", "busybox", "magisk", "sh", "bash", "toolbox",
        "supersu", "daemonsu", "toybox", "sqlite3"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("FakeRoot v2.0\nAnti-detection enabled.");
        setContentView(tv);
        new Thread(this::bootstrap).start();
    }

    private void bootstrap() {
        try {
            createFakeBinaries();
            createFakeProps();
            createFakeSuPolicy();
            createFakeBuildProp();
            setupPathWrapper();
            Log.i(TAG, "Bootstrap complete");
        } catch (Exception e) {
            Log.e(TAG, "Bootstrap failed", e);
        }
    }

    private void createFakeBinaries() {
        File binDir = new File(getFilesDir(), "bin");
        binDir.mkdirs();
        for (String name : FAKE_BINARIES) {
            File f = new File(binDir, name);
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write("#!/system/bin/sh\n".getBytes());
                fos.write(("echo \"" + name + " v26.4\"\\n").getBytes());
                f.setExecutable(true, false);
                f.setReadable(true, false);
                Log.i(TAG, "Created: " + f.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, "Failed: " + name, e);
            }
        }
    }

    private void createFakeProps() {
        File propDir = new File(getFilesDir(), "props");
        propDir.mkdirs();
        File magiskProp = new File(propDir, "magisk.prop");
        try (FileWriter fw = new FileWriter(magiskProp)) {
            fw.write("MAGISK_VER=26.4\n");
            fw.write("MAGISK_VER_CODE=26400\n");
            fw.write("KEEPVERITY=true\n");
            fw.write("KEEPFORCEENCRYPT=true\n");
        } catch (IOException e) {
            Log.e(TAG, "createFakeProps failed", e);
        }
    }

    private void createFakeSuPolicy() {
        File policy = new File(getFilesDir(), "su_policy.sqlite");
        try (FileWriter fw = new FileWriter(policy)) {
            fw.write("SQLite format 3\u0000");
        } catch (IOException e) {
            Log.e(TAG, "createFakeSuPolicy failed", e);
        }
    }

    private void createFakeBuildProp() {
        File buildProp = new File(getFilesDir(), "build.prop");
        try (FileWriter fw = new FileWriter(buildProp)) {
            fw.write("ro.debuggable=1\n");
            fw.write("ro.secure=0\n");
            fw.write("ro.adb.secure=0\n");
        } catch (IOException e) {
            Log.e(TAG, "createFakeBuildProp failed", e);
        }
    }

    private void setupPathWrapper() {
        File wrapperDir = new File(getFilesDir(), "wrapper");
        wrapperDir.mkdirs();
        File suWrapper = new File(wrapperDir, "su");
        try (FileWriter fw = new FileWriter(suWrapper)) {
            fw.write("#!/system/bin/sh\n");
            fw.write("if [ \"$1\" = \"-c\" ]; then\n");
            fw.write("  shift\n");
            fw.write("  exec sh -c \"$@\"\n");
            fw.write("fi\n");
            fw.write("exec sh\n");
        } catch (IOException e) {
            Log.e(TAG, "setupPathWrapper failed", e);
        }
        suWrapper.setExecutable(true);
    }
}
