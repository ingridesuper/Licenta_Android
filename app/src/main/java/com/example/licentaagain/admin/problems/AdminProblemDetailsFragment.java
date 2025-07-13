package com.example.licentaagain.admin.problems;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import com.example.licentaagain.HomePageActivity;
import com.example.licentaagain.R;
import com.example.licentaagain.admin.AdminPageActivity;
import com.example.licentaagain.admin.users.AdminUserAdapter;
import com.example.licentaagain.admin.users.AdminUserDetailsFragment;
import com.example.licentaagain.custom_adapters.ImageAdapterProblemDetails;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.User;
import com.example.licentaagain.repositories.ProblemRepository;
import com.example.licentaagain.repositories.ProblemSignatureRepository;
import com.example.licentaagain.repositories.UserRepository;
import com.example.licentaagain.view_models.SemnatariViewModel;
import com.example.licentaagain.views.WorkaroundMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AdminProblemDetailsFragment extends Fragment implements OnMapReadyCallback {

    private Problem problem;
    private User author;
    private GoogleMap myMap;
    private Button btnClose, btnOpenInGoogleMaps;
    private MaterialButton btnEdit, btnDelete;
    private TextView tvProblemAuthor;

    public AdminProblemDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            problem = (Problem) getArguments().getSerializable("problem");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_problem_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new UserRepository().getUserBasedOnId(problem.getAuthorUid(), result -> {
            author = result;
            if (author != null) {
                fillUiWithProblemData(view);
                fillUpSemnatariRecyclerView(view);
                subscribeButtonsToEvents();
            }
        });

        setUpMapFragment(view);
    }

    private void fillUpSemnatariRecyclerView(View view) {
        RecyclerView rvSemnatari=view.findViewById(R.id.rvSemnatariList);
        rvSemnatari.setNestedScrollingEnabled(false);
        AdminUserAdapter adapter=new AdminUserAdapter(new ArrayList<>());
        rvSemnatari.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvSemnatari.setAdapter(adapter);
        SemnatariViewModel viewModel=new ViewModelProvider(requireActivity()).get(SemnatariViewModel.class);
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> adapter.updateData(users));
        viewModel.getSemnatariOfProblema(problem.getId());
    }

    private void subscribeButtonsToEvents() {
        subscribeBtnCloseToEvent();
        subscribeOpenInGoogleMaps();
        subscribeAuthorClickToEvent();
        subscribeBtnEditToEvent();
        subscribeBtnDeleteToEvent();
    }

    private void subscribeBtnDeleteToEvent() {
        btnDelete.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Sunteți sigur că vreți să ștergeți această problemă?")
                    .setMessage("Sunteți sigur că vreți să ștergeți această problemă? Ștergerea este ireversibilă.")
                    .setPositiveButton("Șterge", (dialog, id) -> {
                        new ProblemRepository().deleteProblem(problem, result ->{
                            if(!result){
                                Toast.makeText(v.getContext(), "A apărut o eroare", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
            builder.create().show();
        });
    }


    private void subscribeBtnEditToEvent() {
        btnEdit.setOnClickListener(v->{
            AdminPageActivity activity = (AdminPageActivity) requireActivity();
                Bundle bundle = new Bundle();
                bundle.putSerializable("problem", problem);

                AdminEditProblemFragment editProblemFragment = new AdminEditProblemFragment();
                editProblemFragment.setArguments(bundle);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, editProblemFragment)
                        .addToBackStack(null)
                        .commit();
        });
    }

    private void subscribeAuthorClickToEvent() {
        tvProblemAuthor.setOnClickListener(v->{
            Context context=v.getContext();
            AdminPageActivity activity = (AdminPageActivity) context;
            if(context!=null){
                AdminUserDetailsFragment userDetailsFragment=new AdminUserDetailsFragment();
                Bundle bundle=new Bundle();
                bundle.putSerializable("user", author);
                userDetailsFragment.setArguments(bundle);
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, userDetailsFragment)
                        .addToBackStack(null)
                        .commit();
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

    private void subscribeBtnCloseToEvent() {
        btnClose.setOnClickListener(v -> navigateBackToProblemList());
    }

    private void navigateBackToProblemList() {
        AdminProblemListFragment adminProblemListFragment=new AdminProblemListFragment();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, adminProblemListFragment)
                .addToBackStack(null)
                .commit();
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
        btnOpenInGoogleMaps=view.findViewById(R.id.btnOpenInGoogleMaps);
        btnEdit=view.findViewById(R.id.btnEdit);
        btnDelete=view.findViewById(R.id.btnDelete);

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

        new ProblemSignatureRepository().numberSignaturesOfProblem(problem.getId(), count->{
            tvNrSemnaturi.setText("Număr semnături: "+count);
        });

        List<String> problemImageUrls=problem.getImageUrls();
        ImageAdapterProblemDetails adapter=new ImageAdapterProblemDetails(getContext(), problemImageUrls);
        recyclerViewPictures.setAdapter(adapter);
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