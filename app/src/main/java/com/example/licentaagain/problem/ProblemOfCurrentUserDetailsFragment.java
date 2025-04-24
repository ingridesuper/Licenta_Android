package com.example.licentaagain.problem;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentaagain.R;
import com.example.licentaagain.account.TopProfileFragment;
import com.example.licentaagain.custom_adapters.ImageAdapterProblemDetails;
import com.example.licentaagain.custom_adapters.SearchUserAdapter;
import com.example.licentaagain.mainpage.MainPageFragment;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.User;
import com.example.licentaagain.repositories.ProblemSignatureRepository;
import com.example.licentaagain.repositories.UserRepository;
import com.example.licentaagain.view_models.SearchedUserViewModel;
import com.example.licentaagain.view_models.SemnatariViewModel;
import com.example.licentaagain.views.WorkaroundMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProblemOfCurrentUserDetailsFragment extends Fragment implements OnMapReadyCallback {
    private Problem problem;
    private GoogleMap myMap;
    private Button btnClose, btnOpenInGoogleMaps;
    private MaterialButton btnTakeAction;
    private SemnatariViewModel viewModel;
    SearchUserAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
        }
        viewModel=new ViewModelProvider(requireActivity()).get(SemnatariViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_problem_of_current_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillUiWithProblemData(view);
        subscribeButtonsToEvents();
        setUpMapFragment(view);
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

    private void subscribeButtonsToEvents() {
        subscribeBtnCloseToEvent();
        subscribeOpenInGoogleMaps();
        subscribeBtnTakeActionToEvent();
    }

    private void subscribeBtnTakeActionToEvent() {
        btnTakeAction.setOnClickListener(v -> {
            new UserRepository().getUsersWhoSignedProblem(problem.getId(), semnatari -> {
                StringBuilder body = new StringBuilder();
                body.append("Bună ziua,\n\n");
                body.append("Aș dori să semnalez următoarea problemă: ").append(problem.getTitle()).append("\n\n");
                body.append("Detalii: ").append(problem.getDescription()).append("\n\n");

                DocumentReference documentReference = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                documentReference.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String currentUserFullName = document.getString("name") + " " + document.getString("surname") + "\n";
                            String currentUserAddress = "Sectorul "+document.getLong("sector").intValue() + "\n"; //de adaugat aici toata adresa!!
                            body.append("Va rog sa imi trimiteti numarul de inregistrare la adresa "+document.getString("email")+"\n\n");
                            body.append(currentUserFullName);
                            body.append(currentUserAddress);

                            body.append("\n\nSemnatari:\n");
                            for (User user : semnatari) {
                                body.append("- ").append(user.getName()).append(" ")
                                        .append(user.getSurname()).append(": ")
                                        .append(user.getEmail()).append("\n");
                            }

                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                            emailIntent.setData(Uri.parse("mailto:"));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sesizare: " + problem.getTitle());
                            emailIntent.putExtra(Intent.EXTRA_TEXT, body.toString());

                            try {
                                getContext().startActivity(Intent.createChooser(emailIntent, "Trimite email cu..."));
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getContext(), "Nu s-a găsit o aplicație de email.", Toast.LENGTH_SHORT).show();
                            }

                            // TODO: schimbă starea problemei în "email trimis"
                        } else {
                            Log.d("Firestore", "Documentul nu există!");
                        }
                    } else {
                        Log.d("Firestore", "Eroare la obținerea documentului: " + task.getException());
                    }
                });
            });
        });
    }




    private void subscribeOpenInGoogleMaps() {
        btnOpenInGoogleMaps.setOnClickListener(v->{
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", problem.getLatitude(), problem.getLongitude(), problem.getLatitude(), problem.getLongitude(), problem.getTitle()+" - "+problem.getAddress());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Google Maps nu este instalat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscribeBtnCloseToEvent() {
        btnClose.setOnClickListener(v->{
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, new TopProfileFragment())
                        .commit();
            }
        });
    }

    private void fillUiWithProblemData(View view) {
        TextView tvProblemTitle= view.findViewById(R.id.tvProblemTitle);
        TextView tvProblemDescription=view.findViewById(R.id.tvProblemDescription);
        TextView tvProblemCategory=view.findViewById(R.id.tvProblemCategory);
        TextView tvProblemAddressSector=view.findViewById(R.id.tvProblemAddressSector);
        RecyclerView recyclerViewPictures=view.findViewById(R.id.recyclerViewPictures);
        TextView tvNrSemnatariHeading=view.findViewById(R.id.tvNrSemnatariHeading);
        btnClose=view.findViewById(R.id.btnClose);
        btnOpenInGoogleMaps=view.findViewById(R.id.btnOpenInGoogleMaps);
        btnTakeAction=view.findViewById(R.id.btnTakeAction);

        tvProblemTitle.setText(problem.getTitle());
        tvProblemDescription.setText(problem.getDescription());
        tvProblemCategory.setText("Categorie: "+problem.getCategorieProblema());
        tvProblemAddressSector.setText(problem.getAddress()+", Sectorul "+problem.getSector());

        new ProblemSignatureRepository().numberSignaturesOfProblem(problem.getId(), result-> tvNrSemnatariHeading.setText("Semnatari ("+result.toString()+"): "));

        List<String> problemImageUrls=problem.getImageUrls();
        ImageAdapterProblemDetails adapter=new ImageAdapterProblemDetails(getContext(), problemImageUrls);
        recyclerViewPictures.setAdapter(adapter);

        fillUpSemnatariRecyclerView(view);
    }

    private void fillUpSemnatariRecyclerView(View view) {
        RecyclerView rvSemnatariList=view.findViewById(R.id.rvSemnatariList);
        rvSemnatariList.setNestedScrollingEnabled(false);
        adapter=new SearchUserAdapter(new ArrayList<>());
        rvSemnatariList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvSemnatariList.setAdapter(adapter);
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            adapter.updateData(users);
            Log.i("fetchedUsers", String.valueOf(users.size())+": "+users.toString());
        });
        viewModel.getSemnatariOfProblema(problem.getId());
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
        Log.i("ProblemDetailsFragment", "Map is ready");
    }
}