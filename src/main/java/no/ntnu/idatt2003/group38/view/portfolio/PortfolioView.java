package no.ntnu.idatt2003.group38.view.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.calculator.SaleCalculator;
import no.ntnu.idatt2003.group38.model.Share;


public class PortfolioView {

    private static final String STYLESHEET = "/stylesheets/portfolio.css";

    private final HBox root;
    private final VBox rowsContainer;

    private final Label cashLabel;
    private final Label portfolioValueLabel;
    private final Label netWorthLabel;

    private final Label symbolLabel;
    private final Label companyLabel;
    private final Label quantityLabel;
    private final Label buyPriceLabel;
    private final Label currentPriceLabel;
    private final Label positionValueLabel;

    private Consumer<Share> onShareSelected = share -> {
    };

    public PortfolioView() {
        Label title = new Label("Portfolio");
        title.getStyleClass().add("portfolio-title");

        this.rowsContainer = new VBox(8);
        this.rowsContainer.getStyleClass().add("portfolio-rows");

        HBox header = buildHeaderRow();

        ScrollPane scrollpane = new ScrollPane(this.rowsContainer);
        scrollpane.setFitToWidth(true);
        scrollpane.getStyleClass().add("portfolio-scroll-pane");
        VBox.setVgrow(scrollpane, Priority.ALWAYS);

        VBox listPanel = new  VBox(16, title, header, scrollpane);
        listPanel.getStyleClass().add("portfolio-list-panel");
        HBox.setHgrow(listPanel, Priority.ALWAYS);


        Label summaryTitle = new Label("Portfolio Summary");
        summaryTitle.getStyleClass().add("portfolio-summary-title");

        this.cashLabel = new Label();
        this.cashLabel.getStyleClass().add("stock-card-line");

        this.portfolioValueLabel = new Label();
        this.portfolioValueLabel.getStyleClass().add("stock-card-line");

        this.netWorthLabel = new Label();
        this.netWorthLabel.getStyleClass().add("stock-card-line");

        Label selectedTitle = new Label("Selected Share");
        selectedTitle.getStyleClass().add("stock-card-title");

        this.symbolLabel = new Label();
        this.symbolLabel.getStyleClass().add("stock-card-title");

        this.companyLabel = new Label();
        this.companyLabel.getStyleClass().add("stock-card-line");

        this.quantityLabel = new Label();
        this.quantityLabel.getStyleClass().add("stock-card-line");

        this.buyPriceLabel = new Label();
        this.buyPriceLabel.getStyleClass().add("stock-card-line");

        this.currentPriceLabel = new Label();
        this.currentPriceLabel.getStyleClass().add("stock-card-line");

        this.positionValueLabel = new Label();
        this.positionValueLabel.getStyleClass().add("stock-card-line");

        VBox detailsPanel = new VBox(
                8,
                summaryTitle,
                this.cashLabel,
                this.portfolioValueLabel,
                this.netWorthLabel,
                new Separator(),
                selectedTitle,
                this.symbolLabel,
                this.companyLabel,
                this.quantityLabel,
                this.buyPriceLabel,
                this.currentPriceLabel,
                this.positionValueLabel);
        detailsPanel.getStyleClass().add("portfolio-details-panel");
        detailsPanel.setAlignment(Pos.TOP_LEFT);
        detailsPanel.setPrefWidth(260);
        detailsPanel.setMinWidth(260);

        this.root = new HBox(20, listPanel, detailsPanel);
        this.root.setPadding(new Insets(20));
        this.root.getStyleClass().add("portfolio-root");

        setSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        showSelectedShare(null);
    }

    public Region getRoot() {
        return this.root;
    }

    public  void setShares(List<Share> shares) {
        Objects.requireNonNull(shares, "Shares cannot be null");
        this.rowsContainer.getChildren().clear();

        if(shares.isEmpty()) {
            Label emptyLabel = new Label("No shares owned yet");
            emptyLabel.getStyleClass().add("stock-card-line");
            this.rowsContainer.getChildren().add(emptyLabel);
            return;
        }

        for(Share share : shares) {
            this.rowsContainer.getChildren().add(buildShareRow(share));
        }
    }

    public void showSelectedShare(Share share) {
        if (share == null) {
            this.symbolLabel.setText("No share selected");
            this.companyLabel.setText("");
            this.quantityLabel.setText("");
            this.buyPriceLabel.setText("");
            this.currentPriceLabel.setText("");
            this.positionValueLabel.setText("");
            return;
        }

        BigDecimal currentPrice = share.getStock().getSalesPrice();
        BigDecimal positionValue = new SaleCalculator(share).calculateTotal();

        this.symbolLabel.setText(share.getStock().getSymbol());
        this.companyLabel.setText(share.getStock().getCompany());
        this.quantityLabel.setText("Quantity: " + formatQuantity(share.getQuantity()));
        this.buyPriceLabel.setText("Buy price: " + formatMoney(share.getPurchasePrice()));
        this.currentPriceLabel.setText("Current price: " + formatMoney(currentPrice));
        this.positionValueLabel.setText("Value: " + formatMoney(positionValue));
    }

    public void setSummary(
            BigDecimal cash,
            BigDecimal portfolioValue,
            BigDecimal netWorth) {

        Objects.requireNonNull(cash, "Cash cannot be null");
        Objects.requireNonNull(portfolioValue, "Portfolio value cannot be null");
        Objects.requireNonNull(netWorth, "Net worth cannot be null");

        this.cashLabel.setText("Cash: " + formatMoney(cash));
        this.portfolioValueLabel.setText("Portfolio: " + formatMoney(portfolioValue));
        this.netWorthLabel.setText("Net worth: " + formatMoney(netWorth));
    }

    public void setOnShareSelected(Consumer<Share> onShareSelected) {
        this.onShareSelected = Objects.requireNonNull(onShareSelected, "onShareSelected cannot be null");
    }

    public void attachTo(Scene scene) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        String css = Objects.requireNonNull(getClass().getResource(STYLESHEET), "Could not find stylesheet at " + STYLESHEET).toExternalForm();
        if(!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    private HBox buildHeaderRow() {
        Label stockHeader = new Label("Stock");
        Label companyHeader = new Label("Company");
        Label quantityHeader = new Label("Qty");
        Label buyHeader = new Label("Buy");
        Label currentHeader = new Label("Current");
        Label valueHeader = new Label("Value");

        for (Label label : List.of(
                stockHeader, companyHeader, quantityHeader, buyHeader, currentHeader, valueHeader)) {
            label.getStyleClass().add("market-column-header");
        }

        HBox header = new HBox(
                cell(stockHeader, 80),
                cell(companyHeader, 220),
                cell(quantityHeader, 70),
                cell(buyHeader, 90),
                cell(currentHeader, 90),
                cell(valueHeader, 100));

        header.getStyleClass().add("market-row-header");
        return header;
    }

    private HBox buildShareRow(Share share) {
        BigDecimal currentPrice = share.getStock().getSalesPrice();
        BigDecimal positionValue = new SaleCalculator(share).calculateTotal();

        Label symbol = new Label(share.getStock().getSymbol());
        Label company = new Label(share.getStock().getCompany());
        Label quantity = new Label(formatQuantity(share.getQuantity()));
        Label buyPrice = new Label(formatMoney(share.getPurchasePrice()));
        Label current = new Label(formatMoney(currentPrice));
        Label value = new Label(formatMoney(positionValue));

        HBox row = new  HBox(
                cell(symbol, 80),
                cell(company, 220),
                cell(quantity, 70),
                cell(buyPrice, 90),
                cell(current, 90),
                cell(value, 100));
        row.getStyleClass().add("market-row");
        row.setOnMouseClicked(event -> this.onShareSelected.accept(share));
        return row;
    }

    private HBox cell(Node content, double width) {
        HBox box = new HBox(content);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(width);
        return box;
    }

    private String formatQuantity(BigDecimal quantity) {
        return quantity.stripTrailingZeros().toPlainString();
    }

    private String formatMoney(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
