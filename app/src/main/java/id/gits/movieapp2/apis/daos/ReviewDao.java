package id.gits.movieapp2.apis.daos;

import id.gits.movieapp2.provider.review.ReviewCursor;

/**
 * Created by ibun on 20/09/15.
 */
public class ReviewDao {
    private String id;
    private String author;
    private String content;
    private String url;

    public ReviewDao(ReviewCursor c) {
        id = c.getReviewId();
        author = c.getAuthor();
        content = c.getContent();
        url = c.getUrl();
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }
}
