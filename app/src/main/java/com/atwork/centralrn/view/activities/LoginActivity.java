package com.atwork.centralrn.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.atwork.centralrn.model.Contributor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText contributorCod;
    private Button btnEnter;

    private App app;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        app = App.getInstance();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        initComponents();
    }

    private void initComponents() {
        contributorCod = this.findViewById(R.id.contributor_cod);
        btnEnter = this.findViewById(R.id.btn_enter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String COD = contributorCod.getText().toString();

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = App.DOMAIN+"centralrn/contributor/find.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Contributor contributor = gson.fromJson(jsonObject.toString(), Contributor.class);

                            app.setupContributor(contributor);

                            SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("contributor_id", contributor.getCod());
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            Toast toast = new Toast(getApplicationContext());
                            toast.makeText(getApplicationContext(), "Erro ao utilizar o código do contribuidor, faça o login novamente.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = new Toast(getApplicationContext());
                        toast.makeText(getApplicationContext(), "Erro de servidor. Tente novamente mais tarde. ERRO: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("cod", COD);
                        return params;
                    }
                };

                queue.add(stringRequest);
            }
        });
    }
}
