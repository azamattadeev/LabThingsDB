import inet.server.Server;
import managers.DBCollectionManager;
import managers.ThingsCollectionManager;
import parsers.CommandParserAndExecutor;

import java.io.Closeable;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by azamat on 12.11.17.
 */
public class Main {
    public static void main(String[] args) {
        int port = -1;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Port must to be positive integer number");
            }
        }
        DBCollectionManager dbCollManager = DBCollectionManager.createDBCollectionManager("jdbc:postgresql:java_lab", "java", "sunjava");
        //ThingsCollectionManager thingsCollManager = new ThingsCollectionManager();
        //thingsCollManager.setUnderstudy(dbCollManager);
        Server server = null;
        if (dbCollManager != null) {
            CommandParserAndExecutor parserAndExecutor = new CommandParserAndExecutor(dbCollManager);
            if (port <= 0) {
                server = Server.createServer(parserAndExecutor, 6666);
            } else {
                server = Server.createServer(parserAndExecutor, port);
            }
            if (server != null) server.start();
        }
        Scanner scanner = new Scanner(System.in);
        String msg = "";
        while (!msg.equals("exit")) {
            msg = scanner.nextLine();
            try {
                Thread.sleep(100);
            } catch (InterruptedException iEx) {
                iEx.printStackTrace();
            }
        }
        server.interrupt();
    }
}
