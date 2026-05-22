package com.game.monopoly.card;

/**
 * Fabryka odpowiedzialna za utworzenie i wypełnienie talii kart (Szansa i Kasa Społeczna).
 */
public class DeckFactory {

    /**
     * Tworzy talię kart Szansy (Chance).
     */
    public static Deck createChanceDeck() {
        Deck deck = new Deck("Szansa");

        // 1) idź na pole niebieskie drugie (40 -> indeks 39: Aleje Ujazdowskie)
        deck.addCard(new MoveToCard("Idź na pole niebieskie drugie (Aleje Ujazdowskie)", 39));
        // 2) idź na pole START (1 -> indeks 0)
        deck.addCard(new MoveToCard("Idź na pole START", 0));
        // 3) idź na pole czerwone trzecie (25 -> indeks 24: Plac Trzech Krzyży)
        deck.addCard(new MoveToCard("Idź na pole czerwone trzecie", 24));
        // 4) idź na pole pomarańczowe pierwsze (17 -> indeks 16: Ul. Świętokrzyska)
        deck.addCard(new MoveToCard("Idź na pole pomarańczowe pierwsze", 16));
        // 5) idź na najbliższą kolej (podwójny czynsz)
        deck.addCard(new NearestRailroadCard("Idź na najbliższą kolej. Jeśli posiadana - płacisz dwukrotny czynsz."));
        // 6) idź na najbliższą kolej (podwójny czynsz) - duplikat w talii wg listy
        deck.addCard(new NearestRailroadCard("Idź na najbliższą kolej. Jeśli posiadana - płacisz dwukrotny czynsz."));
        // 7) idź na najbliższe pole z dodatkiem (rzut x10)
        deck.addCard(new NearestUtilityCard("Idź na najbliższe pole użyteczności publicznej. Rzuć kostkami."));
        // 8) Dostajesz od banku 50$
        deck.addCard(new MoneyCard("Dostajesz od banku 50$", 50));
        // 9) Karta wyjścia z więzienia
        deck.addCard(new GetOutOfJailCard("Karta wyjścia z więzienia"));
        // 10) Cofasz się o 3 pola
        deck.addCard(new MoveForCard("Cofasz się o 3 pola", -3));
        // 11) Idziesz do więzienia
        deck.addCard(new GoToJailCard("Idziesz do więzienia"));
        // 12) Płacisz do banku 25$ za każdy domek i 100$ za każdy hotel
        deck.addCard(new BuildingTaxCard("Płacisz do banku 25$ za każdy domek i 100$ za każdy hotel", 25, 100));
        // 13) Płacisz 15$
        deck.addCard(new MoneyCard("Płacisz 15$", -15));
        // 14) Idziesz na pierwsze pole z koleją (6 -> indeks 5)
        deck.addCard(new MoveToCard("Idziesz na pierwsze pole z koleją. Jeśli przejdziesz przez START weź 200$", 5));
        // 15) Płacisz każdemu graczowi 50$
        deck.addCard(new PlayersMoneyCard("Płacisz każdemu graczowi 50$", -50));
        // 16) Dostajesz od banku 150$
        deck.addCard(new MoneyCard("Dostajesz od banku 150$", 150));

        deck.shuffle();
        return deck;
    }

    /**
     * Tworzy talię kart Kasy Społecznej (Community Chest).
     */
    public static Deck createCommunityChestDeck() {
        Deck deck = new Deck("Kasa Społeczna");

        // 1) Idziesz na pole start, weź 200$
        deck.addCard(new MoveToCard("Idziesz na pole start, weź 200$", 0));
        // 2) Weź 200$
        deck.addCard(new MoneyCard("Weź 200$", 200));
        // 3) Zapłać 50$
        deck.addCard(new MoneyCard("Zapłać 50$", -50));
        // 4) Weź 50$
        deck.addCard(new MoneyCard("Weź 50$", 50));
        // 5) Karta wyjścia z więzienia
        deck.addCard(new GetOutOfJailCard("Karta wyjścia z więzienia"));
        // 6) Idziesz do więzienia
        deck.addCard(new GoToJailCard("Idziesz do więzienia"));
        // 7) Weź 100$
        deck.addCard(new MoneyCard("Weź 100$", 100));
        // 8) Weź 20$
        deck.addCard(new MoneyCard("Weź 20$", 20));
        // 9) Weź 10$ od każdego gracza
        deck.addCard(new PlayersMoneyCard("Weź 10$ od każdego gracza", 10));
        // 10) Weź 100$
        deck.addCard(new MoneyCard("Weź 100$", 100));
        // 11) Zapłać 100$
        deck.addCard(new MoneyCard("Zapłać 100$", -100));
        // 12) Zapłać 50$
        deck.addCard(new MoneyCard("Zapłać 50$", -50));
        // 13) Weź 25$
        deck.addCard(new MoneyCard("Weź 25$", 25));
        // 14) Płacisz 40$ za każdy domek i 115$ za każdy hotel
        deck.addCard(new BuildingTaxCard("Płacisz 40$ za każdy domek i 115$ za każdy hotel", 40, 115));
        // 15) Weź 10$
        deck.addCard(new MoneyCard("Weź 10$", 10));
        // 16) Weź 100$
        deck.addCard(new MoneyCard("Weź 100$", 100));

        deck.shuffle();
        return deck;
    }
}