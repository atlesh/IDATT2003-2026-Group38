package no.ntnu.idatt2003.group38.event;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import no.ntnu.idatt2003.group38.model.Stock;

/**
 * Generates {@link Rumor}s about upcoming price movements.
 *
 * <p>Each time {@link #generate(Collection)} is called, the generator picks
 * up to {@link #DEFAULT_MAX_RUMORS_PER_WEEK} stocks at random and produces
 * a rumor for each.</p>
 */
public class RumorGenerator {

  public static final double DEFAULT_ACCURACY = 0.65;
  public static final int DEFAULT_MAX_RUMORS_PER_WEEK = 2;
  public static final double DEFAULT_RUMOR_CHANCE = 0.40;

  private final Random random;
  private final double accuracy;
  private final int maxRumorsPerWeek;
  private final double rumorChance;

  /**
   * Creates a rumor generator with default settings.
   *
   * @param random the source of randomness.
   * @throws NullPointerException if {@code random} is {@code null}
   */
  public RumorGenerator(Random random) {
    this(random, DEFAULT_ACCURACY, DEFAULT_MAX_RUMORS_PER_WEEK, DEFAULT_RUMOR_CHANCE);
  }

  /**
   * Creates a rumor generator with custom settings.
   *
   * @param random the source of randomness.
   * @param accuracy chance that any single rumor is truthful.
   * @param maxRumorsPerWeek upper bound on rumors generated per week. Must be ≥ 0
   * @param rumorChance chance per week that any rumors are generated.
   * @throws NullPointerException if {@code random} is {@code null}
   * @throws IllegalArgumentException if any probability is out of range or
   *     {@code maxRumorsPerWeek} is negative
   */
  public RumorGenerator(Random random, double accuracy, int maxRumorsPerWeek, double rumorChance) {
    this.random = Objects.requireNonNull(random, "random cannot be null");
    if (accuracy < 0.0 || accuracy > 1.0) {
      throw new IllegalArgumentException("accuracy must be in [0.0, 1.0]");
    }
    if (rumorChance < 0.0 || rumorChance > 1.0) {
      throw new IllegalArgumentException("rumorChance must be in [0.0, 1.0]");
    }
    if (maxRumorsPerWeek < 0) {
      throw new IllegalArgumentException("maxRumorsPerWeek cannot be negative");
    }
    this.accuracy = accuracy;
    this.maxRumorsPerWeek = maxRumorsPerWeek;
    this.rumorChance = rumorChance;
  }

  /**
   * Generates rumors for the upcoming week.
   *
   * <p>With probability {@code rumorChance}, picks up to
   * {@code maxRumorsPerWeek} distinct stocks at random and produces a
   * rumor for each. Returns an empty list otherwise.</p>
   *
   * @param stocks the stocks the rumor mill can draw from; must not be {@code null}
   * @return rumors for the upcoming week, possibly empty, never {@code null}
   * @throws NullPointerException if {@code stocks} is {@code null}
   */
  public List<Rumor> generate(Collection<Stock> stocks) {
    Objects.requireNonNull(stocks, "stocks cannot be null");

    if (stocks.isEmpty() || this.random.nextDouble() >= this.rumorChance) {
      return List.of();
    }

    List<Stock> pool = new java.util.ArrayList<>(stocks);
    java.util.Collections.shuffle(pool, this.random);

    int count = 1 + this.random.nextInt(this.maxRumorsPerWeek);
    count = Math.min(count, pool.size());

    List<Rumor> rumors = new java.util.ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      Stock stock = pool.get(i);
      Rumor.Direction direction = this.random.nextBoolean()
          ? Rumor.Direction.RISE
          : Rumor.Direction.FALL;
      boolean truthful = this.random.nextDouble() < this.accuracy;
      rumors.add(new Rumor(stock, direction, truthful));
    }
    return List.copyOf(rumors);
  }
}
