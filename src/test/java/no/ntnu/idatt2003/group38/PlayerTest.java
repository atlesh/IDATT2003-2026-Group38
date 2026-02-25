package no.ntnu.idatt2003.group38;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

  private Player player;

  @BeforeEach
  void setUp() {
    player = new Player("Alice", new BigDecimal("5000"));
  }

  // POSITIVE TESTS

  @Test
  void constructor_initialisesFieldsCorrectly() {
    assertEquals("Alice", player.getName());
    assertEquals(new BigDecimal("5000"), player.getMoney());
    assertNotNull(player.getPortfolio());
    assertNotNull(player.getTransactionArchive());
  }

  @Test
  void addMoney_increasesBalance() {
    player.addMoney(new BigDecimal("250"));
    assertEquals(new BigDecimal("5250"), player.getMoney());
  }

  @Test
  void withdrawMoney_decreasesBalance() {
    player.withdrawMoney(new BigDecimal("1000"));
    assertEquals(new BigDecimal("4000"), player.getMoney());
  }

  // NEGATIVE TESTS
  @Test
  void nullName_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Player(null, new BigDecimal("1000")));
  }

  @Test
  void nullStartingMoney_throwsException() {
    assertThrows(NullPointerException.class,
        () -> new Player("Bob", null));
  }

  @Test
  void addMoney_nullAmount_throwsException() {
    assertThrows(NullPointerException.class,
        () -> player.addMoney(null));
  }

  @Test
  void addMoney_negativeOrZeroAmount_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> player.addMoney(BigDecimal.ZERO));
    assertThrows(IllegalArgumentException.class,
        () -> player.addMoney(new BigDecimal("-5")));
  }

  @Test
  void withdrawMoney_nullAmount_throwsException() {
    assertThrows(NullPointerException.class,
        () -> player.withdrawMoney(null));
  }

  @Test
  void withdrawMoney_negativeOrZeroAmount_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> player.withdrawMoney(BigDecimal.ZERO));
    assertThrows(IllegalArgumentException.class,
        () -> player.withdrawMoney(new BigDecimal("-10")));
  }
}
