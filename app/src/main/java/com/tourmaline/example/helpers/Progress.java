package com.tourmaline.example.helpers;


import android.app.Activity;
import android.app.ProgressDialog;

import com.tourmaline.example.R;

public class Progress {

    private Progress() {}

    private static ProgressDialog progress;

    public static void show(final Activity activity) {
        if(activity==null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progress!=null) {
                    progress.dismiss();
                    progress = null;
                }
                progress = new ProgressDialog(activity);
                progress.setMessage(activity.getResources().getString(R.string.please_wait));
                progress.show();
            }
        });
    }

    public static void dismiss(final Activity activity) {
        if(activity==null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progress!=null) {
                    progress.dismiss();
                    progress = null;
                }
            }
        });
    }

}
