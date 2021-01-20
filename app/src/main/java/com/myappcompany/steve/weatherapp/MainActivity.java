package com.myappcompany.steve.weatherapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView weatherOutputTextView;
    private EditText cityEditText;
    private String weatherJSONString;
    private String weather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityEditText = findViewById(R.id.cityEditText);

        //makes it so that pressing enter on the EditText field is equivalent to pressing the button.
        cityEditText.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    getWeather(v);
                    return true;
                } else {
                    return false;
                }
            }
        });
        weatherOutputTextView = findViewById(R.id.weatherOutputTextView);


    }

    public void getWeather(View view) {
        cityEditText = findViewById(R.id.cityEditText);
        String city = cityEditText.getText().toString();
        Log.i("Edit text says ", city);

        //Hides the keyboard when an entry is made.
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(cityEditText.getWindowToken(),0);

        if(!(city.equals("") || city.isEmpty())) {
            setWeather(city);
        } else {
            Toast.makeText(getApplicationContext(), "You need to type a city!", Toast.LENGTH_SHORT).show();
        }
    }

    public class DownloadJSONTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection connection;
            Log.i("DownloadJSONTask was passed URL : ", urls[0]);

            try {
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1) {
                    result += (char) data;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Error in doInBackground()", e.toString());
                return null;
            }
        }

        @Override
        //You never want the doInBackground method to alter anything on the UI thread
        //So you can have this method which will do something with the result from
        //the doInBackground method when that method finishes.
        protected void onPostExecute(String resultFromDoInBackground) {
            super.onPostExecute(resultFromDoInBackground);

            try {
                weatherJSONString = resultFromDoInBackground;
                weather = getWeatherString();
                weatherOutputTextView = findViewById(R.id.weatherOutputTextView);
                Log.i("JSON", weather);
                if (!weather.isEmpty()) {
                    weatherOutputTextView.setText(weather);
                }
            } catch(Exception e) {
                e.printStackTrace();
                Log.i("Error in onPostExecute", e.toString());
            }
        }
    }

    public String getWeatherString() {
        HashMap<String, String> weatherMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        if(weatherJSONString.isEmpty()) return "";

        try {
            JSONObject jsonObject = new JSONObject(weatherJSONString);
            if(!jsonObject.has("main")) {
                return "";
            }

            sb.append("City name: ");
            sb.append(jsonObject.getString("name"));
            sb.append("\n");

            String coordInfo = jsonObject.getString("coord");
            JSONObject coordJSONObject = new JSONObject(coordInfo);

            sb.append("Longitude: ");
            sb.append(coordJSONObject.getString("lon"));
            sb.append("\n");

            sb.append("Latitude: ");
            sb.append(coordJSONObject.getString("lat"));
            sb.append("\n");

            String weatherInfo = jsonObject.getString("weather");
            JSONArray arr = new JSONArray(weatherInfo);
            for(int i=0; i < arr.length(); i++) {
                JSONObject jsonPart = arr.getJSONObject(i);
                String weatherType = "";
                if(jsonPart.length() > 1) {
                    weatherType += jsonPart.getString("main").substring(0, 1).toUpperCase() + jsonPart.getString("main").substring(1);
                }

                sb.append(weatherType);
                sb.append(": ");
                sb.append(jsonPart.getString("description"));
                sb.append("\n");
            }

            String tempInfo = jsonObject.getString("main");
            JSONObject tempJSONObject = new JSONObject(tempInfo);
            String tempString = tempJSONObject.getString("temp");

            Double tempK = Double.valueOf(tempString);
            Double tempC = Math.round(100.0 * (tempK - 273.15))/100.0;
            Double tempF = Math.round(100.0 * (tempC * (9.0/5.0) + 32.0))/100.0;
            sb.append("Temperature: ");
            sb.append(String.valueOf(tempC));
            sb.append("°C / ");
            sb.append(String.valueOf(tempF));
            sb.append("°F");
            sb.append("\n");

            weatherMap.put("Feels like temperature", tempJSONObject.getString("feels_like"));
            tempString = tempJSONObject.getString("feels_like");
            tempK = Double.valueOf(tempString);
            tempC = Math.round(100.0 * (tempK - 273.15))/100.0;
            tempF = Math.round(100.0 * (tempC * (9.0/5.0) + 32.0))/100.0;
            sb.append("Feels like: ");
            sb.append(String.valueOf(tempC));
            sb.append("°C / ");
            sb.append(String.valueOf(tempF));
            sb.append("°F");
            sb.append("\n");

            weatherMap.put("Minimum temperature", tempJSONObject.getString("temp_min"));
            tempString = tempJSONObject.getString("temp_min");
            tempK = Double.valueOf(tempString);
            tempC = Math.round(100.0 * (tempK - 273.15))/100.0;
            tempF = Math.round(100.0 * (tempC * (9.0/5.0) + 32.0))/100.0;
            sb.append("Minimum: ");
            sb.append(String.valueOf(tempC));
            sb.append("°C / ");
            sb.append(String.valueOf(tempF));
            sb.append("°F");
            sb.append("\n");

            weatherMap.put("Maximum temperature", tempJSONObject.getString("temp_max"));
            tempString = tempJSONObject.getString("temp_max");
            tempK = Double.valueOf(tempString);
            tempC = Math.round(100.0 * (tempK - 273.15))/100.0;
            tempF = Math.round(100.0 * (tempC * (9.0/5.0) + 32.0))/100.0;
            sb.append("Maximum: ");
            sb.append(String.valueOf(tempC));
            sb.append("°C / ");
            sb.append(String.valueOf(tempF));
            sb.append("°F");
            sb.append("\n");

            String windInfo = jsonObject.getString("wind");
            JSONObject windJSONObject = new JSONObject(windInfo);
            weatherMap.put("Wind speed", windJSONObject.getString("speed"));
            sb.append("Wind speed: ");
            sb.append(windJSONObject.getString("speed"));
            sb.append("\n");

            weatherMap.put("Wind direction degrees", windJSONObject.getString("deg"));
            sb.append("Wind direction: ");
            sb.append(windJSONObject.getString("deg"));
            sb.append("°");
            sb.append("\n");

            String cloudInfo = jsonObject.getString("clouds");
            JSONObject cloudJSONObject = new JSONObject(cloudInfo);
            weatherMap.put("Cloud coverage", cloudJSONObject.getString("all") + "%");
            sb.append("Cloud coverage: ");
            sb.append(cloudJSONObject.getString("all"));
            sb.append("%");

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Error in getWeatherString()", e.toString());
            return "";
        }
    }

    /**
     * Returns a string of weather type : weather value \n lines
     *
     * @param weatherMap a map from weather type to weather detail
     * @return converts the map into a giant string
     */
    public String mapToString(HashMap<String,String> weatherMap) {
        StringBuilder sb = new StringBuilder();

        for(String key : weatherMap.keySet()) {
            sb.append(key);
            sb.append(": ");
            sb.append(weatherMap.get(key));
            sb.append("\n");
        }

        //return all but last entry as a String, since the last entry will be an unnecessary newline.
        return sb.subSequence(0,sb.length()-1).toString();
    }

    public void setWeather(String city) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=f1070582dc959048110216397666c202";
        DownloadJSONTask downloadJSONTask = new DownloadJSONTask();
        try {
            downloadJSONTask.execute(url);
        } catch (Exception e) {
            Log.i("Error in setWeather", e.toString());
            e.printStackTrace();
        }
    }
}