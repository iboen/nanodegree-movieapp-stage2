package id.gits.movieapp2.provider.video;

import android.net.Uri;
import android.provider.BaseColumns;

import id.gits.movieapp2.provider.MovieProvider;
import id.gits.movieapp2.provider.movie.MovieColumns;
import id.gits.movieapp2.provider.review.ReviewColumns;
import id.gits.movieapp2.provider.video.VideoColumns;

/**
 * Videos of movie
 */
public class VideoColumns implements BaseColumns {
    public static final String TABLE_NAME = "video";
    public static final Uri CONTENT_URI = Uri.parse(MovieProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String VIDEO_ID = "video_id";

    public static final String NAME = "name";

    public static final String KEY = "key";

    public static final String SIZE = "size";

    public static final String TYPE = "type";

    public static final String MOVIE_ID = "video__movie_id";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            VIDEO_ID,
            NAME,
            KEY,
            SIZE,
            TYPE,
            MOVIE_ID
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(VIDEO_ID) || c.contains("." + VIDEO_ID)) return true;
            if (c.equals(NAME) || c.contains("." + NAME)) return true;
            if (c.equals(KEY) || c.contains("." + KEY)) return true;
            if (c.equals(SIZE) || c.contains("." + SIZE)) return true;
            if (c.equals(TYPE) || c.contains("." + TYPE)) return true;
            if (c.equals(MOVIE_ID) || c.contains("." + MOVIE_ID)) return true;
        }
        return false;
    }

    public static final String PREFIX_MOVIE = TABLE_NAME + "__" + MovieColumns.TABLE_NAME;
}
