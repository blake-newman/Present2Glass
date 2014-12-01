package present.to.glass.blakenewman.controllers;

import present.to.glass.blakenewman.Main;
import present.to.glass.blakenewman.Presenter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
    private ServerSocket serverSocket;
    private String ip;
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

    public void destroySS(){
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

        public void run(){
            try {
                socket = serverSocket.accept();
                socket.setPerformancePreferences(1,0,0);
                if(ip.isEmpty()) {
                    Main.client.ip = ip = socket.getInetAddress().toString();
                }
                socket.setReuseAddress(true);
                destroySS();
            } catch (IOException ignored) {
            } catch (NullPointerException ignored) {
            } finally {
                if(socket != null && !socket.isClosed()){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (ip.isEmpty()){
                    addConnection();
                }
            }
        }
    }
}