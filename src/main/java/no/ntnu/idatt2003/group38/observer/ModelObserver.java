package no.ntnu.idatt2003.group38.observer;

/**
 * Implemented by classes that wish to be notified when an {@link Observable}
 * model changes state.
 */
public interface ModelObserver {

  /**
   * Called by an {@link Observable} after its state has changed.
   */
  void onModelChanged();
}