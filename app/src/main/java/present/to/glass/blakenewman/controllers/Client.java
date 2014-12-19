package present.to.glass.blakenewman.controllers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

import present.to.glass.blakenewman.Main;
import present.to.glass.blakenewman.Presenter;

public class Client {

    public String ip = "";
    public Boolean listen = true;

    public void start(){
        createThreadedSocket();
    }

    public void destroy(){
        listen = false;
    }


    private void createThreadedSocket(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                DataInputStream in = null;
                DataOutputStream out = null;

                try{
                    socket = createSocket();
                    in = createIn(socket);
                    out = createOut(socket);

                    //Wait for initial response - will tell to start reading data
                    out.writeInt(0);
                    int response = in.readInt();
                    if (response == 1) {
                        if (!Presenter.alive){
                            Main.createPresenter();
                        }
                    } else if (response == 2) {
                        if(Presenter.alive){
                            Presenter.context.finish();
                            Main.context.finish();
                        }
                    } else if (response == 3){
                        if(Presenter.alive){
                            Presenter.update(in.readUTF(), in.readLong());
                        }
                    }
                } catch (IOException ignore){
                } finally {
                    if(listen){
                        closeSocket(socket, in, out);
                        createThreadedSocket();
                    }
                }
            }
        }).start();
    }

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

    private void closeSocket(Socket socket, DataInputStream in, DataOutputStream out){
        try {
            if(socket != null && !socket.isClosed()){
                if(out != null){
                    out.flush();
                    out.close();
                }
                if(in != null){
                    in.close();
                }
                socket.close();
            }
        } catch (IOException ignore) {
        }
    }

    private void createOutSocket(final int code){
        if(ip.isEmpty()) return;
        new Thread(new Runnable() {
            int dropped = 0;
            @Override
            public void run() {
                try{
                    Socket socket = createSocket();
                    DataOutputStream out = createOut(socket);
                    System.out.println(code);
                    out.writeInt(code);
                    closeSocket(socket, null, out);
                } catch (IOException ignore) {
                    if (dropped++ < 20){
                        run();
                    }
                }
            }

        }).start();
    }

    public void startPresentation() {
        createOutSocket(1);
    }

    public void stopPresentation(){
        createOutSocket(2);
    }

    public void nextNote() {
        createOutSocket(3);
    }

    public void nextSlide() {
        createOutSocket(4);
    }

    public void prevNote() {
        createOutSocket(5);
    }

    public void prevSlide() {
        createOutSocket(6);
    }
}