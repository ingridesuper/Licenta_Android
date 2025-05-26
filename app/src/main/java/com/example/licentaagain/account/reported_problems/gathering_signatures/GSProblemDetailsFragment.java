package com.example.licentaagain.account.reported_problems.gathering_signatures;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentaagain.R;
import com.example.licentaagain.account.TopProfileFragment;
import com.example.licentaagain.custom_adapters.ImageAdapterProblemDetails;
import com.example.licentaagain.custom_adapters.SearchUserAdapter;
import com.example.licentaagain.models.DateContact;
import com.example.licentaagain.models.Problem;
import com.example.licentaagain.models.User;
import com.example.licentaagain.repositories.ContactRepository;
import com.example.licentaagain.repositories.ProblemSignatureRepository;
import com.example.licentaagain.repositories.UserRepository;
import com.example.licentaagain.utils.EmailHelper;
import com.example.licentaagain.utils.GeminiHelper;
import com.example.licentaagain.utils.StorageHelper;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GSProblemDetailsFragment extends Fragment implements OnMapReadyCallback {
    private Problem problem;
    private GoogleMap myMap;
    private Button btnClose, btnOpenInGoogleMaps;
    private MaterialButton btnTakeAction;
    private SemnatariViewModel viewModel;
    private SearchUserAdapter adapter;


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
            ProgressDialog loadingDialog = new ProgressDialog(getContext());
            loadingDialog.setMessage("Se generează emailul...");
            loadingDialog.setCancelable(false);
            loadingDialog.show();

            new UserRepository().getUsersWhoSignedProblem(problem.getId(), semnatari -> {
                DocumentReference documentReference = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                documentReference.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String currentUserFullName = document.getString("name") + " " + document.getString("surname");
                            String currentUserSector = "Sectorul " + document.getLong("sector").intValue();
                            String userEmail = document.getString("email");

                            StringBuilder prompt = new StringBuilder();
                            prompt.append("Scrie un email oficial în limba română pentru autorități, ");
                            prompt.append("în care o persoană numită ").append(currentUserFullName)
                                    .append(" din ").append(currentUserSector).append(", dorește să sesizeze următoarea problemă: ");
                            prompt.append(problem.getTitle()).append(".\n");
                            prompt.append("Descriere: ").append(problem.getDescription()).append("\n");
                            prompt.append("Adresa: ").append(problem.getAddress()).append("\n\n");
                            prompt.append("Persoana cere un număr de înregistrare și vrea să primească răspuns la adresa ").append(userEmail).append(".\n");
                            prompt.append("Include și lista următoare de semnatari, cu nume și email:\n\n");

                            for (User user : semnatari) {
                                prompt.append("- ").append(user.getName()).append(" ")
                                        .append(user.getSurname()).append(": ")
                                        .append(user.getEmail()).append("\n");
                            }

                            new ContactRepository().getAiDestinationPrompt(
                                    aiPrompt -> {
                                        prompt.append("\n").append("Pentru adresele de destinatie ale emailului, ia în considerare și următoarele informații:\n");
                                        prompt.append(aiPrompt).append("\n");
                                        prompt.append("Și faptul că adresele de contact sunt (sugerează și una sau ma multe dintre ele):\n");

                                        new ContactRepository().getDateContact(
                                                contactList -> {
                                                    for (DateContact contact : contactList) {
                                                        if(contact.getEmail()!=null){
                                                            prompt.append("- ").append(contact.getInstitutie() != null ? contact.getInstitutie() : "")
                                                                    .append(" | ").append(contact.getEmail() != null ? contact.getEmail() : "")
                                                                    .append("\n");
                                                        }
                                                    }

                                                    GeminiHelper geminiHelper = new GeminiHelper();
                                                    geminiHelper.getResponse(getContext(), prompt.toString(), new GeminiHelper.ResponseCallback() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            StorageHelper.downloadFiles(getContext(), problem.getImageUrls(), new StorageHelper.OnDownloadCompleteListener() {
                                                                @Override
                                                                public void onDownloadComplete(List<File> downloadedFiles) {
                                                                    loadingDialog.dismiss();
                                                                    EmailHelper.sendEmailWithAttachments(
                                                                            getContext(),
                                                                            "Sesizare: " + problem.getTitle(),
                                                                            response,
                                                                            downloadedFiles
                                                                    );
                                                                }

                                                                @Override
                                                                public void onDownloadFailed(Exception e) {
                                                                    loadingDialog.dismiss();
                                                                    Toast.makeText(getContext(), "Eroare la descărcarea imaginilor.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onError(Throwable throwable) {
                                                            loadingDialog.dismiss();
                                                            Log.e("Gemini", throwable.getMessage());
                                                            Toast.makeText(getContext(), "Eroare la generarea emailului.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                },
                                                error -> {
                                                    loadingDialog.dismiss();
                                                    Toast.makeText(getContext(), "Eroare la încărcarea datelor de contact.", Toast.LENGTH_SHORT).show();
                                                }
                                        );
                                    },
                                    error -> {
                                        loadingDialog.dismiss();
                                        Toast.makeText(getContext(), "Eroare la obținerea promptului AI.", Toast.LENGTH_SHORT).show();
                                    }
                            );
                        } else {
                            loadingDialog.dismiss();
                        }
                    } else {
                        loadingDialog.dismiss();
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
        TextView tvStare=view.findViewById(R.id.tvStare);
        RecyclerView recyclerViewPictures=view.findViewById(R.id.recyclerViewPictures);
        TextView tvNrSemnatariHeading=view.findViewById(R.id.tvNrSemnatariHeading);
        TextView tvFacebookLink=view.findViewById(R.id.tvFacebookLink);
        btnClose=view.findViewById(R.id.btnClose);
        btnOpenInGoogleMaps=view.findViewById(R.id.btnOpenInGoogleMaps);
        btnTakeAction=view.findViewById(R.id.btnTakeAction);

        tvProblemTitle.setText(problem.getTitle());
        tvProblemDescription.setText(problem.getDescription());
        if(problem.getFacebookGroupLink()==null || problem.getFacebookGroupLink().isEmpty()){
            tvFacebookLink.setText("Această problemă nu are un grup de Facebook asociat.");
        }
        else {
            tvFacebookLink.setText("Grup de inițiativă: "+problem.getFacebookGroupLink());
        }
        tvProblemCategory.setText("Categorie: "+problem.getCategorieProblema());
        tvStare.setText("Stare problema: "+problem.getStareProblema());
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