package com.example.final_user_uber.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.final_user_uber.Common.Common;
import com.example.final_user_uber.R;
import com.example.final_user_uber.Remote.IGoogleAPI;
import com.example.final_user_uber.Remote.RetrofitClient;
import com.example.final_user_uber.model.EvenBus.DriverRequestReceived;
import com.example.final_user_uber.model.EvenBus.NotifytoRiderEvent;
import com.example.final_user_uber.model.RiderModel;
import com.example.final_user_uber.model.TripPlanModel;
import com.example.final_user_uber.utils.UserUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kusu.loadingbutton.LoadingButton;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    //Routers
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;

    private DriverRequestReceived driverRequestReceived;
    private Disposable countDownEvent; // countdown circle bar

    SupportMapFragment mapFragment;
    //Online system
    DatabaseReference onlineRef, currentUserRef, driversLocationRef;
    GeoFire geoFire;

    private GoogleMap mMap;
    private HomeViewModel homeViewModel;

    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isFirstTime = true;
    private boolean isTripStart = false, onlineSystemAlreadyRegister = false;

    //Decline
    @BindView(R.id.chip_decline)
    Chip chip_decline;
    @BindView(R.id.layout_accept)
    CardView layout_accept;
    @BindView(R.id.circurlarprogessbar)
    CircularProgressBar circularProgressBar;
    @BindView(R.id.txt_estimate_time)
    TextView estimate_time;
    @BindView(R.id.txt_estimate_distance)
    TextView estimate_distance;
    @BindView(R.id.root_layout)
    FrameLayout root_layout;

    //accepct
    @BindView(R.id.txt_rating)
    TextView txt_rating;
    @BindView(R.id.txt_type_uber)
    TextView txt_type_uber;
    @BindView(R.id.img_round)
    ImageView img_round;
    @BindView(R.id.layout_start_uber)
    CardView layout_start_uber;
    @BindView(R.id.txt_rider_name)
    TextView txt_rider_name;
    @BindView(R.id.txt_start_uber_estimate_distance)
    TextView txt_start_uber_estimate_distance;
    @BindView(R.id.txt_start_uber_estimate_time)
    TextView txt_start_uber_estimate_time;
    @BindView(R.id.img_phone_call)
    ImageView img_phone_call;
    @BindView(R.id.loadingButton)
    LoadingButton loadingButton;

    //progessbar notify
    @BindView(R.id.layout_notify_rider)
    LinearLayout layout_notify_rider;
    @BindView(R.id.txt_notify_rider)
    TextView txt_notify_rider;
    @BindView(R.id.progress_notify)
    ProgressBar progress_notify;

    //Complete trip
    @BindView(R.id.loadingButton_complete_trip)
    LoadingButton loadingButton_complete_trip;

    private String tripNumberID = "";
    private static String mRiderPhoneNumber = "";

    private GeoFire pickupGeoFire, destinationGeoFire;
    private GeoQuery pickupGeoQuery, destinationGeoQuery;

    private GeoQueryEventListener pickupGeoQueryListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            Log.d("QuocDev", "Get to pickup");
            Log.d("QuocDev", key);
            loadingButton.setEnabled(true);  // When driver arrived pickup location, they can start the Trip
            UserUtils.sendNotifyToRider(getContext(), root_layout, key);

            if (pickupGeoQuery != null) {
                //Remove GeoFire
                Log.d("QuocDev", "pickup not null");
                pickupGeoFire.removeLocation(key);
                pickupGeoFire = null;
                pickupGeoQuery.removeAllListeners();
            }
        }

        @Override
        public void onKeyExited(String key) {
            loadingButton.setEnabled(false);
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    };
    private GeoQueryEventListener destinationGeoQueryListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {

            loadingButton_complete_trip.setEnabled(true);  // When driver arrived pickup location, they can start the Trip

            if (destinationGeoQuery != null) {
                //Remove GeoFire
                Log.d("QuocDev", "destination not null");
                destinationGeoFire.removeLocation(key);
                destinationGeoFire = null;
                destinationGeoQuery.removeAllListeners();
            }
        }

        @Override
        public void onKeyExited(String key) {

        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {

        }

        @Override
        public void onGeoQueryReady() {

        }

        @Override
        public void onGeoQueryError(DatabaseError error) {

        }
    };

    private CountDownTimer waiting_time;

    @OnClick(R.id.chip_decline)
        //Decline click
    void onDeclineClick() {
        if (driverRequestReceived != null) {
            if (TextUtils.isEmpty(tripNumberID)) {
                if (countDownEvent != null)
                    countDownEvent.dispose();
                chip_decline.setVisibility(View.GONE);
                layout_accept.setVisibility(View.GONE);
                mMap.clear();
                UserUtils.sendDeclineRequest(root_layout, getContext(), driverRequestReceived.getKey());
                driverRequestReceived = null;

            } else {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(e -> {
                            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }).addOnSuccessListener(location -> {
                    chip_decline.setVisibility(View.GONE);
                    layout_start_uber.setVisibility(View.GONE);
                    mMap.clear();
                    UserUtils.sendDeclineAndRemoveTripRequest(root_layout, getContext(),
                            driverRequestReceived.getKey(), tripNumberID);
                    tripNumberID = ""; // Set tripID to null
                    driverRequestReceived = null;
                    makeDriverOnline(location);
                });

            }

        }
    }

    @OnClick(R.id.img_phone_call)
        //Call click
    void onCallPhone() {
        if (layout_start_uber.getVisibility() == View.VISIBLE
                && !mRiderPhoneNumber.isEmpty()) {
            try {
                if (this.getActivity().checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent callPhone = new Intent(Intent.ACTION_CALL);
                    callPhone.setData(Uri.parse("tel:" + mRiderPhoneNumber));
                    this.getActivity().startActivity(callPhone);
                } else {
                    getActivity().requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1111);
                }
            } catch (Exception e) {
                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.loadingButton)
        //Start click
    void onStartUberClick() {
        //Clear Route
        if (blackPolyline != null) blackPolyline.remove();
        if (greyPolyline != null) greyPolyline.remove();

        //cancel waiting time
        if (waiting_time != null) waiting_time.cancel();
        layout_notify_rider.setVisibility(View.GONE);
        if (driverRequestReceived != null) {
            LatLng destinationLatLng = new LatLng(
                    Double.parseDouble(driverRequestReceived.getDestinationLocation().split(",")[0]),
                    Double.parseDouble(driverRequestReceived.getDestinationLocation().split(",")[1])
            );
            mMap.addMarker(new MarkerOptions()
                    .position(destinationLatLng)
                    .title(driverRequestReceived.getDestinationLocationString())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            //draw path
            drawPathFromCurrentLocation(driverRequestReceived.getDestinationLocation());
        }
        loadingButton.setVisibility(View.GONE);
        chip_decline.setVisibility(View.GONE);
        loadingButton_complete_trip.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.loadingButton_complete_trip)
        //Complete Click
    void onCompleteUberClick() {
        Toast.makeText(getContext(), "Complete Trip Test action", Toast.LENGTH_SHORT)
                .show();
    }

    private void drawPathFromCurrentLocation(String destinationLocation) {
        //get current location
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(requireView(), e.getMessage(), Snackbar.LENGTH_LONG).show())
                .addOnSuccessListener(location -> {
                    //request an api
                    compositeDisposable.add(iGoogleAPI.getDirections("driving",
                            "less_driving",
                            new StringBuilder()
                                    .append(location.getLatitude())
                                    .append(",")
                                    .append(location.getLongitude())
                                    .toString(),
                            destinationLocation,
                            getString(R.string.google_api_key))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(returnResult -> {

                                try {
                                    //parse json
                                    JSONObject jsonObject = new JSONObject(returnResult);
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyline = poly.getString("points");
                                        polylineList = Common.decodePoly(polyline);

                                    }
                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.YELLOW);
                                    polylineOptions.width(12);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polylineList);
                                    greyPolyline = mMap.addPolyline(polylineOptions);

                                    blackPolylineOptions = new PolylineOptions();
                                    blackPolylineOptions.color(Color.BLACK);
                                    blackPolylineOptions.width(5);
                                    blackPolylineOptions.startCap(new SquareCap());
                                    blackPolylineOptions.jointType(JointType.ROUND);
                                    blackPolylineOptions.addAll(polylineList);
                                    blackPolyline = mMap.addPolyline(blackPolylineOptions);


                                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                                    LatLng destination = new LatLng(Double.parseDouble(destinationLocation.split(",")[0]),
                                            Double.parseDouble(destinationLocation.split(",")[1]));

                                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                            .include(origin)
                                            .include(destination)
                                            .build();

                                    createGeoFireDestinationLocation(driverRequestReceived.getKey(), destination);


                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 2));

                                } catch (Exception e) {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            })
                    );
                });
    }

    private void createGeoFireDestinationLocation(String key, LatLng destination) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(Common.TRIP_DESTINATION_LOCATION_REF);
        destinationGeoFire = new GeoFire(ref);
        destinationGeoFire.setLocation(key, new GeoLocation(destination.latitude, destination.longitude)
                , new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null)
                            Snackbar.make(root_layout, error.getMessage(), Snackbar.LENGTH_LONG)
                                    .show();
                    }
                });


    }

    ValueEventListener onlineValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && currentUserRef != null) {
                currentUserRef.onDisconnect().removeValue();
                isFirstTime = true;
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    };


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        initView(root);
        init();

        //Obtain the SupportMapFragment and get notified when the map is ready to used
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return root;
    }

    private void initView(View root) {
        ButterKnife.bind(this, root);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueListener);

        if (EventBus.getDefault().hasSubscriberForEvent(DriverRequestReceived.class))
            EventBus.getDefault().removeStickyEvent(DriverRequestReceived.class);

        if (EventBus.getDefault().hasSubscriberForEvent(NotifytoRiderEvent.class))
            EventBus.getDefault().removeStickyEvent(NotifytoRiderEvent.class);
        EventBus.getDefault().unregister(this);

        compositeDisposable.clear();

        onlineSystemAlreadyRegister = false;
    }

    @Override
    public void onStart() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onResume() {
        registerOnlineSystem();
        super.onResume();
    }

    private void registerOnlineSystem() {
        if (!onlineSystemAlreadyRegister) {
            onlineRef.addValueEventListener(onlineValueListener);
            onlineSystemAlreadyRegister = true;
        }
    }

    private void init() {

        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);

        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        buildLocationRequest();

        buildLocationCallback();

        updateLocation();

    }

    private void updateLocation() {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void buildLocationCallback() {
        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude()
                            , locationResult.getLastLocation().getLongitude());

                    //Pickup GeoQuery
                    if (pickupGeoFire != null) // that mean geoFire has been create on Firebase
                    {
                        Log.d("QuocDev", "Pickup GeoCoder Not null");

                        pickupGeoQuery =
                                pickupGeoFire.queryAtLocation(new GeoLocation(locationResult.getLastLocation().getLatitude(),
                                        locationResult.getLastLocation().getLongitude()), Common.MIN_RANGE_PICKUP_IN_KM);
                        pickupGeoQuery.addGeoQueryEventListener(pickupGeoQueryListener);

                    }

                    //Destination GeoQuery
                    if (destinationGeoFire != null) // that mean geoFire has been create on Firebase
                    {
                        Log.d("QuocDev", "Destination GeoCoder Not null");

                        destinationGeoQuery =
                                destinationGeoFire.queryAtLocation(new GeoLocation(locationResult.getLastLocation().getLatitude(),
                                        locationResult.getLastLocation().getLongitude()), Common.MIN_RANGE_PICKUP_IN_KM);
                        destinationGeoQuery.addGeoQueryEventListener(destinationGeoQueryListener);

                    }


                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));


                    if (!isTripStart) {

                        // Location follow CiTyName
                        Context context;
                            makeDriverOnline(locationResult.getLastLocation());
                    } else {
                        if (!TextUtils.isEmpty(tripNumberID)) {
                            //update location of driver
                            Map<String, Object> update_data = new HashMap<>();
                            update_data.put("currentLat", locationResult.getLastLocation().getLatitude());
                            update_data.put("currentLng", locationResult.getLastLocation().getLongitude());

                            FirebaseDatabase.getInstance()
                                    .getReference(Common.Trip)
                                    .child(tripNumberID)
                                    .updateChildren(update_data)
                                    //.updateChildren(update_data)
                                    .addOnFailureListener(e ->
                                            Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG)
                                                    .show())
                                    .addOnSuccessListener(aVoid -> {

                                    });
                        }
                    }
                }
            };
        }
    }

    private void makeDriverOnline(Location location) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocation(location.getLatitude()
                    , location.getLongitude(), 1);
            String cityName = addressList.get(0).getSubAdminArea();

            driversLocationRef = FirebaseDatabase.getInstance().getReference(Common.DRIVER_LOCATION_REFERENCE)
                    .child(cityName);
            currentUserRef = driversLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            geoFire = new GeoFire(driversLocationRef);

            //Update location
            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    new GeoLocation(location.getLatitude(),
                            location.getLongitude()),
                    (key, error) -> {
                        if (error != null)
                            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                                    .show();

                    });
            registerOnlineSystem();

        } catch (IOException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void buildLocationRequest() {
        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setSmallestDisplacement(50f); // 50m
            locationRequest.setInterval(15000); //15sec
            locationRequest.setFastestInterval(10000); //10sec
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Check Permission
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(getView(), getString(R.string.permisson_require), Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                                    .addOnSuccessListener(location -> {
                                        LatLng userLacing = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLacing, 18f));
                                    });
                            return true;
                        });

                        //set layout buttons
                        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        //Right bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0, 0, 0, 50);


                        //Move Location
                        buildLocationRequest();

                        buildLocationCallback();

                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(), "Permission" + permissionDeniedResponse.getPermissionName() + "" +
                                "was denied!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.uber_maps_style));
            if (!success)
                Log.e("STYLE ERROR", "Style parsing error");
        } catch (Resources.NotFoundException e) {
            Log.e("STYLE ERROR", e.getMessage());
        }

        Snackbar.make(mapFragment.getView(), "You're Online", Snackbar.LENGTH_LONG)
                .show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverRequestReceive(DriverRequestReceived event) {


        driverRequestReceived = event;

        //get current location
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(requireView(), e.getMessage(), Snackbar.LENGTH_LONG).show())
                .addOnSuccessListener(location -> {
                    //request an api
                    compositeDisposable.add(iGoogleAPI.getDirections("driving",
                            "less_driving",
                            new StringBuilder()
                                    .append(location.getLatitude())
                                    .append(",")
                                    .append(location.getLongitude())
                                    .toString(),
                            event.getPickupLocation(),
                            getString(R.string.google_api_key))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(returnResult -> {

                                try {
                                    //parse json
                                    JSONObject jsonObject = new JSONObject(returnResult);
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyline = poly.getString("points");
                                        polylineList = Common.decodePoly(polyline);

                                    }
                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.YELLOW);
                                    polylineOptions.width(12);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polylineList);
                                    greyPolyline = mMap.addPolyline(polylineOptions);

                                    blackPolylineOptions = new PolylineOptions();
                                    blackPolylineOptions.color(Color.BLACK);
                                    blackPolylineOptions.width(5);
                                    blackPolylineOptions.startCap(new SquareCap());
                                    blackPolylineOptions.jointType(JointType.ROUND);
                                    blackPolylineOptions.addAll(polylineList);
                                    blackPolyline = mMap.addPolyline(blackPolylineOptions);


                                    //Animator
                                    ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                                    valueAnimator.setDuration(1100);
                                    valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                                    valueAnimator.setInterpolator(new LinearInterpolator());
                                    valueAnimator.addUpdateListener(value -> {
                                        List<LatLng> points = greyPolyline.getPoints();
                                        int percentValue = (int) value.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 100.0f));
                                        List<LatLng> p = points.subList(0, newPoints);
                                        blackPolyline.setPoints(p);
                                    });

                                    valueAnimator.start();

                                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                                    LatLng destination = new LatLng(Double.parseDouble(event.getPickupLocation().split(",")[0]),
                                            Double.parseDouble(event.getPickupLocation().split(",")[1]));

                                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                            .include(origin)
                                            .include(destination)
                                            .build();

                                    //add car icon for origin
                                    JSONObject object = jsonArray.getJSONObject(0);
                                    JSONArray legs = object.getJSONArray("legs");
                                    JSONObject legObjects = legs.getJSONObject(0);

                                    JSONObject time = legObjects.getJSONObject("duration");
                                    String duration = time.getString("text");

                                    JSONObject distanceEstimateTime = legObjects.getJSONObject("distance");
                                    String distance = distanceEstimateTime.getString("text");


                                    estimate_time.setText(duration);
                                    estimate_distance.setText(distance);


                                    mMap.addMarker(new MarkerOptions()
                                            .position(destination)
                                            .icon(BitmapDescriptorFactory.defaultMarker())
                                            .title("Pickup Location"));

                                    createGeoFirePickupLocation(event.getKey(), destination);

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 2));

                                    //Show Layout
                                    chip_decline.setVisibility(View.VISIBLE);
                                    layout_accept.setVisibility(View.VISIBLE);

                                    //Count down
                                    countDownEvent = Observable.interval(100, TimeUnit.MILLISECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnNext(x -> {
                                                circularProgressBar.setProgress(circularProgressBar.getProgress() + 1f);
                                            })
                                            .takeUntil(aLong -> aLong == 100) // 10 sec
                                            .doOnComplete(() -> {
                                                circularProgressBar.setProgress(0);

                                                //Trip Plane Creating
                                                createTripPlan(event, duration, distance);
                                            }).subscribe();

                                } catch (Exception e) {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            })
                    );
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1111) {
                onCallPhone();
            }
        }

    }

    private void createGeoFirePickupLocation(String key, LatLng destination) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(Common.TRIP_PICKUP_REF);
        pickupGeoFire = new GeoFire(ref);
        pickupGeoFire.setLocation(key, new GeoLocation(destination.latitude, destination.longitude)
                , new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null)
                            Snackbar.make(root_layout, error.getMessage(), Snackbar.LENGTH_LONG)
                                    .show();
                        else
                            Log.d("QuocDev", key + " was create success on GeoFire!!");
                    }
                });
    }

    private void createTripPlan(DriverRequestReceived event, String duration, String distance) {
        setProcessLayout(true);

        //sync server time with device
        FirebaseDatabase.getInstance()
                .getReference(".info/serverTimeOffset")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        long timeOffset = snapshot.getValue(Long.class);

                        FirebaseDatabase.getInstance()
                                .getReference(Common.RIDER_INFO)
                                .child(event.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            RiderModel riderModel = snapshot.getValue(RiderModel.class);

                                            //get rider location
                                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                                return;
                                            }
                                            fusedLocationProviderClient.getLastLocation()
                                                    .addOnFailureListener(e -> {
                                                        Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG)
                                                                .show();
                                                    })
                                                    .addOnSuccessListener(location -> {

                                                        //Create Trip Planner
                                                        TripPlanModel tripPlanModel = new TripPlanModel();
                                                        tripPlanModel.setDriver(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                        tripPlanModel.setRider(event.getKey());

                                                        tripPlanModel.setDriverInfoModel(Common.currentUser);
                                                        tripPlanModel.setRiderModel(riderModel);

                                                        tripPlanModel.setOrigin(event.getPickupLocation());
                                                        tripPlanModel.setOriginString(event.getPickupLocationString());

                                                        tripPlanModel.setDestination(event.getDestinationLocation());
                                                        tripPlanModel.setDestinationString(event.getDestinationLocationString());

                                                        tripPlanModel.setDistancePickup(distance);
                                                        tripPlanModel.setDurationPickup(duration);

                                                        tripPlanModel.setCurrentLat(location.getLatitude());
                                                        tripPlanModel.setCurrentLng(location.getLongitude());


                                                        tripNumberID = Common.createUniqueTripIdNumber(timeOffset);

                                                        if (tripPlanModel.getRiderModel() != null) {
                                                            mRiderPhoneNumber = tripPlanModel.getRiderModel().getPhonenumber() == null
                                                                    ? "" : tripPlanModel.getRiderModel().getPhonenumber();
                                                        }

                                                        FirebaseDatabase.getInstance()
                                                                .getReference(Common.Trip)
                                                                .child(tripNumberID)
                                                                .setValue(tripPlanModel)
                                                                .addOnFailureListener(e ->
                                                                        Snackbar.make(mapFragment.getView(), getContext().
                                                                                        getString(R.string.rider_not_found) + "" + event.getKey()
                                                                                , Snackbar.LENGTH_LONG).show())
                                                                .addOnSuccessListener(unused -> {
                                                                    txt_rider_name.setText(riderModel.getFisrtnasme());
                                                                    txt_start_uber_estimate_distance.setText(distance);
                                                                    txt_start_uber_estimate_time.setText(duration);

                                                                    setOfflineModeForDriver(event, duration, distance);

                                                                });

                                                    });

                                        } else
                                            Snackbar.make(mapFragment.getView(), getContext().getString(R.string.rider_not_found) + "" + event.getKey()
                                                    , Snackbar.LENGTH_LONG)
                                                    .show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                        Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                                                .show();

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                                .show();

                    }
                });
    }

    private void setOfflineModeForDriver(DriverRequestReceived event, String duration, String distance) {

        UserUtils.sendAcceptRequestToRider(mapFragment.getView(), getContext(), event.getKey(), tripNumberID);

        //go to offline
        if (currentUserRef != null)
            currentUserRef.removeValue();

        setProcessLayout(false);
        layout_accept.setVisibility(View.GONE);
        layout_start_uber.setVisibility(View.VISIBLE);

        isTripStart = true;

    }

    private void setProcessLayout(boolean isProcess) {
        int color = -1;

        if (isProcess) {
            color = ContextCompat.getColor(getContext(), R.color.dark_gray);
            circularProgressBar.setIndeterminateMode(true);
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_star_24_dark_gray, 0);

        } else {
            color = ContextCompat.getColor(getContext(), R.color.white);
            circularProgressBar.setIndeterminateMode(false);
            circularProgressBar.setProgress(0);
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_stars_24, 0);
        }
        txt_start_uber_estimate_time.setTextColor(color);
        txt_start_uber_estimate_distance.setTextColor(color);
        ImageViewCompat.setImageTintList(img_round, ColorStateList.valueOf(color));
        txt_rating.setTextColor(color);
        txt_type_uber.setTextColor(color);
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onNotifyToRider(NotifytoRiderEvent event) {
        Log.d("QuocDev", "ProgessBar");

        layout_notify_rider.setVisibility(View.VISIBLE);
        progress_notify.setMax(Common.WAIT_TIME_IN_MIN * 60);
        waiting_time = new CountDownTimer(Common.WAIT_TIME_IN_MIN * 60 * 1000, 1000) {
            @Override
            public void onTick(long l) {
                progress_notify.setProgress(progress_notify.getProgress() + 1);

                txt_notify_rider.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l)),
                        TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));


            }

            @Override
            public void onFinish() {
                Snackbar.make(root_layout, getString(R.string.time_over), Snackbar.LENGTH_LONG)
                        .show();

            }
        }.start();
    }
}
