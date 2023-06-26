package com.joinme.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.joinme.chatapp.databinding.ActivitySignupBinding;
import com.joinme.chatapp.utilities.Constants;
import com.joinme.chatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage = null;
    private int ageSelected = 0;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        auth = FirebaseAuth.getInstance();
    }

    private void setListeners() {

        binding.textSignIn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();});
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.imageBack.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();});
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.agePicker.setMinValue(1920);
        binding.agePicker.setMaxValue(2020);
        binding.agePicker.setValue(1980);

        binding.agePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                ageSelected = 2022 - i1;
            }
        });

        ageSelected = 2022 - 1980;

        binding.womenRadioButton.toggle();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp(){
        loading(true);
        auth.createUserWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            HashMap<String, Object> user = new HashMap<>();
                            user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                            user.put(Constants.KEY_SHORT_BIO, binding.inputBio.getText().toString());
                            user.put(Constants.KEY_AGE, String.valueOf(ageSelected));
                            assert firebaseUser != null;
                            user.put(Constants.KEY_USER_ID, firebaseUser.getUid());
                            if(binding.womenRadioButton.isChecked()){
                                user.put(Constants.KEY_GENDER, "Kobieta");
                            }else{
                                user.put(Constants.KEY_GENDER, "Mężczyzna");
                            }

                            user.put(Constants.KEY_IMAGE, encodedImage);
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .add(user)
                                    .addOnSuccessListener(documentReference -> {
                                        loading(false);
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                        preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                                        preferenceManager.putString(Constants.KEY_AGE, String.valueOf(ageSelected));
                                        if(binding.womenRadioButton.isChecked()){
                                            preferenceManager.putString(Constants.KEY_GENDER, "Kobieta");
                                        }else{
                                            preferenceManager.putString(Constants.KEY_GENDER, "Mężczyzna");
                                        }
                                        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                                        preferenceManager.putString(Constants.KEY_SHORT_BIO, binding.inputBio.getText().toString());
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(exception -> {
                                        loading(false);
                                        showToast(exception.getMessage());
                                    });
                        }
                        else{
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (Exception e) {
                                Log.d("TASK UNSUCCESFUL", Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    }
                });
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
                            binding.imageProfile.setImageBitmap(bitmap);
                            encodedImage = encodedImage(bitmap);

                        }catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails(){
        if(encodedImage == null){
            showToast("Wybierz zdjęcie profilowe");
            return false;
        }
        else if(binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Wprowadź imie");
            return false;
        }
        else if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Wprowadź email");
            return false;
        }

        else if(!binding.manRadioButton.isChecked() && !binding.womenRadioButton.isChecked()){
            showToast("Wybierz płeć");
            return false;
        }

        else if(binding.inputBio.getText().toString().trim().isEmpty()){
            showToast("Wprowadź krótki opis");
            return false;
        }
        else if(ageSelected == 0){
            showToast("Wprowadź rok urodzenia");
            return false;
        }

        else if(binding.inputBio.getText().toString().length() > 200 || binding.inputBio.getText().toString().length() < 50){
            showToast("Opis musi wynosić od 50 do 200 znaków");
            return false;
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Wprowadź poprawny email");
            return false;
        }

        else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Wprowadź hasło");
            return false;
        }
        else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Potwierdź hasło");
            return false;
        }
        else if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Hasła muszą być identyczne");
            return false;
        }
        else if(binding.inputPassword.getText().toString().length()<6){
            showToast("Hasło musi wynosić co najmniej 6 znaków");
            return false;
        }
        else {
            return true;
        }
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}