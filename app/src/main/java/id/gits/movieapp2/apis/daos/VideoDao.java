package id.gits.movieapp2.apis.daos;

import id.gits.movieapp2.provider.video.VideoCursor;

/**
 * Created by ibun on 20/09/15.
 */
public class VideoDao {
    private String id;
    private String iso_639_1;
    private String key;
    private String name;
    private String site;
    private int size;
    private String type;

    public VideoDao(VideoCursor c) {
        id = c.getVideoId();
        key = c.getKey();
        name = c.getName();
        size = c.getSize();
        type = c.getType();
    }

    public String getId() {
        return id;
    }

    public String getIso_639_1() {
        return iso_639_1;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }
}
