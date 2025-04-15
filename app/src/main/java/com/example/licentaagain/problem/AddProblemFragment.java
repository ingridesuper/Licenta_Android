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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentaagain.R;
import com.example.licentaagain.custom_adapters.SelectedImagesAdapter;
import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.mainpage.MainPageFragment;
import com.example.licentaagain.models.Problem;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AddProblemFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap myMap;
    private Place selectedPlace;
    private ScrollView scrollView;
    private FirebaseFirestore db;
    private Button btnSave;
    private RelativeLayout loadingOverlay;
    private TextInputEditText etTitle, etDescription;
    private Spinner spnSector, spnCategorie;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private RecyclerView rvSelectedImages;
    private SelectedImagesAdapter selectedImagesAdapter;
    private Uri cameraImageUri;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private final int MAX_IMAGES = 5;
    private String currentProblemId;


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

        addImageUploadSupport();
        registerCameraLauncher();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_problem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeVariables(view);
        enableDragAndDropPictures();
        setupSpinners();
        setUpMapFragment(view);
        setUpAutocompleteFragment();
        btnSaveSubscribeToEvent(view);
        btnAddPicturesSubscribeToEvent(view);
        btnOpenCameraSubscribeToEvent(view);
    }

    private void btnOpenCameraSubscribeToEvent(View view) {
        Button btnTakePhoto=view.findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(v -> {
            if (selectedImageUris.size() >= MAX_IMAGES) {
                showToast("Maximum number of images reached");
                return;
            }

            File imageFile;
            try {
                imageFile = File.createTempFile("IMG_", ".jpg", requireContext().getCacheDir());
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Could not create image file");
                return;
            }

            cameraImageUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    imageFile
            );

            takePictureLauncher.launch(cameraImageUri);
        });
    }

    private void btnAddPicturesSubscribeToEvent(View view) {
        Button btnAddPictures=view.findViewById(R.id.btnAddPictures);
        btnAddPictures.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });
    }
    private void uploadSelectedImages(String problemId, Runnable onComplete) {
        List<Task<?>> uploadTasks = new ArrayList<>();

        for (Uri imageUri : selectedImageUris) {
            String filename = UUID.randomUUID().toString() + ".jpg";
            StorageReference imgRef = FirebaseStorage.getInstance()
                    .getReference("problems/" + problemId + "/images/" + filename);

            Task<Uri> uploadTask = imgRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imgRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUri -> {
                        db.collection("problems")
                                .document(problemId)
                                .update("imageUrls", FieldValue.arrayUnion(downloadUri.toString()));
                    });

            uploadTasks.add(uploadTask);
        }

        Tasks.whenAllSuccess(uploadTasks)
                .addOnSuccessListener(results -> onComplete.run())
                .addOnFailureListener(e -> {
                    showToast("Problem upload failed");
                    onComplete.run();
                });
    }


    private void btnSaveSubscribeToEvent(@NonNull View view) {
        btnSave= view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v-> addProblemToFirebase());
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
        db=FirebaseFirestore.getInstance();
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

    private void setupSpinners() {
        setupSpinner(spnSector, Sector.values());
        setupSpinner(spnCategorie, CategorieProblema.values());
    }

    private <T> void setupSpinner(Spinner spinner, T[] items) {
        ArrayAdapter<T> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        spinner.setAdapter(adapter);
    }

    private void addProblemToFirebase() {
        showLoadingOverlay(true);
        disableAllViews(scrollView);

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

        if (!checkUserInput(description, title, sector, category, selectedPlace)) {
            return;
        }

        LatLng latLng = selectedPlace.getLocation();
        Problem problem = new Problem(
                selectedPlace.getDisplayName(),
                authorUid,
                description,
                latLng.latitude,
                latLng.longitude,
                sector,
                title,
                category
        );

        db.collection("problems").add(problem)
                .addOnSuccessListener(documentReference -> {
                    currentProblemId = documentReference.getId();
                    documentReference.update("createDate", FieldValue.serverTimestamp());

                    if (!selectedImageUris.isEmpty()) {
                        uploadSelectedImages(currentProblemId, () -> {
                            showToast("Problem added");
                            navigateBackToMainPage();
                        });
                    } else {
                        Activity activity = getActivity();
                        if (activity != null) {
                            showToast("Problem added");
                        }
                        navigateBackToMainPage();
                    }
                });
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