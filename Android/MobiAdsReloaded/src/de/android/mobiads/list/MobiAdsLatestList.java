package de.android.mobiads.list;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.android.mobiads.R;
import de.android.mobiads.provider.Indexer;

public class MobiAdsLatestList extends ListActivity implements LoaderManager.LoaderCallbacks<List<AdsEntry>> {
	AdsEntryLatestAdapter mAdapter;
	private BroadcastReceiver mReceiver;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new AdsEntryLatestAdapter(this, R.layout.ads_entry_list_item);
		 setListAdapter(mAdapter);
	    	    
		 getListView().setTextFilterEnabled(true);
	    	        
		 // Tell the list view to show one checked/activated item at a time.
		 getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	        
		 getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final AdsEntry entry = mAdapter.getItem(position);
				
				if (!entry.isRead()) {
					setIsReadEntry(entry);
				}
				
				//Change notification (if there is one)
				Intent updateDatabase = new Intent("de.android.mobiads.MOBIADSRECEIVER");
				sendBroadcast(updateDatabase);
				
				Uri uri = Uri.parse(mAdapter.getItem(position).getURL());
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
				
				//This will update our view showing a nice black background for this item in our list :/
				mAdapter.notifyDataSetChanged();
			}
		 });
		 
		 registerForContextMenu(getListView());
	    
		 getLoaderManager().initLoader(0, null, this);
		
		 mReceiver = new BroadcastReceiver() {
        	
        	@Override
        	public void onReceive(Context context, Intent intent) {
        		String action = intent.getAction();
        		//This will be run in the main thread of this service. It might be interesting to use a Handler
        		//for this receiver implementing its own thread. :/
        		if(action.equals("de.android.mobiads.MOBIADSLISTRECEIVER")){
        			getLoaderManager().restartLoader(0, null, MobiAdsLatestList.this);
        		}
        	}
     	};
     	
     	IntentFilter filter = new IntentFilter();
        filter.addAction("de.android.mobiads.MOBIADSLISTRECEIVER");
        registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
	  unregisterReceiver(mReceiver);
	  super.onDestroy();
	}

	@Override
	public Loader<List<AdsEntry>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.  This
		// sample only has one Loader with no arguments, so it is simple.
		return new AdsListLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<List<AdsEntry>> loader,
			List<AdsEntry> data) {
		
		mAdapter.setData(data);
		
	}

	@Override
	public void onLoaderReset(Loader<List<AdsEntry>> loader) {
		mAdapter.setData(null);
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.selectedmenuads, menu);
    }
	

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
			case R.id.selectedmenu_remove:
				removeAd(info.position);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	public void removeAd(int position){
		final AdsEntry entry = mAdapter.getItem(position);
		int idAd = entry.getIdAd();
		final Uri uriDelete = Uri.parse("content://" + "de.android.mobiads.provider" + "/" + "indexer" 
																						+ "/idad/" + idAd);
		
		getContentResolver().delete(uriDelete, null, null);
		mAdapter.remove(entry);
		
		//Change notification (if there is one)
		Intent updateDatabase = new Intent("de.android.mobiads.MOBIADSRECEIVER");
		sendBroadcast(updateDatabase);
		
		mAdapter.notifyDataSetChanged();
	}
	
	
	private void setIsReadEntry(final AdsEntry entry) {
    	final Uri uriUpdate = Uri.parse("content://" + "de.android.mobiads.provider" + 
    														"/" + "indexer/idad/" + entry.getIdAd());
		final ContentValues values = new ContentValues();
		
		values.put(Indexer.Index.COLUMN_NAME_IS_READ, new Integer(1));

		getContentResolver().update(uriUpdate, values, null, null);
		
		entry.setIsRead(true);
	}
	
	
	/**
	 * A custom Loader that loads all of the installed applications.
	 */
	public static class AdsListLoader extends AsyncTaskLoader<List<AdsEntry>> {
		private static final String TAG = "AdsListLoader";
		List<AdsEntry> mApps;

		public AdsListLoader(Context context) {
			super(context);
		}

		/**
		 * This is where the bulk of our work is done.  This function is
		 * called in a background thread and should generate a new set of
		 * data to be published by the loader.
		 */
		@Override 
		public List<AdsEntry> loadInBackground() {
			// Create corresponding array of entries and load their labels.
			List<AdsEntry> entries = getAdsEntries();

			return entries;
		}

		private List<AdsEntry> getAdsEntries() {
			final List<AdsEntry> entries = new ArrayList<AdsEntry>();
			final Uri uri = Uri.parse("content://" + "de.android.mobiads.provider" + "/" + "indexer" + "/isRead/");
			final ContentValues values = new ContentValues();
	
			Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
			try {
				if (cursor.moveToFirst()) {
					do {
						values.clear();
						Bitmap bitMap = null;
						FileInputStream file = null;
						try {
							file = getContext().openFileInput(cursor.getString(cursor.getColumnIndexOrThrow(Indexer.Index.COLUMN_NAME_PATH)));
							bitMap = BitmapFactory.decodeStream(file);
						} catch (FileNotFoundException e) {
							//Giving more chances to other ads
							continue;
						} catch (IllegalArgumentException e) {
							//Giving more chances to other ads
							continue;
						}
						finally {
							if (file != null) {
								try {
									file.close();
								} catch (IOException e) {
									Log.w(TAG, "Error while closing image file.");
								}
							}
						}
						
						boolean readStatus;
						if ( cursor.getInt(cursor.getColumnIndexOrThrow(Indexer.Index.COLUMN_NAME_IS_READ)) == 0) {
							readStatus = false;
						}
						else {
							readStatus = true;
						}
							
						entries.add(new AdsEntry(cursor.getString(cursor.getColumnIndexOrThrow(Indexer.Index.COLUMN_NAME_AD_NAME)), 
								cursor.getString(cursor.getColumnIndexOrThrow(Indexer.Index.COLUMN_NAME_TEXT)), bitMap,
								cursor.getInt(cursor.getColumnIndexOrThrow(Indexer.Index.COLUMN_NAME_ID_AD)),
								cursor.getString(cursor.getColumnIndexOrThrow(Indexer.Index.COLUMN_NAME_URL)),
								readStatus));
					}while (cursor.moveToNext());
				} 
			}finally {
				cursor.close();
			}

			return entries;
		}

		/**
		 * Called when there is new data to deliver to the client.  The
		 * super class will take care of delivering it; the implementation
		 * here just adds a little more logic.
		 */
		@Override 
		public void deliverResult(List<AdsEntry> apps) {
			mApps = apps;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(apps);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		@Override 
		protected void onStartLoading() {
			if (mApps != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mApps);
			}

			if (takeContentChanged() || mApps == null) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}
		}

		/**
		 * Handles a request to cancel a load.
		 */
		@Override 
		public void onCanceled(List<AdsEntry> apps) {
			super.onCanceled(apps);

			// At this point we can release the resources associated with 'apps'
			// if needed.
		}

		/**
		 * Handles a request to completely reset the Loader.
		 */
		@Override 
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			// At this point we can release the resources associated with 'apps'
			// if needed.
			if (mApps != null) {
				mApps = null;
			}
		}
	}
}
