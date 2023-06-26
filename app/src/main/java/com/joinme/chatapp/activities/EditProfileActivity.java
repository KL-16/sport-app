package com.joinme.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.joinme.chatapp.databinding.ActivityEditProfileBinding;
import com.joinme.chatapp.models.User;
import com.joinme.chatapp.utilities.Constants;
import com.joinme.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class EditProfileActivity extends BaseActivity {

    private ActivityEditProfileBinding binding;
    private PreferenceManager preferenceManager;
    private DocumentReference documentReference;
    private String encodedImage = null;
    private int ageSelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        setMyData();
        setListeners();
    }

    private void setMyData(){
        Objects.requireNonNull(binding.imageProfile).setImageBitmap(getUserImage(preferenceManager.getString((Constants.KEY_IMAGE))));
        Objects.requireNonNull(binding.shortBio).setText(preferenceManager.getString(Constants.KEY_SHORT_BIO));
        Objects.requireNonNull(binding.name).setText(preferenceManager.getString(Constants.KEY_NAME));
        if(preferenceManager.getString(Constants.KEY_GENDER).equals("Mężczyzna")){
            binding.manRadioButton.toggle();
        }
        else{
            binding.womenRadioButton.toggle();
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
            startActivity(intent);
            finish();
        });
        binding.buttonSave.setOnClickListener(view -> onButtonSavePressed());
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.agePicker.setMinValue(1920);
        binding.agePicker.setMaxValue(2020);

        int loadedAge = 2022 - Integer.parseInt(preferenceManager.getString(Constants.KEY_AGE));
        Objects.requireNonNull(binding.agePicker).setValue(loadedAge);
        ageSelected = 2022 - loadedAge;

        Objects.requireNonNull(binding.agePicker).setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                ageSelected = 2021 - i1;
            }
        });
    }

    private void onButtonSavePressed(){
        if(isValidSignUpDetails()){
            if(encodedImage != null){
                documentReference.update(Constants.KEY_NAME, binding.name.getText().toString(), Constants.KEY_SHORT_BIO, binding.shortBio.getText().toString(), Constants.KEY_IMAGE, encodedImage);
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
            }
            else{
                documentReference.update(Constants.KEY_NAME, binding.name.getText().toString(), Constants.KEY_SHORT_BIO, binding.shortBio.getText().toString());
            }
            preferenceManager.putString(Constants.KEY_SHORT_BIO, binding.shortBio.getText().toString());
            preferenceManager.putString(Constants.KEY_NAME, binding.name.getText().toString());
            preferenceManager.putString(Constants.KEY_AGE, String.valueOf(ageSelected));
            if(binding.womenRadioButton.isChecked()){
                preferenceManager.putString(Constants.KEY_GENDER, "Kobieta");
            }else{
                preferenceManager.putString(Constants.KEY_GENDER, "Mężczyzna");
            }
            Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private String encodedImage(Bitmap bitmap){
        int previewWidth = 400;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            assert imageUri != null;
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            Objects.requireNonNull(binding.imageProfile).setImageBitmap(bitmap);
                            encodedImage = encodedImage(bitmap);

                        }catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails(){
        if(binding.name.getText().toString().trim().isEmpty()){
            showToast("Wprowadź imie");
            return false;
        }
        else if(binding.shortBio.getText().toString().trim().isEmpty()){
            showToast("Wprowadź krótki opis");
            return false;
        }
        else if(binding.shortBio.getText().toString().length() > 200 || binding.shortBio.getText().toString().length() < 50){
            showToast("Opis musi wynosić od 50 do 200 znaków");
            return false;
        }
        else {
            return true;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
        startActivity(intent);
        finish();
    }
}