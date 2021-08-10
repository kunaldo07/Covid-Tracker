package com.kunalbadole.covid_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//here we implemented AdapterView.OnItemSelectedListener to add on click listener to the spinner
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String tag="Kunaldo_07";
    CountryCodePicker countryCodePicker;
    TextView mtodaytotal,mtotal,mtodayactive,mrecovered,mdeaths,mtodaydeaths,mactive,mtodayrecovered;

    String country;
    TextView mfilter;
    Spinner spinner;
    String[] types={"cases","deaths","recovered","active"};

    //one list of for our recycler view and another one is for our normal data
    private List<ModelClass> modelClassList;
    private List<ModelClass> modelClassList2;

    PieChart mpiechart;
    private RecyclerView recyclerView;
    com.kunalbadole.covid_tracker.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(tag,"onCreate Initiated");

        getSupportActionBar().hide();
        countryCodePicker=findViewById(R.id.ccp);

        mtodayactive=findViewById(R.id.todayactive);
        mactive=findViewById(R.id.activecase);
        mrecovered=findViewById(R.id.recoveredcase);
        mdeaths=findViewById(R.id.totaldeath);
        mtodayrecovered=findViewById(R.id.todayrecovered);
        mtodaydeaths=findViewById(R.id.todaydeath);
        mtotal=findViewById(R.id.totalcase);
        mtodaytotal=findViewById(R.id.todaytotal);

        mpiechart=findViewById(R.id.piechart);
        spinner=findViewById(R.id.spinner);
        mfilter=findViewById(R.id.filter);
        recyclerView=findViewById(R.id.recyclerview);

        modelClassList=new ArrayList<>();
        modelClassList2=new ArrayList<>();

        Log.d(tag,"Values assigned");


        spinner.setOnItemSelectedListener(this);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,types);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);

        Log.d(tag,"Spinner Done");

        ApiUtilities.getAPIInterface().getcountrydata().enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(Call<List<ModelClass>> call, Response<List<ModelClass>> response) {
                modelClassList2.addAll(response.body());
                //notify the adapter
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ModelClass>> call, Throwable t) {

            }
        });

        Log.d(tag,"ApiUtilities Done");

        //adding the data into the recycler view
        adapter = new Adapter(getApplicationContext(),modelClassList2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        Log.d(tag,"Adding data into recycler view Done");

        //Automatically detects the country which is present in our screen
        countryCodePicker.setAutoDetectedCountry(true);
        //getting the name of that country
        country="India";
        //country=countryCodePicker.getSelectedCountryName();
        Log.d(tag,"country="+country);
        //to change the country after picking
        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                country=countryCodePicker.getSelectedCountryName();
                fetchdata();

            }
        });

        //if user doesnt want to change the country then for intial country
        fetchdata();

        Log.d(tag,"Fetching Done");


    }


    private void fetchdata() {

        ApiUtilities.getAPIInterface().getcountrydata().enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(Call<List<ModelClass>> call, Response<List<ModelClass>> response) {
                //add all country data
                modelClassList.addAll(response.body());
                //to show specific country data

                Log.d(tag,"ModelClasslist size = " +modelClassList.size());
                Log.d(tag,"country="+country);

                for (int i=0;i<modelClassList.size();i++)
                {
                    //if the user clicks on the country that is present on the model list
                    if (modelClassList.get(i).getCountry().equals(country))
                    {
                        Log.d(tag,"country found");
                        //setting the data in card views
                        mactive.setText((modelClassList.get(i).getActive()));
                        mtodaydeaths.setText((modelClassList.get(i).getTodayDeaths()));
                        mtodayrecovered.setText((modelClassList.get(i).getTodayRecovered()));
                        mtodaytotal.setText((modelClassList.get(i).getTodayCases()));
                        mtotal.setText((modelClassList.get(i).getCases()));
                        mdeaths.setText((modelClassList.get(i).getDeaths()));
                        mrecovered.setText((modelClassList.get(i).getRecovered()));

                        int active,total,recovered,deaths;

                        active = Integer.parseInt(modelClassList.get(i).getActive());
                        total = Integer.parseInt(modelClassList.get(i).getCases());
                        recovered = Integer.parseInt(modelClassList.get(i).getRecovered());
                        deaths = Integer.parseInt(modelClassList.get(i).getDeaths());

                        //then we have to update the graph

                        updateGraph(active,total,recovered,deaths);

                        Log.d(tag,"Graph Updation Done");

                    }
                }
            }

            @Override
            public void onFailure(Call<List<ModelClass>> call, Throwable t) {

            }
        });

    }


    //putting the values in the piechart
    private void updateGraph(int active, int total, int recovered, int deaths) {

        Log.d(tag,"Graph Ceared Done");
        mpiechart.clearChart();
        //labeling the sections and assigning the colors
        mpiechart.addPieSlice(new PieModel("Confirm",total,Color.parseColor("#FFB701")));
        mpiechart.addPieSlice(new PieModel("Active",active,Color.parseColor("#FF4CAF50")));
        mpiechart.addPieSlice(new PieModel("Recovered",recovered,Color.parseColor("#38ACCD")));
        mpiechart.addPieSlice(new PieModel("Deaths",deaths,Color.parseColor("#F55c47")));
        mpiechart.startAnimation();

        Log.d(tag,"Graph Animation Done");

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

        //if the user select any item then we have to add it in the adaptor

        String item = types[position];
        mfilter.setText(item);
        adapter.filter(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}