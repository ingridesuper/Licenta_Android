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
        TextView tvTutorial=view.findViewById(R.id.tvTutorial);
        TextView tvUndeTrimitiSesizare=view.findViewById(R.id.tvUndeTrimitiSesizare);
        TextView tvSector1Date=view.findViewById(R.id.tvSector1Date);
        TextView tvSector2Date=view.findViewById(R.id.tvSector2Date);
        TextView tvSector3Date=view.findViewById(R.id.tvSector3Date);
        TextView tvSector4Date=view.findViewById(R.id.tvSector4Date);
        TextView tvSector5Date=view.findViewById(R.id.tvSector5Date);
        TextView tvSector6Date=view.findViewById(R.id.tvSector6Date);
        TextView tvPMBDate=view.findViewById(R.id.tvPMBDate);

        String[] bulletsHowToUseApp = {
                "Ești pe stradă și vezi ceva ce nu e în regulă – un trotuar blocat de mașini, o gură de canalizare deschisă sau un loc de joacă neglijat.",
                "Deschizi aplicația „Conectat la București” și adaugi problema. E simplu: apeși pe butonul „+”, completezi locația, alegi categoria, scrii o scurtă descriere și adaugi câteva poze.",
                "Odată publicată, problema devine vizibilă pentru toată lumea. Alți utilizatori pot semna pentru susținere. Cu cât sunt mai multe semnături, cu atât cresc șansele ca autoritățile să reacționeze!",
                "Când simți că ai strâns destule semnături, apeși pe butonul „Acțiune”. Aplicația generează automat un e-mail către autorități, cu toate informațiile și semnăturile adunate.",
                "Apoi, aștepți un răspuns. E important să știi care sunt drepturile tale – aruncă o privire pe secțiunea acestei pagini de „Tutorial Sesizări”.",
                "Dacă nu primești un răspuns mulțumitor sau vrei să mobilizezi mai mulți oameni, poți crea un grup de Facebook dedicat problemei. Acolo se pot aduna semnatarii și puteți construi împreună o mică comunitate activă. Uneori, e nevoie să ieșim din online și să ne facem auziți mai departe.",
                "Poți schimba oricând starea problemei. Iar când ajungi la „Rezolvat”, felicitări! Tocmai ai făcut Bucureștiul un pic mai bun."
        };


        String[] bulletsTutorial={
                "Ce este o sesizare? \n O sesizare este pur și simplu un mod prin care îi poți cere unei autorități publice să intervină, să investigheze sau să rezolve o problemă ce ține de interesul public. Este un drept legal pe care îl are orice cetățean.",
                "Despre ce poți face sesizări? \n Poți face sesizări legate de orice aspect ce ține de domeniul public, bunurile publice sau bunăstarea comunității: gropi în asfalt, iluminat stradal deficitar, parcuri neîngrijite, clădiri periculoase, poluare, lipsa curățeniei, haos urbanistic, zgomot, defrișări ilegale, și multe altele.",
                "Cere număr de înregistrare! \n Nu uita să ceri un număr de înregistrare pentru sesizarea ta! Este pur și simplu dovada oficială că autoritatea a primit solicitarea și este obligată să răspundă. Fără acest număr, e mult mai greu să urmărești statusul cererii tale.",
                "Ai trimis sesizarea. Ce urmează? \n Conform Ordonanței nr. 27/2002 privind reglementarea activității de soluționare a petițiilor:\n\n" +
                        "Autoritatea este obligată să îți comunice numărul de înregistrare imediat ce a primit sesizarea.\n\n" +
                        "Este obligată să îți ofere un răspuns în termen de maximum 30 de zile calendaristice.\n\n" +
                        "Dacă problema este complexă, termenul poate fi prelungit cu cel mult 15 zile, dar autoritatea trebuie să te notifice despre asta.\n\n" +
                        "Răspunsul trebuie să conțină măsurile luate sau motivele pentru care nu s-au luat măsuri.",
                "Ce faci dacă nu primești un răspuns? \n Reamintește autorității despre obligația legală de a răspunde, folosind numărul de înregistrare. \n\n" +
                        "Trimite o nouă sesizare în care menționezi că nu ai primit răspuns în termenul legal.\n\n" +
                        "Poți face o plângere administrativă sau te poți adresa Avocatului Poporului.\n\n" +
                        "Dacă e vorba de o încălcare gravă, poți merge chiar în instanță, invocând lipsa răspunsului la o petiție, conform legii."
        };

        String[] bulletsWhereToSend={
               "Mașini parcate ilegal: Poliția Locală de Sector",
               "Nereguli în parcurile mari: ALPAB",
                "Nereguli pe bulevardele principale: ASPMB și Primăria de Sector",
                "Nereguli generale (gropi, trotuare, curățenie, locuri de joacă): Primăria de Sector și ADP de Sector"
        };

        String[] dateSector1={
                "ADP S1: secretariat@adp-sector1.ro",
                "Primărie S1: registratura@primarias1.ro",
                "Poliție locală: contact@politialocalasector1.ro, telefon: 0219540"
        };

        String[] dateSector2={
                "ADP S2: office@adp2.ro",
                "Primărie S2: infopublice@ps2.ro",
                "Poliție locală: office@politialocalas2.ro, telefon: 0219941"
        };

        String[] dateSector3={
                "ADP S3: domeniu.public@primarie3.ro",
                "Primărie S3: relatiipublice@primarie3.ro",
                "Poliție locală: secretariat@politialocala3.ro, telefon: 0219543"
        };

        String[] dateSector4={
                "ADP S4: info@totulverde.ro",
                "Primărie S4: contact@ps4.ro",
                "Poliție locală: sesizari@politialocala4.ro, telefon: 0219441"
        };

        String[] dateSector5={
                "ADP S5: dadp@sector5.ro",
                "Primărie S5: sesizari@sector5.ro",
                "Poliție locală: politialocala@sector5.ro, telefon: 0319451"
        };

        String[] dateSector6={
                "ADP S6: contact@adps6.ro",
                "Primărie S6: prim6@primarie6.ro",
                "Poliție locală: office@politia6.ro, telefon: 0219546"
        };

        String[] datePMB={
                "Primăria Generală: relatiipublice@pmb.ro",
                "ALPAB (pentru parcuri mari): office@alpab.ro",
                "Administrația Străzilor (pentru bulevarde mari): office@aspmb.ro",
                "Brigada Rutieră: telefon: 021 9544, sau petiție la adresa https://bpr.politiaromana.ro/ro/petitii-online"
        };

        setBulletPoints(tvHowToUseApp, bulletsHowToUseApp);
        setBulletPoints(tvTutorial, bulletsTutorial);
        setBulletPoints(tvUndeTrimitiSesizare, bulletsWhereToSend);


        setBulletPoints(tvSector1Date, dateSector1);
        setBulletPoints(tvSector2Date, dateSector2);
        setBulletPoints(tvSector3Date, dateSector3);
        setBulletPoints(tvSector4Date, dateSector4);
        setBulletPoints(tvSector5Date, dateSector5);
        setBulletPoints(tvSector6Date, dateSector6);
        setBulletPoints(tvPMBDate, datePMB);
    }

    private void setBulletPoints(TextView textView, String[] bullets){
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (String item : bullets) {
            SpannableString spannable = new SpannableString(item + "\n\n");
            spannable.setSpan(new BulletSpan(40), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(spannable);
        }
        textView.setText(builder);
    }
}