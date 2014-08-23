package cz.ilasek.namedentities.index.models;

public class Article {
    private final String articleId;
    private final String articleTitle;

    public Article(String articleId, String articleTitle) {
        super();
        this.articleId = articleId;
        this.articleTitle = articleTitle;
    }

    public String getArticleId() {
        return articleId;
    }

    public String getArticleTitle() {
        return articleTitle;
    }
}
