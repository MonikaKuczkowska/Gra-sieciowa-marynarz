package com.company;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    private static int PORT = 9090;
    static int MAX_GRACZY =  3;
    public static volatile int aktualnaLiczbaGraczyS;
    public static volatile int czyLogin;
    public static volatile int czyKomenda;
    public static volatile ArrayList <Player> players = new ArrayList<>();
    public static volatile List <Integer> indeksy = new ArrayList<>();
    public static volatile List <String> przegrani = new ArrayList<>();



    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(PORT)) {
            System.out.println("Server is running...");
            var pool = Executors.newFixedThreadPool(3);
            while(true){
                Player player0 = new Player(listener.accept(), 0);
                pool.execute(player0);
                players.add(player0);
                Player player1 = new Player(listener.accept(), 1);
                pool.execute(player1);
                players.add(player1);
                Player player2 = new Player(listener.accept(), 2);
                pool.execute(player2);
                players.add(player2);
            }
        }
    }

}

class Player implements Runnable{

    String ok = "OK";
    String error = "ERROR";
    String witam = "WITAM";
    String runda = "RUNDA";
    String twojRuch = "TWOJ RUCH";
    String koniec = "KONIEC";
    int licznikRunda = 0;
    int idGraczaRozpoczynajacego = 0;
    int i = 0;

    private Socket socket;
    private String login;
    private String command;
    private int id;
    private int licznikBlednychKomend = 0;
    private int ileRazyPrzegral = 0;
    private int podanaWartosc;
    private Scanner input;
    private PrintWriter output;

    public Player(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getIleRazyPrzegral() {
        return ileRazyPrzegral;
    }

    public int getPodanaWartosc() {
        return podanaWartosc;
    }


    @Override
    public void run() {
        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println(witam);

            //Logowanie
            while (licznikBlednychKomend <= 100) {
                command = input.nextLine();
                if (command.matches("LOGIN \\w+")) {
                    output.println(ok);
                    String array[] = command.split(" ");
                    login = array[1];
                    break;
                } else {
                    output.println(error);
                    licznikBlednychKomend++;
                    if (licznikBlednychKomend == 101)
                        socket.close();
                }
            }
            licznikBlednychKomend = 0;
            Server.aktualnaLiczbaGraczyS++;
            Server.czyLogin++;
            while (Server.czyLogin != Server.MAX_GRACZY){

            }
            Server.indeksy.add(getId());
            idGraczaRozpoczynajacego = Server.indeksy.get(i);

            //Rozgrywka
            while (true) {
                if(Server.aktualnaLiczbaGraczyS == 1){
                    StringBuilder builder2 = new StringBuilder();
                    for(int i =  Server.przegrani.size() - 1; i >= 0; i--){
                        builder2.append(Server.przegrani.get(i)).append(" ");
                    }
                    output.println(koniec + " " + builder2);
                    socket.close();
                }
                Server.czyKomenda = 0;
                output.println(runda + " " + licznikRunda + " " + idGraczaRozpoczynajacego);
                output.println(twojRuch);
                while (licznikBlednychKomend <= 100) {
                    command = input.nextLine();
                    if (command.matches("[1-9]|10")) {
                        output.println(ok);
                        podanaWartosc = Integer.parseInt(command);
                        break;
                    } else {
                        output.println(error);
                        licznikBlednychKomend++;
                        if (licznikBlednychKomend == 101) {
                            socket.close();
                            Server.aktualnaLiczbaGraczyS--;
                            Server.indeksy.remove(Integer.valueOf(getId()));
                        }

                    }
                }
                licznikBlednychKomend = 0;
                Server.czyKomenda++;
                while (Server.czyKomenda != Server.aktualnaLiczbaGraczyS){

                }
                TimeUnit.MILLISECONDS.sleep(800);;//Wątki nie nadążały (proste rozwiązanie)

                StringBuilder builder = new StringBuilder();
                for (Player p: Server.players) {
                    builder.append(p.getId()).append(" ");
                    builder.append(p.getPodanaWartosc()).append(" ");
                }
                output.println(Server.aktualnaLiczbaGraczyS + " " + builder.toString());//Wypisanie po kolei id gracza podana wartosc... kazdego z graczy
                int suma = 0;
                for (Player p: Server.players) {
                    suma = suma + p.getPodanaWartosc();
                }
                int modulo = (idGraczaRozpoczynajacego + suma)%Server.aktualnaLiczbaGraczyS;
                if(id == Server.indeksy.get(modulo)){
                    ileRazyPrzegral++;
                }

                TimeUnit.MILLISECONDS.sleep(800);
                StringBuilder builder1 = new StringBuilder();
                for (Player p: Server.players) {
                    builder1.append(p.getId()).append(" ");
                    builder1.append(p.getIleRazyPrzegral()).append(" ");
                }
                output.println(Server.aktualnaLiczbaGraczyS + " " + builder1.toString());

                if(ileRazyPrzegral == 10){
                    podanaWartosc = 0;
                    Server.aktualnaLiczbaGraczyS--;
                    Server.indeksy.remove(Integer.valueOf(getId()));
                    Server.przegrani.add(login);
                    socket.close();
                }
                else TimeUnit.MILLISECONDS.sleep(800);
                if(Server.aktualnaLiczbaGraczyS == 1){
                    Server.przegrani.add(login);
                }
                licznikRunda++;
                i++;
                if(i >= Server.aktualnaLiczbaGraczyS){
                    i=0;
                }
                idGraczaRozpoczynajacego = Server.indeksy.get(i);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}

