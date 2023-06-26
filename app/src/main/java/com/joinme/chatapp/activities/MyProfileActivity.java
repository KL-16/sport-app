package com.joinme.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import com.joinme.chatapp.databinding.ActivityMyProfileBinding;
import com.joinme.chatapp.utilities.Constants;
import com.joinme.chatapp.utilities.PreferenceManager;

import java.util.Objects;

public class MyProfileActivity extends BaseActivity {

    private ActivityMyProfileBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        setMyData();
    }

    private void setMyData(){
        Objects.requireNonNull(binding.imageProfile).setImageBitmap(getUserImage(preferenceManager.getString((Constants.KEY_IMAGE))));
        Objects.requireNonNull(binding.shortBio).setText(preferenceManager.getString(Constants.KEY_SHORT_BIO));
        Objects.requireNonNull(binding.name).setText(preferenceManager.getString(Constants.KEY_NAME));
        String genderAndAge = preferenceManager.getString(Constants.KEY_GENDER) + ", " + preferenceManager.getString(Constants.KEY_AGE);
        Objects.requireNonNull(binding.gender).setText(genderAndAge);
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
        binding.editProfile.setOnClickListener(view -> onEditImageClicked());
    }

    public void onEditImageClicked() {
        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
        startActivity(intent);
        finish();
    }
}