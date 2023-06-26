package com.joinme.chatapp.activities;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.joinme.chatapp.R;
import com.joinme.chatapp.databinding.ActivityMapsBinding;
import com.joinme.chatapp.models.User;
import com.joinme.chatapp.utilities.Constants;
import com.joinme.chatapp.utilities.PreferenceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String sTitle;
    private String sDescription;
    private String sName;
    private String sGenderAndAge;
    private String sImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        init();
        sTitle = null;
        sGenderAndAge = null;
        sDescription = null;
        sName = null;
        sImage = null;
        setListeners();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        // Customise the styling of the base map using a JSON object defined
        // in a raw resource file.
        googleMap.setMapStyle(MapStyleOptions.
                loadRawResourceStyle(this, R.raw.style_json));
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        LatLng userLocation = new LatLng(Double.parseDouble(preferenceManager.getString(Constants.KEY_LATITUDE)), Double.parseDouble(preferenceManager.getString(Constants.KEY_LONGITUDE)));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12.0f));
        listenEvents();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        sTitle = null;
        sDescription = null;
        sName = null;
        sImage = null;
        sGenderAndAge = null;

        database.collection(Constants.KEY_COLLECTION_EVENTS).document(Objects.requireNonNull(marker.getTitle())).get()
                .addOnCompleteListener(eventsDetailListener);

        return true;
    }

    private OnCompleteListener<DocumentSnapshot> eventsDetailListener = task -> {
        if(task.isSuccessful() && task.getResult() != null){
            DocumentSnapshot documentSnapshot = task.getResult();
            sTitle = documentSnapshot.getString(Constants.KEY_CATEGORY);
            sDescription = documentSnapshot.getString(Constants.KEY_EVENT_DESCRIPTION);
            sName = documentSnapshot.getString(Constants.KEY_NAME);
            sImage = documentSnapshot.getString(Constants.KEY_IMAGE);
            String gender = documentSnapshot.getString(Constants.KEY_GENDER);
            String age = documentSnapshot.getString(Constants.KEY_AGE);
            sGenderAndAge = gender + ", " + age;

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                    MapsActivity.this, R.style.AppBottomSheetDialogTheme
            );
            View bottomSheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.bottom_sheet_layout, findViewById(R.id.bottomSheetContainer));
            TextView title = bottomSheetView.findViewById(R.id.title);
            title.setText(sTitle);
            TextView description = bottomSheetView.findViewById(R.id.description);
            description.setText(sDescription);
            TextView name = bottomSheetView.findViewById(R.id.creatorName);
            name.setText(sName);
            TextView genderAndAge = bottomSheetView.findViewById(R.id.creatorGenderAndAge);
            genderAndAge.setText(sGenderAndAge);
            ImageView image = bottomSheetView.findViewById(R.id.imageProfile);
            image.setImageBitmap(getUserImage(sImage));

            name.setOnClickListener(view -> {
                CheckProfile(documentSnapshot.getString(Constants.KEY_USER_ID));
            });

            image.setOnClickListener(view -> {
                CheckProfile(documentSnapshot.getString(Constants.KEY_USER_ID));
            });

            Button buttonChat = bottomSheetView.findViewById(R.id.buttonChat);
            if(Objects.equals(documentSnapshot.getString(Constants.KEY_USER_ID), preferenceManager.getString(Constants.KEY_USER_ID))){
                buttonChat.setText(R.string.delete);
                buttonChat.setOnClickListener(view -> {
                    DeleteEvent(documentSnapshot.getId(), bottomSheetDialog);
                });
            }
            else{
                buttonChat.setOnClickListener(view -> {
                    StartChat(documentSnapshot.getString(Constants.KEY_USER_ID));
                });
            }

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        }
    };

    private void DeleteEvent(String eventId, BottomSheetDialog bottomSheetDialog){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_EVENTS).document(eventId);
        documentReference.delete();
        bottomSheetDialog.hide();
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void DeleteEventDuringMapBuild(String eventId){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_EVENTS).document(eventId);
        documentReference.delete();
    }

    private void CheckProfile(String userId){
        database.collection(Constants.KEY_COLLECTION_USERS).document(Objects.requireNonNull(userId)).get()
                .addOnCompleteListener(userDetailListener);
    }

    private OnCompleteListener<DocumentSnapshot> userDetailListener = task -> {
        if(task.isSuccessful() && task.getResult() != null){
            DocumentSnapshot documentSnapshot = task.getResult();
            User user = new User();

            user.name = documentSnapshot.getString(Constants.KEY_NAME);
            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
            user.age = documentSnapshot.getString(Constants.KEY_AGE);
            user.gender = documentSnapshot.getString(Constants.KEY_GENDER);
            user.shortBio = documentSnapshot.getString(Constants.KEY_SHORT_BIO);
            user.id = documentSnapshot.getId();

            if(!user.id.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                Intent intent = new Intent(getApplicationContext(), CheckProfileActivity.class);
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                startActivity(intent);
            }
        }
    };

    private void StartChat(String userId){

        database.collection(Constants.KEY_COLLECTION_USERS).document(Objects.requireNonNull(userId)).get()
                .addOnCompleteListener(userChatListener);
    }

    private OnCompleteListener<DocumentSnapshot> userChatListener = task -> {
        if(task.isSuccessful() && task.getResult() != null){
            DocumentSnapshot documentSnapshot = task.getResult();
            User user = new User();

            user.name = documentSnapshot.getString(Constants.KEY_NAME);
            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
            user.age = documentSnapshot.getString(Constants.KEY_AGE);
            user.gender = documentSnapshot.getString(Constants.KEY_GENDER);
            user.shortBio = documentSnapshot.getString(Constants.KEY_SHORT_BIO);
            user.id = documentSnapshot.getId();

            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            intent.putExtra("chatFromMap", sTitle);
            startActivity(intent);
        }
    };

    private void setListeners(){

        binding.addActivity.setOnClickListener(v ->
                onAddActivityClicked());
        binding.confirmLocation.setOnClickListener(v ->
                        confirmEventLocation());
        binding.cancel.setOnClickListener(view -> {
            onCancelClicked();
        });
        binding.imageBack.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void confirmEventLocation(){
        binding.activityLocation.setVisibility(View.GONE);
        binding.confirmLocation.setVisibility(View.GONE);
        binding.cancel.setVisibility(View.GONE);
        binding.addActivity.setVisibility(View.VISIBLE);
        String sMyLat = Double.toString(mMap.getCameraPosition().target.latitude);
        String sMyLng = Double.toString(mMap.getCameraPosition().target.longitude);
        Intent intent = new Intent(getApplicationContext(), AddMapActivity.class);
        intent.putExtra("Latitude", sMyLat);
        intent.putExtra("Longitude", sMyLng);
        startActivity(intent);
    }

    private void onAddActivityClicked(){
        binding.activityLocation.setVisibility(View.VISIBLE);
        binding.confirmLocation.setVisibility(View.VISIBLE);
        binding.cancel.setVisibility(View.VISIBLE);
        binding.addActivity.setVisibility(View.GONE);
    }
    private void onCancelClicked(){
        binding.activityLocation.setVisibility(View.GONE);
        binding.confirmLocation.setVisibility(View.GONE);
        binding.cancel.setVisibility(View.GONE);
        binding.addActivity.setVisibility(View.VISIBLE);
    }


    private void listenEvents(){
        database.collection(Constants.KEY_COLLECTION_EVENTS)
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            long currentDate = new Date().getTime();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if ((documentChange.getType() == DocumentChange.Type.ADDED && distance(preferenceManager.getString(Constants.KEY_LATITUDE),
                        preferenceManager.getString(Constants.KEY_LONGITUDE),
                        Objects.requireNonNull(documentChange.getDocument().getString(Constants.KEY_EVENT_LATITUDE)),
                        Objects.requireNonNull(documentChange.getDocument().getString(Constants.KEY_EVENT_LONGITUDE))) < 20.0)
                        || (documentChange.getType() == DocumentChange.Type.ADDED && (preferenceManager.getString(Constants.KEY_USER_ID).equals(Objects.requireNonNull(documentChange.getDocument().getString(Constants.KEY_USER_ID)))))) {

                    if(isStillValid(currentDate, documentChange)){
                        LatLng eventLoc = new LatLng(Double.parseDouble(Objects.requireNonNull(documentChange.getDocument().getString(Constants.KEY_EVENT_LATITUDE))), Double.parseDouble(Objects.requireNonNull(documentChange.getDocument().getString(Constants.KEY_EVENT_LONGITUDE))));

                        MarkerOptions marker = new MarkerOptions().position(eventLoc).title(documentChange.getDocument().getId());
                        int markerIcon = getMarkerIcon(documentChange);

                        marker.icon(BitmapDescriptorFactory.fromResource(markerIcon));
                        mMap.addMarker(marker);
                    }
                    else{
                        DeleteEventDuringMapBuild(documentChange.getDocument().getId());
                    }

                }
            }
        }
    };

    private int getMarkerIcon(DocumentChange documentChange){
        String category = Objects.requireNonNull(documentChange.getDocument().getString(Constants.KEY_CATEGORY));
        switch (category){

            case "Bar":
                return R.drawable.bar;
            case "Badminton":
                return R.drawable.badminton;
            case "Bieganie":
                return R.drawable.jogging;
            case "Bilard":
                return R.drawable.billiard;
            case "Box":
                return R.drawable.boxing;
            case "Camping":
                return R.drawable.campingcar;
            case "Czytanie":
                return R.drawable.library;
            case "Domki letniskowe":
                return R.drawable.cabin;
            case "Fitness":
                return R.drawable.fitness;
            case "Frisbee":
                return R.drawable.frisbee;
            case "Gokarty":
                return R.drawable.karting;
            case "Grill":
                return R.drawable.barbecue;
            case "Gry karciane":
                return R.drawable.poker;
            case "Hiking":
                return R.drawable.hiking;
            case "Hockey":
                return R.drawable.icehockey;
            case "Informacja":
                return R.drawable.information;
            case "Jazda na koniu":
                return R.drawable.horseriding;
            case "Jazda na nartach":
                return R.drawable.skiing;
            case "Joga":
                return R.drawable.yoga;
            case "Kajaki":
                return R.drawable.kayaking;
            case "Kino":
                return R.drawable.cinema;
            case "Kitesurfing":
                return R.drawable.kitesurfing;
            case "Koszykówka":
                return R.drawable.basketball;
            case "Kręgle":
                return R.drawable.bowling;
            case "Las":
                return R.drawable.forest;
            case "Lody":
                return R.drawable.icecream;
            case "Łowienie ryb":
                return R.drawable.fishing;
            case "Łucznictwo":
                return R.drawable.archery;
            case "Łyżwy":
                return R.drawable.iceskating;
            case "Muzeum":
                return R.drawable.artmuseum;
            case "Na łonie natury":
                return R.drawable.cloudysunny;
            case "Nauka":
                return R.drawable.museumscience;
            case "Nordic skiing":
                return R.drawable.nordicskiing;
            case "Odpoczynek za miastem":
                return R.drawable.riparianhabitat;
            case "Piłka nożna":
                return R.drawable.soccer;
            case "Piłka ręczna":
                return R.drawable.handball;
            case "Piwo w plenerze":
                return R.drawable.beergarden;
            case "Pływanie":
                return R.drawable.swimming;
            case "Podwózka":
                return R.drawable.car;
            case "Pomoc/naprawa":
                return R.drawable.repair;
            case "Restauracja":
                return R.drawable.restaurant;
            case "Rolki":
                return R.drawable.rollerblade;
            case "Rowery":
                return R.drawable.cycling;
            case "Rowery górskie":
                return R.drawable.bikedownhill;
            case "Siatkówka":
                return R.drawable.volleyball;
            case "Siatkówka plażowa":
                return R.drawable.beachvolleyball;
            case "Siłownia":
                return R.drawable.weights;
            case "Skateboarding":
                return R.drawable.rollerskate;
            case "Snowboarding":
                return R.drawable.snowboarding;
            case "Squash":
                return R.drawable.squash;
            case "Szachy":
                return R.drawable.chess;
            case "Taniec":
                return R.drawable.danceclass;
            case "Teatr":
                return R.drawable.theater;
            case "Tenis":
                return R.drawable.tennis;
            case "Tenis stołowy":
                return R.drawable.tebletennis;
            case "Wesołe miasteczko":
                return R.drawable.ferriswheel;
            case "Wspinaczka":
                return R.drawable.climbing;
            case "Wyjście do klubu":
                return R.drawable.dancinghall;
            case "Wyjście na drinka":
                return R.drawable.barcoktail;
            case "Wypad nad morze":
                return R.drawable.lake;
            case "Wypad w góry":
                return R.drawable.mountains;
            case "Wyprowadzanie psa":
                return R.drawable.dogsleash;
            case "Zoo":
                return R.drawable.zoo;
            case "Zwiedzanie":
                return R.drawable.castle;
            case "Żeglowanie":
                return R.drawable.sailing;
            default:
                return R.drawable.other;
        }
    }

    private boolean isStillValid(long currentDate, DocumentChange documentChange){
        Date dtStart = Objects.requireNonNull(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
            long date = dtStart.getTime();
            long duration = 0;
            switch (Objects.requireNonNull(documentChange.getDocument().getString(Constants.KEY_DURATION))){
                case "1 godzina":
                    duration = 3600000;
                    break;
                case "3 godziny":
                    duration = 10800000;
                    break;
                case "5 godzin":
                    duration = 18000000;
                    break;
                case "12 godzin":
                    duration = 43200000;
                    break;
                case "24 godziny":
                    duration = 86400000;
                    break;
                case "2 dni":
                    duration = 172800000;
                    break;
                case "3 dni":
                    duration = 259200000;
                    break;
                case "7 dni":
                    duration = 604800000;
                    break;
                case "2 tygodnie":
                    duration = 1209600000;
                    break;
                default:
                    break;
            }
            return duration != 0 && (currentDate - date) < duration;
    }

    private double distance(@NonNull String slat1,@NonNull String slon1,@NonNull String slat2,@NonNull String slon2) {

        double lat1 = Double.parseDouble(slat1);
        double lon1 = Double.parseDouble(slon1);
        double lat2 = Double.parseDouble(slat2);
        double lon2 = Double.parseDouble(slon2);

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist/0.62137); //in kilometers
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        assert vectorDrawable != null;
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth()*2,
                vectorDrawable.getIntrinsicHeight()*2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
