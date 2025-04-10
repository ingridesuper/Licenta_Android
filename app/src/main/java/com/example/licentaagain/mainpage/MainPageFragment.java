package com.example.licentaagain.mainpage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.licentaagain.R;
import com.example.licentaagain.views.WorkaroundMapFragment;
import com.example.licentaagain.models.Problem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

public class MainPageFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap myMap;
    private ProblemViewModel problemViewModel;
    FragmentManager fragmentManager;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public MainPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeVariables();
        setupPermissionLauncher();
        getLastLocation();

    }

    private void initializeVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fragmentManager=getChildFragmentManager();
        problemViewModel = new ViewModelProvider(requireActivity()).get(ProblemViewModel.class);

    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                getLastLocation();
            } else {
                Toast.makeText(getActivity(), "Location permission is denied, please allow", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                Log.i("cur", currentLocation.getLongitude() + " " + currentLocation.getLatitude());
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(MainPageFragment.this);
                }
            } else {
                Log.i("cur", "Failed to retrieve location");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLastLocation();
        setUpCustomMapFragmentScroll(view);
        setUpProblemListFragment();
        observeMapMarkers();
        setUpSearchEvents(view);
    }

    private void setUpSearchEvents(View view) {
        SearchView searchView=view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                problemViewModel.searchDataTitleDescription(query);
                Log.i("search", query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    private void observeMapMarkers() {
        problemViewModel.getProblems().observe(getViewLifecycleOwner(), problems -> {
            if (myMap != null && currentLocation != null) {
                for (Problem problem : problems) {
                    LatLng location = new LatLng(problem.getLatitude(), problem.getLongitude());
                    myMap.addMarker(new MarkerOptions().position(location).title(problem.getTitle()));
                    updateMap();
                }
            }
        });
    }

    private void setUpProblemListFragment() {
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        ProblemListFragment problemListFragment=new ProblemListFragment();
        fragmentTransaction.add(R.id.problemListFragment, problemListFragment);
        fragmentTransaction.commit();
    }

    private void setUpCustomMapFragmentScroll(View view) {
        NestedScrollView mScrollView = view.findViewById(R.id.scrollView);
        WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.i("map found", "Map found and initialized");
            // set custom listener to prevent ScrollView interference when touching the map
            mapFragment.setListener(() -> mScrollView.requestDisallowInterceptTouchEvent(true));
        } else {
            Log.e("MainPageFragment", "Map fragment not found");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.myMap = googleMap;
        UiSettings uiSettings = myMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        }

        updateMap();
    }

    private void updateMap() {
        if (myMap != null && currentLocation != null) {
            LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 11));
        } else {
            Log.i("map", "Map or location not available yet");
        }
    }


}

/*
 * Initial Setup:
 * The fragment is created and its layout is inflated.
 * Permission Check:
 * The app checks if location permission is granted.
 * Location Fetching:
 * If permission is granted, the app retrieves the last known location using Google’s location service.
 * Map Initialization:
 * The map is loaded using SupportMapFragment.
 * Map Display:
 * The camera moves to the user’s current location.
 * Permission Handling:
 * If the user denies permission, a warning is shown via a Toast.
 */
