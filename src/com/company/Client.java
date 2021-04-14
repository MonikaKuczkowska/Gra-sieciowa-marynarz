package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client{

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9090;

    public static void main(String[] args) throws IOException {
        Socket cSocket = new Socket(SERVER_IP, SERVER_PORT);
        String command;
        int licznikBlednychKomend = 0;
        //Komunikat odebrany z serwera
        BufferedReader input = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
        //Wejscie klienta - klawiatura
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        //Komunikat wysylany do serwera
        PrintWriter output = new PrintWriter(cSocket.getOutputStream(), true);

        //Wiadomosc powitalna serwera po poprawynym polaczeniu
        String serverResponse = input.readLine();
        System.out.println(serverResponse);

        //Logowanie klienta
        while(!serverResponse.equals("OK")){
            command = keyboard.readLine();
            output.println(command);
            serverResponse = input.readLine();
            System.out.println(serverResponse);
            if (serverResponse.equals("ERROR"))
                licznikBlednychKomend++;
            if (licznikBlednychKomend == 101)
                cSocket.close();
        }
        licznikBlednychKomend = 0;

        //Rozgrywka
        while (true){
            serverResponse = input.readLine();
            System.out.println(serverResponse);
            if (serverResponse.contains("KONIEC"))
                break;
            serverResponse = input.readLine();
            System.out.println(serverResponse);
            while (!serverResponse.equals("OK")){
                command = keyboard.readLine();
                output.println(command);
                serverResponse = input.readLine();
                System.out.println(serverResponse);
                if(serverResponse.equals("ERROR"))
                    licznikBlednychKomend++;
                if(licznikBlednychKomend == 101)
                    cSocket.close();
            }
            serverResponse = input.readLine();
            System.out.println(serverResponse);
            serverResponse = input.readLine();
            System.out.println(serverResponse);
        }

    }
}
