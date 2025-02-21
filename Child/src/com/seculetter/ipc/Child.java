package com.seculetter.ipc;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Child {

//  private static final String LOCK_FILE = "/tmp/child_process.lock";
  private static final int THREAD_COUNT = 4;


  public static void main(String[] args) {
//    if (acquireLock() == false) {
//      System.out.println("Child process is already running.");
//      System.exit(0);
//    }
//    Runtime.getRuntime()
//           .addShutdownHook(new Thread(Child::releaseLock));

    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

    try {
      ObjectInputStream objectInputStream = new ObjectInputStream(System.in);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(System.out);
      objectOutputStream.flush();

      while (true) {
        Message receivedMessage = (Message) objectInputStream.readObject();
        if (receivedMessage.getContent().equals("exit"))
          break;
        executorService.submit(() -> process(objectOutputStream, receivedMessage));

        // TEST: random (10%) crash
        if (Math.random() < 0.1) {
          System.out.println("Testing random crash");
          System.exit(1);
        }
      }

      executorService.shutdown();
      while (executorService.isTerminated() == false)
        Thread.sleep(1000);

      objectOutputStream.close();
      objectInputStream.close();
    }
    catch (IOException | ClassNotFoundException | InterruptedException e) {
      e.printStackTrace();
    }
  }


//  private static boolean acquireLock() {
//    try {
//      File lockFile = new File(LOCK_FILE);
//      Path path = lockFile.toPath();
//      System.out.println("path = " + path.toAbsolutePath());
//      File parentDir = lockFile.getParentFile();
//      if (parentDir != null && parentDir.exists() == false)
//        parentDir.mkdirs();
//
//      if (lockFile.exists()) {
//        String pid = Files.readString(path).trim();
//        if (isProcessRunning(pid)) {
//          System.err.println("Child process is already running with PID: " + pid);
//          return false;
//        }
//      }
//
//      long processId = ProcessHandle.current().pid();
//      String pid = String.valueOf(processId);
//      Files.writeString(path, pid);
//      System.err.println("Acquired lock with PID: " + pid);
//      return true;
//    }
//    catch (IOException e) {
//      e.printStackTrace();
//      return false;
//    }
//  }
//
//
//  private static boolean isProcessRunning(String pid) {
//    return ProcessHandle.of(Long.parseLong(pid))
//                        .map(ProcessHandle::isAlive)
//                        .orElse(false);
//  }
//
//
//  private static void releaseLock() {
//    try {
//      Files.deleteIfExists(Path.of(LOCK_FILE));
//    }
//    catch (IOException e) {
//      e.printStackTrace();
//    }
//  }


  private static void process(ObjectOutputStream objectOutputStream, Message message) {
    try {
      String responseText = "Processed: " + message.getContent().toUpperCase();
      Message responseMessage = new Message(responseText);

      synchronized (objectOutputStream) {
        objectOutputStream.writeObject(responseMessage);
        objectOutputStream.flush();
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}