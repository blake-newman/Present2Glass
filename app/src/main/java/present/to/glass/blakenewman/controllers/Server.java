package present.to.glass.blakenewman.controllers;

import present.to.glass.blakenewman.Main;
import present.to.glass.blakenewman.Presenter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
    private ServerSocket serverSocket;

    public Server(){
        if(serverSocket != null) return;
        try {
            serverSocket = new ServerSocket(7628);
            serverSocket.setReuseAddress(true);
            int num = 0;
            while(num++ < 50){
                addConnection();
            }
        } catch (IOException e) {
            System.out.println("ERROR - NOT ABLE TO START SERVER ANOTHER CLIENT IS OPEN");
            System.exit(0);
        }
    }

    private void addConnection(){
        Connection thread = new Connection();
        thread.start();
    }

    public void destroy(){
        if(serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private class Connection extends Thread{
        public Socket socket;
        public DataOutputStream out;
        public DataInputStream in;

        public void run(){
            if(serverSocket.isClosed()){
                return;
            }
            try {
                socket = serverSocket.accept();
                socket.setPerformancePreferences(1,0,0);
                Main.client.ip = socket.getInetAddress().toString();
                socket.setReuseAddress(true);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                int code = in.readInt();
                switch(code) {
                    case (1):
                        Main.createPresenter();
                        break;
                    case (2):
                        if(Presenter.context != null) {
                            Presenter.context.finish();
                        }
                        break;
                    case (3):
                        String note = in.readUTF();
                        Boolean stream = in.readBoolean();
                        Long time = in.readLong();
                        Presenter.update(note, stream, time);
                        break;
                    case(4):
                        System.out.println("Glass - Connection Ended");
                        Main.client.ip = "";
                        break;
                }
            } catch (IOException ignore) {

            } finally {
                if(socket != null && !socket.isClosed()){
                    try {
                        if(in != null){
                            in.close();
                        }
                        if(out != null){
                            out.close();
                        }
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                run();
            }

        }
    }
}