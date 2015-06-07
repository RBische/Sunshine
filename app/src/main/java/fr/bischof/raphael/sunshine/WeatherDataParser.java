package fr.bischof.raphael.sunshine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataParser {

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        JSONArray list = new JSONObject(weatherJsonStr).getJSONArray("list");
        JSONObject detailsDay = list.getJSONObject(dayIndex);
        JSONObject temp = detailsDay.getJSONObject("temp");
        double max = temp.getDouble("max");
        return max;
    }

}

