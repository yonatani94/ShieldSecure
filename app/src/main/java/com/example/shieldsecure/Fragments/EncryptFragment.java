package com.example.shieldsecure.Fragments;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.shieldsecure.MainActivity;
import com.example.shieldsecure.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.muddzdev.quickshot.QuickShot;

import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class EncryptFragment extends Fragment {
    // CONST
    public static final String TAG = "johny";
    private static final int ENCRYPTION_ID = 770;
    protected View view;
    private ShapeableImageView openImage;
    private EditText plainTextEdt;
    private MaterialButton encryptBtn;
    private MaterialButton saveImageToDeviceBtn;
    private MaterialButton shareImageBtn;
    private Drawable defaultBtnBackground;

    // Global
    private String plainImageType = "";
    private Bitmap plainBitmap;
    private Bitmap encryptedBitmap;
    private String plainText = "";

    public EncryptFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_encrypt, container, false);
        }
        initViews();
        return view;
    }

    private void initViews() {
        Log.d(TAG, "initViews: ");
        openImage = view.findViewById(R.id.encryptFragment_IMG_plainImage);
        openImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onClick: User already given permission, moving straight to storage");
                    openStorage();
                } else {
                    checkForStoragePermissions();
                }
            }
        });
        plainTextEdt = view.findViewById(R.id.encryptFragment_EDT_inputText);
        encryptBtn = view.findViewById(R.id.encryptFragment_BTN_EncryptText);
        encryptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (plainTextEdt.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Please enter plain text!",
                            Toast.LENGTH_SHORT).show();
                } else if (plainTextEdt.getText().toString().length() > 1000) {
                    Toast.makeText(getContext(), "Text too long! Max 1000 characters",
                            Toast.LENGTH_SHORT).show();
                } else {
                    plainText = plainTextEdt.getText().toString();
                    Log.d(TAG, "onClick: plain text: " + plainText);
                    if (checkDimensions()) {
                        byte bytesToEncrypt[] = getBytesToEncrypt(); // An array of bytes to be encrypted in the image
                        encryptMessage(bytesToEncrypt);
                    }
                }
            }
        });
        saveImageToDeviceBtn = view.findViewById(R.id.encryptFragment_BTN_saveImage);
        defaultBtnBackground = saveImageToDeviceBtn.getBackground();
        saveImageToDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFileNameFromUser();
            }
        });
        shareImageBtn = view.findViewById(R.id.encryptFragment_BTN_share);
        shareImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage();
            }
        });
    }

    /**
     * A method that will generate an alert dialog with input box to get the file name
     */
    private void getFileNameFromUser() {
        Log.d(TAG, "getFileNameFromUser: ");
        createInputDialog("Please enter file name");
    }

    /**
     * A method to share the encrypted image
     */
    private void shareImage() {
        Log.d(TAG, "shareImage: ");
        Toast.makeText(getContext(), "In Construction!", Toast.LENGTH_SHORT).show();
//        QuickShot.QuickShotListener quickShotListener = new QuickShot.QuickShotListener() {
//            @Override
//            public void onQuickShotSuccess(String path) {
//                Log.d(TAG, "onQuickShotSuccess: " + path);
//                Toast.makeText(getContext(), "Image Saved Successfully!", Toast.LENGTH_SHORT).show();
//                Intent share = new Intent(android.content.Intent.ACTION_SEND);
//                share.setType("image/png");
//                Uri photoURI = FileProvider.getUriForFile(getContext()
//                        , getActivity().getApplicationContext().getPackageName() + ".provider"
//                        , new File(Environment.getExternalStorageDirectory(), path));
//                share.putExtra(Intent.EXTRA_STREAM, photoURI);
//                share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                getContext().startActivity(Intent.createChooser(share, "Share image"));
//            }
//
//            @Override
//            public void onQuickShotFailed(String path, String errorMsg) {
//                Log.d(TAG, "onQuickShotFailed: ");
//                Toast.makeText(getContext(), "Image Saving Failed!", Toast.LENGTH_SHORT).show();
//                saveImageToDeviceBtn.setEnabled(true);
//            }
//        };
//        QuickShot.of(encryptedBitmap, getContext()).setResultListener(quickShotListener)
//                .enableLogging()
//                .setFilename("encrypted_image " + ThreadLocalRandom.current().nextInt(0, 60000 + 1))
//                .setPath("CipherZ")
//                .toPNG()
//                .save();
    }


    /**
     * A method to save the encrypted image to the device
     */
    private void saveImageToDevice(String filename) {
        Log.d(TAG, "saveImageToDevice: ");

        CircularProgressDrawable drawable = new CircularProgressDrawable(getContext());
        drawable.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.black);
        drawable.setCenterRadius(30f);
        drawable.setStrokeWidth(5f);
        drawable.start();
        saveImageToDeviceBtn.setBackgroundDrawable(drawable);
        saveImageToDeviceBtn.setText("Processing image");
        saveImageToDeviceBtn.setEnabled(false);

        QuickShot.QuickShotListener quickShotListener = new QuickShot.QuickShotListener() {
            @Override
            public void onQuickShotSuccess(String path) {
                Log.d(TAG, "onQuickShotSuccess: ");
                Toast.makeText(getContext(), "Image Saved Successfully!", Toast.LENGTH_SHORT).show();
                saveImageToDeviceBtn.setText("Save image to device");
                saveImageToDeviceBtn.setBackground(shareImageBtn.getBackground());
                saveImageToDeviceBtn.setEnabled(true);
            }

            @Override
            public void onQuickShotFailed(String path, String errorMsg) {
                Log.d(TAG, "onQuickShotFailed: ");
                Toast.makeText(getContext(), "Image Saving Failed!", Toast.LENGTH_SHORT).show();
                saveImageToDeviceBtn.setEnabled(true);
                saveImageToDeviceBtn.setText("Save image to device");
            }
        };

        QuickShot.of(encryptedBitmap, getContext()).setResultListener(quickShotListener)
                .enableLogging()
                .setFilename(filename)
                .setPath("CipherZ")
                .toPNG()
                .save();
    }

    /**
     * A method to create input alert dialog
     */
    private void createInputDialog(String title) {
        Log.d(TAG, "createInputDialog: ");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveImageToDevice(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveImageToDeviceBtn.setEnabled(true);
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * A method to convert the users plain image into a byte array
     *
     * @return a byte array that represents the user plain image
     */
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        Log.d(TAG, "bitmapToByteArray: ");
        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        return byteBuffer.array();
    }

    /**
     * A method to encrypt the given byte array into the plain image
     */
    private void encryptMessage(byte[] bytesToEncrypt) {
        Log.d(TAG, "encryptMessage: ");
        // The plain image byte array will be 4 times bigger than the image size
        // since the image size is in pixels, and each pixel consists of 4 bytes. (alpha,R,G,B)

        byte[] plainImage = bitmapToByteArray(plainBitmap);
        int plainBytesIndex = 0;
        for (int i = 0; i < bytesToEncrypt.length; i++) {
            int mask = 1;
            for (int j = 0; j < 8; j++) {
                int bitToWrite = bytesToEncrypt[i] & mask; // get the proper bit of the current byte
                bitToWrite >>= j; // move the bit to the LSB
                if (bitToWrite == 1) {
                    // &= will write the 0, |= will write the 1
                    plainImage[plainBytesIndex] |= bitToWrite; // replace LSB of the plain byte with our message byte
                } else {
                    bitToWrite |= 0xfe; // turn on all other bits except the lsb
                    // &= will write the 0, |= will write the 1
                    plainImage[plainBytesIndex] &= bitToWrite; // replace LSB of the plain byte with our message byte
                }
                mask <<= 1; // move the mask one bit left
                plainBytesIndex++;
            }
        }

        encryptedBitmap = byteArrayToBitmap(plainImage);
        Toast.makeText(getContext(), "Message encryption successfull!", Toast.LENGTH_SHORT).show();
        saveImageToDeviceBtn.setEnabled(true);
//        shareImageBtn.setEnabled(true);
    }

    /**
     * @param src - byte array to be converted to an image
     * @return bitmap that represents the encrypted image
     */
    private Bitmap byteArrayToBitmap(byte[] src) {
        Log.d(TAG, "byteArrayToBitmap: ");
        Bitmap.Config configBmp = Bitmap.Config.valueOf(plainBitmap.getConfig().name());
        Bitmap bitmap_tmp = Bitmap.createBitmap(plainBitmap.getWidth(), plainBitmap.getHeight(), configBmp);
        ByteBuffer buffer = ByteBuffer.wrap(src);
        bitmap_tmp.copyPixelsFromBuffer(buffer);
        return bitmap_tmp;
    }

    /**
     * A method to open the storage to fetch a photo
     */
    private void openStorage() {
        Log.d(TAG, "openStorage: opening storage");
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, MainActivity.STORAGE_PICTURE_REQUEST);
    }


    /**
     * A method to check for storage permissions
     */
    private void checkForStoragePermissions() {
        Log.d(TAG, "checkingForStoragePermissions: checking for storage permissions");

        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Log.d(TAG, "onPermissionGranted: User given permission");
                        openStorage();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            Log.d(TAG, "onPermissionDenied: User denied permission permanently!");
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(getString(R.string.permission_denied))
                                    .setMessage(getString(R.string.permission_denied_explication_storage))
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Log.d(TAG, "onClick: Opening settings activity");
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                                            startActivityForResult(intent, MainActivity.STORAGE_PERMISSION_SETTINGS_REQUEST);
                                        }
                                    }).show();
                        } else {
                            Log.d(TAG, "onPermissionDenied: User denied permission!");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * A method to create header and convert plaintext to bytes.
     * The method will return the byte array to be encrypted inside the image
     */
    private byte[] getBytesToEncrypt() {
        Log.d(TAG, "getBytesToEncrypt: ");
        byte encryption_id[] = intToBytes(ENCRYPTION_ID); // ENCRYPTION_ID = the first 2 bytes represent that the image was encrypted.
        // Like some sort of a key that is ENCRYPTION_ID.
        byte textLength[] = intToBytes(plainText.length() * 8); // get the header byte array (2 bytes)
        byte plainTextBytes[] = plainText.getBytes();
        byte bytesToEncrypt[] = new byte[plainTextBytes.length + 4]; // byte for each letter + 2 bytes header + 2 bytes id
        bytesToEncrypt[0] = encryption_id[0];
        bytesToEncrypt[1] = encryption_id[1];
        bytesToEncrypt[2] = textLength[0];
        bytesToEncrypt[3] = textLength[1];
        int index = 4;
        for (int i = 0; i < plainTextBytes.length; i++) {
            bytesToEncrypt[index] = plainTextBytes[i];
            index++;
        }
        Log.d(TAG, "getBytesToEncrypt: TESTING:");
        printByteArray(bytesToEncrypt);
        return bytesToEncrypt;
    }

    /**
     * A method to convert an integer to a byte array
     */
    public byte[] intToBytes(final int i) {
        Log.d(TAG, "intToBytes: ");
        byte temp[] = ByteBuffer.allocate(4).putInt(i).array();
        byte result[] = {temp[2], temp[3]};
        return result;
    }

    /**
     * A method to check dimensions of image and plaintext
     */
    private boolean checkDimensions() {
        Log.d(TAG, "encodeImage: ");
        Bitmap encodedBitmap = plainBitmap;
        int imageHeight = plainBitmap.getHeight();
        int imageWidth = plainBitmap.getWidth();
        int totalPixels = imageHeight * imageWidth;

        // 32 first bits (4 bytes) - reserved for header (id & text length)
        // next bits are for the plaintext itself (max 1000 chars)
        int totalSizeNeeded = 32 + (plainText.getBytes().length * 8);

        if (imageHeight * imageWidth < totalSizeNeeded) { // checking image dimensions fit plaintext size
            Log.d(TAG, "checkDimensions: Image too small for the text!");
            return false;
        } else {
            return true;
        }
    }


    /**
     * A method to print a byte array
     *
     * @param arr = array to print
     */
    public void printByteArray(byte[] arr) {
        for (int i = 0; i < arr.length; i++) {
            Log.d(TAG, "byte[" + i + "] = " + arr[i]);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MainActivity.STORAGE_PERMISSION_SETTINGS_REQUEST:
                Log.d(TAG, "onActivityResult: I came from app settings: storage");
                break;
            case MainActivity.STORAGE_PICTURE_REQUEST:
                Log.d(TAG, "onActivityResult: I came from storage with picture");
                if (data != null) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        plainImageType = filePath.substring(filePath.lastIndexOf(".") + 1);
                        Log.d(TAG, "onActivityResult: File type: " + plainImageType);
                    }
                    cursor.close();

                    if (plainImageType.equals("jpg")) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                            plainBitmap = bitmap;
                            openImage.setStrokeWidth(30);
                            openImage.setStrokeColor(getActivity().getColorStateList(R.color.colorPrimary));
                            openImage.setImageBitmap(bitmap);
                            encryptBtn.setEnabled(true);
                        } catch (IOException e) {
                            Log.i("TAG", "Some exception " + e);
                        }
                    } else {
                        openImage.setStrokeWidth(0);
                        openImage.setStrokeColor(getActivity().getColorStateList(R.color.colorSecondary));
                        openImage.setImageDrawable(getActivity().getDrawable(R.drawable.photo_empty_frame));
                        displayAlertDialog("Error!", "Image has to be JPG format!");
                    }
                }
                break;
        }
    }

    /**
     * A method to display an alert dialog to indicate something
     *
     * @param message - the message to display to the user
     */
    private void displayAlertDialog(String title, String message) {
        Log.d(TAG, "displayAlertDialog: ");
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
//                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
