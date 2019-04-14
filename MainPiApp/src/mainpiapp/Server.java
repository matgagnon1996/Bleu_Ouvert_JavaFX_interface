/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpiapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author mathieu
 */
public class Server {
   
    private ServerSocket server;
    private int port;
    
    public Server(String serverAddr, int port) throws Exception
    {
        this.port = port;
        this.server = new ServerSocket(this.port, 0, InetAddress.getByName(serverAddr));
    }
    
    /*  
        Retourne le socket du client venant de se connecter
    */
    public Socket waitConnection() throws Exception
    {
        Socket client = this.server.accept();
        
        return client;
    }
    
    public ServerSocket getServer()
    {
        return this.server;
    }
   
    public int getServerPort()
    {
        return this.port;
    }
    
}
