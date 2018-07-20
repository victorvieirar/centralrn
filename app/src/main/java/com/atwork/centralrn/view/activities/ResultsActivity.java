package com.atwork.centralrn.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.atwork.centralrn.App;
import com.atwork.centralrn.R;
import com.atwork.centralrn.model.Result;
import com.atwork.centralrn.model.ResultsEntity;
import com.atwork.centralrn.view.extras.ResultsAdapter;
import com.atwork.centralrn.view.extras.SchedulesAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private RecyclerView listResults;

    private ResultsAdapter adapter;

    private Gson gson;
    private final ResultsEntity entity = new ResultsEntity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        getResultGroups();
    }

    private void initComponents() {
        actionBar = getSupportActionBar();
        actionBar.setTitle("Resultados");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setSubtitle("Veja os novos resultados");

        final List<Result> results = entity.getResults();

        adapter = new ResultsAdapter(results, new ResultsAdapter.OnResultClickListener() {
            @Override
            public void onItemClick(View view) {
                Result result = (Result) view.getTag();
                Intent intent = new Intent(ResultsActivity.this, DetailResultActivity.class);
                intent.putExtra("result", result);
                startActivity(intent);
            }
        }, this);

        listResults = findViewById(R.id.list_results);
        listResults.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        listResults.setLayoutManager(layoutManager);
    }

    private void getResultGroups() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = App.DOMAIN+"centralrn/result/find_groups.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    List<Result> results = new ArrayList<Result>();
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        results.add(gson.fromJson(jsonObject.toString(), Result.class));
                    }

                    entity.setResults(results);
                    initComponents();
                } catch (JSONException e) {
                    Toast toast = new Toast(getApplicationContext());
                    toast.makeText(getApplicationContext(), "Erro ao extrair resultados.", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = new Toast(getApplicationContext());
                toast.makeText(getApplicationContext(), "Erro de servidor. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
