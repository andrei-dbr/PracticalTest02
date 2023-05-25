package ro.pub.cs.systems.eim.practicaltest02;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;
import java.util.Date;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class CommunicationThread extends Thread {
    ServerThread serverThread;
    Socket socket;

    BufferedReader bufferedReader;
    PrintWriter printWriter;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            bufferedReader = Utilities.getReader(socket);
            printWriter = Utilities.getWriter(socket);

            // read input
            String line = bufferedReader.readLine();

            String operation = line.split(",")[0];
            String key = line.split(",")[1];
            String value = null;

            if (line.split(",").length > 2) {
                value = line.split(",")[2];
            }

            // request time

            HttpClient httpClient = new DefaultHttpClient();

            HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);

            HttpResponse httpResponse;

            try {
                httpResponse = httpClient. execute(httpGet);
            } catch (Exception e) {
                return;
            }

            HttpEntity httpGetEntity = httpResponse.getEntity();
            if (httpGetEntity == null) {
                return;
            }

            String pageSource = EntityUtils.toString(httpGetEntity);
            JSONObject content = new JSONObject(pageSource);

            String timeStringNow = content.getString("datetime");

            Time timeNow = Time.valueOf(timeStringNow.split("T")[1].split("\\.")[0]);

            System.out.println(timeNow);

            if (operation.equals("put")) {
                serverThread.dataCache.put(key, value);
                serverThread.timeCache.put(key, timeNow);

                printWriter.println("ok");

                return;
            }

            if (operation.equals("get")) {
                if (serverThread.dataCache.containsKey(key)) {

                    Time beforeTime = serverThread.timeCache.get(key);

                    if (timeNow.getTime() - beforeTime.getTime() < 15000) {
                        printWriter.println(serverThread.dataCache.get(key));

                    } else {
                        printWriter.println("none");
                    }



                } else {
                    printWriter.println("none");
                }

                return;
            }

            // search in cache

            // if not found, search on the internet

            // parse the response

            // add to cache


        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
