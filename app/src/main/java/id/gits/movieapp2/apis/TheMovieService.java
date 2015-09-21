package id.gits.movieapp2.apis;

import id.gits.movieapp2.apis.daos.BaseListApiDao;
import id.gits.movieapp2.apis.daos.MovieDao;
import id.gits.movieapp2.apis.daos.ReviewDao;
import id.gits.movieapp2.apis.daos.VideoDao;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TheMovieService {
    @GET("discover/movie")
    Call<BaseListApiDao<MovieDao>> discoverMovie(@Query("api_key") String apiKey, @Query("sort_by") String sortBy);

    @GET("movie/{movie_id}/reviews")
    Call<BaseListApiDao<ReviewDao>> movieReviews(
            @Path("movie_id") long movieId, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}/videos")
    Call<BaseListApiDao<VideoDao>> movieVideos(
            @Path("movie_id") long movieId, @Query("api_key") String apiKey);
}