package com.game.monopoly.engine;

// Importy klas z innych pakietów, którymi zarządza Silnik
import com.game.monopoly.board.Board;
import com.game.monopoly.card.Deck;
import com.game.monopoly.economy.Bank;
import com.game.monopoly.player.Player;
import com.game.monopoly.player.state.ActiveState;
import com.game.monopoly.player.state.BankruptState;

import java.util.ArrayList;
import java.util.List;

/**
 * Główna klasa silnika gry.
 * Pełni rolę Fasady/Mediatora zarządzającego całą rozgrywką.
 */
public class GameEngine {

    // --- ATRYBUTY (Kompozycje i podstawowe pola) ---
    private List<Player> players;
    private Board board;
    private Bank bank;
    private Dice dice;
    private Player currentPlayer;
    private Deck chanceDeck;
    private Deck communityChestDeck;

    // Lista interfejsów do komunikacji z GUI
    private List<GameObserver> observers;

    // --- KONSTRUKTOR ---
    public GameEngine() {
        this.players = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.currentPlayer = null;

        // Inicjalizacja głównych komponentów gry
        this.board = new Board(); // To wywoła BoardFactory pod spodem
        this.bank = new Bank();
        this.dice = new Dice();
        this.chanceDeck = new Deck("Szansa");
        this.communityChestDeck = new Deck("Kasa Społeczna");
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Inicjuje nową grę, tworząc obiekty graczy na podstawie przekazanych imion.
     */
    public void start(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("Gra wymaga co najmniej jednego gracza.");
        }

        // Tworzenie graczy i przypisywanie im domyślnego stanu (ActiveState)
        for (String name : names) {
            Player newPlayer = new Player(name);
            newPlayer.changeState(new ActiveState());
            this.players.add(newPlayer);
        }

        // Tasowanie kart przed rozpoczęciem gry
        this.chanceDeck.shuffle();
        this.communityChestDeck.shuffle();

        // Ustawienie pierwszego gracza i wysłanie powiadomienia
        this.currentPlayer = players.get(0);
        notifyMessage("Gra wystartowała! Zaczyna gracz: " + currentPlayer.getName());
    }

    /**
     * Główna metoda odpalająca turę aktywnego gracza.
     */
    public void playTurn() {
        if (currentPlayer != null) {
            notifyMessage("Rozpoczyna się tura gracza: " + currentPlayer.getName());
            // Delegacja do maszyny stanów gracza
            currentPlayer.executeTurn(this);
        }
    }

    /**
     * Kończy turę aktywnego gracza i wywołuje nextPlayer().
     */
    public void endTurn() {
        notifyMessage("Koniec tury gracza: " + currentPlayer.getName());
        nextPlayer();
    }

    /**
     * Obsługuje proces bankructwa danego gracza (oddanie majątku, usunięcie z gry).
     */
    public void handleBankruptcy(Player player) {
        notifyMessage("Gracz " + player.getName() + " ogłasza bankructwo i odpada z gry!");

        // Zmiana stanu na bankruta
        player.changeState(new BankruptState());
        notifyPlayerStateChanged(player);

        // Usunięcie z listy aktywnych graczy, aby omijała go kolejka
        players.remove(player);

        // Sprawdzenie warunku wygranej
        if (players.size() == 1) {
            notifyMessage("KONIEC GRY! Wygrywa: " + players.get(0).getName());
        } else if (this.currentPlayer == player) {
            // Jeśli zbankrutował w swojej własnej turze, od razu przekaż kolejkę
            nextPlayer();
        }
    }

    /**
     * Zmienia indeks currentPlayerIndex na kolejnego aktywnego gracza.
     */
    public void nextPlayer() {
        if (players.isEmpty()) return;

        int currentIndex = players.indexOf(currentPlayer);
        // Zabezpieczenie: jeśli obecny gracz został usunięty (zbankrutował),
        // indexOf zwróci -1, więc ( -1 + 1 ) % size da nam indeks 0.
        int nextIndex = (currentIndex + 1) % players.size();

        this.currentPlayer = players.get(nextIndex);
        notifyMessage("Kolejka przechodzi na gracza: " + currentPlayer.getName());
    }

    public Board getBoard() { return board; }
    public Bank getBank() { return bank; }
    public Dice getDice() { return dice; }
    public Deck getChanceDeck() { return chanceDeck; }
    public Deck getCommunityChestDeck() { return communityChestDeck; }
    public Player getCurrentPlayer() { return currentPlayer; }

    // --- WZORZEC OBSERWATOR (Komunikacja z GUI) ---

    /**
     * Rejestruje nowy widok/interfejs do nasłuchiwania zdarzeń z silnika.
     */
    public void addObserver(GameObserver obs) {
        if (obs != null) {
            this.observers.add(obs);
        }
    }

    public void notifyMessage(String message) {
        for (GameObserver obs : observers) {
            obs.onMessage(message);
        }
    }

    public void notifyPlayerMoved(Player player, int newPosition) {
        for (GameObserver obs : observers) {
            obs.onPlayerMoved(player, newPosition);
        }
    }

    /**
     * Metoda powiadamiająca wszystkie podpięte widoki o zmianie.
     * (Dodana, aby wzorzec Obserwatora był kompletny).
     */
    public void notifyPlayerStateChanged(Player player) {
        for (GameObserver obs : observers) {
            obs.onPlayerStateChanged(player);
        }
    }

    public List<Player> getPlayers() {
        return players;
    }
}