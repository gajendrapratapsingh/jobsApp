package com.example.util;


import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.io.File;

public class PickerObserver implements DefaultLifecycleObserver {
    private final ActivityResultRegistry registry;
    private final Activity activity;

    private PickerListener pickerListener;
    ActivityResultCallback<Uri> galleryResultUri = new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            if (uri != null) {
                String path = saveInCache(uri);
                pickerListener.onPicked(new File(path));
            }
        }
    };
    ActivityResultCallback<ActivityResult> cameraResultIntent = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            Intent data = result.getData();
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                String path = saveInCache(uri);
                pickerListener.onPicked(new File(path));
            }
        }
    };

    ActivityResultCallback<ActivityResult> fileResultIntent = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            Intent data = result.getData();
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                String path = saveInCache(uri);
                pickerListener.onPicked(new File(path));
            }
        }
    };

    ActivityResultCallback<ActivityResult> galleryOnlyResultIntent = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int resultCode = result.getResultCode();
            Intent data = result.getData();
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                String path = saveInCache(uri);
                pickerListener.onPicked(new File(path));
            }
        }
    };

    private ActivityResultLauncher<String> imageLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> fileLauncher;
    private ActivityResultLauncher<Intent> galleryOnlyLauncher;

    public PickerObserver(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        this.registry = activity.getActivityResultRegistry();
    }

    public PickerObserver(@NonNull Fragment fr) {
        this.activity = fr.requireActivity();
        this.registry = fr.requireActivity().getActivityResultRegistry();
    }

    private String saveInCache(Uri uri) {
        String fileName = PickerFileUtils.getFileName(activity, uri);
        File cacheDir = PickerFileUtils.getDocumentCacheDir(activity);
        File file = PickerFileUtils.generateFileName(fileName, cacheDir);
        String destinationPath = null;
        if (file != null) {
            destinationPath = file.getAbsolutePath();
            PickerFileUtils.saveFileFromUri(activity, uri, destinationPath);
        }
        return destinationPath;
    }


    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        //  DefaultLifecycleObserver.super.onCreate(owner);
        imageLauncher = registry.register("key", owner, new ActivityResultContracts.GetContent(), galleryResultUri);
        cameraLauncher = registry.register("key1", owner, new ActivityResultContracts.StartActivityForResult(), cameraResultIntent);
        fileLauncher = registry.register("key2", owner, new ActivityResultContracts.StartActivityForResult(), fileResultIntent);
        galleryOnlyLauncher = registry.register("key3", owner, new ActivityResultContracts.StartActivityForResult(), galleryOnlyResultIntent);
    }

    public void pickGallery(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        imageLauncher.launch("image/*");
    }

    public void pickCamera(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    public void pickFile(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        fileLauncher.launch(intent);
    }

    public void pickGalleryOnly(PickerListener pickerListener) {
        this.pickerListener = pickerListener;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        galleryOnlyLauncher.launch(intent);
    }

    public void pickImage(PickerListener pickerListener) {
        final CharSequence[] options = {"From Camera", "From Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Pick Image!");
        builder.setItems(options, (dialog, item) -> {
            if (item == 0) {
                pickCamera(pickerListener);
            } else if (item == 1) {
                pickGallery(pickerListener);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        //  DefaultLifecycleObserver.super.onStart(owner);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        //DefaultLifecycleObserver.super.onStop(owner);
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        //DefaultLifecycleObserver.super.onPause(owner);
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        //DefaultLifecycleObserver.super.onDestroy(owner);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        //DefaultLifecycleObserver.super.onResume(owner);
    }
}
