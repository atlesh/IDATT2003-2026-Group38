package no.ntnu.idatt2003.group38.observer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Observable}.
 */
public class ObservableTest {

  private static final class TestObservable extends Observable {
    void fire() {
      notifyObservers();
    }
  }

  private static final class CountingObserver implements ModelObserver {
    int count = 0;

    @Override
    public void onModelChanged() {
      this.count++;
    }
  }

  private TestObservable observable;

  @BeforeEach
  void setUp() {
    observable = new TestObservable();
  }

  // POSITIVE TESTS

  @Test
  void addObserver_registeredObserverReceivesNotification() {
    CountingObserver observer = new CountingObserver();
    observable.addObserver(observer);

    observable.fire();

    assertEquals(1, observer.count);
  }

  @Test
  void multipleObservers_allNotifiedOnFire() {
    CountingObserver first = new CountingObserver();
    CountingObserver second = new CountingObserver();
    observable.addObserver(first);
    observable.addObserver(second);

    observable.fire();

    assertEquals(1, first.count);
    assertEquals(1, second.count);
  }

  @Test
  void observersFiredInRegistrationOrder() {
    List<String> calls = new ArrayList<>();
    observable.addObserver(() -> calls.add("first"));
    observable.addObserver(() -> calls.add("second"));
    observable.addObserver(() -> calls.add("third"));

    observable.fire();

    assertEquals(List.of("first", "second", "third"), calls);
  }

  @Test
  void observerNoLongerReceivesNotification() {
    CountingObserver observer = new CountingObserver();
    observable.addObserver(observer);
    observable.removeObserver(observer);

    observable.fire();

    assertEquals(0, observer.count);
  }

  @Test
  void withoutObservers_doesNotThrow() {
    assertDoesNotThrow(() -> observable.fire());
  }

  @Test
  void unknownObserver_doesNotThrow() {
    CountingObserver observer = new CountingObserver();

    // Removing an observer that was never added should be a no-op
    assertDoesNotThrow(() -> observable.removeObserver(observer));
  }

  // NEGATIVE TESTS

  @Test
  void nullObserver_throwsException() {
    assertThrows(NullPointerException.class, () -> observable.addObserver(null));
  }
}
