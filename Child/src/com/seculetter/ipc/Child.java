package com.seculetter.ipc;

import java.util.Scanner;


public class Child {

  // stdin/out 으로 문자열 IPC
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    while (scanner.hasNextLine()) {
      String input = scanner.nextLine();
      if (input.equals("exit")) {
        break;
      }
      System.out.println("Hello, World!");
    }

    scanner.close();
  }

}