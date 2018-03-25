package com.anonymous.finance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.anonymous.finance.Adapters.TransactionsAdapter;
import com.anonymous.finance.Classes.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ImageButton add, remove;
    private RecyclerView recyclerView;
    private android.support.v7.widget.Toolbar toolbar;

    private int mYear, mMonth, mDay;

    ArrayList<Transaction> transactions = new ArrayList<>();

    ProgressDialog progressDialog;

    private TextView total;

    public DatePickerDialog.OnDateSetListener date;
    public Calendar myCalendar;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    TransactionsAdapter adapter;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(sharedPreferences.getString(getString(R.string.shared_preference_username), "").contains("user")){
            CardView cardView = findViewById(R.id.card_view);
            cardView.setVisibility(View.INVISIBLE);

            // android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            // toolbar.setVisibility(View.INVISIBLE);
        }

        total = findViewById(R.id.total);

        add = findViewById(R.id.add);
        remove = findViewById(R.id.remove);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            getContents();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debit();
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                credit();
            }
        });

        registerForContextMenu(recyclerView);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int POS = item.getOrder();
        // int position = item.getIntent().getIntExtra("position", -1);
        if(item.getTitle() == "delete"){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
                    .setCancelable(false)
                    .setMessage("Are you sure you want to delete the transaction?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Transaction selectedTrans = transactions.get(POS);
                            deleteTransaction(selectedTrans.getUuid());
                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            return false;
        }
        return true;
    }

    private void deleteTransaction(String uuid) {
        new DeleteTransaction().execute(uuid);
    }

    private void credit() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_amount_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText amount = (EditText) promptsView
                .findViewById(R.id.prompt_amount);

        final EditText comment = promptsView.findViewById(R.id.prompt_comment);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("SUBMIT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                new DebitCredit().execute(amount.getText().toString(), comment.getText().toString(), "credit");
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    private void debit() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_amount_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText amount = (EditText) promptsView
                .findViewById(R.id.prompt_amount);

        final EditText comment = promptsView.findViewById(R.id.prompt_comment);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("SUBMIT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                new DebitCredit().execute(amount.getText().toString(), comment.getText().toString(), "debit");
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void getContents() throws ExecutionException, InterruptedException {

        transactions = new GetTransactions().execute().get();
        adapter = new TransactionsAdapter(this, transactions);
        recyclerView.setAdapter(adapter);

        String total_str = new GetTotal().execute().get();
        if(total_str.contains("-")){
            total.setText(total_str);
            total.setTextColor(Color.RED);
        } else if(total_str.contains("NA")){
            total.setText("NA");
        } else {
            total.setText(total_str);
            total.setTextColor(Color.parseColor("#008000"));
        }

        // Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show();
        Snackbar.make(findViewById(R.id.coordinatorLayout), "Data Updated", Snackbar.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.sign_out){
            editor.putBoolean(getString(R.string.shared_preference_login_boolean), false);
            editor.putString(getString(R.string.shared_preference_username), "");
            editor.putString(getString(R.string.shared_preference_password), "");
            editor.putString(getString(R.string.shared_preference_access_level), "");
            editor.commit();
            Intent toLogin = new Intent(MainActivity.this, LoginActivity.class);
            toLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(toLogin);
        } else if(item.getItemId() == R.id.refresh){

            // Toast.makeText(this, "refresh", Toast.LENGTH_SHORT).show();
            try {
                getContents();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if (item.getItemId() == R.id.reset){
            reset();
        } else if (item.getItemId() == R.id.filter){

            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            StringBuilder sort_date = new StringBuilder();

                            sort_date.append(year);
                            sort_date.append("-");
                            if(String.valueOf(monthOfYear).length() == 1){
                                sort_date.append("0" + String.valueOf(monthOfYear + 1));
                            } else {
                                sort_date.append(monthOfYear + 1);
                            }
                            sort_date.append("-");
                            sort_date.append(dayOfMonth);

                            sortList(sort_date.toString());
                            // Toast.makeText(MainActivity.this, "" + sort_date.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();

        }
        return true;
    }

    private void sortList(String date) {

        // Toast.makeText(this, "" + date, Toast.LENGTH_SHORT).show();
        new SortByDate().execute(date);

    }

    private void reset() {

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.reset_promt , null);

        final EditText rootUsername = promptsView.findViewById(R.id.root_username);
        final EditText rootPassword = promptsView.findViewById(R.id.root_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setView(promptsView);

        builder
                .setCancelable(false)
                .setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(rootUsername.getText().toString().contains("admin") && rootPassword.getText().toString().contains("admin")) {
                            new Reset().execute();
                        } else {
                            dialogInterface.dismiss();
                            // Toast.makeText(MainActivity.this, "username and password incorrect.", Toast.LENGTH_SHORT).show();
                            Snackbar.make(findViewById(R.id.coordinatorLayout), "username and password incorrect", Snackbar.LENGTH_SHORT).show();

                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private class GetTransactions extends AsyncTask<Void, Void, ArrayList<Transaction>>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("getting transactions...");
            progressDialog.show();
        }

        @Override
        protected ArrayList<Transaction> doInBackground(Void... voids) {
            ArrayList<Transaction> transactions = new ArrayList<>();

            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            try {

                URL url = new URL("http://fardeenpanjwani.com/money/get/get_transactions.php");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                String line;
                StringBuilder response = new StringBuilder();

                while((line = bufferedReader.readLine()) != null){
                    response.append(line);
                }

                if(response.toString().contains("error")){
                    // Toast.makeText(MainActivity.this, "Empty list returned", Toast.LENGTH_SHORT).show();
                    Snackbar.make(findViewById(R.id.coordinatorLayout), "The list seems to be empty", Snackbar.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return transactions;
                } else {

                    JSONArray root = new JSONArray(response.toString());
                    for(int i = 0; i < root.length(); i++){

                        JSONObject transaction = root.getJSONObject(i);
                        String username = transaction.getString("username");
                        String type = transaction.getString("type");
                        String comment = transaction.getString("comment");
                        String uuid = transaction.getString("uuid");
                        String amount = "";
                        if(type.contains("debit")) {
                            amount = "+ " + transaction.getString("amount");
                        } else if(type.contains("credit")){
                            amount = "- " + transaction.getString("amount");
                        }
                        String date = transaction.getString("date");
                        String board = transaction.getString("board");
                        String changed_balance = transaction.getString("changed_balance");
                        Transaction current_transaction = new Transaction(comment, type, amount, username, date, board, changed_balance, uuid);
                        transactions.add(current_transaction);

                    }

                    progressDialog.dismiss();
                    return transactions;

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                return transactions;
            } catch (IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                return transactions;
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                return transactions;
            } finally {
                if(httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
                if(bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class GetTotal extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("getting total...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {

            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            try {

                URL url = new URL("http://fardeenpanjwani.com/money/get/calculate_total.php");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                String line;
                StringBuilder response = new StringBuilder();

                while((line = bufferedReader.readLine()) != null){
                    response.append(line);
                }

                // Toast.makeText(MainActivity.this, "" + response.toString(), Toast.LENGTH_SHORT).show();

                progressDialog.dismiss();

                if(response.toString().isEmpty()){
                    return "NA";
                } else {
                    return response.toString();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                return "url: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                return "io: " + e.toString();
            }

        }
    }

    private class DebitCredit extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("running payment protocol....");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String amount = strings[0];
            String comment = strings[1];
            String type = strings[2];
            String username = MainActivity.this
                    .getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)
                    .getString(getString(R.string.shared_preference_username), "");

            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            BufferedWriter bufferedWriter = null;

            try {

                URL url = new URL("http://fardeenpanjwani.com/money/update/add_transaction.php");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));

                String data = URLEncoder.encode("username", "UTF-8") +"="+ URLEncoder.encode(username, "UTF-8") +"&"+
                        URLEncoder.encode("type", "UTF-8") +"="+ URLEncoder.encode(type, "UTF-8") +"&"+
                        URLEncoder.encode("amount", "UTF-8") +"="+ URLEncoder.encode(amount, "UTF-8") +"&"+
                        URLEncoder.encode("comment", "UTF-8") +"="+ URLEncoder.encode(comment, "UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();

                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                String line;
                StringBuilder response = new StringBuilder();

                while((line = bufferedReader.readLine()) != null) response.append(line);

                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "url: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "IO: " + e.toString();
            } finally {
                if(httpURLConnection != null) httpURLConnection.disconnect();
                if(bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if(s.contains("fail")){
                // Toast.makeText(MainActivity.this, "and error occurred when paying amount\n" + s, Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.coordinatorLayout), "and error occurred when paying amount\n" + s, Snackbar.LENGTH_SHORT).show();
                try {
                    getContents();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(s.contains("success")){
                // Toast.makeText(MainActivity.this, "payment successful", Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.coordinatorLayout), "payment successful", Snackbar.LENGTH_SHORT).show();
                try {
                    getContents();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class DeleteTransaction extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("deleting transaction...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String uuid = strings[0];

            HttpURLConnection httpURLConnection = null;
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {

                URL url = new URL("http://fardeenpanjwani.com/money/update/delete_transaction.php");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));

                String data = URLEncoder.encode("uuid", "UTF-8") +"="+ URLEncoder.encode(uuid, "UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();

                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                String line;
                StringBuilder response = new StringBuilder();

                while((line = bufferedReader.readLine()) != null) response.append(line);

                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "url: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "IO: " + e.toString();
            } finally {
                if(httpURLConnection != null) httpURLConnection.disconnect();
                if(bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if(s.contains("success")){
//                Toast.makeText(MainActivity.this, "entry successfully deleted", Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.coordinatorLayout), "transaction successfully deleted", Toast.LENGTH_LONG).show();
                try {
                    getContents();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(s.contains("fail")){
                // Toast.makeText(MainActivity.this, "an error occured while deleting transaction: \n" + s, Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.coordinatorLayout), "an error occured while deleting transaction: \n" + s, Snackbar.LENGTH_SHORT).show();
                try {
                    getContents();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Reset extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("resetting....");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            try {

                URL url = new URL("http://fardeenpanjwani.com/money/update/reset.php");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                String line;
                StringBuilder response = new StringBuilder();

                while((line = bufferedReader.readLine()) != null) response.append(line);

                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "url: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "IO: " + e.toString();
            } finally {
                if(httpURLConnection != null) httpURLConnection.disconnect();
                if(bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if(s.contains("success")){
                // Toast.makeText(MainActivity.this, "data successfully reset", Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.coordinatorLayout), "data reset successful", Snackbar.LENGTH_SHORT).show();
                try {
                    getContents();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (s.contains("fail")){
                // Toast.makeText(MainActivity.this, "an error occurred while resetting the data: \n" + s, Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.coordinatorLayout), "an error occurred while resetting the data: \n" + s, Snackbar.LENGTH_SHORT).show();
                try {
                    getContents();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SortByDate extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("applying filter..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String date = strings[0];

            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            BufferedWriter bufferedWriter = null;

            try {

                URL url = new URL("http://fardeenpanjwani.com/money/get/get_by_date.php");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));

                String data = URLEncoder.encode("date", "UTF-8") +"="+ URLEncoder.encode(date, "UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();

                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                String line;
                StringBuilder response = new StringBuilder();

                while((line = bufferedReader.readLine()) != null){
                    response.append(line);
                }

                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "url: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "IO: " + e.toString();
            } finally {
                if(httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
                if(bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            ArrayList<Transaction> transactionsFiltered = new ArrayList<>();
            if(s.contains("fail")){
                // Toast.makeText(MainActivity.this, "something went wrong: \n" + s, Toast.LENGTH_SHORT).show();
                Snackbar.make(findViewById(R.id.coordinatorLayout), "someting went wrong: \n" + s, Snackbar.LENGTH_SHORT).show();
            } else {

                try {

                    JSONArray root = new JSONArray(s);
                    if(root.length() > 0) {
                        for (int i = 0; i < root.length(); i++) {

                            JSONObject transaction = root.getJSONObject(i);
                            String username = transaction.getString("username");
                            String type = transaction.getString("type");
                            String comment = transaction.getString("comment");
                            String uuid = transaction.getString("uuid");
                            String amount = "";
                            if (type.contains("debit")) {
                                amount = "+ " + transaction.getString("amount");
                            } else if (type.contains("credit")) {
                                amount = "- " + transaction.getString("amount");
                            }
                            String date = transaction.getString("date");
                            String board = transaction.getString("board");
                            String changed_balance = transaction.getString("changed_balance");
                            Transaction current_transaction = new Transaction(comment, type, amount, username, date, board, changed_balance, uuid);
                            transactionsFiltered.add(current_transaction);

                        }

                        adapter = new TransactionsAdapter(MainActivity.this, transactionsFiltered);
                        recyclerView.setAdapter(adapter);

                    } else {
                        recyclerView.setVisibility(View.GONE);
                        ImageView error = findViewById(R.id.not_found);
                        error.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
