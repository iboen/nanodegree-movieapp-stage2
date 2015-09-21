package id.gits.movieapp2.provider.video;

import id.gits.movieapp2.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Videos of movie
 */
public interface VideoModel extends BaseModel {

    /**
     * Get the {@code video_id} value.
     * Can be {@code null}.
     */
    @Nullable
    String getVideoId();

    /**
     * Get the {@code name} value.
     * Can be {@code null}.
     */
    @Nullable
    String getName();

    /**
     * Get the {@code key} value.
     * Can be {@code null}.
     */
    @Nullable
    String getKey();

    /**
     * Get the {@code size} value.
     * Can be {@code null}.
     */
    @Nullable
    Integer getSize();

    /**
     * Get the {@code type} value.
     * Can be {@code null}.
     */
    @Nullable
    String getType();

    /**
     * Get the {@code movie_id} value.
     */
    long getMovieId();
}
