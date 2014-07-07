package com.fredericcodet.frederic.spellingbee0;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.fredericcodet.frederic.spellingbee0.dummy.DummyContent;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list fragment representing a list of SpellingLists. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link SpellingListDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class SpellingListListFragment extends ListFragment {
    public static final String TAG = SpellingListListFragment.class.getSimpleName();

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SpellingListListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: replace with a real list adapter.
        /*
        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                DummyContent.ITEMS));
        */

        //SimpleAdapter adapter = new SimpleAdapter(this, blogPosts,
        //        android.R.layout.simple_list_item_1, keys, ids);
        if (isNetworkAvailable()) {
            //mProgressBar.setVisibility(View.VISIBLE);
            GetSpellingListsTask getSpellingListsTask  = new GetSpellingListsTask();
            getSpellingListsTask.execute();
        }
        else {
            Toast.makeText(getActivity(), "Network unavailable", Toast.LENGTH_LONG).show();
        }


        List<Map<String, String>> spellingLists = new ArrayList<Map<String,String>>();



        spellingLists.add(createList("spellingList","SpellingList 1"));
        spellingLists.add(createList("spellingList","SpellingList 2"));
        spellingLists.add(createList("spellingList","SpellingList 3"));


        //SimpleAdapter adapter = new SimpleAdapter(this, spellingLists, android.R.layout.simple_list_item_1,  new String[] {"planet"}, new int[] {android.R.id.text1});
        SimpleAdapter adapter = new SimpleAdapter(getActivity(),spellingLists,android.R.layout.simple_list_item_2,new String[] {"spellingList",""},new int[] {android.R.id.text1,android.R.id.text2} );
        setListAdapter(adapter);
    }

    private HashMap<String, String> createList(String key, String name) {
        HashMap<String, String> list = new HashMap<String, String>();
        list.put(key, name);

        return list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
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
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo= manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if ((networkInfo != null) && (networkInfo.isConnected())) {
            isAvailable = true;
        }
        return isAvailable;
    }


    private class GetSpellingListsTask extends AsyncTask<Object, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Object... params) {
            int responseCode = -1;
            JSONObject jsonResponse = null;
            try {
                URL blogFeedUrl = new URL("http://192.168.56.1/SpellingList_Animals.txt");
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG,"SUCCESSFUL");

                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);
                    String responseData = new String(charArray);
                    Log.i(TAG,responseData);
                    //jsonResponse = new JSONObject(responseData);


                }

                else {
                    Log.i(TAG, "Unsuccessful HTTP response Code: " + responseCode);
                }

            }
            catch (MalformedURLException e){
                Log.e(TAG, "Exception caught", e);
            }
            catch (IOException e) {
                Log.e(TAG, "Exception caught", e);
            }
            catch (Exception e) {
                Log.e(TAG, "Exception caught", e);
            }

            //return jsonResponse;
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            //mBlogData = result;
            //handleBlogResponse();
        }

    }
}
