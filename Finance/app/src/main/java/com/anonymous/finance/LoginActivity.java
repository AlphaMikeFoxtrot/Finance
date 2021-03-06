package com.anonymous.finance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private Button login;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

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
        setContentView(R.layout.activity_login);

        sharedPreferences = this.getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(sharedPreferences.getBoolean(getString(R.string.shared_preference_login_boolean), false)){
            // user already logged in
            Intent toMain = new Intent(LoginActivity.this, MainActivity.class);
            toMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(toMain);
        }

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin(username.getText().toString(), password.getText().toString());
            }
        });
    }

    private void checkLogin(String username, String password) {

        if(username.length() >= 4 && password.length() >= 4){
            new CheckLogin().execute(username, password);
        } else {
            Toast.makeText(this, "Username/Password invalid!", Toast.LENGTH_SHORT).show();
        }

    }

    private class CheckLogin extends AsyncTask<String, Void, String>{

        public String username, password;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("logging you in...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            username = strings[0];
            password = strings[1];

            HttpURLConnection httpURLConnection = null;
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {

                URL url = new URL(getString(R.string.API_LOGIN));
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));

                String data = URLEncoder.encode("username", "UTF-8") +"="+ URLEncoder.encode(username, "UTF-8") +"&"+
                        URLEncoder.encode("password", "UTF-8") +"="+ URLEncoder.encode(password, "UTF-8");

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
                return "URL: " + e.toString();
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
            if(s.contains("auth_fail")){
                Toast.makeText(LoginActivity.this, "username/password incorrect", Toast.LENGTH_SHORT).show();
            } else {
                editor.putString(getString(R.string.shared_preference_username), username);
                editor.putString(getString(R.string.shared_preference_password), password);
                editor.putString(getString(R.string.shared_preference_access_level), s);
                editor.putBoolean(getString(R.string.shared_preference_login_boolean), true);
                editor.commit();
                Intent toMain = new Intent(LoginActivity.this, MainActivity.class);
                toMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(toMain);
            }
        }
    }
}
