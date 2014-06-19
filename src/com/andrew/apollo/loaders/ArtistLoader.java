/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.provider.MediaStore.Audio.AudioColumns;

import com.andrew.apollo.R;
import com.andrew.apollo.model.Artist;
import com.andrew.apollo.utils.Lists;
import com.andrew.apollo.utils.MusicUtils;
import com.andrew.apollo.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to query {@link MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI} and
 * return the artists on a user's device.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class ArtistLoader extends WrappedAsyncTaskLoader<List<Artist>> {

	/**
	 * The result
	 */
	private final ArrayList<Artist> mArtistsList = Lists.newArrayList();

	/**
	 * The {@link Cursor} used to run the query.
	 */
	private Cursor mCursor, fCursor;

	/**
	 * Constructor of <code>ArtistLoader</code>
	 * 
	 * @param context
	 *            The {@link Context} to use
	 */
	public ArtistLoader(final Context context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Artist> loadInBackground() {
		// Create the Cursor
		mCursor = makeArtistCursor(getContext());
		// Gather the data
		if (mCursor != null && mCursor.moveToFirst()) {
			do {
				// Copy the artist id
				final String id = mCursor.getString(0);

				fCursor = makeArtistSongCursor(getContext(), Long.parseLong(id));
				if (fCursor != null && fCursor.moveToFirst()) {
					fCursor.close();

					// Copy the artist name
					final String artistName = mCursor.getString(1);

					// Copy the number of albums
					final String albumCount = mCursor.getString(2);

					// Copy the number of songs
					final String songCount = mCursor.getString(3);

					// Make the album label
					final String albumCountFormatted = MusicUtils.makeLabel(getContext(),
							R.plurals.Nalbums, albumCount);

					// Make the song label
					final String songCountFormatted = MusicUtils.makeLabel(getContext(),
							R.plurals.Nsongs, songCount);

					// Create a new artist
					final Artist artist = new Artist(id, artistName, songCountFormatted,
							albumCountFormatted);

					// Add everything up
					mArtistsList.add(artist);
				}
				fCursor = null;
			} while (mCursor.moveToNext());
		}
		// Close the cursor
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		return mArtistsList;
	}

	/**
	 * Creates the {@link Cursor} used to run the query.
	 * 
	 * @param context
	 *            The {@link Context} to use.
	 * @return The {@link Cursor} used to run the artist query.
	 */
	public static final Cursor makeArtistCursor(final Context context) {
		final StringBuilder selection = new StringBuilder();
		selection.append(AudioColumns.IS_MUSIC + "=1");
		selection.append(" AND " + AudioColumns.TITLE + " != ''");
		for (String str : PreferenceUtils.getInstace(context).getExcludeFolders()) {
			selection.append(" AND " + AudioColumns.DATA + " NOT LIKE " + "'" + str + "'");
		}
		;

		return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
				new String[] {
				/* 0 */
				BaseColumns._ID,
				/* 1 */
				ArtistColumns.ARTIST,
				/* 2 */
				ArtistColumns.NUMBER_OF_ALBUMS,
				/* 3 */
				ArtistColumns.NUMBER_OF_TRACKS }, null, null,
				PreferenceUtils.getInstace(context).getArtistSortOrder());
	}

	/**
	 * @param context
	 *            The {@link Context} to use.
	 * @param artistId
	 *            The Id of the artist the songs belong to.
	 * @return The {@link Cursor} used to run the query.
	 */
	public static final Cursor makeArtistSongCursor(final Context context, final Long artistId) {
		// Match the songs up with the artist
		final StringBuilder selection = new StringBuilder();
		selection.append(AudioColumns.IS_MUSIC + "=1");
		selection.append(" AND " + AudioColumns.TITLE + " != ''");
		for (String str : PreferenceUtils.getInstace(context).getExcludeFolders()) {
			selection.append(" AND " + AudioColumns.DATA + " NOT LIKE " + "'" + str + "'");
		}
		;
		selection.append(" AND " + AudioColumns.ARTIST_ID + "=" + artistId);
		return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] {
				/* 0 */
				BaseColumns._ID }, selection.toString(), null, null);
	}
}
