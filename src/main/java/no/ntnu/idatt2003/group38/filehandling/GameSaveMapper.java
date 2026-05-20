package no.ntnu.idatt2003.group38.filehandling;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.transaction.Transaction;
import no.ntnu.idatt2003.group38.transaction.Sale;
import no.ntnu.idatt2003.group38.transaction.Purchase;

/**
 * Maps between the in-memory game model and the serializable save-game format.
 *
 * <p>The mapper converts a running {@link Player}/{@link Exchange} pair into a
 * {@link GameSave}, and reconstructs a {@link LoadedGame} from a previously
 * saved snapshot.</p>
 */
public class GameSaveMapper {

    private GameSaveMapper() {

    }

    /**
     * Creates a serializable save snapshot from the current game state.
     *
     * @param player the current player; must not be {@code null}
     * @param exchange the current exchange; must not be {@code null}
     * @return a save snapshot containing the full game state
     */
    public static GameSave toSave(Player player, Exchange exchange) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(exchange, "Exchange cannot be null");

        return new GameSave(
                exchange.getName(),
                exchange.getWeek(),
                mapPlayer(player),
                mapStocks(exchange)
        );
    }

    /**
     * Reconstructs a fully loaded game from a saved snapshot.
     *
     * @param save the saved game data to restore; must not be {@code null}
     * @return a loaded game containing the restored player and exchange
     */
    public static LoadedGame fromSave(GameSave save) {
        Objects.requireNonNull(save, "Save cannot be null");

        List<Stock> stocks = restoreStocks(save.stocks());
        Map<String, Stock> stocksBySymbol = indexStocks(stocks);

        List<Share> shares = restoreShares(
                save.player().portfolio(),
                stocksBySymbol
        );

        List<Transaction> transactions = restoreTransactions(
                save.player().transactions(),
                stocksBySymbol
        );

        Player player = Player.restore(
                save.player().name(),
                save.player().startingMoney(),
                save.player().money(),
                shares,
                transactions,
                save.player().netWorthHistory()
        );

        Exchange exchange = Exchange.restore(
                save.exchangeName(),
                save.week(),
                stocks
        );

        return new LoadedGame(player, exchange);
    }

    private static GameSave.SavedPlayer mapPlayer(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        return new GameSave.SavedPlayer(
                player.getName(),
                player.getStartingMoney(),
                player.getMoney(),
                player.getNetWorthHistory(),
                mapShares(player),
                mapTransactions(player)
        );
    }

    private static List<GameSave.SavedStock>  mapStocks(Exchange exchange) {
        Objects.requireNonNull(exchange, "Exchange cannot be null");

        List<Stock> stocks = new ArrayList<>(exchange.findStocks(""));
        stocks.sort(Comparator.comparing(Stock::getSymbol));

        return stocks.stream()
                .map(stock -> new GameSave.SavedStock(
                        stock.getSymbol(),
                        stock.getCompany(),
                        List.copyOf(stock.getHistoricalPrices())
                )).toList();
    }

    private static List<GameSave.SavedShare> mapShares(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        return player.getPortfolio().getShares().stream()
                .map(share -> new GameSave.SavedShare(
                        share.getStock().getSymbol(),
                        share.getQuantity(),
                        share.getPurchasePrice()
                )).toList();
    }

    private static List<GameSave.SavedTransaction> mapTransactions(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");

        return player.getTransactionArchive().getTransactions().stream()
                .map(GameSaveMapper::mapTransaction)
                .toList();
    }

    private static GameSave.SavedTransaction mapTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");

        GameSave.TransactionType type = transaction instanceof Purchase ? GameSave.TransactionType.PURCHASE : GameSave.TransactionType.SALE;

        BigDecimal salesPrice = transaction instanceof Sale ? extractSalePrice(transaction) : null;

        return new GameSave.SavedTransaction(
                type,
                transaction.getShare().getStock().getSymbol(),
                transaction.getShare().getQuantity(),
                transaction.getShare().getPurchasePrice(),
                salesPrice,
                transaction.getWeek()
        );
    }

    private static List<Stock> restoreStocks(List<GameSave.SavedStock> savedStocks) {
        Objects.requireNonNull(savedStocks, "Saved Stocks cannot be null");

        List<Stock> stocks = new ArrayList<>();

        for (GameSave.SavedStock savedStock : savedStocks) {
            Objects.requireNonNull(savedStock, "Saved Stocks cannot be null");

            List<BigDecimal> historicalPrices = savedStock.historicalPrices();

            if (historicalPrices == null || historicalPrices.isEmpty()) {
                throw new IllegalArgumentException("Saved stock must contain at least one historical price");
            }

            Stock stock = new Stock(
                    savedStock.stockSymbol(),
                    savedStock.company(),
                    historicalPrices.getFirst()
            );

            for (int i = 1; i < historicalPrices.size(); i++) {
                stock.addNewSalesPrice(historicalPrices.get(i));
            }

            stocks.add(stock);
        }

        return stocks;
    }

    private static Map<String, Stock> indexStocks(List<Stock> stocks) {
        Objects.requireNonNull(stocks, "Stocks cannot be null");

        Map<String, Stock> stocksBySymbol = new HashMap<>();

        for (Stock stock : stocks) {
            stocksBySymbol.put(stock.getSymbol().toUpperCase(), stock);
        }

        return stocksBySymbol;
    }

    private static List<Share> restoreShares(
            List<GameSave.SavedShare> savedShares,
            Map<String, Stock> stocksBySymbol) {

        Objects.requireNonNull(savedShares, "Saved Shares cannot be null");
        Objects.requireNonNull(stocksBySymbol, "Stocks By Symbol cannot be null");

        List<Share> shares = new ArrayList<>();

        for (GameSave.SavedShare savedShare : savedShares) {
            shares.add(restoreShare(savedShare, stocksBySymbol));
        }

        return shares;
    }

    private static List<Transaction> restoreTransactions(
            List<GameSave.SavedTransaction> savedTransactions,
            Map<String, Stock> stocksBySymbol) {

        Objects.requireNonNull(savedTransactions, "Saved Transactions cannot be null");
        Objects.requireNonNull(stocksBySymbol, "Stocks By Symbol cannot be null");

        List<Transaction> transactions = new ArrayList<>();

        for (GameSave.SavedTransaction savedTransaction : savedTransactions) {
            transactions.add(restoreTransaction(savedTransaction, stocksBySymbol));
        }

        return transactions;
    }

    private static Share restoreShare(
            GameSave.SavedShare savedShare,
            Map<String, Stock> stocksBySymbol) {

        Objects.requireNonNull(savedShare, "Saved Share cannot be null");
        Objects.requireNonNull(stocksBySymbol, "Stocks By Symbol cannot be null");

        Stock stock = stocksBySymbol.get(savedShare.stockSymbol().toUpperCase());

        if (stock == null) {
            throw new IllegalArgumentException("Unknown stock symbol in saved share: " + savedShare.stockSymbol());
        }

        return new Share(
                stock,
                savedShare.quantity(),
                savedShare.purchasePrice()
        );
    }

    private static Transaction restoreTransaction(
            GameSave.SavedTransaction savedTransaction,
            Map<String, Stock> stocksBySymbol) {

        Objects.requireNonNull(savedTransaction, "Saved Transaction cannot be null");
        Objects.requireNonNull(stocksBySymbol, "Stocks By Symbol cannot be null");

        Stock stock = stocksBySymbol.get(savedTransaction.stockSymbol().toUpperCase());

        if (stock == null) {
            throw new IllegalArgumentException("Unknown stock symbol in saved transaction: " + savedTransaction.stockSymbol());
        }

        Share share = new Share(
                stock,
                savedTransaction.quantity(),
                savedTransaction.purchasePrice()
        );

        return switch (savedTransaction.type()) {
            case PURCHASE -> Purchase.restore(share, savedTransaction.week());
            case SALE -> {
                if (savedTransaction.salePrice() == null) {
                    throw new IllegalArgumentException("Saved sale transaction is missing Sale Price");
                }

                yield Sale.restore(share, savedTransaction.week(), savedTransaction.salePrice());
            }
        };
    }

    private static BigDecimal extractSalePrice(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");

        BigDecimal quantity = transaction.getShare().getQuantity();

        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return transaction.getCalculator().calculateGross().divide(quantity, 10, RoundingMode.HALF_UP);
    }
}
