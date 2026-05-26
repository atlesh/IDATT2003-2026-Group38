package no.ntnu.idatt2003.group38.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for observable model components.
 *
 * <p>Maintains a list of {@link ModelObserver}s that are notified whenever
 * {@link #notifyObservers()} is called by a subclass after a state change.
 */
public abstract class Observable {

  private final List<ModelObserver> observers = new ArrayList<>();

  /**
   * Registers an observer to be notified of future state changes.
   *
   * @param observer the observer to register; must not be {@code null}
   * @throws NullPointerException if {@code observer} is {@code null}
   */
  public void addObserver(ModelObserver observer) {
    Objects.requireNonNull(observer, "observer cannot be null");
    this.observers.add(observer);
  }

  /**
   * Unregisters an observer so it no longer receives notifications.
   *
   * @param observer the observer to remove, or {@code null} to do nothing
   */
  public void removeObserver(ModelObserver observer) {
    this.observers.remove(observer);
  }

  /**
   * Notifies all registered observers of a state change. Subclasses should
   * call this after mutating their state.
   */
  protected void notifyObservers() {
    for (ModelObserver observer : this.observers) {
      observer.onModelChanged();
    }
  }
}
