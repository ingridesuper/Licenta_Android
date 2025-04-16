package com.example.licentaagain.problem;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_adapters.SelectedImagesAdapter;
import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.views.WorkaroundMapFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditProblemFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap myMap;
    private Uri cameraImageUri;
    private Place selectedPlace;
    private AutocompleteSupportFragment autocompleteFragment;

    private Problem problem;
    private RecyclerView rvSelectedImages;
    private ScrollView scrollView;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private final int MAX_IMAGES = 5;
    private FirebaseFirestore db;
    private TextInputEditText etTitle, etDescription;
    private Spinner spnSector, spnCategorie;
    private RelativeLayout loadingOverlay;
    private SelectedImagesAdapter selectedImagesAdapter;
    private ActivityResultLauncher<Uri> takePictureLauncher;


    public EditProblemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sterge harcodare aici!
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyBbOOjwR9Eq3CGJnGZhg9fMssUBRFlMDpc");
        }

        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
        }

        addImageUploadSupport();
        registerCameraLauncher();
    }

    private void initializeVariables(@NonNull View view) {
        db= FirebaseFirestore.getInstance();
        spnCategorie=view.findViewById(R.id.spnCategorie);
        spnSector=view.findViewById(R.id.spnSector);
        etDescription=view.findViewById(R.id.etDescription);
        etTitle=view.findViewById(R.id.etTitle);
        rvSelectedImages = view.findViewById(R.id.rvSelectedImages);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        selectedImagesAdapter = new SelectedImagesAdapter(getContext(), selectedImageUris);
        rvSelectedImages.setAdapter(selectedImagesAdapter);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        scrollView=view.findViewById(R.id.scrollView);
    }

    private void addImageUploadSupport() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ClipData clipData = result.getData().getClipData();
                        selectedImageUris.clear();

                        if (clipData != null) {
                            int count = Math.min(clipData.getItemCount(), MAX_IMAGES);
                            for (int i = 0; i < count; i++) {
                                selectedImageUris.add(clipData.getItemAt(i).getUri());
                            }
                        } else {
                            selectedImageUris.add(result.getData().getData());
                        }
                        selectedImagesAdapter.notifyDataSetChanged();
                        rvSelectedImages.setVisibility(View.VISIBLE);
                    }
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_problem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeVariables(view);
        enableDragAndDropPictures();
        setUpAutocompleteFragment();
        setupSpinners();
        setUpMapFragment(view);
        fillUiWithProblemData(view);


        //btnAddPicturesSubscribeToEvent(view);
        //btnOpenCameraSubscribeToEvent(view);

    }
    private void enableDragAndDropPictures() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                Collections.swap(selectedImageUris, fromPosition, toPosition);
                selectedImagesAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // usually used for deleting -> we already did the x button
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSelectedImages);
    }

    private void setUpAutocompleteFragment() {
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION));
        autocompleteFragment.setHint(getString(R.string.search_place));

        // Handle the place selection
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                selectedPlace=place;
                LatLng latLng = place.getLocation();
                if (latLng != null) {
                    myMap.clear();
                    myMap.addMarker(new MarkerOptions().position(latLng).title(place.getDisplayName()));
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    Log.i("Selected Place", "Place: " + place.getDisplayName() + ", LatLng: " + latLng);
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("Autocomplete Error", "Error: " + status.getStatusMessage());
            }
        });
    }


    private void registerCameraLauncher() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && cameraImageUri != null) {
                        if (selectedImageUris.size() < MAX_IMAGES) {
                            selectedImageUris.add(cameraImageUri);
                            selectedImagesAdapter.notifyDataSetChanged();
                            rvSelectedImages.setVisibility(View.VISIBLE);
                        } else {
                            showToast("Maximum number of images reached");
                        }
                    }
                }
        );

    }

    private void setupSpinners() {
        setupSpinner(spnSector, Sector.values());
        setupSpinner(spnCategorie, CategorieProblema.values());
    }

    private <T> void setupSpinner(Spinner spinner, T[] items) {
        ArrayAdapter<T> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        spinner.setAdapter(adapter);
    }

    private void setUpMapFragment(View view) {
        ScrollView mScrollView = view.findViewById(R.id.scrollView);
        WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.i("map found", "Map found and initialized");
            mapFragment.setListener(() -> mScrollView.requestDisallowInterceptTouchEvent(true));
        } else {
            Log.e("map not found", "Map fragment not found");
        }
    }

    private void fillUiWithProblemData(View view) {
        etTitle.setText(problem.getTitle());
        etDescription.setText(problem.getDescription());
        int sectorNumar = problem.getSector();
        Sector selectedSector = null;
        for (Sector s : Sector.values()) {
            if (s.getNumar() == sectorNumar) {
                selectedSector = s;
                break;
            }
        }
        if (selectedSector != null) {
            ArrayAdapter<Sector> adapter = (ArrayAdapter<Sector>) spnSector.getAdapter();
            int position = adapter.getPosition(selectedSector);
            spnSector.setSelection(position);
        }

        String categorieText = problem.getCategorieProblema();
        CategorieProblema selectedCategorie = null;
        for (CategorieProblema c : CategorieProblema.values()) {
            if (c.getCategorie().equalsIgnoreCase(categorieText)) {
                selectedCategorie = c;
                break;
            }
        }
        if (selectedCategorie != null) {
            ArrayAdapter<CategorieProblema> categorieAdapter = (ArrayAdapter<CategorieProblema>) spnCategorie.getAdapter();
            int categoriePosition = categorieAdapter.getPosition(selectedCategorie);
            spnCategorie.setSelection(categoriePosition);
        }

        autocompleteFragment.setHint(problem.getAddress());
        List<String> problemImageUrls = problem.getImageUrls();
        if (problemImageUrls != null) {
            selectedImageUris.clear();
            for (String url : problemImageUrls) {
                selectedImageUris.add(Uri.parse(url));
            }
            selectedImagesAdapter.notifyDataSetChanged();
            rvSelectedImages.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng location = new LatLng(problem.getLatitude(), problem.getLongitude());
        myMap.addMarker(new MarkerOptions().position(location).title(problem.getTitle()));
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

        UiSettings uiSettings= myMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        Log.i("EditProblemFragment", "Map is ready");
    }

    private void showToast(String message) {
        Activity activity = getActivity();
        if (activity != null) {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    }

}