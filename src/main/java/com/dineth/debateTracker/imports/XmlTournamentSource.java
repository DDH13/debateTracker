package com.dineth.debateTracker.imports;

import com.dineth.debateTracker.utils.ParseTabbycatXML;

import java.io.InputStream;

/**
 * A {@link TournamentSource} backed by a Tabbycat XML export. Wraps {@link ParseTabbycatXML} (the
 * DOM parser) and assembles its per-section accessors into a single {@link TournamentImportData}.
 *
 * <p>Construct from a file path on disk or from an arbitrary stream (e.g. an uploaded file).
 */
public class XmlTournamentSource implements TournamentSource {

    private final String xmlPath;
    private final InputStream inputStream;

    public XmlTournamentSource(String xmlPath) {
        this.xmlPath = xmlPath;
        this.inputStream = null;
    }

    public XmlTournamentSource(InputStream inputStream) {
        this.xmlPath = null;
        this.inputStream = inputStream;
    }

    @Override
    public TournamentImportData load() {
        ParseTabbycatXML parser = inputStream != null
                ? new ParseTabbycatXML(inputStream)
                : new ParseTabbycatXML(xmlPath);
        parser.parseXML();
        return new TournamentImportData(
                parser.getTournamentDTO(),
                parser.getInstitutionDTOs(),
                parser.getJudgeDTOs(),
                parser.getTeamDTOs(),
                parser.getMotionDTOs(),
                parser.getRoundsDTO(),
                parser.getBreakCategoryDTOs());
    }
}
