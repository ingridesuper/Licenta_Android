package com.example.licentaagain.problem;

import android.app.Activity;
import android.app.AlertDialog;
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
import androidx.lifecycle.ViewModelProvider;
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
import com.example.licentaagain.account.ProblemByUserViewModel;
import com.example.licentaagain.custom_adapters.SelectedImagesAdapter;
import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.utils.ImagePickerHelper;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditProblemFragment extends Fragment implements OnMapReadyCallback {

    //DEOCAMDATA NU VOI ADAUGA POSIBILITATEA DE A SCHIMBA POZELE

    private GoogleMap myMap;
    private Place selectedPlace;
    private AutocompleteSupportFragment autocompleteFragment;
    private ProblemByUserViewModel viewModel;
    private ImagePickerHelper imagePickerHelper;


    private Problem problem;
    private RecyclerView rvSelectedImages;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private TextInputEditText etTitle, etDescription;
    private Spinner spnSector, spnCategorie;
    private RelativeLayout loadingOverlay;
    private SelectedImagesAdapter selectedImagesAdapter;


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

        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
            selectedPlace=Place.builder()
                    .setDisplayName(problem.getAddress())
                    .setLocation(new LatLng(problem.getLatitude(), problem.getLongitude()))
                    .build();

//            selectedImageUris = new ArrayList<>();
//            for (String imageUrl : problem.getImageUrls()) {
//                Uri imageUri = Uri.parse(imageUrl); // Convert string URL to Uri
//                selectedImageUris.add(imageUri);
//            }
        }

        viewModel = new ViewModelProvider(requireActivity()).get(ProblemByUserViewModel.class);
    }

    private void initializeVariables(@NonNull View view) {
        spnCategorie=view.findViewById(R.id.spnCategorie);
        spnSector=view.findViewById(R.id.spnSector);
        etDescription=view.findViewById(R.id.etDescription);
        etTitle=view.findViewById(R.id.etTitle);
        rvSelectedImages = view.findViewById(R.id.rvSelectedImages);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        selectedImagesAdapter = new SelectedImagesAdapter(getContext(), selectedImageUris);
        rvSelectedImages.setAdapter(selectedImagesAdapter);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_problem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeVariables(view);
        imagePickerHelper.enableDragAndDrop(rvSelectedImages, selectedImagesAdapter);
        setUpAutocompleteFragment();
        setupSpinners();
        setUpMapFragment(view);
        fillUiWithProblemData(view);
//        btnAddPicturesSubscribeToEvent(view);
//        btnOpenCameraSubscribeToEvent(view);
        btnCancelSubscribeToEvent(view);
        btnDeleteProblemSubscribeToEvent(view);
        btnEditProblemSubscribeToEvent(view);
    }

    private void btnEditProblemSubscribeToEvent(View view) {
        Button btnSaveEdits=view.findViewById(R.id.btnSaveEdits);
        btnSaveEdits.setOnClickListener(v->{
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirmare ștergere")
                    .setMessage("Sunteți sigur că vrei să editați această problemă?")
                    .setPositiveButton("Editează", (dialog, which) -> {
                        String authorUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String description = etDescription.getText().toString();
                        String title = etTitle.getText().toString();
                        int sector = ((Sector) spnSector.getSelectedItem()).getNumar();
                        String category = String.valueOf(spnCategorie.getSelectedItem());
                        if (!checkUserInput(description, title, sector, category, selectedPlace)) {
                            Toast.makeText(getContext(), "Nu ati completat tot ce este necesar", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(selectedImageUris.isEmpty()){
                            Toast.makeText(getContext(), "Va rugam atasati cel putin o imagine", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        LatLng latLng = selectedPlace.getLocation();
                        Problem newProblem = new Problem(
                                selectedPlace.getDisplayName(),
                                authorUid,
                                description,
                                latLng.latitude,
                                latLng.longitude,
                                sector,
                                title,
                                category
                        );

                        viewModel.updateProblemWithoutPictureChange(problem.getId(), newProblem);
                        navigateBackToProblemList();
                    })
                    .setNegativeButton("Anulează", null)
                    .show();

        });
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

    private void btnDeleteProblemSubscribeToEvent(View view) {
        MaterialButton btnDeleteProblem=view.findViewById(R.id.btnDeleteProblem);
        btnDeleteProblem.setOnClickListener(v->{
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirmare ștergere")
                    .setMessage("Sunteți sigur că vrei să ștergeți această problemă? Tot progresul asociat, împreună cu semnăturile strânse, vor fi șterse.")
                    .setPositiveButton("Șterge", (dialog, which) -> {
                        viewModel.deleteProblem(problem);
                        navigateBackToProblemList();
                    })
                    .setNegativeButton("Anulează", null)
                    .show();
        });
    }

    private void btnCancelSubscribeToEvent(View view) {
        MaterialButton btnCancel=view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v-> navigateBackToProblemList());
    }

    private void navigateBackToProblemList() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
    }

    private void btnAddPicturesSubscribeToEvent(View view) {
        Button btnAddPictures=view.findViewById(R.id.btnAddPictures);
        btnAddPictures.setOnClickListener(v->imagePickerHelper.openGallery());
    }

    private void btnOpenCameraSubscribeToEvent(View view) {
        Button btnTakePhoto=view.findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(v -> imagePickerHelper.openCamera());
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