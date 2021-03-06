package com.da;

import com.da.util.JsonUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Client {

    private Clock currentClock;//clock of this server
    private int rate = 900;//clock rate

    public Client() throws IOException {
        //create a clock for this machine and set its current time
        currentClock = new Clock(new Date().getTime());
        rate += new Random().nextInt(200);

        //a thread to update clock with a specific frequency
        new Thread(()->{
            while(true){
                try {
                    Thread.sleep(1000);
                    currentClock.updateCurrentTime(1l * rate);
                    System.out.println("[current this follower's time: ]"+new Date(currentClock.getCurrentTime())+" [Rate: ]"+rate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Socket socket = new Socket("localhost",8090) ;

        //read the message received
        BufferedReader br = null ;
        br = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
        String jsonStr = null ;
        while( (jsonStr = br.readLine()) != null)  {
            Long amountToAdjust = JsonUtil.json2long(jsonStr);

            if(amountToAdjust == -1l){   //if -1, send current time as a response
                System.out.println("Receive time lookup request from master");
                sendCurrentTime(socket);
            }
            else if (amountToAdjust == 0l) {
                System.out.println("Time running is proper, so it is unchanged");
            }
            else{//else clock needs to be adjusted
                adjustClock(amountToAdjust);
            }
        }
        System.out.println("out of the while");
    }

    /**
     *    adjust time on this server
     */
    public void adjustClock(Long amountToAdjust) {
        if(amountToAdjust > 0l) {//adjust clock forward
            System.out.println("[Clock is slower. Should be adjust by: ]" + amountToAdjust);
            currentClock.updateCurrentTime(amountToAdjust);
        }
        else {//slow the clock
            System.out.println("[Clock is quicker. The skew is: ]" + amountToAdjust);
            rate = (int) (rate * 0.99);
            System.out.println("[Now clock rate has been adjust as: ]" + rate);
        }
    }

    /**
     *    after receive a request from master, give a response
     */
    public void sendCurrentTime(Socket socket){
        Long currentTime = currentClock.getCurrentTime();
        String jsonStr = JsonUtil.long2Json(currentTime);
        PrintWriter out;
        try  {
            //send the data to server
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())) ;
            out.println(jsonStr) ;
            out.flush();
        }catch(Exception e1)  {
            e1.printStackTrace();
        }
    }

    public static void main(String args[])  {
        try {
            new Client() ;
        } catch(Exception e)  {
            e.printStackTrace();
        }
    }
}

