package com.atwork.centralrn.view.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.atwork.centralrn.App;
import com.atwork.centralrn.R;
import com.atwork.centralrn.model.Bet;
import com.atwork.centralrn.model.Ticket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MakeBetActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private NestedScrollView mainLayout;
    private EditText betSetter;
    private EditText betValue;
    private ToggleButton betValueDivision;
    private Button btnConfirm;

    private LinearLayout printLayout;
    private Button btnPrint;

    private LinearLayout giftChiliad;
    private Button btnConfirmGiftChiliad;
    private EditText giftChiliadSetter;

    private List<Integer> bets = new ArrayList<>();

    private boolean isBetsSetted = false;
    private int betGiftChiliad = -1;
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
        setContentView(R.layout.activity_make_bet);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        Intent intent = this.getIntent();
        if(intent.getExtras().getBoolean("finalizeBet")) {
            final Dialog dialog = new Dialog(MakeBetActivity.this);
            dialog.setContentView(R.layout.dialog_add_bet);

            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
            TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
            Button dialogButtonLeft = dialog.findViewById(R.id.dialog_button_left);
            Button dialogButtonRight = dialog.findViewById(R.id.dialog_button_right);

            dialogButtonLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent intent = new Intent(MakeBetActivity.this, BetActivity.class);
                    intent.putExtra("addingNewBet", true);
                    startActivity(intent);
                    finish();
                }
            });

            dialogButtonRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    app.closeTicket();
                    try {
                        findBluetoothDevice();
                        openBluetoothPrinter();
                        printBet(app.getBets());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });

            dialogTitle.setText("Sucesso!");
            dialogMessage.setText("Aposta criada com sucesso!");
            dialogButtonLeft.setText("Criar nova");
            dialogButtonRight.setText("Imprimir");

            dialog.show();
        } else {
            initComponents();
        }
    }

    private void initComponents() {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Configurar");
        actionBar.setSubtitle("Dados da aposta");

        final Bet bet = (Bet) getIntent().getSerializableExtra("bet");

        betSetter = findViewById(R.id.bet_setter);
        betSetter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                betSetter.removeTextChangedListener(this);
                switch(bet.getType()/10) {
                    case 1:
                        if (betSetter.getText().toString().length() == 2) {
                            groupParser(Integer.parseInt(betSetter.getText().toString()));
                            betSetter.setText("");
                        }
                        break;
                    case 2:
                        if(betSetter.getText().toString().length() == 4) {
                            chiliadParser(Integer.parseInt(betSetter.getText().toString()));
                            betSetter.setText("");
                        }
                    case 3:
                        break;
                }
                betSetter.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        betSetter.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                return false;
            }
        });

        mainLayout = findViewById(R.id.main_layout);

        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setEnabled(false);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBet();
            }
        });

        betValue = findViewById(R.id.bet_value);
        betValueDivision = findViewById(R.id.btn_value_division);

        betValue.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(isBetsSetted)
                    btnConfirm.setEnabled(true);
                else
                    btnConfirm.setEnabled(false);
                return false;
            }
        });
        betValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            private String current = "";
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(!s.toString().equals(current)) {
                    Locale myLocale = new Locale("pt", "BR");

                    betValue.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[R$,.]", "").trim();
                    cleanString = cleanString.replace(" ", "");
                    Double parsed = Double.parseDouble(cleanString);
                    String formatted = NumberFormat.getCurrencyInstance(myLocale).format((parsed / 100));
                    current = formatted;
                    betValue.setText(formatted);
                    betValue.setSelection(formatted.length());

                    betValue.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        printLayout = findViewById(R.id.print_layout);
        btnPrint = findViewById(R.id.btn_print);

        giftChiliad = findViewById(R.id.gift_chiliad);
        btnConfirmGiftChiliad = findViewById(R.id.btn_confirm_gift_chiliad);
        giftChiliadSetter = findViewById(R.id.gift_chiliad_setter);
    }

    private void createFloatingButton(int bet) {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout floatingButton = (RelativeLayout) inflater.inflate(R.layout.floating_button, null);

        TextView txtBet = floatingButton.findViewById(R.id.txt_bet);
        txtBet.setText(bet+"");

        int index = bets.size();
        bets.add(bet);

        Button btnDelete = floatingButton.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bets.remove(view.getTag());
                View v = (View) view.getParent();
                ((ViewGroup) v.getParent()).removeView(v);
                if(bets.size() == 0) {
                    betSetter.setEnabled(true);
                    btnConfirm.setEnabled(false);
                    isBetsSetted = false;
                }
            }
        });
        btnDelete.setTag(bet);

        ViewGroup viewGroup = findViewById(R.id.bets_panel);
        viewGroup.addView(floatingButton, index);

        isBetsSetted = true;
        btnConfirm.setEnabled(true);
    }

    private void groupParser(int bet) {
        boolean hasBet = false;

        for(int b : bets) {
            if(b == bet) {
                vibrate();
                hasBet = true;
            }
        }

        if(!hasBet && bet > 0 && bet < 26) {
            createFloatingButton(bet);
        } else {
            vibrate();
        }
    }

    private void chiliadParser(int bet) {
        boolean hasBet = false;

        for (int b : bets) {
            if (b == bet) {
                vibrate();
                hasBet = true;
            }
        }

        if (!hasBet && bet >= 0 && bet < 10000) {
            createFloatingButton(bet);
        } else {
            vibrate();
        }
    }

    private void saveBet() {
        long ticket;
        if(app.hasTicket()) {
            ticket = app.getTicket().getId();

            String datetime = generateDate();

            String cleanString = betValue.getText().toString().replaceAll("[R$,.]", "").trim();
            cleanString = cleanString.replace(" ", "");
            double value = Double.parseDouble(cleanString)/100;
            double total = value;

            if(betValueDivision.isChecked()) {
                value = value/bets.size();
            }

            if(total >= 2 && betGiftChiliad == -1 && !app.hasGiftChiliad) {
                giftChiliad.setVisibility(View.VISIBLE);
                mainLayout.setVisibility(View.GONE);
                btnConfirmGiftChiliad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        betGiftChiliad = Integer.parseInt(giftChiliadSetter.getText().toString());
                        app.hasGiftChiliad = true;
                        saveBet();
                    }
                });

                giftChiliadSetter.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        if(giftChiliadSetter.getText().toString().length() == 4)
                            btnConfirmGiftChiliad.setEnabled(true);
                        else
                            btnConfirmGiftChiliad.setEnabled(false);
                        return false;
                    }
                });
            } else {
                int count = 0;
                for (int b : bets) {
                    Bet modelBet = (Bet) getIntent().getSerializableExtra("bet");
                    Bet bet = new Bet();
                    bet.setDatetime(datetime);
                    bet.setBet(b);
                    bet.setTicket(ticket);
                    bet.setValue(value);
                    if(count == 0) bet.setGiftChiliad(betGiftChiliad);
                    bet.setSchedule(modelBet.getSchedule());
                    bet.setType(modelBet.getType());
                    bet.setContributor(modelBet.getContributor());

                    final int index = ++count;
                    app.addBet(bet);
                    sendBet(bet, index == bets.size());
                }
            }

        } else {
            createTicket();
        }
    }

    private void sendBet(final Bet bet, final boolean isLast) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = App.DOMAIN+"centralrn/bet/create.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getBoolean("success")) {
                        if(isLast) {
                            final Dialog dialog = new Dialog(MakeBetActivity.this);
                            dialog.setContentView(R.layout.dialog_add_bet);

                            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
                            TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
                            Button dialogButtonLeft = dialog.findViewById(R.id.dialog_button_left);
                            Button dialogButtonRight = dialog.findViewById(R.id.dialog_button_right);

                            dialogButtonLeft.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(MakeBetActivity.this, BetActivity.class);
                                    intent.putExtra("addingNewBet", true);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            dialogButtonRight.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    app.closeTicket();
                                    try {
                                        findBluetoothDevice();
                                        openBluetoothPrinter();
                                        printBet(app.getBets());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                            dialogTitle.setText("Sucesso!");
                            dialogMessage.setText("Aposta criada com sucesso!");
                            dialogButtonLeft.setText("Criar nova");
                            dialogButtonRight.setText("Imprimir");

                            dialog.show();
                        }
                    } else {
                        if(isLast) {
                            Dialog dialog = new Dialog(MakeBetActivity.this);
                            dialog.setContentView(R.layout.dialog_standard);

                            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
                            TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
                            Button dialogButton = dialog.findViewById(R.id.dialog_button);

                            dialogTitle.setText("Erro!");
                            dialogMessage.setText("Não foi possível cadastrar a aposta.\nTente novamente mais tarde.");
                            dialogButton.setText("Criar nova");

                            dialog.show();
                        }
                    }
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
                Map<String, String> params = bet.toMap();
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void createTicket() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = App.DOMAIN+"centralrn/ticket/generate.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    app.openTicket(new Ticket(jsonObject.getLong("ticket")));
                    saveBet();
                } catch (JSONException e) {
                    Toast toast = new Toast(getApplicationContext());
                    toast.makeText(getApplicationContext(), "Erro ao criar ticket. Erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = new Toast(getApplicationContext());
                toast.makeText(getApplicationContext(), "Erro de servidor. Tente novamente mais tarde. ERRO: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long milliseconds = 30;
        vibrator.vibrate(milliseconds);
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

    private void printBet(List<Bet> bets) throws  IOException {
        try {
            Locale myLocale = new Locale("pt", "BR");

            String msg = "";
            String sep = "--------------------------------";
            String nl = "\n";
            String tab = "   ";

            String contributorName = app.getContributor().getName();

            String date = bets.get(0).getDatetime().split(" ")[0];
            String time = bets.get(0).getDatetime().split(" ")[1];

            String schedule = bets.get(0).getSchedule();

            msg += nl+nl+nl;
            msg += "Pule:"+tab+tab+bets.get(0).getTicket()+nl;
            msg += sep+nl;
            msg += "Ag.:"+tab+contributorName+nl;
            msg += sep+nl;
            msg += date+tab+time+nl;
            msg += sep+nl;
            msg += schedule+nl;
            msg += "#"+tab+"Aposta"+tab+"R$"+nl+sep+nl;
            msg += nl+nl+nl;

            int index = 0;
            double total = 0;
            for(Bet b : bets) {
                index++;
                String position = index+"º";
                String betNumber = b.getBet()+"";

                String value = NumberFormat.getCurrencyInstance(myLocale).format(b.getValue());

                total += b.getValue();

                msg += position+tab+betNumber+tab+value+nl;
            }

            String totalValue = NumberFormat.getCurrencyInstance(myLocale).format(total);
            msg += sep+nl;
            msg += "TOTAL"+tab+tab+totalValue+nl;
            msg += sep+nl;

            outputStream.write(msg.getBytes());
            app.resetBets();
            app.hasGiftChiliad = false;

            double cashier = app.getContributor().getCashier();
            cashier += total;
            app.getContributor().setCashier(cashier);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String generateDate() {
        GregorianCalendar gc = new GregorianCalendar();
        int year = gc.get(Calendar.YEAR);
        int month = gc.get(Calendar.MONTH);
        int day = gc.get(Calendar.DAY_OF_MONTH);

        int hour = gc.get(Calendar.HOUR);
        int min = gc.get(Calendar.MINUTE);

        String dateSep = "-";
        String timeSep = ":";

        return year+dateSep+month+dateSep+day+" "+hour+timeSep+min;
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
