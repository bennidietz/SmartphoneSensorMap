package com.thomaskuenneth.locationdemo2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Camera_Helper {

    private boolean tabletFrameworkFunktioniert = false;
    Calendar calendar;
    SimpleDateFormat sdf;
    String datepic = "";
    Intent cameraintent;
    Context context = null;
    static final int CAM_REQUEST = 1;
    private Uri imageUri;


    public Camera_Helper(Context con) {
        context = con;
    }

    public void initializeCamera() {
        initializeCamera("");
    }

    public void initializeCamera(String prefix) {
        try {
            tabletCameraFramework(prefix);
            tabletFrameworkFunktioniert = true;
        } catch (Exception e) {
            //falls tabletCameraFramwork abstürzt kommt man hier rein und durchläuft die etwas schlechtere Variante
            smartphoneCameraFramework();
            tabletFrameworkFunktioniert = false;
        }
    }

    private void tabletCameraFramework(String prefix) {
        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        datepic = sdf.format(calendar.getTime());
        datepic = datepic.replace(":", "").replace(" ","_");
        datepic = prefix +  datepic + ".jpg"; //Name des Bildes beinhaltet aktuelles Datum + Uhrzeit + WK-Nummer
        System.out.println("oygkcdfg " + datepic);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build()); // auch gut für GLS Check APP
        cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getFile(context.getFilesDir().getAbsolutePath(), datepic); //Pfad, in dem Bild gepeichert wird
        cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        ((Activity) context).startActivityForResult(cameraintent, CAM_REQUEST);
    }

    private void smartphoneCameraFramework() {
        //gespeichertes Bild wird in default Ordner der Gallerie gespeichert
        ContentValues camvalues = new ContentValues();
        camvalues .put(MediaStore.Images.Media.TITLE,
                getString(R.string.app_name));
        camvalues .put(MediaStore.Images.Media.DESCRIPTION,
                getString(R.string.descr));
        camvalues .put(MediaStore.Images.Media.MIME_TYPE,
                "image/jpeg");
        imageUri = context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                camvalues ); //imageUri wird später bei onActivityResult benutzt und Kopie in "richtigen" Ordner angelegt
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        ((Activity) context).startActivityForResult(intent, CAM_REQUEST);

    }

    private String getString(int rescource_id) {
        return ((Activity) context).getString(rescource_id);
    }

    private File getFile(String path, String filename) {
        File folder = new File(path);

        if(!folder.exists()) {
            folder.mkdir();
        }

        File imageFile = new File(folder, filename);
        return imageFile;
    }

    public String onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!tabletFrameworkFunktioniert) {
            if (resultCode == ((Activity) context).RESULT_OK && requestCode == CAM_REQUEST) {
                try {
                    // Smartphone-Version, wo die "einfachere" Variante nicht funktioniert
                    // Bild wird erneut unter PATH_BILDER gespeicht
                    final InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    calendar = Calendar.getInstance();
                    sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    //datepic = sdf.format(calendar.getTime());
                    //datepic = datepic.replace(":", "");
                    //datepic = wknummer + "_" + datepic + ".jpg"; //Dateiname festgelegt
                    File filename = getFile(context.getFilesDir().getAbsolutePath(), datepic); //kompletter Pfad, wo Bild gespeichert werden soll
                    OutputStream out = null;
                    //try {
                    out = new FileOutputStream(filename);
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                    //bildergespeichert.add(datepic); //ermöglicht Bild in Datei einzubeziehen
                    Toast.makeText(context, "Bilder gespeichert!", Toast.LENGTH_SHORT).show();
                    if (!datepic.isEmpty()) return datepic;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Die Bild-Datei konnte nicht gefunden werden", Toast.LENGTH_LONG).show();
                    return null;
                }

            }

        } else {
            //Tablet-Framework funktioniert, Bild ist also schon richtig abgespeichert, muss nur noch der Messung zugeordnet werden
            if ((requestCode == CAM_REQUEST) && (resultCode == Activity.RESULT_OK)) {
                Uri u = cameraintent.getData();
                //bildergespeichert.add(datepic);
                Toast.makeText(context, "Bild gespeichert!", Toast.LENGTH_SHORT).show();
                if (!datepic.isEmpty()) return datepic;
            } else try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    public static void nogoingBack(Context context, int bilderAnzahl) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warnung:");
        if(bilderAnzahl==1) {
            builder.setMessage("Sie haben für diesen Check " + bilderAnzahl + " Bild gespeichert. Sichern Sie den Check, damit das Bild zugeordnet werden kann.");
        } else builder.setMessage("Sie haben für diesen Check " + bilderAnzahl + " Bilder gespeichert. Sichern Sie den Check, damit die Bilder zugeordnet werden können.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }



}
