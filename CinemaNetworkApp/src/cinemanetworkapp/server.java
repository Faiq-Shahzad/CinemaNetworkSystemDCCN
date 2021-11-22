/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cinemanetworkapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 *
 * @author Ahmed
 */
public class server {
    static DatagramSocket serverSocket;
    
    static ArrayList<Movie> mylist = new ArrayList<>();
    
    public static void main(String[] args) throws SocketException, IOException, ClassNotFoundException {
        readDataFromFile();
        serverSocket = new DatagramSocket(7000);
        
        while(true){
            byte[] receiveData = new byte[1024];
        
            DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);

            serverSocket.receive(receivedPacket);

            byte[] data = receivedPacket.getData();
            
            InetAddress IPAddress = receivedPacket.getAddress();
            int port = receivedPacket.getPort();

            ByteArrayInputStream bais = new ByteArrayInputStream(data);

            ObjectInputStream ois = new ObjectInputStream(bais);
            Object receivedObject = ois.readObject();
            if(receivedObject instanceof Movie){
                
                Movie movieData = (Movie) receivedObject;
                
                int op = movieData.getOperation();
                
                switch (op){
                        case 0:
                            System.out.println("ADDING DATA TO SERVER");
                            System.out.println(movieData.toString());
                            mylist.add(movieData);
                            writeDataToFile();
                            sendStringResponse("DATA ADDED SUCCESSFULLY", IPAddress, port);
                            System.out.println("Response Sent");
                            break;
                        case 1:
                            System.out.println("Viewing All movies");
                            int movieCount = mylist.size();
                            if(movieCount<1){
                                sendStringResponse("No Movie Record Found", IPAddress, port);
                                break;
                            }
                            sendStringResponse("array-"+movieCount, IPAddress, port);
                            for (Movie mov: mylist){
                                sendMovieResponse(mov, IPAddress, port);
                                System.out.println("Sending an Object");
                            }
                            break;
                        case 2:
                            System.out.println("Searching by id");
                            int searchId = movieData.id;
                            
                            boolean isFound=false;
                            for (Movie mov: mylist){
                                if(mov.id == searchId){
                                sendMovieResponse(mov, IPAddress, port);
                                System.out.println("Sending an Object");
                                isFound=true;
                                break;
                                }
                                
                                
                            }
                            if(!isFound){
                                sendStringResponse("No Movie Record with ID Found", IPAddress, port);
                            }
                            break;
                        default:
                            System.out.println("Invalid Operation");
                            break;
                }
                
            }
            
        }
        
        
        
        
        
    }
    
    public static void sendStringResponse(String msg, InetAddress IPAddress, int port) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        Message msgObj = new Message(msg);
        oos.writeObject(msgObj);

        byte[] data = baos.toByteArray();


        DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, port);

        serverSocket.send(packet);
    }
    
    public static void sendMovieResponse(Movie movieObject, InetAddress IPAddress, int port) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(movieObject);

        byte[] data = baos.toByteArray();


        DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, port);

        serverSocket.send(packet);
    }
    
    public static void writeDataToFile() throws FileNotFoundException, IOException{
        FileOutputStream fos = new FileOutputStream("movies.txt");
        ObjectOutputStream oos = new ObjectOutputStream(fos);   
        oos.writeObject(mylist);
        oos.flush();
        oos.close();
    }
    
    public static void readDataFromFile() throws FileNotFoundException, IOException, ClassNotFoundException{
        
        File movieFile = new File("movies.txt");
        if(!movieFile.exists() || movieFile.length()==0){
            return;
        }
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("movies.txt"));

        mylist = (ArrayList<Movie>) ois.readObject();
        ois.close();
    }
}
