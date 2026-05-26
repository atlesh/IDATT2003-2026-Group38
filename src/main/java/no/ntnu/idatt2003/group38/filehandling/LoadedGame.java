package no.ntnu.idatt2003.group38.filehandling;

import no.ntnu.idatt2003.group38.exchange.Exchange;
import no.ntnu.idatt2003.group38.model.Player;

/**
 * Holds a fully reconstructed game after loading saved state from disk.
 *
 * @param player   the restored player
 * @param exchange the restored exchange
 */
public record LoadedGame(Player player, Exchange exchange) {
}
