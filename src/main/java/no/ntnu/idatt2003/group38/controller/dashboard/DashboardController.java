package no.ntnu.idatt2003.group38.controller.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.model.Stock;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.transaction.Transaction;
import no.ntnu.idatt2003.group38.view.DashboardView;
import no.ntnu.idatt2003.group38.transaction.Purchase;
import no.ntnu.idatt2003.group38.view.shell.Page;
import no.ntnu.idatt2003.group38.calculator.PurchaseCalculator;
import no.ntnu.idatt2003.group38.calculator.SaleCalculator;

public class DashboardController implements Page, ModelObserver {

    private final DashboardView view;
    private final Exchange exchange;
    private final Player player;

    private final DecimalFormat moneyFormat;

    public DashboardController(Exchange exchange, Player player) {
        this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
        this.player = Objects.requireNonNull(player, "Player cannot be null");

        this.view = new DashboardView();
        this.view.setOnAdvanceWeek(this::handleAdvanceWeek);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        this.moneyFormat = new DecimalFormat("#,##0", symbols);
    }

    @Override
    public Region getRoot() {
        return this.view.getRoot();
    }

    @Override
    public void onAttach() {
        this.exchange.addObserver(this);
        Scene scene = this.view.getRoot().getScene();
        if (scene != null) {
            this.view.attachTo(scene);
        }
        refresh();
    }

    @Override
    public void onDetach() {
        this.exchange.removeObserver(this);
    }

    @Override
    public void onModelChanged() {
        refresh();
    }

    public void handleAdvanceWeek() {
        this.exchange.advance();
    }

    public void refresh() {
        List<Share> lots = this.player.getPortfolio().getShares();
        List<Share> positions = aggregateShares(lots);

        BigDecimal cash = this.player.getMoney();
        BigDecimal portfolioValue = this.player.getPortfolio().getNetWorth();
        BigDecimal netWorth = this.player.getNetWorth();

        int week = this.exchange.getWeek();
        String playerName = this.player.getName();
        String status = this.player.getPlayerStatus();

        int openPositions = positions.size();

        Share largestPosition = findLargestPosition(positions);
        Share bestHolding = findBestHolding(positions);
        Share worstHolding = findWorstHolding(positions);

        BigDecimal cashRatio = calculateCashRatio(cash, netWorth);

        List<String> gainers = buildGainers();
        List<String> losers = buildLosers();
        List<String> recentActivity = buildRecentActivity();

        this.view.setHeader(playerName, week, status);
        this.view.setOverview(
                formatMoney(cash),
                formatMoney(portfolioValue),
                formatMoney(netWorth),
                String.valueOf(openPositions)
        );

        this.view.setPortfolioSnapshot(
                formatPositionSummary(largestPosition),
                formatHoldingSummary(bestHolding),
                formatHoldingSummary(worstHolding),
                formatPercent(cashRatio)
        );

        this.view.setMarketMovers(gainers, losers);
        this.view.setRecentActivity(recentActivity);
    }

    private List<Share>  aggregateShares(List<Share> shares) {
        Map<String, ShareAccumulator> groups = new LinkedHashMap<>();

        for  (Share share : shares) {
            String symbol = share.getStock().getSymbol();
            ShareAccumulator accumulator = groups.computeIfAbsent(symbol, ignored -> new ShareAccumulator(share));
            accumulator.add(share);
        }
        return groups.values().stream()
                .map(ShareAccumulator::toShare)
                .toList();
    }

    private Share findLargestPosition(List<Share> positions) {
        return positions.stream()
                .max(Comparator.comparing(this::calculatePositionValue))
                .orElse(null);
    }

    private Share findBestHolding(List<Share> positions) {
        return positions.stream()
                .max(Comparator.comparing(this::calculateGainLoss))
                .orElse(null);
    }

    private Share  findWorstHolding(List<Share> positions) {
        return positions.stream()
                .min(Comparator.comparing(this::calculateGainLoss))
                .orElse(null);
    }

    private BigDecimal calculatePositionValue(Share share) {
        return new SaleCalculator(share).calculateTotal();
    }

    private BigDecimal calculateCashRatio(BigDecimal cash, BigDecimal netWorth) {
        if (netWorth.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return cash.divide(netWorth, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateGainLoss(Share share) {
        BigDecimal positionValue = new SaleCalculator(share).calculateTotal();
        BigDecimal invested = new PurchaseCalculator(share).calculateTotal();
        return positionValue.subtract(invested);
    }

    private BigDecimal calculatePercent(BigDecimal value, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(base, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private List<String> buildGainers() {
        if (this.exchange.getWeek() == 1) {
            return List.of("No market movement yet");
        }

        List<Stock> gainers = this.exchange.getGainers(3);

        if (gainers.isEmpty()) {
            return List.of("No gainers this week");
        }

        return gainers.stream()
                .map(stock -> stock.getSymbol() + " " + formatSignedPercent(stock.getLatestPriceChangePercent()))
                .toList();
    }

    private List<String> buildLosers() {
        if (this.exchange.getWeek() == 1) {
            return List.of("No market movement yet");
        }

        List<Stock> losers = this.exchange.getLosers(3);

        if (losers.isEmpty()) {
            return List.of("No losers this week");
        }

        return losers.stream()
                .map(stock -> stock.getSymbol() + " " + formatSignedPercent(stock.getLatestPriceChangePercent()))
                .toList();
    }

    private List<String> buildRecentActivity() {
        List<Transaction> transactions = new ArrayList<>(this.player.getTransactionArchive().getTransactions());

        if (transactions.isEmpty()) {
            return List.of("No transactions yet");
        }

        List<String> rows = new ArrayList<>();
        for (int i = transactions.size() - 1; i >= 0 && rows.size() < 5; i--) {
            rows.add(formatTransactionRow(transactions.get(i)));
        }

        return rows;
    }

    private String formatTransactionRow(Transaction transaction) {
        boolean isPurchase = transaction instanceof Purchase;
        String type = isPurchase ? "Buy" : "Sale";
        String symbol = transaction.getShare().getStock().getSymbol();
        BigDecimal total = transaction.getCalculator().calculateTotal();

        String signedTotal = isPurchase ? "-" + formatMoneyValue(total) : "+" + formatMoneyValue(total);

        return "W" + transaction.getWeek() + " " + type + " " + symbol + " " + signedTotal;
    }

    private String formatPositionSummary(Share share) {
        if (share == null) {
            return "No positions";
        }

        return share.getStock().getSymbol() + " - " + formatMoney(calculatePositionValue(share));
    }

    private String formatHoldingSummary(Share share) {
        if (share == null) {
            return "No positions";
        }

        return share.getStock().getSymbol() + " - " + formatSignedMoney(calculateGainLoss(share));
    }

    private String formatMoney(BigDecimal value) {
        return this.moneyFormat.format(value.setScale(0, RoundingMode.HALF_UP));
    }

    private String formatMoneyValue(BigDecimal value) {
        return formatMoney(value);
    }

    private String formatSignedMoney(BigDecimal value) {
        String sign = value.signum() > 0 ? "+" : "";
        return sign + formatMoney(value);
    }

    private String formatPercent(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String formatSignedPercent(BigDecimal value) {
        String sign = value.signum() > 0 ? "+" : "";
        return sign + value.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private static final class ShareAccumulator {
        private final Share firstShare;
        private BigDecimal totalQuantity = BigDecimal.ZERO;
        private BigDecimal totalPurchaseGross = BigDecimal.ZERO;

        ShareAccumulator(Share firstShare) {
            this.firstShare = firstShare;
        }

        void add(Share share) {
            this.totalQuantity = this.totalQuantity.add(share.getQuantity());
            this.totalPurchaseGross = this.totalPurchaseGross.add(share.getPurchasePrice().multiply(share.getQuantity()));
        }

        Share toShare() {
            BigDecimal averagePurchasePrice = this.totalPurchaseGross.divide(this.totalQuantity, 10, RoundingMode.HALF_UP);

            return new Share(this.firstShare.getStock(), this.totalQuantity, averagePurchasePrice);
        }
    }
}
