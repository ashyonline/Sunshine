package com.ashionline.sunshine;

/**
 * Created by ashi on 3/15/15.
 */

import android.net.Uri;

/**
 * A callback interface that all activities containing this fragment must
 * implement. This mechanism allows activities to be notified of item
 * selections.
 */
public interface Callback {
    /**
     * DetailFragmentCallback for when an item has been selected.
     */
    public void onItemSelected(Uri dateUri);
}