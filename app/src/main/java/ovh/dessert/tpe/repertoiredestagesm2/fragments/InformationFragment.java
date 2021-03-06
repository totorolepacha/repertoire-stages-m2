package ovh.dessert.tpe.repertoiredestagesm2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ovh.dessert.tpe.repertoiredestagesm2.R;
import ovh.dessert.tpe.repertoiredestagesm2.SearchMap;
import ovh.dessert.tpe.repertoiredestagesm2.adapters.LocalisationAdapter;
import ovh.dessert.tpe.repertoiredestagesm2.entities.Localisation;

public class InformationFragment extends Fragment implements LocalisationAdapter.LocalisationAdapterListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_NAME = "name";
    private static final String ARG_WEBSITE = "website";
    protected static List<Localisation> LIST;

    public InformationFragment() {
    //    STAGES = StagesDAO.getInstance(this.getContext());
    }

    /**
     * Crée un Fragment contenant les détails de l'entreprise : Localisations, site et nom.
     * @param name Le nom de l'entreprise
     * @param website Le site internet de l'entreprise
     * @param list La liste de localisations disponibles
     * @return Une nouvelle instance du Fragment Information
     */
    public static InformationFragment newInstance(String name, String website, List<Localisation> list) {
        InformationFragment fragment = new InformationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_WEBSITE, website);
        LIST = list;
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Crée la page contenant les informations de l'entreprise
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return La vue contenant les informations
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_information, container, false);

        TextView textView = (TextView) rootView.findViewById(R.id.title_entreprise);
        textView.setText(getArguments().getString(ARG_NAME));

        TextView textViewSite = (TextView) rootView.findViewById(R.id.site_entreprise);
        textViewSite.setText(getArguments().getString(ARG_WEBSITE));

        ListView listView = (ListView) rootView.findViewById(R.id.locations_entreprise);
        LocalisationAdapter adapter = new LocalisationAdapter(this.getContext(), LIST);
        adapter.addListener(this);
        listView.setAdapter(adapter);

        return rootView;
    }

    /**
     * Ouvre une carte situant les localisations de l'entreprise.
     * @param item La localisation selectionnée
     * @param position La position de cette localisation
     */
    @Override
    public void onClickLocalisation(Localisation item, int position) {
        ArrayList<Localisation> temp = new ArrayList<>();
        temp.add(item);
        Intent intent = new Intent(this.getContext(), SearchMap.class);
        intent.putParcelableArrayListExtra("<Localisations>", temp);

        startActivity(intent);
    }
}