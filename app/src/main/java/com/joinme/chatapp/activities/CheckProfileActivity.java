package com.joinme.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.joinme.chatapp.databinding.ActivityCheckProfileBinding;
import com.joinme.chatapp.models.User;
import com.joinme.chatapp.utilities.Constants;

import java.util.Objects;

public class CheckProfileActivity extends BaseActivity {

    private ActivityCheckProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        init();
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
        binding.chat.setOnClickListener(view -> onChatClicked());
    }

    public void onChatClicked() {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, (User) getIntent().getSerializableExtra(Constants.KEY_USER));
        startActivity(intent);
        finish();
    }

    private void init(){
        User receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        assert receiverUser != null;
        setUserData(receiverUser);
    }

    private void setUserData(@NonNull User user){
        Objects.requireNonNull(binding.imageProfile).setImageBitmap(getUserImage(user.image));
        Objects.requireNonNull(binding.shortBio).setText(user.shortBio);
        Objects.requireNonNull(binding.name).setText(user.name);
        String genderAndAge = user.gender + ", " + user.age;
        Objects.requireNonNull(binding.gender).setText(genderAndAge);
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}