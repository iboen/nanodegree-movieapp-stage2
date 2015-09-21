package id.gits.movieapp2.provider.review;

import id.gits.movieapp2.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Reviews of movie
 */
public interface ReviewModel extends BaseModel {

    /**
     * Get the {@code review_id} value.
     * Can be {@code null}.
     */
    @Nullable
    String getReviewId();

    /**
     * Get the {@code author} value.
     * Can be {@code null}.
     */
    @Nullable
    String getAuthor();

    /**
     * Get the {@code content} value.
     * Can be {@code null}.
     */
    @Nullable
    String getContent();

    /**
     * Get the {@code url} value.
     * Can be {@code null}.
     */
    @Nullable
    String getUrl();

    /**
     * Get the {@code movie_id} value.
     */
    long getMovieId();
}
