package com.dineth.debateTracker.imports;

/**
 * A source of tournament data to import. Implementations gather <b>all</b> data up front and return it
 * as a {@link TournamentImportData} bundle, so the importer can run its persistence as a single
 * transaction without any source-specific I/O leaking into it.
 *
 * <p>Implementations: {@link XmlTournamentSource} (a Tabbycat XML export) and
 * {@code ApiTournamentSource} (the Tabbycat REST API).
 */
public interface TournamentSource {

    /**
     * Read/fetch and assemble the full tournament dataset. Should perform all parsing or network I/O
     * here (before any transaction is opened by the importer).
     */
    TournamentImportData load();
}
