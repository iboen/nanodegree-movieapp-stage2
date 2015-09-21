package id.gits.movieapp2.apis.daos;

import java.util.List;

/**
 * Created by ibun on 19/09/15.
 */
public class BaseListApiDao<T> {
    private int page;
    private int total_pages;
    private long total_results;
    private List<T> results;

    public List<T> getResults() {
        return results;
    }

    public long getTotal_results() {
        return total_results;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public int getPage() {
        return page;
    }


}
