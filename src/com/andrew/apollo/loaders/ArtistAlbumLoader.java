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
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.AudioColumns;

import com.andrew.apollo.R;
import com.andrew.apollo.model.Album;
import com.andrew.apollo.utils.Lists;
import com.andrew.apollo.utils.MusicUtils;
import com.andrew.apollo.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to query {@link MediaStore.Audio.Artists.Albums} and return the albums
 * for a particular artist.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class ArtistAlbumLoader extends WrappedAsyncTaskLoader<List<Album>> {

	/**
	 * The result
	 */
	private final ArrayList<Album> mAlbumsList = Lists.newArrayList();

	/**
	 * The {@link Cursor} used to run the query.
	 */
	private Cursor mCursor, fCursor;

	/**
	 * The Id of the artist the albums belong to.
	 */
	private final Long mArtistID;

	/**
	 * Constructor of <code>ArtistAlbumHandler</code>
	 * 
	 * @param context
	 *            The {@link Context} to use.
	 * @param artistId
	 *            The Id of the artist the albums belong to.
	 */
	public ArtistAlbumLoader(final Context context, final Long artistId) {
		super(context);
		mArtistID = artistId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Album> loadInBackground() {
		// Create the Cursor
		mCursor = makeArtistAlbumCursor(getContext(), mArtistID);
		// Gather the dataS
		if (mCursor != null && mCursor.moveToFirst()) {
			do {
				// Copy the album id
				final String id = mCursor.getString(0);

				fCursor = makeAlbumSongCursor(getContext(), Long.parseLong(id));
				if (fCursor != null && fCursor.moveToFirst()) {
					fCursor.close();

					// Copy the album name
					final String albumName = mCursor.getString(1);

					// Copy the artist name
					final String artist = mCursor.getString(2);

					// Copy the number of songs
					final String songCount = mCursor.getString(3);

					// Copy the release year
					final String year = mCursor.getString(4);

					// Make the song label
					final String songCountFormatted = MusicUtils.makeLabel(getContext(),
							R.plurals.Nsongs, songCount);

					// Create a new album
					final Album album = new Album(id, albumName, artist, songCountFormatted, year);

					// Add everything up
					mAlbumsList.add(album);
				}
				fCursor = null;
			} while (mCursor.moveToNext());
		}
		// Close the cursor
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		return mAlbumsList;
	}

	/**
	 * @param context
	 *            The {@link Context} to use.
	 * @param artistId
	 *            The Id of the artist the albums belong to.
	 */
	public static final Cursor makeArtistAlbumCursor(final Context context, final Long artistId) {

		final StringBuilder selection = new StringBuilder();

		selection.append(AudioColumns.ARTIST_ID + "=" + artistId);
		
		return context.getContentResolver().query(
				MediaStore.Audio.Artists.Albums.getContentUri("external", artistId), new String[] {
				/* 0 */
				BaseColumns._ID,
				/* 1 */
				AlbumColumns.ALBUM,
				/* 2 */
				AlbumColumns.ARTIST,
				/* 3 */
				AlbumColumns.NUMBER_OF_SONGS,
				/* 4 */
				AlbumColumns.FIRST_YEAR }, selection.toString(), null,
				PreferenceUtils.getInstace(context).getArtistAlbumSortOrder());
	}

	/**
	 * @param context
	 *            The {@link Context} to use.
	 * @param artistId
	 *            The Id of the artist the songs belong to.
	 * @return The {@link Cursor} used to run the query.
	 */
	public static final Cursor makeAlbumSongCursor(final Context context, final Long albumId) {
		// Match the songs up with the artist
		final StringBuilder selection = new StringBuilder();
		selection.append(AudioColumns.IS_MUSIC + "=1");
		selection.append(" AND " + AudioColumns.TITLE + " != ''");
		for (String str : PreferenceUtils.getInstace(context).getExcludeFolders()) {
			selection.append(" AND " + AudioColumns.DATA + " NOT LIKE " + "'" + str + "'");
		}
		;
		selection.append(" AND " + AudioColumns.ALBUM_ID + "=" + albumId);
		return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] {
				/* 0 */
				BaseColumns._ID }, selection.toString(), null, null);
	}

}
