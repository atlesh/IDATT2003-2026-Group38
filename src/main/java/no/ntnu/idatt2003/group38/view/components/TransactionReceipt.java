package no.ntnu.idatt2003.group38.view.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import no.ntnu.idatt2003.group38.model.Share;
import no.ntnu.idatt2003.group38.transaction.Purchase;
import no.ntnu.idatt2003.group38.transaction.Transaction;

/**
 * A receipt panel shown after a transaction completes.
 *
 * <p>Displays the action ("Bought" or "Sold"), the share affected, and the
 * full breakdown of values and costs from the transaction's calculator:
 * gross value, commission, tax and total.
 */
public class TransactionReceipt {

  private final VBox root;
  private Runnable onClose = () -> { };

  /**
   * Builds a receipt for the completed transaction.
   *
   * @param transaction the transaction to render. Must not be {@code null}
   */
  public TransactionReceipt(Transaction transaction) {
    Objects.requireNonNull(transaction, "transaction cannot be null");

    Share share = transaction.getShare();
    boolean isPurchase = transaction instanceof Purchase;
    String actionWord = isPurchase ? "Bought" : "Sold";
    String unitPrice = isPurchase
        ? formatAmount(share.getPurchasePrice())
        : formatAmount(share.getStock().getSalesPrice());

    Label header = new Label("Transaction completed");
    header.getStyleClass().add("receipt-header");

    Label summary = new Label(String.format("%s %s × %s @ %s",
        actionWord,
        formatQuantity(share.getQuantity()),
        share.getStock().getSymbol(),
        unitPrice));
    summary.getStyleClass().add("receipt-summary");

    VBox breakdown = new VBox(6,
        row("Gross", transaction.getCalculator().calculateGross()),
        row("Commission", transaction.getCalculator().calculateCommission()),
        row("Tax", transaction.getCalculator().calculateTax()));
    breakdown.getStyleClass().add("receipt-breakdown");

    Region divider = new Region();
    divider.getStyleClass().add("receipt-divider");

    HBox totalRow = row("Total", transaction.getCalculator().calculateTotal());
    totalRow.getStyleClass().add("receipt-total");

    Button okButton = new Button("OK");
    okButton.getStyleClass().add("receipt-ok-button");
    okButton.setDefaultButton(true);
    okButton.setOnAction(e -> this.onClose.run());

    this.root = new VBox(14, header, summary, breakdown, divider, totalRow, okButton);
    this.root.setAlignment(Pos.CENTER);
    this.root.getStyleClass().add("receipt");
  }

  /**
   * Returns the root node so it can be mounted as modal content.
   *
   * @return the root layout container of the receipt
   */
  public Region getRoot() {
    return this.root;
  }

  /**
   * Registers the action to invoke when the user clicks "ok".
   *
   * @param onClose the action. Must not be {@code null}
   */
  public void setOnClose(Runnable onClose) {
    this.onClose = Objects.requireNonNull(onClose, "onClose cannot be null");
  }

  // Helpers

  /**
   * Builds a label/value row used for both the breakdown items and the total.
   */
  private HBox row(String label, BigDecimal value) {
    Label name = new Label(label);
    name.getStyleClass().add("receipt-row-label");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Label amount = new Label(formatAmount(value));
    amount.getStyleClass().add("receipt-row-value");

    HBox row = new HBox(name, spacer, amount);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("receipt-row");
    return row;
  }

  private String formatAmount(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private String formatQuantity(BigDecimal quantity) {
    return quantity.stripTrailingZeros().toPlainString();
  }
}