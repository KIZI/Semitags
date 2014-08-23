package cz.ilasek.namedentities.reader;

import cz.ilasek.namedentities.index.models.Article;
import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.index.models.Paragraph;

public interface Reader {
    Article readArticle() throws ReaderException;
    Paragraph readParagraph() throws ReaderException;
    EntityMentionInParagraph readEntityMention() throws ReaderException;
    String readSurfaceForm() throws ReaderException;
    String readEntity() throws ReaderException;
    void close() throws ReaderException;
}
