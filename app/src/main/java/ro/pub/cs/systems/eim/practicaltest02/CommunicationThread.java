package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class CommunicationThread extends Thread {
    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    public static BufferedReader getReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }


    @Override
    public void run() {
        if (socket == null) {
            Log.e("abc", "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        Log.d("abc", "Started Communication Thread");
        try {
            BufferedReader bufferedReader = getReader(socket);
            PrintWriter printWriter = getWriter(socket);

            if (bufferedReader == null || printWriter == null) {
                Log.e("abc", "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i("abc", "[COMMUNICATION THREAD] Waiting for parameters from client (word / minimum length!");


            // We read the first query sent in the ClientThread
            String entireInput = bufferedReader.readLine();
            String[] tokens = entireInput.split(",");

            String query1 = tokens[0];
            String lettersString = tokens[1];

            if (query1 == null || query1.isEmpty() || lettersString == null || lettersString.isEmpty()) {
                Log.e("abc", "[COMMUNICATION THREAD] Error receiving parameters from client (word / minimum length!");
                return;
            }

            OwnContainer responseData;
            String result;

            Log.i("abc", "[COMMUNICATION THREAD] Getting the information from the webservice...");
            HttpClient httpClient = new DefaultHttpClient();
            // In case of POST change to HttpPost and remover the arghuments from the urkl
            HttpGet httpPost = new HttpGet("http://services.aonaware.com/CountCheatService/CountCheatService.asmx/LetterSolutionsMin?anagram=" + query1 + "&minLetters=" + lettersString);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String pageSourceCode = httpClient.execute(httpPost, responseHandler);
            if (pageSourceCode == null) {
                Log.e("abc", "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                return;
            }

            StringBuilder allAnagrams = new StringBuilder();
            while (!pageSourceCode.isEmpty()) {
                int start = pageSourceCode.indexOf("<string>");
                if (start < 0)
                    break;

                int end = pageSourceCode.indexOf("</string>");
                if (end < 0)
                    break;

                String querryData = pageSourceCode.substring(start + "<string>".length(), end);
                allAnagrams.append(querryData);
                allAnagrams.append(" ");

                pageSourceCode = pageSourceCode.substring(end + "</string>".length());
            }

            if (allAnagrams.toString().trim().isEmpty())
                allAnagrams.append("No anagrams found! :(");

            responseData = new OwnContainer(allAnagrams.toString());
            result = responseData.queryResponse1;

            // Send the data to the client
            printWriter.println(result);
            printWriter.flush();

            socket.close();
        }catch (Exception e){
            Log.d("abc", "Exceptie: + " + e);
        }
    }
}