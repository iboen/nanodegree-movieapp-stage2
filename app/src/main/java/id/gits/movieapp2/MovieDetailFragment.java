package id.gits.movieapp2;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.gits.movieapp2.apis.RetrofitHelper;
import id.gits.movieapp2.apis.daos.BaseListApiDao;
import id.gits.movieapp2.apis.daos.MovieDao;
import id.gits.movieapp2.apis.daos.ReviewDao;
import id.gits.movieapp2.apis.daos.VideoDao;
import id.gits.movieapp2.provider.movie.MovieContentValues;
import id.gits.movieapp2.provider.movie.MovieCursor;
import id.gits.movieapp2.provider.movie.MovieSelection;
import id.gits.movieapp2.provider.review.ReviewContentValues;
import id.gits.movieapp2.provider.review.ReviewCursor;
import id.gits.movieapp2.provider.review.ReviewSelection;
import id.gits.movieapp2.provider.video.VideoContentValues;
import id.gits.movieapp2.provider.video.VideoCursor;
import id.gits.movieapp2.provider.video.VideoSelection;
import id.gits.movieapp2.utils.Constant;
import retrofit.Callback;
import retrofit.Response;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item that this fragment
     * represents.
     */
    public static final String ARG_ITEM = "item";

    @Bind(R.id.tv_title)
    TextView mTvTitle;
    @Bind(R.id.tv_releasedate)
    TextView mTvReleaseDate;
    @Bind(R.id.tv_synopsis)
    TextView mTvSynopsis;
    @Bind(R.id.tv_voteavg)
    TextView mTvVoteAvg;
    @Bind(R.id.iv_poster)
    ImageView mIvPoster;
    @Bind(R.id.layout_videos)
    LinearLayout mLayoutVideos;
    @Bind(R.id.layout_reviews)
    LinearLayout mLayoutReviews;
    @Bind(R.id.tv_video_label)
    TextView mTvVideoLabel;
    @Bind(R.id.tv_review_label)
    TextView mTvReviewLabel;
    @Bind(R.id.btn_add_fav)
    Button mBtnAddFav;

    private MovieDao mMovieDao;
    private List<VideoDao> mVideosDao = new ArrayList<>();
    private List<ReviewDao> mReviewsDao = new ArrayList<>();
    private String mFirstVideoUrl;

    private boolean isFavorited = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_ITEM)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mMovieDao = getArguments().getParcelable(ARG_ITEM);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mMovieDao.getTitle());
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            if (!TextUtils.isEmpty(mFirstVideoUrl)) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_SUBJECT, mMovieDao.getTitle());
                share.putExtra(Intent.EXTRA_TEXT, mFirstVideoUrl);

                startActivity(Intent.createChooser(share, "Share video!"));
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        ButterKnife.bind(this, rootView);

        // Show the dummy content as text in a TextView.
        if (mMovieDao != null) {
            initLayout();

            callApis(mMovieDao.getId());
        }

        //check is favorited
        MovieSelection movieSelection = new MovieSelection();
        movieSelection.movieId(mMovieDao.getId());
        MovieCursor c = movieSelection.query(getActivity().getContentResolver());
        toggleButtonFav(c.getCount() > 0);

        return rootView;
    }

    private void callApis(long id) {
        callReviewsApi(id);
        callVideosApi(id);
    }

    private void callReviewsApi(long id) {
        ReviewSelection reviewSelection = new ReviewSelection();
        reviewSelection.movieMovieId(mMovieDao.getId());
        ReviewCursor c = reviewSelection.query(getActivity().getContentResolver());
        if (c.getCount() > 0) {
            List<ReviewDao> reviews = new ArrayList<>();
            while (c.moveToNext()) {
                reviews.add(new ReviewDao(c));
            }
            initReviewsLayout(reviews);
            c.close();
        } else {
            RetrofitHelper.getInstance().getService().movieReviews(id, Constant.MOVIEDB_APIKEY)
                    .enqueue(new Callback<BaseListApiDao<ReviewDao>>() {
                                 @Override
                                 public void onResponse(Response<BaseListApiDao<ReviewDao>> response) {
                                     if (response.body() != null) {
                                         mReviewsDao.addAll(response.body().getResults());
                                         if (mReviewsDao.size() > 0) {
                                             initReviewsLayout(mReviewsDao);
                                         } else {
                                             mTvReviewLabel.setText(R.string.detail_no_reviews);
                                         }
                                     }
                                 }

                                 @Override
                                 public void onFailure(Throwable t) {

                                 }
                             }

                    );
        }
    }

    private void callVideosApi(long id) {
        //get video from api
        VideoSelection videoSelection = new VideoSelection();
        videoSelection.movieMovieId(mMovieDao.getId());
        VideoCursor videoCursor = videoSelection.query(getActivity().getContentResolver());
        if (videoCursor.getCount() > 0) {
            List<VideoDao> videos = new ArrayList<>();
            while (videoCursor.moveToNext()) {
                videos.add(new VideoDao(videoCursor));
            }
            initVideosLayout(videos);
            videoCursor.close();
        } else {
            RetrofitHelper.getInstance()
                    .getService()
                    .movieVideos(id, Constant.MOVIEDB_APIKEY)
                    .enqueue(new Callback<BaseListApiDao<VideoDao>>() {
                                 @Override
                                 public void onResponse(Response<BaseListApiDao<VideoDao>> response) {
                                     if (response.body() != null) {
                                         mVideosDao.addAll(response.body().getResults());
                                         if (mVideosDao.size() > 0) {
                                             mFirstVideoUrl = getYoutubeUrl(mVideosDao.get(0).getKey());
                                             initVideosLayout(mVideosDao);
                                         } else {
                                             mTvVideoLabel.setText(R.string.detail_no_videos);
                                         }
                                     }
                                 }

                                 @Override
                                 public void onFailure(Throwable t) {

                                 }
                             }

                    );
        }
    }

    private String getYoutubeUrl(String key) {
        return "https://www.youtube.com/watch?v=" + key;
    }

    private void initLayout() {
        mTvTitle.setText(mMovieDao.getTitle());

        //set release date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate;
        try {
            Date dateRelease = sdf.parse(mMovieDao.getRelease_date());
            formattedDate = DateFormat.format("dd MMM yyyy", dateRelease).toString();
        } catch (ParseException e) {
            e.printStackTrace();
            formattedDate = mMovieDao.getRelease_date();
        }
        mTvReleaseDate.setText(formattedDate);

        mTvVoteAvg.setText(mMovieDao.getVote_average() + "/" + "10");
        if (!TextUtils.isEmpty(mMovieDao.getOverview())) {
            mTvSynopsis.setText(Html.fromHtml(mMovieDao.getOverview()));
        } else {
            mTvSynopsis.setText("No description");
        }

        //set image
        Picasso.with(getActivity()).load(Constant.ROOT_POSTER_IMAGE_URL + mMovieDao.getPoster_path())
                .error(R.color.grey).placeholder(R.color.grey).into(mIvPoster);
    }

    private void initVideosLayout(List<VideoDao> videos) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i = 0; i < videos.size(); i++) {
            final VideoDao video = videos.get(i);

            View v = inflater.inflate(R.layout.item_video, mLayoutVideos, false);
            ImageView ivThumb = (ImageView) v.findViewById(R.id.iv_video_thumbnail);
            TextView tvTitle = (TextView) v.findViewById(R.id.tv_video_title);

            tvTitle.setText(video.getName());
            Picasso.with(getActivity()).load("http://img.youtube.com/vi/" + video.getKey() + "/0.jpg")
                    .error(R.color.colorPrimary).placeholder(R.color.grey).into(ivThumb);

            //open youtube link
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getYoutubeUrl(video.getKey())));
                    startActivity(intent);
                }
            });
            mLayoutVideos.addView(v);
        }
    }

    private void initReviewsLayout(List<ReviewDao> reviews) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i = 0; i < reviews.size(); i++) {
            ReviewDao review = reviews.get(i);

            View v = inflater.inflate(R.layout.item_review, mLayoutReviews, false);
            TextView tvContent = (TextView) v.findViewById(R.id.tv_review_content);
            TextView tvAuthor = (TextView) v.findViewById(R.id.tv_review_author);

            tvContent.setText("\"" + review.getContent() + "\"");
            tvAuthor.setText(review.getAuthor());

            mLayoutReviews.addView(v);
        }
    }

    @OnClick(R.id.btn_add_fav)
    public void onClickAddFav(View v) {
        if (isFavorited) {
            //REMOVE FROM FAV
            MovieSelection movieSelection = new MovieSelection();
            movieSelection.movieId(mMovieDao.getId());
            movieSelection.delete(getActivity().getContentResolver());

            VideoSelection videoSelection = new VideoSelection();
            videoSelection.movieId(mMovieDao.getId());
            videoSelection.delete(getActivity().getContentResolver());

            ReviewSelection reviewSelection = new ReviewSelection();
            reviewSelection.movieId(mMovieDao.getId());
            reviewSelection.delete(getActivity().getContentResolver());


            toggleButtonFav(false);
        } else {
            //SAVE MOVIE
            MovieContentValues values = new MovieContentValues();
            values.putBackdropPath(mMovieDao.getBackdrop_path());
            values.putMovieId(mMovieDao.getId());
            values.putOverview(mMovieDao.getOverview());
            values.putPosterPath(mMovieDao.getPoster_path());
            values.putReleaseDate(mMovieDao.getRelease_date());
            values.putTitle(mMovieDao.getTitle());
            values.putVoteAverage(mMovieDao.getVote_average());

            Uri uri = values.insert(getActivity().getContentResolver());

            //SAVE VIDEOS
            for (int i = 0; i < mVideosDao.size(); i++) {
                VideoDao dao = mVideosDao.get(i);
                VideoContentValues cv = new VideoContentValues();
                cv.putKey(dao.getKey());
                cv.putVideoId(dao.getId());
                cv.putName(dao.getName());
                cv.putSize(dao.getSize());
                cv.putType(dao.getType());
                cv.putMovieId(ContentUris.parseId(uri));
                cv.insert(getActivity().getContentResolver());
            }

            //SAVE REVIEWS
            for (int i = 0; i < mReviewsDao.size(); i++) {
                ReviewDao dao = mReviewsDao.get(i);
                ReviewContentValues cv = new ReviewContentValues();
                cv.putMovieId(ContentUris.parseId(uri));
                cv.putAuthor(dao.getAuthor());
                cv.putContent(dao.getContent());
                cv.putReviewId(dao.getId());
                cv.insert(getActivity().getContentResolver());
            }

            // change button to added to fav
            toggleButtonFav(true);
        }
    }

    private void toggleButtonFav(boolean setToAdded) {
        if (setToAdded) {
            isFavorited = true;
            mBtnAddFav.setText(R.string.main_remove_from_fav);
        } else {
            isFavorited = false;
            mBtnAddFav.setText(R.string.main_add_to_fav);
        }

    }

}
