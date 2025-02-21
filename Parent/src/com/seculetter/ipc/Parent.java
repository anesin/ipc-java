package com.seculetter.ipc;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Parent {

  private static final int PRODUCER_THREAD_COUNT = 3;
  private static final ExecutorService producerPool = Executors.newFixedThreadPool(PRODUCER_THREAD_COUNT);

  private static Process childProcess = null;
  private static ObjectInputStream objectInputStream = null;
  private static ObjectOutputStream objectOutputStream = null;


  public static void main(String[] args) {
    try {
      startChildProcess();

      for (int i = 0; i < PRODUCER_THREAD_COUNT; ++i) {
        int threadId = i;
        producerPool.submit(() -> runThread(threadId));
      }

      producerPool.shutdown();
      while (producerPool.isTerminated() == false) {
        Thread.sleep(100);
      }

      sendMessage(new Message("exit"));
      closeAllStream();
      childProcess.waitFor();
    }
    catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }


  private static synchronized void ensureChildProcess() {
    if (childProcess.isAlive())
      return;

    System.out.println("Child process is dead. Restarting...");
    try {
      if (childProcess != null) {
        childProcess.destroy();
        childProcess.waitFor();
      }
      startChildProcess();
    }
    catch (IOException | InterruptedException e) {
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

    closeAllStream();

    OutputStream outputStream = childProcess.getOutputStream();
    objectOutputStream = new ObjectOutputStream(outputStream);
    objectOutputStream.flush();

    InputStream inputStream = childProcess.getInputStream();
    objectInputStream = new ObjectInputStream(inputStream);
  }


  private static void closeAllStream() {
    closeStream(objectOutputStream);
    objectOutputStream = null;

    closeStream(objectInputStream);
    objectInputStream = null;
  }


  private static void closeStream(Closeable closable) {
    if (closable == null)
      return;

    try {
      closable.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }


  private static void runThread(int threadId) {
    try {
      for (int i = 0; i < 3; ++i) {
        String content = "Hello, World! " + threadId + " - " + i;
        process(content);
        Thread.sleep(1000);
      }
    }
    catch (ClassNotFoundException | InterruptedException e) {
      e.printStackTrace();
    }
  }


  private static void process(String content) throws ClassNotFoundException {
    while (true) {
      ensureChildProcess();
      try {
        sendMessage(new Message(content));
        Message responseMessage = receiveMessage();
        System.out.println("Received from child: " + responseMessage.getContent());
        break;
      }
      catch (IOException ex) {
        System.out.println("Child process is dead.");
      }
    }
  }


  private static synchronized void sendMessage(Message message) throws IOException {
    ensureChildProcess();
    synchronized (objectOutputStream) {
      System.out.println("sendMessage: " + message.getContent());
      objectOutputStream.writeObject(message);
      objectOutputStream.flush();
    }
  }


  private static synchronized Message receiveMessage() throws IOException, ClassNotFoundException {
    synchronized (objectInputStream) {
      return (Message) objectInputStream.readObject();
    }
  }

}