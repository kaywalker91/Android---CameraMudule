package com.example.cameramodule2;

import static android.os.Environment.getExternalStoragePublicDirectory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath;
    private Uri photoUri;

    ImageView iv_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_result = (ImageView)findViewById(R.id.iv_result);

        //권한 체크
//        TedPermission.with(getApplicationContext())
//                .setPermissionListener(permissionlistener)
//                .setRationaleMessage("카메라 권한이 필요합니다.")
//                .setDeniedMessage("거부하셨습니다.")
//                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
//                .check();

        findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(intent.resolveActivity(getPackageManager()) != null){

                    File photoFile = null;

                    photoFile = createImageFile();

                    if(photoFile != null){

                        photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

                    }

                }
            }

        });

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                galleryAddPic();

            }
        });
    }

    private File createImageFile(){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Test_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = null;

        try {

            image = new File(storageDir, imageFileName + ".jpg");
            if (!image.exists()) image.createNewFile();

//            image = File.createTempFile(
//                    imageFileName,
//                    "jpg",
//                    storageDir
//            );

        } catch (IOException exception) {

            exception.printStackTrace();

        }

        imageFilePath = image.getAbsolutePath();
        return image;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){

            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exifInterface = null;

            try {

                exifInterface = new ExifInterface(imageFilePath);

            }catch (IOException exception){
                exception.printStackTrace();
            }

            int exifOrientaion;
            int exifDegree;

            if(exifInterface != null){

                exifOrientaion = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientaionToDegress(exifOrientaion);

            }else{

                exifDegree = 0;

            }

            ((ImageView)findViewById(R.id.iv_result)).setImageBitmap(rotate(bitmap,exifDegree));

        }

    }

    private int exifOrientaionToDegress(int exifOrientaion){

        if(exifOrientaion == ExifInterface.ORIENTATION_ROTATE_90){

            return 90;

        }else if(exifOrientaion == ExifInterface.ORIENTATION_ROTATE_180){

            return 180;

        }else if(exifOrientaion == ExifInterface.ORIENTATION_ROTATE_270){

            return 270;

        }

        return  0;

    }

    private Bitmap rotate(Bitmap bitmap, float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void galleryAddPic() {

        BitmapDrawable bitmapDrawable = (BitmapDrawable) iv_result.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        saveImageToGallery(bitmap);

    }

    private void saveImageToGallery(Bitmap bitmap){

        FileOutputStream fileOutputStream;

        try {

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){

                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"Image_"+".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+File.separator+"TestFolder");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                fileOutputStream = (FileOutputStream)resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                Objects.requireNonNull(fileOutputStream);

                Toast.makeText(getApplicationContext(), "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();

            }

        }catch (Exception e){

            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }

    }

//    PermissionListener permissionlistener = new PermissionListener() {
//        @Override
//        public void onPermissionGranted() {
//            Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onPermissionDenied(List<String> deniedPermissions) {
//            Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
//        }
//
//    };

}