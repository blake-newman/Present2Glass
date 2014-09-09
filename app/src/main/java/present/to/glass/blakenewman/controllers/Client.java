package present.to.glass.blakenewman.controllers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

import present.to.glass.blakenewman.Main;

public class Client {

    public String ip = "";

    private Socket createSocket() throws IOException {
        String host = ip;
        String ip = host == null || host.isEmpty() ? "255.255.255.0" : host.substring(1);
        Socket socket = new Socket();
        socket.setSoLinger(false, 0);
        socket.setReuseAddress(true);
        socket.setPerformancePreferences(1,0,0);
        socket.connect(new InetSocketAddress(ip, 7629), 500);

        return socket;
    }

    private DataInputStream createIn(Socket socket) throws IOException {
        return new DataInputStream(socket.getInputStream());
    }

    private DataOutputStream createOut(Socket socket) throws IOException {
        return new DataOutputStream(socket.getOutputStream());
    }

    private void closeSocket(Socket socket, DataInputStream in, DataOutputStream out) throws IOException{
        if(socket != null && !socket.isClosed()){
            if(out != null){
                out.flush();
                out.close();
            }
            if(in != null){
                in.close();
            }
            socket.close();
        } else {
            throw new IOException("SOCKET NOT EXISTS");
        }
    }

    public void stopPresentation(){
        if(ip.isEmpty()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Socket socket = createSocket();
                    DataOutputStream out = createOut(socket);
                    out.writeInt(2);
                    closeSocket(socket, null, out);
                } catch (IOException ignore){
                    run();
                }
            }
        }).start();
    }

    public void startPresentation() {
        if(ip.isEmpty()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Socket socket = createSocket();
                    DataOutputStream out = createOut(socket);
                    out.writeInt(1);
                    closeSocket(socket, null, out);
                } catch (IOException ignore){
                    run();
                }
            }

        }).start();
    }
}