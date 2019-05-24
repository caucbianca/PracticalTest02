package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private String address;
    private int port;
    private String query;
    private TextView showDataTextView;
    private int letters;

    private Socket socket;

    public ClientThread(String address, int port, String query, int letters, TextView showDataTextView) {
        this.address = address;
        this.port = port;
        this.query = query;
        this.showDataTextView = showDataTextView;
        this.letters = letters;
    }

    public static BufferedReader getReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e("abc", "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = getReader(socket);
            PrintWriter printWriter = getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e("abc", "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            printWriter.println(query + "," + letters);
            printWriter.flush();

            StringBuilder finfin = new StringBuilder();
            String weatherInformation;
            while ((weatherInformation = bufferedReader.readLine()) != null) {
                Log.d("abc", weatherInformation);
                finfin.append(weatherInformation);
            }
            final String finalizedWeateherInformation = finfin.toString();
            showDataTextView.post(new Runnable() {
                @Override
                public void run() {

                    showDataTextView.setText(finalizedWeateherInformation);
                }
            });
        } catch (IOException ioException) {
            Log.e("abc", "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.d("abc", "nu merge");
                }
            }
        }
    }
}