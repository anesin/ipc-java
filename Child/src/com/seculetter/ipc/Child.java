package com.seculetter.ipc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Child {

  public static void main(String[] args) {
    try {
      ObjectInputStream objectInputStream = new ObjectInputStream(System.in);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(System.out);
      objectOutputStream.flush();

      while (true) {
        Message receivedMessage = (Message) objectInputStream.readObject();
        if (receivedMessage.getContent().equals("exit")) {
          break;
        }

        String responseText = "Processed: " + receivedMessage.getContent().toUpperCase();
        Message responseMessage = new Message(responseText);
        objectOutputStream.writeObject(responseMessage);
        objectOutputStream.flush();

        // TEST: random crash
        if (Math.random() < 0.5) {
          System.out.println("Testing random crash");
          System.exit(1);
        }
      }

      objectInputStream.close();
      objectOutputStream.close();
    }
    catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

}