package com.atwork.centralrn.view.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ConfirmResultActivity extends AppCompatActivity {

    private ToggleButton toggleDate;
    private EditText ticketValue;
    private Button btnConfirmResult;
    private Spinner spinnerDate;
    private DatePicker datePicker;

    private ActionBar actionBar;

    private ScheduleEntity entity;
    private Schedule selectedSchedule;
    private Gson gson;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    private App app = App.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_result);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        entity = new ScheduleEntity();
        getSchedules();
    }

    private void initComponents() {
        actionBar = getSupportActionBar();
        actionBar.setTitle("Confirmar Resultado");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        toggleDate = findViewById(R.id.toggle_date);
        datePicker = findViewById(R.id.date_picker);
        ticketValue = findViewById(R.id.ticket_value);
        btnConfirmResult = findViewById(R.id.btn_confirm_result);

        toggleDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggleDate.isChecked()) {
                    datePicker.setVisibility(View.GONE);
                } else {
                    datePicker.setVisibility(View.VISIBLE);
                }
            }
        });

        btnConfirmResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ticketValue.getText().toString().length() > 0)
                search();
            }
        });

        List<String> schedules = new ArrayList<>();
        for(Schedule s : entity.getSchedules()) {
            schedules.add(s.getIdentifier()+" | "+s.getScheduleTime());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.standard_spinner_item, schedules);

        adapter.setDropDownViewResource(R.layout.standard_spinner_item);
        spinnerDate.setAdapter(adapter);
        spinnerDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String scheduleTime = ((TextView) view).getText().toString().split(" | ")[1];
                selectedSchedule = new Schedule();
                for(Schedule s : entity.getSchedules()) {
                    if(s.getScheduleTime().equals(scheduleTime)) {
                        selectedSchedule = s;
                    }
                }
            }
        });


    }

    private void getSchedules() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = App.DOMAIN+"centralrn/schedule/find.php";
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

    private void search() {
        boolean isToday = toggleDate.isChecked();
        String date = null;

        if(isToday) {
            date = getActualDate() + " " + selectedSchedule.getScheduleTime();
        } else {
            date = getActualDate() + " " + selectedSchedule.getScheduleTime();
        }

        final String datetime = date;

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = App.DOMAIN+"centralrn/result/get_wins.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    double wins = jsonObject.getDouble("wins");

                    showPrizes(wins);
                } catch (JSONException e) {
                    Toast toast = new Toast(getApplicationContext());
                    toast.makeText(getApplicationContext(), "Erro ao confirmar resultados.", Toast.LENGTH_SHORT).show();
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
            protected Map<String, String> getParams() throws AuthFailureError {
                String ticket = ticketValue.getText().toString();

                Map<String, String> params = new HashMap<>();
                params.put("datetime", datetime);
                params.put("ticket", ticket);

                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void showPrizes(final double wins) {
        if(wins > 0) {
            Locale myLocale = new Locale("pt", "BR");
            String formattedWins = NumberFormat.getCurrencyInstance(myLocale).format(wins);

            final Dialog dialog = new Dialog(ConfirmResultActivity.this);
            dialog.setContentView(R.layout.dialog_add_bet);

            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
            TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
            Button dialogButtonLeft = dialog.findViewById(R.id.dialog_button_left);
            Button dialogButtonRight = dialog.findViewById(R.id.dialog_button_right);

            dialogButtonLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if(app.getContributor().getCashier() >= wins) {
                        payPunter(wins);
                        findBluetoothDevice();
                        try {
                            openBluetoothPrinter();
                            printReceipt(wins);
                        } catch(IOException ioe) {
                            ioe.printStackTrace();
                            Toast toast = new Toast(getApplicationContext());
                            toast.makeText(getApplicationContext(), "Erro ao imprimir.", Toast.LENGTH_SHORT).show();
                        } finally {
                            finish();
                        }
                    } else {
                        Toast toast = new Toast(getApplicationContext());
                        toast.makeText(getApplicationContext(), "Dinheiro insuficiente, insira mais dinheiro no caixa.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            dialogButtonRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    finish();
                }
            });

            dialogTitle.setText("Parabéns!");
            dialogMessage.setText("Você ganhou "+formattedWins+".");
            dialogButtonLeft.setText("Retirar");
            dialogButtonRight.setText("Fechar");

            dialog.show();
        } else {

        }
    }

    private void payPunter(final double wins) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = App.DOMAIN+"centralrn/movements/add.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getBoolean("success")) {
                        double cashier = app.getContributor().getCashier();
                        cashier -= wins;

                        app.getContributor().setCashier(cashier);
                    } else {
                        Toast toast = new Toast(getApplicationContext());
                        toast.makeText(getApplicationContext(), "Erro ao fazer transação.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast toast = new Toast(getApplicationContext());
                    toast.makeText(getApplicationContext(), "Erro ao fazer transação.", Toast.LENGTH_SHORT).show();
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
                Map<String, String> params = new HashMap<>();
                params.put("contributor", app.getContributor().getCod()+"");
                params.put("value", (wins*-1)+"");

                return params;
            }
        };

        queue.add(stringRequest);
    }

    private String getActualDate() {
        GregorianCalendar gc = new GregorianCalendar();

        int year = gc.get(Calendar.YEAR);
        int month = gc.get(Calendar.MONTH);
        int day = gc.get(Calendar.DAY_OF_MONTH);

        String sep = "-";
        return year+sep+month+sep+day;
    }

    private void findBluetoothDevice() {

        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT,0);
            }

            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

            if(pairedDevice.size() > 0){
                for(BluetoothDevice pairedDev:pairedDevice){
                    bluetoothDevice = pairedDev;
                    break;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    private void openBluetoothPrinter() throws IOException{
        try {
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            beginListenData();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void beginListenData(){
        try{

            final Handler handler =new Handler();
            final byte delimiter=10;
            stopWorker =false;
            readBufferPosition=0;
            readBuffer = new byte[1024];

            thread=new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker){
                        try{
                            int byteAvailable = inputStream.available();
                            if(byteAvailable>0){
                                byte[] packetByte = new byte[byteAvailable];
                                inputStream.read(packetByte);

                                for(int i=0; i<byteAvailable; i++){
                                    byte b = packetByte[i];
                                    if(b==delimiter){
                                        byte[] encodedByte = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer,0,
                                                encodedByte,0,
                                                encodedByte.length
                                        );
                                        final String data = new String(encodedByte,"US-ASCII");
                                        readBufferPosition=0;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {

                                            }
                                        });
                                    }else{
                                        readBuffer[readBufferPosition++]=b;
                                    }
                                }
                            }
                        }catch(Exception ex){
                            stopWorker=true;
                        }
                    }

                }
            });

            thread.start();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void printReceipt(double value) throws  IOException {
        try {
            Locale myLocale = new Locale("pt", "BR");

            String msg = "";
            String sep = "--------------------------------";
            String nl = "\n";
            String tab = "   ";

            String contributorName = app.getContributor().getName();

            GregorianCalendar gc = new GregorianCalendar();
            int hour = gc.get(Calendar.HOUR);
            int min = gc.get(Calendar.MINUTE);

            String date = getActualDate();
            String time = hour+":"+min;

            String schedule = selectedSchedule.getIdentifier();

            msg += nl+nl+nl;
            msg += "Comprovante de Saque"+nl;
            msg += sep+nl;
            msg += "Pule:"+tab+tab+ticketValue.getText().toString()+nl;
            msg += sep+nl;
            msg += "Ag.:"+tab+contributorName+nl;
            msg += sep+nl;
            msg += date+tab+time+nl;
            msg += sep+nl;
            msg += schedule+nl;
            msg += sep+nl;

            String totalValue = NumberFormat.getCurrencyInstance(myLocale).format(value);

            msg += "VALOR RETIRADO"+tab+totalValue+nl;
            msg += sep+nl+nl+nl+nl;

            outputStream.write(msg.getBytes());
            app.resetBets();
            app.hasGiftChiliad = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
