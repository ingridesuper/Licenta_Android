package com.example.licentaagain.admin.problems;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
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
import com.example.licentaagain.admin.AdminPageActivity;
import com.example.licentaagain.custom_adapters.SelectedImagesAdapter;
import com.example.licentaagain.enums.CategorieProblema;
import com.example.licentaagain.enums.Sector;
import com.example.licentaagain.enums.StareProblema;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminEditProblemFragment extends Fragment implements OnMapReadyCallback {

    //DEOCAMDATA NU VOI ADAUGA POSIBILITATEA DE A SCHIMBA POZELE

    private GoogleMap myMap;
    private Place selectedPlace;
    private AutocompleteSupportFragment autocompleteFragment;
    private AdminProblemsViewModel viewModel;
    private ImagePickerHelper imagePickerHelper;


    private Problem problem;
    private RecyclerView rvSelectedImages;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<String> originalImageUrls = new ArrayList<>();
    private String facebookGroupLink;

    private TextInputEditText etTitle, etDescription, etFacebookLink;
    private Spinner spnSector, spnCategorie, spnStareProblema;
    private RelativeLayout loadingOverlay;
    private SelectedImagesAdapter selectedImagesAdapter;


    public AdminEditProblemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sterge harcodare aici!
        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
            facebookGroupLink=problem.getFacebookGroupLink();
            selectedPlace=Place.builder()
                    .setDisplayName(problem.getAddress())
                    .setLocation(new LatLng(problem.getLatitude(), problem.getLongitude()))
                    .build();
            if (!Places.isInitialized()) {
                Places.initialize(requireContext(), "AIzaSyBbOOjwR9Eq3CGJnGZhg9fMssUBRFlMDpc");
            }

            for(String url:problem.getImageUrls()){
                selectedImageUris.add(Uri.parse(url));
                originalImageUrls.add(url);
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

        viewModel = new ViewModelProvider(requireActivity()).get(AdminProblemsViewModel.class);
    }

    private void initializeVariables(@NonNull View view) {
        spnCategorie=view.findViewById(R.id.spnCategorie);
        spnSector=view.findViewById(R.id.spnSector);
        spnStareProblema=view.findViewById(R.id.spnStareProblema);
        etDescription=view.findViewById(R.id.etDescription);
        etTitle=view.findViewById(R.id.etTitle);
        etFacebookLink=view.findViewById(R.id.etFacebookLink);
        rvSelectedImages = view.findViewById(R.id.rvSelectedImages);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        selectedImagesAdapter = new SelectedImagesAdapter(getContext(), selectedImageUris);
        rvSelectedImages.setAdapter(selectedImagesAdapter);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_edit_problem, container, false);
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
        btnAddPicturesSubscribeToEvent(view);
        btnOpenCameraSubscribeToEvent(view);
        btnCancelSubscribeToEvent(view);
        btnDeleteProblemSubscribeToEvent(view);
        btnEditProblemSubscribeToEvent(view);
    }

    private void btnEditProblemSubscribeToEvent(View view) {
        Button btnSaveEdits=view.findViewById(R.id.btnSaveEdits);
        btnSaveEdits.setOnClickListener(v->{
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirmare editare")
                    .setMessage("Sunteți sigur că vrei să editați această problemă?")
                    .setPositiveButton("Editează", (dialog, which) -> {
                        String authorUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String description = etDescription.getText().toString();
                        String title = etTitle.getText().toString();
                        int sector = ((Sector) spnSector.getSelectedItem()).getNumar();
                        String category = String.valueOf(spnCategorie.getSelectedItem());
                        String stareProblema= String.valueOf(spnStareProblema.getSelectedItem());
                        facebookGroupLink=etFacebookLink.getText().toString().isEmpty()?null:etFacebookLink.getText().toString();
                        if (!checkUserInput(description, title, sector, category, selectedPlace)) {
                            Toast.makeText(getContext(), "Nu ati completat tot ce este necesar", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(selectedImageUris.isEmpty()){
                            Toast.makeText(getContext(), "Va rugam atasati cel putin o imagine", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(facebookGroupLink!=null && !facebookGroupLink.isEmpty() && !isValidFacebookGroupLink(facebookGroupLink)){
                            Toast.makeText(getContext(), "Vă rugăm să introduceți un link de grup valid.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        showLoadingOverlay(true);
                        ScrollView scrollView=view.findViewById(R.id.scrollView);
                        disableAllViews(scrollView);

                        LatLng latLng = selectedPlace.getLocation();
                        Problem newProblem = new Problem(
                                selectedPlace.getDisplayName(),
                                authorUid,
                                description,
                                latLng.latitude,
                                latLng.longitude,
                                sector,
                                title,
                                category,
                                StareProblema.fromString(problem.getStareProblema()),
                                facebookGroupLink
                        );
                        newProblem.setStareProblema(stareProblema);
                        List<String> currentUrls = new ArrayList<>(); //current urls still being used
                        List<Uri> newLocalUris = new ArrayList<>(); //totally new urls

                        for (Uri uri : selectedImageUris) {
                            String uriStr = uri.toString();
                            if (ImagePickerHelper.isRemoteUri(uri)) {
                                currentUrls.add(uriStr);
                            } else {
                                newLocalUris.add(uri);
                            }
                        }

                        boolean imageListUnchanged = newLocalUris.isEmpty() && currentUrls.size() == originalImageUrls.size()
                                && originalImageUrls.containsAll(currentUrls);

                        //if i add pictures
                        //in stirage only the new ones appear, all old ones even some in use are eleted
                        //if i just remove pictures in storage the file of the photo is completely deleted
                        //tho in firestore things seem to work
                        if (imageListUnchanged) {
                            viewModel.updateProblemWithoutPictureChange(problem.getId(), newProblem);
                        } else {
                            //viewModel.updateProblemWithPictureChange(problem, newProblem, newLocalUris, currentUrls);
                        }
                        navigateBackToProblemList();

                    })
                    .setNegativeButton("Anulează", null)
                    .show();

        });
    }

    private void showLoadingOverlay(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
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
                        new ProblemRepository().deleteProblem(problem, result ->{
                            if(!result){
                                Toast.makeText(getContext(), "A apărut o eroare", Toast.LENGTH_SHORT).show();
                            }
                            navigateBackToProblemList();
                        });
                    })
                    .setNegativeButton("Anulează", null)
                    .show();
        });
    }

    private void navigateBackToProblemList() {
        AdminProblemListFragment adminProblemListFragment=new AdminProblemListFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, adminProblemListFragment)
                .addToBackStack(null)
                .commit();
    }

    private void btnCancelSubscribeToEvent(View view) {
        MaterialButton btnCancel=view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v-> navigateBackToProblemDetails());
    }

    private void btnAddPicturesSubscribeToEvent(View view) {
        Button btnAddPictures=view.findViewById(R.id.btnAddPictures);
        btnAddPictures.setOnClickListener(v->imagePickerHelper.openGallery());
    }
    private void btnOpenCameraSubscribeToEvent(View view) {
        Button btnTakePhoto=view.findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(v -> imagePickerHelper.openCamera());
    }
    private void fillUiWithProblemData(View view) {
        etTitle.setText(problem.getTitle());
        etDescription.setText(problem.getDescription());
        etFacebookLink.setText(facebookGroupLink==null?"":facebookGroupLink);
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

        String stareProblemaText=problem.getStareProblema();
        StareProblema selectedStare=null;
        for(StareProblema s: StareProblema.values()){
            if(s.getStare().equalsIgnoreCase(stareProblemaText)){
                selectedStare=s;
                break;
            }
        }
        if(selectedStare!=null){
            ArrayAdapter<StareProblema> stareAdapter=(ArrayAdapter<StareProblema>) spnStareProblema.getAdapter();
            int starePosition=stareAdapter.getPosition(selectedStare);
            spnStareProblema.setSelection(starePosition);
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
    private void navigateBackToProblemDetails() {
        AdminPageActivity activity = (AdminPageActivity) requireActivity();
        Bundle bundle = new Bundle();
        bundle.putSerializable("problem", problem);

        AdminProblemDetailsFragment problemDetailsFragment = new AdminProblemDetailsFragment();
        problemDetailsFragment.setArguments(bundle);

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, problemDetailsFragment)
                .addToBackStack(null)
                .commit();
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
        setupSpinner(spnStareProblema, StareProblema.values());
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
        Log.i("AdminEditProblemFragment", "Map is ready");
    }


}