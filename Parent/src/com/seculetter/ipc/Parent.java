package com.seculetter.ipc;

import java.io.*;


public class Parent {

  // stdin/out 으로 문자열 IPC
  public static void main(String[] args) {
    try {
      String childClasspath = "../Child/out/production/Child";
      String childClassName = "com.seculetter.ipc.Child";
      ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", childClasspath, childClassName);
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();

      OutputStream outputStream = process.getOutputStream();
      PrintWriter printWriter = new PrintWriter(outputStream, true);

      InputStream inputStream = process.getInputStream();
      BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(inputStream)));

      printWriter.println("Hello, World!");
      printWriter.println("Bye~");
      printWriter.println("exit");

      String line;
      while ((line = bufferedReader.readLine()) != null) {
        System.out.println("Received from child" + line);
      }

      printWriter.close();
      bufferedReader.close();
      process.waitFor();
    }
    catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

}