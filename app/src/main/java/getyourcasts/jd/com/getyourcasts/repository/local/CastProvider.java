package getyourcasts.jd.com.getyourcasts.repository.local;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class CastProvider extends ContentProvider {

    /*STATIC URI MATCHER */
    private static final int PODCAST_ALL = 100;
    private static final int PODCAST_SPECIFIC = 101;
    private static final int EPISODES_OF_PODCAST = 102;
    private static final int EPISODES_SPECIFIC = 103;


    private static final UriMatcher uriMatcher = buildUriMatcher();

    private SqliteHelper dbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_PODCAST, PODCAST_ALL);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_PODCAST_ID, PODCAST_SPECIFIC);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_EPISODES_ID, EPISODES_SPECIFIC);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_ALL_EPISODES_OF_POCAST, EPISODES_OF_PODCAST);
        return matcher;
    }

    public CastProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;

        if (null == selection) {
            selection = "1";
        }
        switch (uriMatcher.match(uri)) {
            case PODCAST_SPECIFIC:
                String podcastId = Contract.PodcastTable.getPodcastIdFromUri(uri);
                rowsDeleted = db.delete(
                        Contract.PodcastTable.TABLE_NAME,
                        '"' + podcastId + '"' + " = " + Contract.PodcastTable.UNIQUE_ID,
                        selectionArgs
                );
                break;

            case EPISODES_SPECIFIC:
                String episodeId = Contract.EpisodeTable.getEpisodeIdFromUri(uri);
                rowsDeleted = db.delete(
                        Contract.EpisodeTable.TABLE_NAME,
                        '"' + episodeId + '"' + " =" + Contract.EpisodeTable.UNIQUE_ID,
                        selectionArgs
                );
                break;
            case EPISODES_OF_PODCAST:
                String podId = Contract.EpisodeTable.getEpisodeIdFromUri(uri);
                rowsDeleted = db.delete(
                        Contract.EpisodeTable.TABLE_NAME,
                        '"' + podId + '"' + " =" + Contract.EpisodeTable.PODCAST_ID,
                        selectionArgs
                );
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        if (rowsDeleted != 0) {
            setNotifyChange(uri);
        }
        return rowsDeleted;
    }

    private void setNotifyChange(Uri uri) {
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {

            case PODCAST_SPECIFIC:
                db.insert(
                        Contract.PodcastTable.TABLE_NAME,
                        null,
                        values
                );
                returnUri = Contract.PodcastTable.URI_ID;
                break;
            case EPISODES_SPECIFIC:
                db.insert(
                        Contract.PodcastTable.TABLE_NAME,
                        null,
                        values
                );
                returnUri = Contract.EpisodeTable.URI_EPISODE_ID;
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);

        }
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = SqliteHelper.getInstance(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case PODCAST_ALL:
                returnCursor = db.query(
                        Contract.PodcastTable.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PODCAST_SPECIFIC:
                String podcastId = Contract.PodcastTable.getPodcastIdFromUri(uri);
                returnCursor = db.query(
                        Contract.PodcastTable.TABLE_NAME,
                        projection,
                        Contract.PodcastTable.UNIQUE_ID + " = ?",
                        new String[]{podcastId},
                        null,
                        null,
                        sortOrder
                );
                break;

            case EPISODES_SPECIFIC:
                String episodeId = Contract.EpisodeTable.getEpisodeIdFromUri(uri);
                returnCursor = db.query(
                        Contract.PodcastTable.TABLE_NAME,
                        projection,
                        Contract.EpisodeTable.UNIQUE_ID + " = ?",
                        new String[]{episodeId},
                        null,
                        null,
                        sortOrder
                );
                break;

            case EPISODES_OF_PODCAST:
                String podId = Contract.EpisodeTable.getEpisodeIdFromUri(uri);
                returnCursor = db.query(
                        Contract.PodcastTable.TABLE_NAME,
                        projection,
                        Contract.EpisodeTable.PODCAST_ID + " = ?",
                        new String[]{podId},
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }


        setNotificationUri(uri, returnCursor);
        returnCursor.moveToFirst();
        return returnCursor;
    }

    private void setNotificationUri(Uri uri, Cursor cursor) {
        Context context = getContext();
        if (context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
    }

    @Override
    public int update(Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs) {
        int count = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {

            case PODCAST_SPECIFIC:
                //update(String table, ContentValues values, String whereClause, String[] whereArgs)
                String podId = Contract.PodcastTable.getPodcastIdFromUri(uri);
                count = db.update(
                        Contract.PodcastTable.TABLE_NAME,
                        values,
                        '"' + podId + '"' + " =" + Contract.PodcastTable.UNIQUE_ID,
                        selectionArgs
                );

                break;
            case EPISODES_SPECIFIC:
                String episodeId = Contract.EpisodeTable.getEpisodeIdFromUri(uri);
                count = db.update(
                        Contract.EpisodeTable.TABLE_NAME,
                        values,
                        '"' + episodeId + '"' + " =" + Contract.EpisodeTable.UNIQUE_ID,
                        selectionArgs
                );

                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);

        }
        if (count > 0) {
            setNotifyChange(uri);
        }
        return count;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch(uriMatcher.match(uri)){
            case EPISODES_OF_PODCAST:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues cv : values){
                        db.insert(Contract.EpisodeTable.TABLE_NAME, null, cv);
                        returnCount += 1;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                setNotifyChange(uri);
                return returnCount;
            default:
                throw new UnsupportedOperationException();

        }

    }
}
