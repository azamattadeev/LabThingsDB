package inet.server;

import parsers.CommandParser;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by azamat on 14.11.17.
 */
public class Server extends Thread implements Closeable{
    private CommandParser commandParser;
    private DatagramChannel inChannel;

    private Server(CommandParser commandParser, DatagramChannel inChannel){
        this.commandParser = commandParser;
        this.inChannel = inChannel;
    }

    public static Server createServer(CommandParser commandParser, int port) {
        if (commandParser == null) {
            return null;
        }
        try {
            DatagramChannel inChannel = DatagramChannel.open();
            try{
                inChannel.bind(new InetSocketAddress(port));
            }catch (BindException bEx){
                System.out.println("Port " + port + " are busy");
                return null;
            }
            return new Server(commandParser, inChannel);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return null;
        }
    }


    public void run(){
        byte[] inByte = new byte[4096];
        ByteBuffer inBuffer = ByteBuffer.wrap(inByte);
        try {
            m:
            do {
                InetSocketAddress socketAddress = (InetSocketAddress) inChannel.receive(inBuffer);
                if(interrupted()) {
                    close();
                    break m;
                }
                int position = inBuffer.position();
                byte[] commandBytes = inByte.clone();
                new Thread() {
                    public void run() {
                        String ip = socketAddress.getHostName();
                        byte[] commandRezBytes = new byte[position];
                        for (int i = 0; i < position; i++) {
                            commandRezBytes[i] = commandBytes[i];
                        }
                        String commandRez = new String(commandRezBytes);
                        String[] split = commandRez.split(new String(new char[]{30}));
                        int backPort = Integer.parseInt(split[0]);
                        commandRez = split[1];
                        String rez = commandParser.parse(commandRez);
                        ByteBuffer answerBuffer = ByteBuffer.allocate(4096);
                        answerBuffer.put(rez.getBytes());
                        answerBuffer.rewind();
                        try {
                            DatagramChannel channel = DatagramChannel.open();
                            channel.send(answerBuffer, new InetSocketAddress(ip, backPort));
                        } catch (IOException ioEx) {
                            ioEx.printStackTrace();
                        }
                    }
                }.start();
                inBuffer.clear();
                inBuffer.rewind();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException iEx) {
                    iEx.printStackTrace();
                }
            }while (true);
        }catch (IOException ioEx){
            //ioEx.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            inChannel.close();
        }catch (IOException ioEx){
            ioEx.printStackTrace();
        }
    }

}
