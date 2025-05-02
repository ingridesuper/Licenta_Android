package com.example.licentaagain.problem;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentaagain.HomePageActivity;
import com.example.licentaagain.R;
import com.example.licentaagain.account.TopProfileFragment;
import com.example.licentaagain.custom_adapters.ImageAdapterProblemDetails;
import com.example.licentaagain.disabled_user.DisabledSearchedUserFragment;
import com.example.licentaagain.mainpage.MainPageFragment;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.ProblemSignature;
import com.example.licentaagain.models.User;
import com.example.licentaagain.repositories.ProblemSignatureRepository;
import com.example.licentaagain.repositories.UserRepository;
import com.example.licentaagain.user_page.OtherUserFragment;
import com.example.licentaagain.views.WorkaroundMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;


public class ProblemDetailsFragment extends Fragment implements OnMapReadyCallback {
    private Problem problem;
    private User author;
    private GoogleMap myMap;
    private Button btnClose, btnSign, btnSigned, btnOpenInGoogleMaps;
    private TextView tvProblemAuthor, tvStareProblema;
    private ProblemSignatureRepository problemSignatureRepository;


    public ProblemDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
        }
        problemSignatureRepository=new ProblemSignatureRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_problem_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(problem.getFacebookGroupLink()==null || problem.getFacebookGroupLink().isEmpty()){
            Button btnJoinGrupInitiativa=view.findViewById(R.id.btnJoinGrupInitiativa);
            btnJoinGrupInitiativa.setVisibility(View.INVISIBLE);
        }

        new UserRepository().getUserBasedOnId(problem.getAuthorUid(), result -> {
            author = result;
            if (author != null) {
                fillUiWithProblemData(view);
                subscribeButtonsToEvents(view);
            }
        });

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

    private void subscribeButtonsToEvents(View view) {
        subscribeBtnCloseToEvent();
        subscribeSigningButtonsToEvents();
        subscribeOpenInGoogleMaps();
        subscribeAuthorClickToEvent();
        subscribeBtnJoinGroupToEvent(view);
    }

    private void subscribeBtnJoinGroupToEvent(View view) {
        Button btnJoinGrupInitiativa=view.findViewById(R.id.btnJoinGrupInitiativa);
        btnJoinGrupInitiativa.setOnClickListener(v -> {
            String facebookGroupLink = problem.getFacebookGroupLink();
            if (facebookGroupLink != null && !facebookGroupLink.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookGroupLink));
                try {
                    v.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(v.getContext(), "Link-ul nu este valid", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Nu există link de grup asociat.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscribeAuthorClickToEvent() {
        tvProblemAuthor.setOnClickListener(v->{
            Context context=v.getContext();
            HomePageActivity activity = (HomePageActivity) context;

            if(author.isDisabled()){
                    DisabledSearchedUserFragment disabledSearchedUserFragment=new DisabledSearchedUserFragment();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_view, disabledSearchedUserFragment)
                            .addToBackStack(null)
                            .commit();
            }

            else if(!author.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                if (context instanceof HomePageActivity) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", author);

                    OtherUserFragment otherUserFragment = new OtherUserFragment();
                    otherUserFragment.setArguments(bundle);
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_view, otherUserFragment)
                            .addToBackStack(null)
                            .commit();
                    }
                }

            else {
                if (context instanceof HomePageActivity) {
                    TopProfileFragment topProfileFragment = new TopProfileFragment();
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_view, topProfileFragment)
                            .addToBackStack(null)
                            .commit();
                    }
                }
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

    private void subscribeSigningButtonsToEvents() {
        btnSign.setOnClickListener(v ->
                addSignature(problem, success -> {
                    if(success){
                        updateButtonVisibility(true);
                    }
                })
        );

        btnSigned.setOnClickListener(v ->
                removeSignature(problem, success -> {
                    if(success){
                        updateButtonVisibility(false);
                    }
                })
        );
    }

    private void addSignature(Problem problem, Consumer<Boolean> callback) {
        new UserRepository().checkIfUserHasNameSurnameSectorData(FirebaseAuth.getInstance().getCurrentUser().getUid(), result->{
            if(result){

                problemSignatureRepository.addProblemSignature(problem.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), getContext(), addingResult->{
                    if(addingResult){
                        callback.accept(true);
                    }
                    else {
                        callback.accept(false);
                    }
                });
            }
            else{
                Toast.makeText(getContext(), "Completati-va profilul pentru a putea semna!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeSignature(Problem problem, Consumer<Boolean> callback){
        problemSignatureRepository.removeSignature(problem.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), result->{
            if(result){
                callback.accept(true);
            }
            else {
                callback.accept(false);
            }
        });
    }

    private void subscribeBtnCloseToEvent() {
        btnClose.setOnClickListener(v->{
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
        });
    }

    private void fillUiWithProblemData(@NonNull View view) {
        TextView tvProblemTitle= view.findViewById(R.id.tvProblemTitle);
        TextView tvNrSemnaturi=view.findViewById(R.id.tvNrSemnaturi);
        TextView tvStareProblema=view.findViewById(R.id.tvStareProblema);
        tvProblemAuthor=view.findViewById(R.id.tvProblemAuthor);
        TextView tvProblemDescription=view.findViewById(R.id.tvProblemDescription);
        TextView tvProblemCategory=view.findViewById(R.id.tvProblemCategory);
        TextView tvProblemAddressSector=view.findViewById(R.id.tvProblemAddressSector);
        RecyclerView recyclerViewPictures=view.findViewById(R.id.recyclerViewPictures);
        btnClose=view.findViewById(R.id.btnClose);
        btnSign=view.findViewById(R.id.btnSign);
        btnSigned=view.findViewById(R.id.btnSigned);
        btnOpenInGoogleMaps=view.findViewById(R.id.btnOpenInGoogleMaps);

        problemSignedByUser(problem, isSigned -> {
            updateButtonVisibility(isSigned);
        });

        tvProblemTitle.setText(problem.getTitle());
        if(author.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            tvProblemAuthor.setText("Dvs");
        }
        else {
            tvProblemAuthor.setText(author.getName()+" "+author.getSurname());
        }
        tvProblemDescription.setText(problem.getDescription());
        tvProblemCategory.setText("Categorie: "+problem.getCategorieProblema());
        tvProblemAddressSector.setText(problem.getAddress()+", Sectorul "+problem.getSector());
        tvStareProblema.setText("Stare problemă: "+problem.getStareProblema());

        problemSignatureRepository.numberSignaturesOfProblem(problem.getId(), count->{
            tvNrSemnaturi.setText("Număr semnături: "+count);
        });

        List<String> problemImageUrls=problem.getImageUrls();
        ImageAdapterProblemDetails adapter=new ImageAdapterProblemDetails(getContext(), problemImageUrls);
        recyclerViewPictures.setAdapter(adapter);
    }

    private void updateButtonVisibility(Boolean isSigned) {
        btnSign.setVisibility(isSigned ? View.INVISIBLE : View.VISIBLE);
        btnSigned.setVisibility(isSigned ? View.VISIBLE : View.INVISIBLE);
    }

    private void problemSignedByUser(Problem problem, Consumer<Boolean> callback) {
        problemSignatureRepository.problemSignedByUser(problem.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), result->{
            if(result){
                callback.accept(true);
            }
            else {
                callback.accept(false);
            }
        });
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