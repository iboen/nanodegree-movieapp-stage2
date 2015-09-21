package id.gits.movieapp2;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import id.gits.movieapp2.adapters.MovieListAdapter;
import id.gits.movieapp2.apis.RetrofitHelper;
import id.gits.movieapp2.apis.daos.BaseListApiDao;
import id.gits.movieapp2.apis.daos.ErrorApiDao;
import id.gits.movieapp2.apis.daos.MovieDao;
import id.gits.movieapp2.provider.movie.MovieCursor;
import id.gits.movieapp2.provider.movie.MovieSelection;
import id.gits.movieapp2.utils.Constant;
import id.gits.movieapp2.utils.MyProgressView;
import retrofit.Callback;
import retrofit.Response;

/**
 * A list fragment representing a list of Movies. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link MovieDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MovieListFragment extends Fragment {

    private final static String STATE_SORT = "stateSort";

    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.progress_view)
    MyProgressView mProgressView;

    private StaggeredGridLayoutManager mLayoutManager;
    private MovieListAdapter mAdapter;

    private List<MovieDao> mDataset = new ArrayList<>();

    private String[] mArrSort;
    private int mSort = 0;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(MovieDao movieDao);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(MovieDao movieDao) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mArrSort = new String[]{getString(R.string.main_sort_most_popular),
                getString(R.string.main_sort_highest_rated),
                getString(R.string.main_sort_favorites)};
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showSortDialog();

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_SORT)) {
            mSort = savedInstanceState.getInt(STATE_SORT);
        }

        mRecyclerView.setVisibility(View.GONE);

        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MovieListAdapter(mDataset, mCallbacks);
        mRecyclerView.setAdapter(mAdapter);

        mProgressView.setRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callApi();
            }
        });

        //change span dynamically based on screen width
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int viewWidth = mRecyclerView.getMeasuredWidth();
                        int newSpanCount = Math.max(2, (int) Math.floor(viewWidth / MovieListActivity.ITEM_WIDTH));
                        mLayoutManager.setSpanCount(newSpanCount);
                        mLayoutManager.requestLayout();
                    }
                });
            }
        });

        callApi();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SORT, mSort);
    }

    /**
     * Show dialog to choose sort mode
     */
    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.main_sort_by)
                .setSingleChoiceItems(
                        mArrSort,
                        mSort,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mSort = which;
                                if (which == 2) {
                                    //if favorites
                                    queryFavoritesData();
                                } else {
                                    callApi();
                                }
                                dialog.dismiss();
                            }
                        });
        builder.create().show();
    }

    private void queryFavoritesData() {
        mDataset.clear();
        MovieSelection productSelection = new MovieSelection();
        MovieCursor c = productSelection.query(getActivity().getContentResolver());
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                mDataset.add(new MovieDao(c));
            }
            c.close();
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressView.stopAndGone();
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mProgressView.stopAndError(getString(R.string.main_no_favorites), false);
        }

    }

    /**
     * Call api to get list of movies
     */
    private void callApi() {
        String sortMode = null;
        if (mArrSort[mSort].equals(getString(R.string.main_sort_most_popular))) {
            sortMode = Constant.SORT_POPULAR;
        } else if (mArrSort[mSort].equals(getString(R.string.main_sort_highest_rated))) {
            sortMode = Constant.SORT_HIGHEST_RATED;
        }
        mProgressView.startProgress();
        RetrofitHelper.getInstance().getService()
                .discoverMovie(Constant.MOVIEDB_APIKEY, sortMode)
                .enqueue(new Callback<BaseListApiDao<MovieDao>>() {
                    @Override
                    public void onResponse(Response<BaseListApiDao<MovieDao>> response) {
                        if (response.body() != null) {
                            mDataset.clear();
                            mDataset.addAll(response.body().getResults());
                            mAdapter.notifyDataSetChanged();

                            mRecyclerView.setVisibility(View.VISIBLE);
                            mProgressView.stopAndGone();
                        } else {
                            Gson gson = new Gson();
                            try {
                                ErrorApiDao errorApiDao = gson.fromJson(response.errorBody().string().toString(), ErrorApiDao.class);
                                mProgressView.stopAndError(errorApiDao.getStatus_message(), true);
                            } catch (IOException e) {
                                e.printStackTrace();
                                mProgressView.stopAndError(e.getMessage(), true);
                            }

                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        mProgressView.stopAndError(t.getMessage(), true);
                    }
                });
    }
}
