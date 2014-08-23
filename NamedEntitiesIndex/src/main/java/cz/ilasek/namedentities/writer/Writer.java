package cz.ilasek.namedentities.writer;

import java.util.Set;

import cz.ilasek.namedentities.index.models.EntitySurfaceFormMention;
import cz.ilasek.namedentities.set.EntitySet;
import cz.ilasek.namedentities.set.SurfaceFormSet;

public interface Writer {
    void appendArticle(String articleId, String articleTitle) throws WriterException;
    void appenParagraph(String paragraphUri, String text) throws WriterException;
    void appendEntitiesMentions(Set<EntitySurfaceFormMention> entitiyMentions, String paragraphUri) throws WriterException;
    void appendEntityMention(String entityUri, String surfaceForm, String paragraphUri) throws WriterException;
    void appendEntity(String entityUri) throws WriterException;
    void appendSurfaceForm(String surfaceForm) throws WriterException;
    void persistSurfaceForms(SurfaceFormSet surfaceForms) throws WriterException;
    void persistEntities(EntitySet entities) throws WriterException;
    void close() throws WriterException;
}
