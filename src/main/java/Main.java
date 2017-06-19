import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Artem on 17.06.2017.
 * <p>
 * ВАЖНО: ЧИТАТЬ README
 */

public class Main {

    static JsonElement elementsFromCRM = null;

    public static void main(String[] args) {

        JSONObject objCRMJson = getJsonFromCRM();

      /*  TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());*/

        Date currentDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        List<User> listUserdWithPoints = new ArrayList<>();
        addUsersToList(elementsFromCRM, objCRMJson, dateFormat.format(currentDate), listUserdWithPoints);
        writeToJson(listUserdWithPoints);

    }

    private static JSONObject getJsonFromCRM() {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(
                    "http://progress.juja.com.ua/api/users/users");
            getRequest.addHeader("content-type", "application/json");

            org.apache.http.HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                elementsFromCRM = (new JsonParser()).parse(output);
            }
            httpClient.getConnectionManager().shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder jsonData = new StringBuilder();
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader("src/main/java/source/input_JSON.txt"));
            while ((line = br.readLine()) != null) {
                jsonData.append(line).append("\n");

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return new JSONObject(jsonData.toString());
    }

    private static void addUsersToList(JsonElement elementFromCRM, JSONObject obj, String nowAsISO, List<User> list) {
        for (int i = 0; i < elementFromCRM.getAsJsonArray().size(); i++)
            try {
                if (elementFromCRM.getAsJsonArray().get(i).getAsJsonObject().get("slack") != null) {
                    String user = String.valueOf(elementFromCRM.getAsJsonArray().get(i).getAsJsonObject().get("slack").getAsString());
                    String slack = String.valueOf(obj.getJSONObject(user).get("Слак"));//Так и должно быть
                    int points = Integer.parseInt(String.valueOf(obj.getJSONObject(user).get("Джуджики")));
                    String uuid = String.valueOf(elementFromCRM.getAsJsonArray().get(i).getAsJsonObject().get("uuid").getAsString());
                    list.add(new User(uuid, uuid, nowAsISO, points, "Start points", "START"));
                }
            } catch (JSONException e) {
                System.out.println("Пользователь для миграции джуджиков в output.json НЕ добавлен! По причине отсутствия слакнейма в CRM или отсутствия данных в одном и сверяемых файлах");
                System.out.println("name " + elementFromCRM.getAsJsonArray().get(i).getAsJsonObject().get("name"));
                System.out.println("slack " + elementFromCRM.getAsJsonArray().get(i).getAsJsonObject().get("slack"));
                System.out.println("uuid " + elementFromCRM.getAsJsonArray().get(i).getAsJsonObject().get("uuid"));
                System.out.println("skype " + elementFromCRM.getAsJsonArray().get(i).getAsJsonObject().get("skype"));
                System.out.println();

            }
    }

    private static void writeToJson(List<User> list) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<User>>() {
        }.getType();
        String json = gson.toJson(list, type);
        try {
            try (PrintWriter out = new PrintWriter("src/main/java/source/output.json")) {
                out.println(json);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
