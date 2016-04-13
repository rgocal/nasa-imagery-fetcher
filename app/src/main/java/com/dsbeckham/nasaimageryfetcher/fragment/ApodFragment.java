package com.dsbeckham.nasaimageryfetcher.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.adapter.ApodAdapter;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaModel;
import com.dsbeckham.nasaimageryfetcher.util.QueryUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ApodFragment extends Fragment {
    public FastItemAdapter<ApodAdapter> fastItemAdapter = new FastItemAdapter<>();
    public FooterAdapter<ProgressItem> footerAdapter = new FooterAdapter<>();

    @Bind(R.id.fragment_apod_progress_bar)
    public View progressBar;
    @Bind(R.id.fragment_apod_recycler_view)
    public RecyclerView recyclerView;
    @Bind(R.id.fragment_apod_swipe_refresh)
    public SwipeRefreshLayout swipeContainer;

    public List<ApodMorphIoModel> apodMorphIoModels = new ArrayList<>();
    public List<ApodNasaModel> apodNasaModels = new ArrayList<>();

    public Calendar calendar = Calendar.getInstance();
    public boolean loadingData = false;
    public int nasaApiQueryCount = QueryUtils.APOD_NASA_API_QUERIES;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apod, container, false);
        ButterKnife.bind(this, view);

        fastItemAdapter.withSavedInstanceState(savedInstanceState);
        fastItemAdapter.withOnClickListener(new FastAdapter.OnClickListener<ApodAdapter>() {
            @Override
            public boolean onClick(View view, IAdapter<ApodAdapter> iAdapter, ApodAdapter apodAdapter, int position) {
                // This will be the entry point for the ViewPager.
                // Intent intent = new Intent(getActivity(), ViewPagerActivity.class);
                // startActivity(intent);

                // This is just a little test message.
                Toast.makeText(view.getContext(), String.format("You clicked %s!", ((ApodMorphIoModel) apodAdapter.apodModel).getTitle()), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(footerAdapter.wrap(fastItemAdapter));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(final int currentPage) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        footerAdapter.clear();
                        footerAdapter.add(new ProgressItem());
                        QueryUtils.beginApodQuery(getActivity());
                    }
                }, 10);
            }
        });

        swipeContainer.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorAccent,
                R.color.colorAccent);

        // This is a workaround for a bug that causes the progress bar to be hidden underneath the
        // action bar.
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);
        swipeContainer.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typedValue.resourceId)
                + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                QueryUtils.clearApodData(getActivity());
                QueryUtils.beginApodQuery(getActivity());
            }
        });

        if (savedInstanceState == null) {
            QueryUtils.beginApodQuery(getActivity());
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState = fastItemAdapter.saveInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }
}
