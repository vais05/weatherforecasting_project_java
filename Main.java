package main;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;


interface WeatherDataRetriever {
    String retrieveWeatherData(String city, String url);
}

abstract class AbstractWeatherDataRetriever implements WeatherDataRetriever {
    protected String apiKey;

    public AbstractWeatherDataRetriever(String apiKey) {
        this.apiKey = apiKey;
    }
}

class OpenWeatherMapRetriever extends AbstractWeatherDataRetriever {

    public OpenWeatherMapRetriever(String apiKey) {
        super(apiKey);
    }

    public String retrieveWeatherData(String city, String url) {
        try {
            String fullUrl = url + "?q=" + city + "&units=metric&appid=" + apiKey;

            URL apiUrl = new URL(fullUrl);

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

class WeatherForecastData {
    private String date;
    private String temperature;
    private String humidity;
    private String city;
    private String weatherCondition;

    public WeatherForecastData(String date, String temperature, String humidity, String city, String weatherCondition) {
        this.date = date;
        this.temperature = temperature;
        this.humidity = humidity;
        this.city = city;
        this.weatherCondition = weatherCondition;
    }

    public String getDate() {
        return date;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getCity() {
        return city;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }
}

public class Main {
    private static final String API_KEY = "873179812fcf5caeb8c1c66ee7fd268e"; 

    public static void main(String[] args) {
        WeatherDataRetriever retriever = new OpenWeatherMapRetriever(API_KEY);

        String city = getUserInput("Enter the country or city for weather forecasting:");

        String baseUrl = "http://api.openweathermap.org/data/2.5/forecast";

        String weatherData = retriever.retrieveWeatherData(city, baseUrl);

        if (weatherData != null) {
            List<WeatherForecastData> forecastDataList = parseWeatherData(weatherData, city);
            if (!forecastDataList.isEmpty()) {
                displayWeatherForecast(forecastDataList);
            } else {
                showMessageDialog("Failed to parse weather data.");
            }
        } else {
            showMessageDialog("Failed to retrieve weather data.");
        }
    }

    private static String getUserInput(String prompt) {
        return JOptionPane.showInputDialog(null, prompt);
    }

    private static void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static List<WeatherForecastData> parseWeatherData(String weatherData, String city) {
        List<WeatherForecastData> forecastDataList = new ArrayList<>();
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(weatherData, JsonObject.class);
            JsonArray forecastArray = jsonObject.getAsJsonArray("list");

            for (JsonElement element : forecastArray) {
                JsonObject forecastObject = element.getAsJsonObject();
                String date = forecastObject.get("dt_txt").getAsString();
                JsonObject mainObject = forecastObject.getAsJsonObject("main");
                String temperature = mainObject.get("temp").getAsString();
                String humidity = mainObject.get("humidity").getAsString();

                JsonArray weatherArray = forecastObject.getAsJsonArray("weather");
                JsonObject weatherObject = weatherArray.get(0).getAsJsonObject();
                String weatherCondition = weatherObject.get("main").getAsString();

                WeatherForecastData forecastData = new WeatherForecastData(date, temperature, humidity, city, weatherCondition);
                forecastDataList.add(forecastData);
            }

        } catch (JsonParseException e) {
            e.printStackTrace();
        }
        return forecastDataList;
    }

    private static void displayWeatherForecast(List<WeatherForecastData> forecastDataList) {
        StringBuilder message = new StringBuilder();
        message.append("Weather Forecast for ").append(forecastDataList.get(0).getCity()).append("\n\n");

        for (WeatherForecastData forecastData : forecastDataList) {
            message.append("Date: ").append(forecastData.getDate()).append("\n");
            message.append("Temperature: ").append(forecastData.getTemperature()).append(" °C\n");
            message.append("Humidity: ").append(forecastData.getHumidity()).append("%\n");
            message.append("Condition: ").append(getWeatherConditionIcon(forecastData.getWeatherCondition())).append("\n");
            message.append("\n");
        }

        JOptionPane.showMessageDialog(null, message.toString(), "Weather Forecast", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String getWeatherConditionIcon(String weatherCondition) {
        switch (weatherCondition) {
            case "Clear":
                return "☀"; 
            case "Clouds":
                return "☁"; 
            case "Rain":
                return "🌧"; 
            case "Snow":
                return "❄"; 
            default:
                return "🌤"; 
        }
    }
}
