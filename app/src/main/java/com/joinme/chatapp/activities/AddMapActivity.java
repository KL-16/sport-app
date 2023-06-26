package com.joinme.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.joinme.chatapp.R;
import com.joinme.chatapp.databinding.ActivityAddMapBinding;
import com.joinme.chatapp.utilities.Constants;
import com.joinme.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;

public class AddMapActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityAddMapBinding binding;
    private String duration = null;
    private String category = null;
    private int spinnerSelection = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sports, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        binding.spinner.setAdapter(adapter);
        binding.spinner.setOnItemSelectedListener(this);
        binding.spinner.setSelection(spinnerSelection);

        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.duration, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(R.layout.spinner_item);
        binding.timeSpinner.setAdapter(timeAdapter);
        binding.timeSpinner.setOnItemSelectedListener(this);
        binding.timeSpinner.setSelection(spinnerSelection);

        setListeners();
    }

    private void setListeners(){
        binding.cancel.setOnClickListener(view -> onBackPressed());
        binding.accept.setOnClickListener(view -> confirmMarker());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void confirmMarker(){

        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        HashMap<String, Object> mapEvent = new HashMap<>();
        mapEvent.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        mapEvent.put(Constants.KEY_DURATION, duration);
        mapEvent.put(Constants.KEY_CATEGORY, category);
        mapEvent.put(Constants.KEY_TIMESTAMP, new Date());
        mapEvent.put(Constants.KEY_EVENT_DESCRIPTION, binding.description.getText().toString());
        Intent intent = getIntent();
        mapEvent.put(Constants.KEY_EVENT_LATITUDE, intent.getStringExtra("Latitude"));
        mapEvent.put(Constants.KEY_EVENT_LONGITUDE, intent.getStringExtra("Longitude"));
        mapEvent.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        mapEvent.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
        mapEvent.put(Constants.KEY_GENDER, preferenceManager.getString(Constants.KEY_GENDER));
        mapEvent.put(Constants.KEY_AGE, preferenceManager.getString(Constants.KEY_AGE));
        addMapEvent(mapEvent);
    }

    private void addMapEvent(HashMap<String, Object> mapEvent){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_EVENTS)
                .add(mapEvent)
                .addOnSuccessListener(documentReference -> onBackPressed());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String choice = adapterView.getItemAtPosition(i).toString();
        switch (adapterView.getId()){
            case R.id.spinner:
                category = choice;
                break;
            case R.id.timeSpinner:
                duration = choice;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        String choice = adapterView.getItemAtPosition(spinnerSelection).toString();
        switch (adapterView.getId()){
            case R.id.spinner:
                category = choice;
                break;
            case R.id.timeSpinner:
                duration = choice;
                break;
        }
    }
}