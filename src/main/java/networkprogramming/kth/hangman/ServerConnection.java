package networkprogramming.kth.hangman;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import networkprogramming.kth.hangman.common.Message;

public class ServerConnection implements Runnable
{
    private final InetAddress host;
    private final int port;
    protected final CommunicationHandler gui;
    private PrintWriter toServer;
    private BufferedReader fromServer;
    //private ObjectInputStream fromServer;

    public ServerConnection(final CommunicationHandler gui, InetAddress host, int port)
    {
        this.host = host;
        this.port = port;
        this.gui = gui;
    }

    @Override
    public void run()
    {
        listen();
    }

    void connect()
    {
        try
        {
            Socket socket = new Socket("192.168.10.245", port);
            toServer = new PrintWriter(socket.getOutputStream(),true);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //fromServer = new ObjectInputStream(socket.getInputStream());
        } catch (UnknownHostException e)
        {
            System.err.println("Don't know about host: " + host + ".");
            System.exit(1);
        } catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: "
                    + host + ".");
            System.exit(1);
        }
    }
    public void sendGuess(String guess) throws IOException, NullPointerException {
        toServer.println(guess);
        toServer.flush();
    }
    public void startGame() throws IOException {
        toServer.println("start");
        toServer.flush();
    }


    public void listen() {
        while(true){
            try {
                //Message message = (Message) fromServer.readObject();
                String mes = fromServer.readLine();
                String[] parts = mes.split("#");
                Message message = new Message(parts[1].toCharArray(),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),parts[0]);
                gui.updateView(message);
            } catch (Throwable connectionFailure) {
                gui.quitGame();
            }
        }
    }


    }
