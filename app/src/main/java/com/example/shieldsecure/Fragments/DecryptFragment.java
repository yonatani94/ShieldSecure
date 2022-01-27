package com.example.shieldsecure.Fragments;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.io.IOException;
import java.nio.ByteBuffer;

public class DecryptFragment extends Fragment {

    // CONST
    public static final String TAG = "johny";
    private static final int ENCRYPTION_ID = 770;
    protected View view;
    private ShapeableImageView openImage;
    private TextView plainTextLbl;
    private MaterialButton decryptTextBtn;

    // Global
    private String plainImageType = "";
    private Bitmap plainBitmap;
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    public DecryptFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_decrypt, container, false);
        }
        initViews();
        return view;
    }

    /**
     * A method to initialize the views
     */
    private void initViews() {
        Log.d(TAG, "initViews: ");
        openImage = view.findViewById(R.id.decryptFragment_IMG_plainImage);
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
        plainTextLbl = view.findViewById(R.id.decryptFragment_EDT_inputText);
        plainTextLbl.setMovementMethod(new ScrollingMovementMethod());
        plainTextLbl.setTextIsSelectable(true);
        decryptTextBtn = view.findViewById(R.id.decryptFragment_BTN_DecryptText);
        decryptTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // ==================== decryption ========================
                byte[] imageToDecrypt = bitmapToByteArray(plainBitmap);
                if (checkValidID(imageToDecrypt) == ENCRYPTION_ID) {
                    String secret = decryptByteArray(imageToDecrypt);
                    plainTextLbl.setText(secret);
                    Log.d(TAG, "Secret: " + secret);
                } else {
                    openImage.setImageDrawable(getActivity().getDrawable(R.drawable.photo_empty_frame));
                    openImage.setStrokeWidth(0);
                    openImage.setStrokeColor(getActivity().getColorStateList(R.color.colorSecondary));
                    displayAlertDialog("Error", "The image is corrupted due to bad transfer or " +
                            "the image was never encrypted!");
                }
            }
        });
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
     * A method to open the storage to fetch a photo
     */
    private void openStorage() {
        Log.d(TAG, "openStorage: opening storage");
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, MainActivity.STORAGE_PICTURE_REQUEST);
    }

    /**
     * @param imageToDecrypt a byte array that represents the image to decrypt
     * @return a bite array that contains the encrypted message
     */
    private String decryptByteArray(byte[] imageToDecrypt) {
        Log.d(TAG, "decryptByteArray: ");
        // 16 first bytes, their LSB,  represent the size of the 2 first bytes, which
        // represent the image size

        int hiddenTextSizeBits = getPlainTextSizeBits(imageToDecrypt); // get the number of bits of the plaintext
        Log.d(TAG, "decryptByteArray: Textsizebits = " + hiddenTextSizeBits);
        byte plainText[] = new byte[hiddenTextSizeBits / 8];
        int index = 0;
        for (int i = 32; i < (hiddenTextSizeBits + 32); i += 8) {
            plainText[index] = decryptSingleByte(imageToDecrypt, i);
            index++;
        }
        for (int i = 0; i < plainText.length; i++) {
            plainText[i] = (byte) reverseBitsByte(plainText[i]);
        }

        //TODO: Maybe will need to add UTF stuff for hebrew and such
        return new String(plainText);
    }

    /**
     * A method to get the plaintext size from the secret image
     *
     * @param imageToDecrypt - the byte representation of the secret image
     * @return the size in bits of the plaintext
     */
    private int checkValidID(byte[] imageToDecrypt) {
        Log.d(TAG, "getPlainTextSizeBits: ");
        byte textSize[] = {decryptSingleByte(imageToDecrypt, 0), decryptSingleByte(imageToDecrypt, 8)};
        for (int i = 0; i < textSize.length; i++) {
            textSize[i] = (byte) reverseBitsByte(textSize[i]);
        }
        byte helper[] = {0, 0, textSize[0], textSize[1]};
        return bytesToInt(helper);
    }

    /**
     * A method to get the plaintext size from the secret image
     *
     * @param imageToDecrypt - the byte representation of the secret image
     * @return the size in bits of the plaintext
     */
    private int getPlainTextSizeBits(byte[] imageToDecrypt) {
        Log.d(TAG, "getPlainTextSizeBits: ");
        byte textSize[] = {decryptSingleByte(imageToDecrypt, 16), decryptSingleByte(imageToDecrypt, 24)};
        for (int i = 0; i < textSize.length; i++) {
            textSize[i] = (byte) reverseBitsByte(textSize[i]);
        }
        byte helper[] = {0, 0, textSize[0], textSize[1]};
        return bytesToInt(helper);
    }

    /**
     * A method to convert a given byte array to an integer
     *
     * @param arr - byte array to convert
     * @return an integer that was represented by the byte array
     */
    public int bytesToInt(byte[] arr) {
        Log.d(TAG, "bytesToInt: ");
        return ByteBuffer.wrap(arr).getInt();
    }


    /**
     * A method to reverse the bits of a given byte
     *
     * @param b - the byte to be reversed
     * @return - the byte with reversed bit order
     */
    public int reverseBitsByte(byte b) {
        Log.d(TAG, "reverseBitsByte: ");
        int res = 0;
        for (int bi = b, i = 0; i < 8; i++, bi >>>= 1)
            res = (res << 1) | (bi & 1);

        return res;
    }

    /**
     * A method to dectypt a single byte
     *
     * @param imageToDecrypt - the given image to decrypt
     * @param i              - starting index of the decryption
     * @return return the decrypted byte
     */
    private byte decryptSingleByte(byte[] imageToDecrypt, int i) {
        Log.d(TAG, "getSingleByte: ");
        int currentBit = (imageToDecrypt[i] & 0x1);
        byte result = initBlock(currentBit);
        int endIndex = i + 8;
        for (; i < endIndex; i++) { // iterate 8 bytes of secret image to get their LSB
            currentBit = (imageToDecrypt[i] & 0x1);
            result <<= 1;
            result |= currentBit;
        }
        return result;
    }

    /**
     * A method to initialize a new byte with given start bit
     *
     * @param i = starting bit
     * @return return a byte with the starting bit
     */
    private byte initBlock(int i) {
        Log.d(TAG, "initBlock: ");
        byte res = 0;
        return res |= i;
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
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                        plainBitmap = bitmap;
                        openImage.setStrokeWidth(30);
                        openImage.setStrokeColor(getActivity().getColorStateList(R.color.colorPrimary));
                        openImage.setImageBitmap(bitmap);
                        decryptTextBtn.setEnabled(true);
                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                }
                break;
        }
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
