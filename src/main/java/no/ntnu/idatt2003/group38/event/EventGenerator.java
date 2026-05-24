package no.ntnu.idatt2003.group38.event;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

/**
 * Samples random {@link MarketEvent}s on a per-stock basis.
 *
 * <p>Each time {@link #sample()} is called, the generator rolls a die and
 * either returns an empty {@link Optional} (no event) or a randomly chosen
 * event. The chance of an event firing is intentionally low so that events
 * feel like rare news rather than weekly noise.</p>
 *
 * <p>Events are constructed lazily through factory lambdas, so each call to
 * {@link #sample()} returns a fresh instance — useful if events ever carry
 * per-instance state.</p>
 */
public class EventGenerator {

  public static final double DEFAULT_EVENT_CHANCE = 0.01;

  private final Random random;
  private final double eventChance;
  private final List<Function<Random, MarketEvent>> eventFactories;

  /**
   * Creates a generator with the default {@value #DEFAULT_EVENT_CHANCE} chance
   * per stock per week, and the default event catalogue
   * ({@link EarningsBeatEvent} and {@link ScandalEvent}).
   *
   * @param random the source of randomness; must not be {@code null}
   */
  public EventGenerator(Random random) {
    this(random, DEFAULT_EVENT_CHANCE, List.of(
        EarningsBeatEvent::new,
        ScandalEvent::new
    ));
  }

  /**
   * Creates a generator with a custom firing chance and event catalogue.
   *
   * <p>Useful for testing: supply a seeded {@code Random}, {@code eventChance = 1.0}
   * and a single factory to make the outcome deterministic.</p>
   *
   * @param random the source of randomness; must not be {@code null}
   * @param eventChance the chance of an event firing per call, in {@code [0.0, 1.0]}
   * @param eventFactories factories for each event type the generator may pick from;
   *     must not be {@code null} or empty
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if {@code eventChance} is outside
   *     {@code [0.0, 1.0]} or {@code eventFactories} is empty
   */
  public EventGenerator(
      Random random,
      double eventChance,
      List<Function<Random, MarketEvent>> eventFactories) {
    this.random = Objects.requireNonNull(random, "random cannot be null");
    Objects.requireNonNull(eventFactories, "eventFactories cannot be null");
    if (eventChance < 0.0 || eventChance > 1.0) {
      throw new IllegalArgumentException("eventChance must be in [0.0, 1.0]");
    }
    if (eventFactories.isEmpty()) {
      throw new IllegalArgumentException("eventFactories cannot be empty");
    }
    this.eventChance = eventChance;
    this.eventFactories = List.copyOf(eventFactories);
  }

  /**
   * Rolls the die and either returns a randomly selected event or an empty
   * {@link Optional}.
   *
   * @return a new {@link MarketEvent} if one fires, otherwise {@link Optional#empty()}
   */
  public Optional<MarketEvent> sample() {
    if (this.random.nextDouble() >= this.eventChance) {
      return Optional.empty();
    }
    int index = this.random.nextInt(this.eventFactories.size());
    return Optional.of(this.eventFactories.get(index).apply(this.random));
  }
}