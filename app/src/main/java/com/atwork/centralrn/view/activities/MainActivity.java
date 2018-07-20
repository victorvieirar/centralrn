package com.atwork.centralrn.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.atwork.centralrn.App;
import com.atwork.centralrn.R;
import com.atwork.centralrn.model.Contributor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button btnBet;
    private Button btnResult;
    private Button btnOptions;
    private Button btnExit;
    private TextView username;
    private LinearLayout mainOptions;
    private NestedScrollView anotherOptions;

    private int step;

    private Gson gson;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = App.getInstance();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        load();
    }

    private void load() {
        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        if (preferences.contains("contributor_id") && App.getInstance().getContributor() == null) {
            final int contributorID = preferences.getInt("contributor_id", -1);
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = App.DOMAIN+"centralrn/contributor/find.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Contributor contributor = gson.fromJson(jsonObject.toString(), Contributor.class);
                        app.setupContributor(contributor);

                        initComponents();
                    } catch (JSONException e) {
                        Toast toast = new Toast(getApplicationContext());
                        toast.makeText(getApplicationContext(), "Erro ao utilizar o código do contribuidor, faça o login novamente. Erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast toast = new Toast(getApplicationContext());
                    toast.makeText(getApplicationContext(), "Erro de servidor. Tente novamente mais tarde. ERRO: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("cod", contributorID + "");
                    return params;
                }
            };
            queue.add(stringRequest);
        } else {
            if (app.getContributor() == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                initComponents();
            }
        }
    }

    private void initComponents() {
        btnBet = findViewById(R.id.btn_bet);
        btnResult = findViewById(R.id.btn_result);
        btnOptions = findViewById(R.id.btn_options);
        btnExit = findViewById(R.id.btn_exit);

        mainOptions = findViewById(R.id.main_options);
        anotherOptions = findViewById(R.id.another_options);

        username = findViewById(R.id.username);
        username.setText(app.getContributor().getName().split(" ")[0]);

        btnBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BetActivity.class);
                startActivity(intent);
            }
        });

        btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                startActivity(intent);
            }
        });

        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainOptions.setVisibility(View.GONE);
                anotherOptions.setVisibility(View.VISIBLE);
                step++;
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(step == 1) {
            step--;
            mainOptions.setVisibility(View.VISIBLE);
            anotherOptions.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
