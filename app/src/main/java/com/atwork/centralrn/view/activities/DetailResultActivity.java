package com.atwork.centralrn.view.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.atwork.centralrn.view.extras.DetailResultAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DetailResultActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private RecyclerView listDetailResult;

    private Result result;
    private Gson gson;
    private DetailResultAdapter adapter;
    private final ResultsEntity entity = new ResultsEntity();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_result);

        result = (Result) this.getIntent().getSerializableExtra("result");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        getResults();
    }

    private void initComponents() {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Detalhes do resultado");
        actionBar.setSubtitle(result.getDatetime());

        List<Result> results = entity.getResults();

        adapter = new DetailResultAdapter(results, this);

        listDetailResult = findViewById(R.id.list_detail_result);
        listDetailResult.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listDetailResult.setLayoutManager(layoutManager);
    }

    private void getResults() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = App.DOMAIN+"centralrn/result/find.php";
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
                    toast.makeText(getApplicationContext(), "Erro ao extrair detalhes do resultado.", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = new Toast(getApplicationContext());
                toast.makeText(getApplicationContext(), "Erro de servidor. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("datetime", result.getDatetime());
                return params;
            }
        };

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
