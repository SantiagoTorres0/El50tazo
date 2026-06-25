package com.example.elcincuentazo.Model;

public class main {
    public static void main(String[] args) {
        // 1. Instanciamos la lógica del juego (esto ejecuta el constructor y crea las 48 cartas)
        GameLogic juego = new GameLogic();

        System.out.println("=== Iniciando El Cincuentazo ===");
        System.out.println("Cartas iniciales en el mazo: " + juego.getDeck().size());

        // 2. Ejecutamos el reparto de las 4 cartas al azar
        System.out.println("\nRepartiendo cartas...");
        juego.GiveCardsToPlayer();
    }
}
