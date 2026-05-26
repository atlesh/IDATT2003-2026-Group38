package no.ntnu.idatt2003.group38.controller.statistics;

import java.util.Objects;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.observer.ModelObserver;
import no.ntnu.idatt2003.group38.view.shell.Page;
import no.ntnu.idatt2003.group38.view.statistics.WeeklyStatisticsView;

/**
 * Controller for the Weekly Statistics page.
 *
 * <p>Connects the {@link WeeklyStatisticsView} to the {@link Exchange} model.
 * Pulls the week's gainers and losers from the exchange and observes it so the
 * panels refresh whenever the market advances.
 *
 * <p>Implements {@link Page} so the {@code ShellController} can manage its
 * lifecycle. The observer is registered in {@link #onAttach()} and removed in
 * {@link #onDetach()}.
 */
public class WeeklyStatisticsController implements Page, ModelObserver {

  private static final int LIMIT = 10;

  private final WeeklyStatisticsView view;
  private final Exchange exchange;

  /**
   * Creates a new weekly statistics controller. The view is built but not
   * populated until {@link #onAttach()} is called by the shell.
   *
   * @param exchange the exchange to read gainers and losers from.
   * Must not be {@code null}
   */
  public WeeklyStatisticsController(Exchange exchange) {
    this.exchange = Objects.requireNonNull(exchange, "exchange cannot be null");
    this.view = new WeeklyStatisticsView();
  }

  // Page

  /** {@inheritDoc} */
  @Override
  public Region getRoot() {
    return this.view.getRoot();
  }

  /** {@inheritDoc} */
  @Override
  public void onAttach() {
    this.exchange.addObserver(this);
    attachStylesheet();
    refresh();
  }

  /** {@inheritDoc} */
  @Override
  public void onDetach() {
    this.exchange.removeObserver(this);
  }

  // ModelObserver

  /** {@inheritDoc} */
  @Override
  public void onModelChanged() {
    refresh();
  }

  // Internal

  /**
   * Pulls the current gainers and losers from the exchange and pushes
   * them to the view.
   */
  private void refresh() {
    this.view.setGainers(this.exchange.getGainers(LIMIT));
    this.view.setLosers(this.exchange.getLosers(LIMIT));
  }

  /**
   * Attaches the view's stylesheet once the view is part of a scene.
   */
  private void attachStylesheet() {
    Scene scene = this.view.getRoot().getScene();
    if (scene != null) {
      this.view.attachTo(scene);
    }
  }
}
