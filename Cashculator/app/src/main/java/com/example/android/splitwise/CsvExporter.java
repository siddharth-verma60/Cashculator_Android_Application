package com.example.android.splitwise;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.android.splitwise.data.Calculation;
import com.example.android.splitwise.filter.CsvOutput;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.Manifest.permission;

class CsvExporter{

	static private final int MAX_FILENUM = 1000;

    static void export(Calculation calculation, Context context) {
        Resources res = context.getResources();

        File root = Environment.getExternalStorageDirectory();

//        if (Environment.MEDIA_MOUNTED.equals(root)) {
//            Log.v("Test", "sdcard mounted and writable");
//        }
//        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(root)) {
//            Log.v("Test", "sdcard mounted readonly");
//        }
//        else {
//            Log.v("Test", "sdcard state: " + root);
//        }



//        if (!root.canWrite()) {
//            Toast.makeText(context, res.getString(R.string.export_error_no_permission), Toast.LENGTH_LONG).show();
//            return;
//        }

        File dir = new File(root, res.getString(R.string.app_name));

        if (!dir.exists() && !dir.mkdir()) {
            String message = String.format(res.getString(R.string.export_error_mkdir), dir.toString());
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            return;
        }

        File csvFile = determineFileName(calculation, dir);
        if (csvFile == null) {
            Toast.makeText(context, res.getString(R.string.export_error_no_free_file), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            FileWriter writer = new FileWriter(csvFile);
            CsvOutput csv = new CsvOutput(calculation);
            writer.write(csv.toCsv());
            writer.close();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                triggerMediaRescan(csvFile, context);

            String message = String.format(res.getString(R.string.export_success), csvFile.toString());
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            String message = String.format(res.getString(R.string.export_error_write_failed), csvFile.toString());
            //Toast.makeText(context, message + ":" + e.toString(), Toast.LENGTH_LONG).show();
            Log.e("moneybalance", message, e);
        }
    }

	private static File determineFileName(Calculation calculation, File dir) {
		String base = calculation.getTitle();
		final char[] ILLEGAL_CHARS = { '\\', '/', '<', '>', '?', ':', '*', '|', '"', '\'' };
		for (char illegal : ILLEGAL_CHARS)
			base = base.replace(illegal, '_');

		File file = new File(dir, base + ".csv");
		if (!file.exists())
			return file;

		for (int i = 1; i < MAX_FILENUM; i++) {
			file = new File(dir, String.format("%s (%d).csv", base, i));
			if (!file.exists())
				return file;
		}
		return null;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private static void triggerMediaRescan(File file, Context context) {
		MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath() }, null, null);
	}

}
