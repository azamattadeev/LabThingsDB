package inet.client;

import commands.Commands;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;

/**
 * Created by azamat on 12.11.17.
 */
public class Connector implements Closeable {
    private int myPort;
    private InetAddress serverAddress;
    private int serverPort;
    private DatagramSocket socket;

    public static final String DELIMITER = new String(new char[]{30});

    private Connector(InetAddress serverAddress, int serverPort, DatagramSocket socket, int myPort){
        this.myPort = myPort;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.socket = socket;
    }

    public static Connector createConnector(InetAddress serverAddress, int serverPort){
        DatagramSocket socket = null;
        int port = 0 ;
        try{
            socket = new DatagramSocket();
            port = ((InetSocketAddress)socket.getLocalSocketAddress()).getPort();
        }catch (SocketException ex){
            ex.printStackTrace();
        }
        try{
            String message = port + DELIMITER + Commands.createConnectCommand();
            byte[] registerBytes = message.getBytes();
            socket.setSoTimeout(1000);
            DatagramPacket outPacket = new DatagramPacket(registerBytes, registerBytes.length, serverAddress, serverPort);
            byte[] inBytes = new byte[1024];
            DatagramPacket inPacket = new DatagramPacket(inBytes,inBytes.length);
            boolean ok = false;
            int count = 0;
            String rez = "";
            while(!ok && count<=2) {
                try {
                    socket.send(outPacket);
                    socket.receive(inPacket);
                    byte[] answerBytes = inPacket.getData();
                    rez = new String(answerBytes);
                    ok = true;
                } catch (SocketTimeoutException steEx) {
                    count++;
                }
            }
            if (!ok) {
                if(socket != null) socket.close();
                return null;
            }
            rez = rez.trim();
            if(rez.equals("ready")) {
                return new Connector(serverAddress, serverPort, socket, port);
            }else{
                if(socket != null) socket.close();
                return null;
            }
        }catch (SocketException ex){
            if(socket!=null) socket.close();
            return null;
        }catch (IOException ioEx){
            if(socket!=null) socket.close();
            return null;
        }
    }

    public String send(String command){
        String msg = myPort + DELIMITER + command;
        byte[] msgBytes = msg.getBytes();
        DatagramPacket outPacket = new DatagramPacket(msgBytes, msgBytes.length, serverAddress, serverPort);
        int count = 0;
        while(count <= 2) {
            try {
                byte[] inBytes = new byte[4096];
                DatagramPacket inPacket = new DatagramPacket(inBytes, inBytes.length);
                socket.send(outPacket);
                socket.receive(inPacket);
                int countPosition = 0;
                while(inBytes[countPosition] != 0) countPosition++;
                countPosition--;
                byte[] rezBytes = new byte[countPosition+1];
                for(int i = 0; i < rezBytes.length; i++){
                    rezBytes[i] = inBytes[i];
                }
                return new String(rezBytes);
            } catch (SocketTimeoutException steEx) {
                count++;
            }catch (IOException ex){

            }
        }
        return "Server is not responding!";
    }

    public static byte[] getBytesIP(String ip){
        if (isValidIP(ip)){
            String[] stringBytes = ip.split("\\.");
            int[] intIP = new int[4];
            for(int i = 0; i < 4; i++) {
                intIP[i] = Integer.parseInt(stringBytes[i]);
            }
            byte[] rez = new byte[4];
            for (int i = 0; i < 4; i++){
                if (intIP[i] > 127) intIP[i]-=256;
                rez[i] = (byte) intIP[i];
            }
            return rez;
        }else {
            return null;
        }
    }

    public static boolean isValidIP(String ip){
        return ip.equals("localhost") || ip.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    }


    public void close(){
        socket.close();
    }

}
