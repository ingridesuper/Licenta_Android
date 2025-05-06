package com.example.licentaagain.about;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.licentaagain.R;


public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvHowToUseApp=view.findViewById(R.id.tvHowToUseApp);
        SpannableStringBuilder builder = new SpannableStringBuilder();

        String[] bullets = {
                "Ești pe stradă și vezi ceva ce nu e în regulă – un trotuar blocat de mașini, o gură de canalizare deschisă sau un loc de joacă neglijat.",
                "Deschizi aplicația „Conectat la București” și adaugi problema. E simplu: apeși pe butonul „+”, completezi locația, alegi categoria, scrii o scurtă descriere și adaugi câteva poze.",
                "Odată publicată, problema devine vizibilă pentru toată lumea. Alți utilizatori pot semna pentru susținere. Cu cât sunt mai multe semnături, cu atât cresc șansele ca autoritățile să reacționeze!",
                "Când simți că ai strâns destule semnături, apeși pe butonul „Acțiune”. Aplicația generează automat un e-mail către autorități, cu toate informațiile și semnăturile adunate.",
                "Apoi, aștepți un răspuns. E important să știi care sunt drepturile tale – aruncă o privire pe secțiunea acestei pagini de „Tutorial Sesizări”.",
                "Dacă nu primești un răspuns mulțumitor sau vrei să mobilizezi mai mulți oameni, poți crea un grup de Facebook dedicat problemei. Acolo se pot aduna semnatarii și puteți construi împreună o mică comunitate activă. Uneori, e nevoie să ieșim din online și să ne facem auziți mai departe.",
                "Poți schimba oricând starea problemei. Iar când ajungi la „Rezolvat”, felicitări! Tocmai ai făcut Bucureștiul un pic mai bun."
        };

        for (String item : bullets) {
            SpannableString spannable = new SpannableString(item + "\n\n");
            spannable.setSpan(new BulletSpan(40), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(spannable);
        }

        tvHowToUseApp.setText(builder);
    }
}