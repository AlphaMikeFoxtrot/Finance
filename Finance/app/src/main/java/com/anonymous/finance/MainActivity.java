package com.anonymous.finance;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.anonymous.finance.Adapters.TransactionsAdapter;
import com.anonymous.finance.Classes.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ImageButton add, remove;
    private RecyclerView recyclerView;
    private android.support.v7.widget.Toolbar toolbar;

    ProgressDialog progressDialog;

    private TextView total;

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
                // Toast.makeText(MainActivity.this, "ADD", Toast.LENGTH_SHORT).show();
                addAmount();
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "REMOVE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAmount() {
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
                                Toast.makeText(MainActivity.this, "" + amount.getText().toString() + "\n" + comment.getText().toString(), Toast.LENGTH_SHORT).show();
                                try {
                                    getContents();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

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

        adapter = new TransactionsAdapter(this, new GetTransactions().execute().get());
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

        }
        return true;
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
                    Toast.makeText(MainActivity.this, "Empty list returned", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return transactions;
                } else {

                    JSONArray root = new JSONArray(response.toString());
                    for(int i = 0; i < root.length(); i++){

                        JSONObject transaction = root.getJSONObject(i);
                        String username = transaction.getString("username");
                        String type = transaction.getString("type");
                        String comment = transaction.getString("comment");
                        String amount = "";
                        if(type.contains("debit")) {
                            amount = "+ " + transaction.getString("amount");
                        } else if(type.contains("credit")){
                            amount = "- " + transaction.getString("amount");
                        }
                        String date = transaction.getString("date");
                        String board = transaction.getString("board");
                        String changed_balance = transaction.getString("changed_balance");
                        Transaction current_transaction = new Transaction(comment, type, amount, username, date, board, changed_balance);
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

                URL url = new URL("http://fardeenpanjwani.com/money/get/get_total.php");
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
}
