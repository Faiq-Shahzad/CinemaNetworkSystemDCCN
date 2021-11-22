/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cinemanetworkapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 *
 * @author Ahmed
 */
public class client {
    
    static DatagramSocket clientSocket;
    
    public static void main(String[] args) throws SocketException, IOException, ClassNotFoundException, InterruptedException {
        Scanner input= new Scanner(System.in);
        
        clientSocket=new DatagramSocket(5000);
        
        Movie dataObject;
        
        while(true){

            //----------MAIN MENU-----------

            System.out.println("MAIN MENU");
            System.out.println("1. ADD MOVIE DATA");
            System.out.println("2. VIEW MOVIE RECORDS");
            System.out.println("3. SEARCH MOVIE RECORD by ID");
            System.out.println("4. EXIT");
            
            System.out.print("Enter Your Choice: ");
            int userIn = input.nextInt();

            if(userIn < 0 && userIn > 4){
                continue;
            }else if(userIn == 4){
                break;

            //----------ADD MOVIE-----------

            }else if (userIn == 1){
                System.out.print("Enter Movie Id: ");
                int id = input.nextInt();
                System.out.print("Enter Movie Name: ");
                String name = input.next();
                System.out.print("Enter Movie Rating: ");
                int rating = input.nextInt();
                System.out.print("Enter Movie Year: ");
                int year = input.nextInt();
                
                dataObject = new Movie(id, name, rating, year);
        
                dataObject.setOperation(0);

            //----------VIEW MOVIE RECORD-----------
                
            }else if (userIn == 2){
                dataObject=new Movie(0, "", 0, 0);
        
                dataObject.setOperation(1);

            //----------SEARCH MOVIE BY ID-----------

            }else{
                System.out.println("Enter Movie Id: ");
                int id = input.nextInt();
                dataObject=new Movie(id, "", 0, 0);
        
                dataObject.setOperation(2);
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(dataObject);

            byte[] data = baos.toByteArray();

            InetAddress IPAddress=InetAddress.getByName("DELL-021");

            DatagramPacket packet = new DatagramPacket(data, data.length, IPAddress, 7000);

            clientSocket.send(packet);
            
            System.out.println("Waiting For Response");
            
            Thread.sleep(1000);
            //Waiting For Response
            byte[] receiveData = new byte[1024];
        
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            clientSocket.receive(receivePacket);
            byte[] recData = receivePacket.getData();

            ByteArrayInputStream bais = new ByteArrayInputStream(recData);

            ObjectInputStream ois = new ObjectInputStream(bais);
            Object receivedObject = ois.readObject();
            
            if(receivedObject instanceof Message){
                Message msgObj = (Message) receivedObject;
                
                String msgData = msgObj.getData();
                
                if(msgData.contains("array")){
                    String[] msg = msgData.split("-");
                    int count=Integer.parseInt(msg[1]);
                    System.out.println("Total Objects: "+count+"\n");
                    while(count>0){
                        receiveMovieData();
                        count--;
                    }
                }else{
                    System.out.println("\n"+msgData);
                }
            }else if(receivedObject instanceof Movie){
                Movie movieData = (Movie)receivedObject;
                System.out.println(movieData.toString());
            }
            
            
            System.out.println("\nDo you want to continue (Y\\N)");
            String temp = input.next();
            if(temp.equalsIgnoreCase("N")){
                break;
            }
       
            
        }
        
    }
    
    public static void receiveMovieData() throws IOException, ClassNotFoundException{
        byte[] receiveData = new byte[1024];
        
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            clientSocket.receive(receivePacket);
            byte[] recData = receivePacket.getData();

            ByteArrayInputStream bais = new ByteArrayInputStream(recData);

            ObjectInputStream ois = new ObjectInputStream(bais);
            Movie receivedObject = (Movie) ois.readObject();
            System.out.println(receivedObject.toString());
            
    }
}
