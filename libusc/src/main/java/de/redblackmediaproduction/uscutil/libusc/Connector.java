package de.redblackmediaproduction.uscutil.libusc;

import de.redblackmediaproduction.uscutil.libusc.atr.AtrParser;
import de.redblackmediaproduction.uscutil.libusc.atr.ParsedAtr;
import de.redblackmediaproduction.uscutil.libusc.exception.ConnectionImpossibleException;
import de.redblackmediaproduction.uscutil.libusc.exception.InvalidAtrException;
import de.redblackmediaproduction.uscutil.libusc.exception.NoCardInsertedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.util.List;

/**
 * Central interface with libusc
 */
public class Connector {
    private static final Logger logger = LogManager.getLogger(Connector.class);
    protected CardTerminal activeTerminal = null;
    protected Card activeCard = null;
    protected ParsedAtr activeCardAtr = null;

    public Connector(CardTerminal terminal) {
        this.activeTerminal = terminal;
    }

    /**
     * Get all terminals connected to the system
     * <p>
     * Taken straight from <a href="https://docs.oracle.com/javase/8/docs/jre/api/security/smartcardio/spec/javax/smartcardio/package-summary.html">Java documentation</a>
     * </p>
     *
     * @return list of all terminals
     */
    public static List<CardTerminal> getTerminals() throws CardException {
        TerminalFactory factory = TerminalFactory.getDefault();
        return factory.terminals().list();
    }

    /**
     * Get a Connector instance for a PC/SC terminal
     *
     * @param terminal
     * @return Connector instance
     */
    public static Connector getForTerminal(CardTerminal terminal) {
        return new Connector(terminal);
    }

    public CardTerminal getActiveTerminal() {
        return activeTerminal;
    }

    public Card getActiveCard() {
        return activeCard;
    }

    public ParsedAtr getActiveCardAtr() {
        return activeCardAtr;
    }

    /**
     * Attempt to connect to the card using the specified protocol
     *
     * @param protocol
     * @return
     * @throws CardException
     */
    public Card connect(String protocol) throws CardException, InvalidAtrException {
        Card card = activeTerminal.connect(protocol);
        this.activeCard = card;
        this.activeCardAtr = AtrParser.fromAtr(card.getATR());
        return card;
    }

    /**
     * Attempt to connect to the card that's present
     *
     * <p>Unfortunately, Java doesn't provide us the ATR prior to connection, so we attempt to first connect via the old T=0 protocol, then T=1, and finally T=CL (contactless).</p>
     * <p>We only use contactless as last-resort because some cards may decide to not expose full functionality via NFC!</p>
     *
     * @return
     * @throws CardException
     * @throws NoCardInsertedException
     */
    public Card connect() throws CardException, NoCardInsertedException, ConnectionImpossibleException {
        if (!activeTerminal.isCardPresent())
            throw new NoCardInsertedException();
        String[] protocols = {"T=0", "T=1", "T=CL", "*"};
        Card ret = null;
        for (String protocol : protocols) {
            try {
                ret = connect(protocol);
                System.err.println("Connected using " + protocol);
                System.err.println("ATR: " + ret.getATR().toString());
                logger.debug("Parsed ATR: " + this.activeCardAtr.toHexString(" "));

                return ret;
            } catch (Exception e) {
                System.err.println("Failed to connect using " + protocol);
            }
        }
        if (ret == null) {
            throw new ConnectionImpossibleException();
        }
        return ret;
    }
}
