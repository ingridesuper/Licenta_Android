package com.example.licentaagain.problem;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_adapters.SelectedImagesAdapter;
import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.enums.StareProblema;
import com.example.licentaagain.mainpage.MainPageFragment;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.repositories.ProblemRepository;
import com.example.licentaagain.utils.ImagePickerHelper;
import com.example.licentaagain.views.WorkaroundMapFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddProblemFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap myMap;
    private ImagePickerHelper imagePickerHelper;
    private Place selectedPlace;
    private ScrollView scrollView;
    private RelativeLayout loadingOverlay;
    private TextInputEditText etTitle, etDescription, etFacebookLink;
    private Spinner spnSector, spnCategorie;
    private RecyclerView rvSelectedImages;
    private SelectedImagesAdapter selectedImagesAdapter;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private ProblemRepository problemRepository;


    public AddProblemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //sterge harcodare aici!
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyBbOOjwR9Eq3CGJnGZhg9fMssUBRFlMDpc");
        }

        imagePickerHelper = new ImagePickerHelper(this, selectedImageUris, new ImagePickerHelper.ImagePickerCallback() {
            @Override
            public void onImagesSelected(List<Uri> imageUris) {
                if (selectedImagesAdapter != null) {
                    selectedImagesAdapter.notifyDataSetChanged();
                    rvSelectedImages.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onImageCaptureFailed(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_problem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeVariables(view);
        imagePickerHelper.enableDragAndDrop(rvSelectedImages, selectedImagesAdapter);
        setupSpinners();
        setUpMapFragment(view);
        setUpAutocompleteFragment();
        btnSaveSubscribeToEvent(view);
        btnAddPicturesSubscribeToEvent(view);
        btnOpenCameraSubscribeToEvent(view);
    }

    private void btnOpenCameraSubscribeToEvent(View view) {
        Button btnTakePhoto=view.findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(v -> imagePickerHelper.openCamera());
    }
    private void btnAddPicturesSubscribeToEvent(View view) {
        Button btnAddPictures=view.findViewById(R.id.btnAddPictures);
        btnAddPictures.setOnClickListener(v->imagePickerHelper.openGallery());
    }

    private void addProblemToFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String authorUid = currentUser.getUid();
        String description = etDescription.getText().toString();
        String title = etTitle.getText().toString();
        int sector = ((Sector) spnSector.getSelectedItem()).getNumar();
        String category = String.valueOf(spnCategorie.getSelectedItem());
        String facebookGroupLink=etFacebookLink.getText().toString().isEmpty()?null:etFacebookLink.getText().toString();

        if (!checkUserInput(description, title, sector, category, selectedPlace)) {
            Toast.makeText(getContext(), "Nu ati completat tot ce este necesar", Toast.LENGTH_SHORT).show();
            return;
        }
        if(facebookGroupLink!=null && !facebookGroupLink.isEmpty() && !isValidFacebookGroupLink(facebookGroupLink)){
            Toast.makeText(getContext(), "Vă rugăm să introduceți un link de grup valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(selectedImageUris.isEmpty()){
            showToast("Va rugam atasati cel putin o imagine");
            return;
        }
        showLoadingOverlay(true);
        disableAllViews(scrollView);

        LatLng latLng = selectedPlace.getLocation();
        Problem problem = new Problem(
                selectedPlace.getDisplayName(),
                authorUid,
                description,
                latLng.latitude,
                latLng.longitude,
                sector,
                title,
                category,
                StareProblema.CURS_STRANGERE_SEMNATURI,
                facebookGroupLink
        );

        problemRepository.addProblem(problem, selectedImageUris, new ProblemRepository.ProblemCreationCallback() {
            @Override
            public void onSuccess() {
                showToast("Problem added");
                navigateBackToMainPage();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "Failed to add problem", e);
                showToast("Error adding problem"); //aici intra, dar se adauga in firebase si nu in storage; dupa da crash
                showLoadingOverlay(false);
            }
        });

    }
    private void btnSaveSubscribeToEvent(@NonNull View view) {
        Button btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v-> addProblemToFirebase());
    }

    public boolean isValidFacebookGroupLink(String facebookGroupLink) {
        if (facebookGroupLink == null || facebookGroupLink.isEmpty()) {
            return false;
        }
        String groupPrefix = "https://www.facebook.com/groups/";
        if (!facebookGroupLink.startsWith(groupPrefix)) {
            return false;
        }
        String afterPrefix = facebookGroupLink.substring(groupPrefix.length());
        return !afterPrefix.isEmpty();
    }


    private void setUpAutocompleteFragment() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
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
    private void showLoadingOverlay(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void disableAllViews(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                disableAllViews((ViewGroup) child);
            } else if (child instanceof Button || child instanceof TextView || child instanceof Spinner) {
                child.setEnabled(false);
            }
        }
    }

    private void initializeVariables(@NonNull View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        problemRepository=new ProblemRepository();
        spnCategorie=view.findViewById(R.id.spnCategorie);
        spnSector=view.findViewById(R.id.spnSector);
        etDescription=view.findViewById(R.id.etDescription);
        etTitle=view.findViewById(R.id.etTitle);
        rvSelectedImages = view.findViewById(R.id.rvSelectedImages);
        etFacebookLink=view.findViewById(R.id.etFacebookLink);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        selectedImagesAdapter = new SelectedImagesAdapter(getContext(), selectedImageUris);
        rvSelectedImages.setAdapter(selectedImagesAdapter);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        scrollView=view.findViewById(R.id.scrollView);
    }

    private void setupSpinners() {
        setupSpinner(spnSector, Sector.values());
        setupSpinner(spnCategorie, CategorieProblema.values());
    }

    private <T> void setupSpinner(Spinner spinner, T[] items) {
        ArrayAdapter<T> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, items);
        spinner.setAdapter(adapter);
    }

    private void showToast(String message) {
        Activity activity = getActivity();
        if (activity != null) {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBackToMainPage() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();  // revine la fragmentul anterior fara a crea unul nou
            //cred ca aucu mai bn te uiti la cum il pornesti si alegi una (mai tarziu)
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, new MainPageFragment())
                    .commit();
        }
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bmHome);
    }

    private boolean checkUserInput(String description, String title, int sector, String category, Place selectedPlace) {
        if (title.isEmpty()) {
            etTitle.setError(String.valueOf(R.string.required_title_error_message));
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError(String.valueOf(R.string.required_description_error_message));
            return false;
        }

        if (sector <= 0) {
            Toast.makeText(getActivity(), String.valueOf(R.string.required_sector_error_message), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (category == null || category.isEmpty()) {
            Toast.makeText(getActivity(), String.valueOf(R.string.required_category_error_message), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedPlace == null) {
            Toast.makeText(getActivity(), String.valueOf(R.string.required_location_error_message), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        //setez poz originala pe Bucuresti
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.4268, 26.1025), 12));

        UiSettings uiSettings= myMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        Log.i("AddProblemFragment", "Map is ready");
    }
}