package de.redblackmediaproduction.uscutil.gui;

import de.redblackmediaproduction.uscutil.libusc.Connector;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import java.util.List;

public class CliMain {
    public static void main(String[] args) {
        System.out.println("List of terminals");
        try {
            List<CardTerminal> terminals = Connector.getTerminals();
            for (int i = 0; i < terminals.size(); i++) {
                CardTerminal terminal = terminals.get(i);
                System.out.println("Terminal #" + i + ": " + terminal.getName() + (terminal.isCardPresent() ? " (card present)" : ""));
            }
            if (terminals.isEmpty()) {
                throw new Exception("No terminal present");
            }
            CardTerminal activeTerminal = terminals.get(0);
            Connector uscConnector = Connector.getForTerminal(activeTerminal);
            System.out.println("Polling terminal");
            boolean lastIsPresent = false;
            while (true) {
                boolean newIsPresent = uscConnector.getActiveTerminal().isCardPresent();
                if (lastIsPresent != newIsPresent) {
                    System.out.println("State change detected, now: " + (newIsPresent ? "card present" : "card not present"));
                    lastIsPresent = newIsPresent;
                    if (newIsPresent) {
                        Card activeCard = uscConnector.connect();
                        System.out.println("ATR: " + uscConnector.getActiveCardAtr().toString());
                    }

                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("Caught " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}