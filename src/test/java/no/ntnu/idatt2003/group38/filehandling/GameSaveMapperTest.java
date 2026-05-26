package no.ntnu.idatt2003.group38.filehandling;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.math.BigDecimal;
import java.util.List;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GameSaveMapper}.
 */
public class GameSaveMapperTest {

    private Exchange exchange;
    private Player player;
    private Stock appleStock;
    private Stock microsoftStock;

    @BeforeEach
    void setUp() {
        this.appleStock = new Stock("AAPL", "Apple", new BigDecimal("150.00"));
        this.microsoftStock = new Stock("MSFT", "Microsoft", new BigDecimal("300.00"));

        this.exchange = new Exchange("Test Exchange", List.of(this.appleStock, this.microsoftStock));

        this.player = new Player("Jeff", new BigDecimal("10000.00"));

        this.exchange.buy("AAPL", new BigDecimal("10"), this.player);
        this.exchange.buy("MSFT", new BigDecimal("5"), this.player);

        this.exchange.advance();
        this.player.recordNetWorthSnapshot();

        Share ownedAppleShare = this.player.getPortfolio().getShares("AAPL").getFirst();
        this.exchange.sell(ownedAppleShare, new BigDecimal("4"), this.player);
    }

    @Test
    void toSave_populatedGame_mapsCoreFieldsCorrectly() {
        GameSave save = GameSaveMapper.toSave(this.player, this.exchange);

        assertEquals("Test Exchange", save.exchangeName());
        assertEquals(this.exchange.getWeek(), save.week());
        assertEquals("Jeff", save.player().name());
        assertEquals(2, save.stocks().size());
        assertEquals(2, save.player().portfolio().size());
        assertEquals(3, save.player().transactions().size());
        assertTrue(save.player().transactions().stream().anyMatch(transaction -> transaction.type() == GameSave.TransactionType.SALE));
    }

    @Test
    void fromSave_roundTrip_restoresPlayerAndExchangeState() {
        GameSave save = GameSaveMapper.toSave(this.player, this.exchange);

        LoadedGame loadedGame = GameSaveMapper.fromSave(save);

        assertEquals(this.player.getName(), loadedGame.player().getName());
        assertEquals(this.exchange.getWeek(), loadedGame.exchange().getWeek());
        assertEquals(
                this.player.getTransactionArchive().getTransactions().size(),
                loadedGame.player().getTransactionArchive().getTransactions().size()
        );
        assertEquals(
                this.player.getNetWorthHistory().size(),
                loadedGame.player().getNetWorthHistory().size()
        );

        Share restoredShare = loadedGame.player().getPortfolio().getShares().getFirst();
        assertSame(
                loadedGame.exchange().getStock(restoredShare.getStock().getSymbol()),
                restoredShare.getStock()
        );
    }

    @Test
    void fromSave_saleWithoutSalePrice_throwsException() {
        GameSave save = new GameSave(
                "Test Exchange",
                2,
                new GameSave.SavedPlayer(
                        "Sigurd",
                        new BigDecimal("10000.00"),
                        new BigDecimal("9000.00"),
                        List.of(new BigDecimal("10000.00"), new BigDecimal("9500.00")),
                        List.of(),
                        List.of(
                                new GameSave.SavedTransaction(
                                        GameSave.TransactionType.SALE,
                                        "AAPL",
                                        new BigDecimal("2"),
                                        new BigDecimal("150.00"),
                                        null,
                                        2
                                )
                        )
                ),
                List.of(
                        new GameSave.SavedStock(
                                "AAPL",
                                "Apple",
                                List.of(new BigDecimal("150.00"), new BigDecimal("160.00"))
                        )
                )
        );

        assertThrows(IllegalArgumentException.class, () -> GameSaveMapper.fromSave(save));
    }

    @Test
    void fromSave_stockWithoutPriceHistory_throwsException() {
        GameSave save = new GameSave(
                "Test Exchange",
                1,
                new GameSave.SavedPlayer(
                        "Jeff",
                        new BigDecimal("10000.00"),
                        new BigDecimal("10000.00"),
                        List.of(new BigDecimal("10000.00")),
                        List.of(),
                        List.of()
                ),
                List.of(
                        new GameSave.SavedStock(
                                "AAPL",
                                "Apple",
                                List.of()
                        )
                )
        );

        assertThrows(IllegalArgumentException.class, () -> GameSaveMapper.fromSave(save));
    }

    @Test
    void fromSave_missingPlayerData_throwsException() {
        GameSave save = new GameSave(
                "Test Exchange",
                1,
                null,
                List.of(
                        new GameSave.SavedStock(
                                "AAPL",
                                "Apple",
                                List.of(new BigDecimal("150.00"))
                        )
                )
        );

        assertThrows(IllegalArgumentException.class, () -> GameSaveMapper.fromSave(save));
    }

    @Test
    void fromSave_transactionWithoutType_throwsException() {
        GameSave save = new GameSave(
                "Test Exchange",
                2,
                new GameSave.SavedPlayer(
                        "Sigurd",
                        new BigDecimal("10000.00"),
                        new BigDecimal("9000.00"),
                        List.of(new BigDecimal("10000.00"), new BigDecimal("9500.00")),
                        List.of(),
                        List.of(
                                new GameSave.SavedTransaction(
                                        null,
                                        "AAPL",
                                        new BigDecimal("2"),
                                        new BigDecimal("150.00"),
                                        null,
                                        2
                                )
                        )
                ),
                List.of(
                        new GameSave.SavedStock(
                                "AAPL",
                                "Apple",
                                List.of(new BigDecimal("150.00"), new BigDecimal("160.00"))
                        )
                )
        );

        assertThrows(IllegalArgumentException.class, () -> GameSaveMapper.fromSave(save));
    }

    @Test
    void fromSave_netWorthHistoryWithNullEntry_throwsException() {
        GameSave save = new GameSave(
                "Test Exchange",
                1,
                new GameSave.SavedPlayer(
                        "Jeff",
                        new BigDecimal("10000.00"),
                        new BigDecimal("10000.00"),
                        Arrays.asList(new BigDecimal("10000.00"), null),
                        List.of(),
                        List.of()
                ),
                List.of(
                        new GameSave.SavedStock(
                                "AAPL",
                                "Apple",
                                List.of(new BigDecimal("150.00"))
                        )
                )
        );

        assertThrows(IllegalArgumentException.class, () -> GameSaveMapper.fromSave(save));
    }
}
