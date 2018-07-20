package com.atwork.centralrn.view.activities;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.atwork.centralrn.model.Bet;
import com.atwork.centralrn.model.Schedule;
import com.atwork.centralrn.model.ScheduleEntity;
import com.atwork.centralrn.view.extras.SchedulesAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BetActivity extends AppCompatActivity implements View.OnClickListener {

    private ActionBar actionBar;
    private LinearLayout scheduleLayout;
    private RecyclerView listSchedule;

    private NestedScrollView typesLayout;
    private Button btnGroup;
    private Button btnChiliad;
    private Button btnHundred;
    private Button btnTen;
    private Button btnChiliadHundred;

    private NestedScrollView styleLayout;
    private Button btnSingleBet;
    private Button btnFiveFirst;
    private Button btnFiveLast;
    private Button btnSecondToFifth;
    private Button btnSecondToLast;
    private Button btnSixthToLast;

    private SchedulesAdapter adapter;
    private ScheduleEntity entity;
    private Schedule selectedSchedule;
    private int selectedType;

    private int step = 0;

    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bet);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        entity = new ScheduleEntity();

        Intent intent = this.getIntent();
        boolean addingNewBet = false;
        try {
            addingNewBet = intent.getExtras().getBoolean("addingNewBet");
        } catch(NullPointerException npe) {
            //do nothing
        }

        if(!addingNewBet && App.getInstance().hasTicket()) {
            final Dialog dialog = new Dialog(BetActivity.this);
            dialog.setContentView(R.layout.dialog_add_bet);

            ImageView dialogIcon = dialog.findViewById(R.id.dialog_icon);
            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
            TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
            Button dialogButtonLeft = dialog.findViewById(R.id.dialog_button_left);
            Button dialogButtonRight = dialog.findViewById(R.id.dialog_button_right);

            dialogButtonLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialogButtonRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(BetActivity.this, MakeBetActivity.class);
                    intent.putExtra("finalizeBet", true);
                    startActivity(intent);
                    finish();
                }
            });

            dialogIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_warning_24dp));
            dialogIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);

            dialogTitle.setText("Pule aberto!");
            dialogMessage.setText("Existe um pule aberto no sistema, você deseja continuar utilizando ou encerrar e imprimir?");
            dialogButtonLeft.setText("Continuar");
            dialogButtonRight.setText("Imprimir");

            dialog.show();
        }

        getAvailableSchedules();
    }

    private void initComponents() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Apostas");
        actionBar.setSubtitle("Faça sua aposta");

        scheduleLayout = findViewById(R.id.schedule_layout);
        scheduleLayout.setVisibility(View.VISIBLE);

        final List<Schedule> schedules = entity.getSchedules();

        adapter = new SchedulesAdapter(schedules, new SchedulesAdapter.OnScheduleClickListener() {
            @Override
            public void onItemClick(View view) {
                selectedSchedule = (Schedule) view.getTag();
                scheduleLayout.setVisibility(View.GONE);
                typesLayout.setVisibility(View.VISIBLE);
                step++;
            }
        }, this);

        listSchedule = findViewById(R.id.list_schedule);
        listSchedule.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        listSchedule.setLayoutManager(layoutManager);

        typesLayout = findViewById(R.id.types_layout);
        typesLayout.setVisibility(View.GONE);

        btnGroup = findViewById(R.id.btn_group);
        btnChiliad = findViewById(R.id.btn_chiliad);
        btnHundred = findViewById(R.id.btn_hundred);
        btnTen = findViewById(R.id.btn_ten);
        btnChiliadHundred = findViewById(R.id.btn_chiliad_hundred);

        btnGroup.setOnClickListener(this);
        btnChiliad.setOnClickListener(this);
        btnHundred.setOnClickListener(this);
        btnTen.setOnClickListener(this);
        btnChiliadHundred.setOnClickListener(this);

        styleLayout = findViewById(R.id.style_layout);
        styleLayout.setVisibility(View.GONE);

        btnSingleBet = findViewById(R.id.btn_single_bet);
        btnFiveFirst = findViewById(R.id.btn_5_first);
        btnFiveLast = findViewById(R.id.btn_5_last);
        btnSecondToFifth = findViewById(R.id.btn_2_5);
        btnSecondToLast = findViewById(R.id.btn_2_10);
        btnSixthToLast = findViewById(R.id.btn_6_10);

        btnSixthToLast.setVisibility(View.GONE);

        btnSingleBet.setOnClickListener(this);
        btnFiveFirst.setOnClickListener(this);
        btnFiveLast.setOnClickListener(this);
        btnSecondToFifth.setOnClickListener(this);
        btnSecondToLast.setOnClickListener(this);
        btnSixthToLast.setOnClickListener(this);
    }

    private void getAvailableSchedules() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = App.DOMAIN+"centralrn/schedule/find_available.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    List<Schedule> schedules = new ArrayList<Schedule>();
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        schedules.add(gson.fromJson(jsonObject.toString(), Schedule.class));
                    }

                    entity.setSchedules(schedules);
                    initComponents();
                } catch (JSONException e) {
                    Toast toast = new Toast(getApplicationContext());
                    toast.makeText(getApplicationContext(), "Erro ao extrair horários.", Toast.LENGTH_SHORT).show();
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

    private void selectType(int type) {
        typesLayout.setVisibility(View.GONE);
        styleLayout.setVisibility(View.VISIBLE);
        step++;

        switch (type) {
            case 1:
                btnSingleBet.setVisibility(View.VISIBLE);
                btnFiveFirst.setVisibility(View.VISIBLE);
                btnFiveLast.setVisibility(View.VISIBLE);
                btnSecondToFifth.setVisibility(View.VISIBLE);
                btnSecondToLast.setVisibility(View.VISIBLE);
                btnSixthToLast.setVisibility(View.GONE);
                break;
            case 2:
                btnSingleBet.setVisibility(View.GONE);
                btnFiveFirst.setVisibility(View.GONE);
                btnFiveLast.setVisibility(View.GONE);
                btnSecondToFifth.setVisibility(View.GONE);
                btnSecondToLast.setVisibility(View.GONE);
                btnSixthToLast.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void createBet(int type) {
        Bet bet = new Bet();
        bet.setType(type);
        bet.setContributor(App.getInstance().getContributor().getCod());
        bet.setSchedule(selectedSchedule.getScheduleTime());

        Intent intent = new Intent(BetActivity.this, MakeBetActivity.class);
        intent.putExtra("bet", bet);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_group:
                selectType(1);
                selectedType = 10;
                break;
            case R.id.btn_chiliad:
                selectType(1);
                selectedType = 20;
                break;
            case R.id.btn_hundred:
                selectType(1);
                selectedType = 30;
                break;
            case R.id.btn_ten:
                selectType(1);
                selectedType = 40;
                break;
            case R.id.btn_chiliad_hundred:
                selectType(1);
                selectedType = 50;
                break;

            case R.id.btn_single_bet:
                createBet(selectedType+1);
                break;
            case R.id.btn_5_first:
                createBet(selectedType+2);
                break;
            case R.id.btn_5_last:
                createBet(selectedType+3);
                break;
            case R.id.btn_2_5:
                createBet(selectedType+4);
                break;
            case R.id.btn_2_10:
                createBet(selectedType+5);
                break;
            case R.id.btn_6_10:
                createBet(selectedType+6);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                switch (step) {
                    case 0:
                        finish();
                        step--;
                        break;
                    case 1:
                        typesLayout.setVisibility(View.GONE);
                        scheduleLayout.setVisibility(View.VISIBLE);
                        selectedSchedule = null;
                        step--;
                        break;
                    case 2:
                        styleLayout.setVisibility(View.GONE);
                        typesLayout.setVisibility(View.VISIBLE);
                        step--;
                        break;
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        switch (step) {
            case 0:
                super.onBackPressed();
                step--;
                break;
            case 1:
                typesLayout.setVisibility(View.GONE);
                scheduleLayout.setVisibility(View.VISIBLE);
                selectedSchedule = null;
                step--;
                break;
            case 2:
                styleLayout.setVisibility(View.GONE);
                typesLayout.setVisibility(View.VISIBLE);
                step--;
                break;
        }
    }
}
