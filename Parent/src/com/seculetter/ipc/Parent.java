package com.seculetter.ipc;

import java.io.*;


public class Parent {

  private static Process childProcess = null;
  private static ObjectInputStream objectInputStream = null;
  private static ObjectOutputStream objectOutputStream = null;


  public static void main(String[] args) {
    try {
      startChildProcess();

      for (int i = 0; i < 3; ++i) {
        sendMessage(new Message("Hello, World! " + i));
        Message responseMessage = receiveMessage();
        System.out.println("Received from child: " + responseMessage.getContent());

        ensureChildProcess();

        Thread.sleep(3000);
      }

      sendMessage(new Message("exit"));
      objectOutputStream.close();
      objectInputStream.close();
      childProcess.waitFor();
    }
    catch (IOException | InterruptedException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }


  private static void ensureChildProcess() {
    if (childProcess.isAlive())
      return;

    System.out.println("Child process is dead. Restarting...");
    try {
      if (childProcess != null)
        childProcess.destroy();
      startChildProcess();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }


  private static void startChildProcess() throws IOException {
    System.out.println("Starting Child process...");
    String childClasspath = "../Child/out/production/Child";
    String childClassName = "com.seculetter.ipc.Child";
    ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", childClasspath, childClassName);
    processBuilder.redirectErrorStream(true);
    childProcess = processBuilder.start();
    System.out.println("Started Child process... " + childProcess);

    if (objectOutputStream != null)
      objectOutputStream.close();
    OutputStream outputStream = childProcess.getOutputStream();
    objectOutputStream = new ObjectOutputStream(outputStream);
    objectOutputStream.flush();

    if (objectInputStream != null)
      objectInputStream.close();
    InputStream inputStream = childProcess.getInputStream();
    objectInputStream = new ObjectInputStream(inputStream);
  }


  private static void sendMessage(Message message)
      throws IOException {
    ensureChildProcess();
    System.out.println("sendMessage: " + message.getContent());
    objectOutputStream.writeObject(message);
    objectOutputStream.flush();
  }


  private static Message receiveMessage()
      throws IOException, ClassNotFoundException {
    return (Message) objectInputStream.readObject();
  }

}