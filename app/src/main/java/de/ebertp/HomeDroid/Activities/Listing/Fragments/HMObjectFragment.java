package de.ebertp.HomeDroid.Activities.Listing.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.appcompat.app.AlertDialog;

import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.SQLException;

import de.ebertp.HomeDroid.Activities.GallerySelectIcon;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.HMObject;
import de.ebertp.HomeDroid.Model.HMObjectSettings;
import de.ebertp.HomeDroid.R;
import de.ebertp.HomeDroid.Utils.Compatibility.ThumbNailGetter;
import de.ebertp.HomeDroid.Utils.PermissionUtil;
import de.ebertp.HomeDroid.Utils.Util;

public abstract class HMObjectFragment extends DataFragment {

    private static final int REQUEST_FILESYSTEM_ICON = 0;

    public abstract HMObject getItem(int position);

    public int getMenuRes() {
        return R.menu.context_hm_object;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(getMenuRes(), menu);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        if (Util.hasIcon(dbManager, getItem(info.position).getRowId())) {
            menu.add(0, R.id.remove_icon, 0, R.string.remove_icon);
        }

        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onContextItemSelected(item);
                return true;
            }
        };

        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setOnMenuItemClickListener(listener);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final HMObject hmObject = getItem(info.position);
        final int rowId = hmObject.getRowId();

        switch (item.getItemId()) {
            case R.id.change_icon_external:
                rowIdExternalIcon = rowId;

                if (!PermissionUtil.hasFilePermissions(getActivity())) {
                    PermissionUtil.requestFilePermission(this, REQUEST_FILESYSTEM_ICON);
                    return true;
                }

                pickExternalIcon();
                return true;
            case R.id.change_icon_internal:
                Intent i = new Intent(getActivity(), GallerySelectIcon.class);
                i.putExtra("rowId", rowId);
                startActivityForResult(i, Util.REQUEST_SELECT_ICON_INTERNAL);
                return true;
            case R.id.remove_icon:
                Util.removeIcon(dbManager, rowId);
                refreshData();
                return true;
            case R.id.rename_object: {
                showRenameDialog(rowId);
            }
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showRenameDialog(final int rowId) {
        androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(getContext());

        HMObjectSettings settings = HMObjectSettings.getSettingsForId(rowId);

        String name;
        if (settings == null || settings.getCustomName() == null || settings.getCustomName().isEmpty()) {
            name = getRealNameForId(rowId);
        } else {
            name = settings.getCustomName();

            alert.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        HMObjectSettings settings = HMObjectSettings.getSettingsForId(rowId);
                        settings.setCustomName(null);
                        HomeDroidApp.db().getHmOjectSettingsDao().update(settings);
                        refreshData();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        final EditText input = new EditText(getContext());
        input.setText(name);

        alert.setTitle(R.string.rename);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                try {
                    HMObjectSettings settings = HMObjectSettings.getSettingsForId(rowId);
                    if (settings == null) {
                        settings = new HMObjectSettings(rowId, value, 0);
                    } else {
                        settings.setCustomName(value);
                    }
                    HomeDroidApp.db().getHmOjectSettingsDao().createOrUpdate(settings);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                refreshData();
            }
        });
        alert.setNegativeButton(R.string.cancel, null);
        alert.show();
    }

    private String getRealNameForId(int rowId) {
        Cursor c;
        String name;

        if (this instanceof GridDataFragment) {
            c = HomeDroidApp.db().roomsDbAdapter.fetchItem(rowId);
            if (c.getCount() == 0) {
                c = HomeDroidApp.db().categoriesDbAdapter.fetchItem(rowId);
            }
            if(c.getCount() == 0) {
                c = HomeDroidApp.db().ccuFavListsAdapter.fetchItem(rowId);
            }

            name = c.getString(c.getColumnIndex("title"));
        } else {
            c = HomeDroidApp.db().channelsDbAdapter.fetchItem(rowId);
            if (c.getCount() == 0) {
                c = HomeDroidApp.db().varsDbAdapter.fetchItem(rowId);
            }
            if (c.getCount() == 0) {
                c = HomeDroidApp.db().programsDbAdapter.fetchItem(rowId);
            }
            name = c.getString(c.getColumnIndex("name"));
        }

        c.close();
        return name;
    }

    private void pickExternalIcon() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Util.REQUEST_SELECT_ICON_EXTERNAL);
    }

    private int rowIdExternalIcon;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.REQUEST_SELECT_ICON_INTERNAL) {
            if (resultCode == Activity.RESULT_OK) {
                refreshData();
            }
        } else if (requestCode == Util.REQUEST_SELECT_ICON_EXTERNAL) {
            if (resultCode == Activity.RESULT_OK) {
                // External icon change
                Uri uri = data.getData();
                String[] projection = {MediaStore.Images.Media._ID};

                Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
                cursor.moveToFirst();
                int imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));

                ThumbNailGetter.getThumbnail(getActivity(), imageId);

                de.ebertp.HomeDroid.Utils.Util.closeCursor(cursor);

                HomeDroidApp.db().iconDbAdapter.deleteItem(imageId);
                HomeDroidApp.db().externalIconDbAdapter.replaceItem(rowIdExternalIcon, imageId);

                refreshData();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_FILESYSTEM_ICON) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickExternalIcon();
            } else {
                Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }
}
