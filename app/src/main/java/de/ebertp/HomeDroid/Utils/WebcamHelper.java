package de.ebertp.HomeDroid.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.sql.SQLException;
import java.util.UUID;

import de.ebertp.HomeDroid.Connection.BroadcastHelper;
import de.ebertp.HomeDroid.DbAdapter.BaseDbAdapter;
import de.ebertp.HomeDroid.DbAdapter.ConcreteHelpers.FavRelationsDbAdapter;
import de.ebertp.HomeDroid.HomeDroidApp;
import de.ebertp.HomeDroid.Model.WebCam;
import de.ebertp.HomeDroid.R;

public class WebcamHelper {

    public static void showWebCam(final Context context, final WebCam item, final Handler refreshHandler) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;


        switch (item.getType()) {
            case WebCam.TYPE_MPEG: {
                Toast.makeText(context, "Not implemented yet", Toast.LENGTH_LONG).show();
//                View content = LayoutInflater.from(context).inflate(R.layout.webcam_mjpeg, null);
//
//                final MjpegView mjpegView = content.findViewById(R.id.mjpeg_view);
//
//                Mjpeg.newInstance()
//                        //.credential(item.get, "PASSWORD")
//                        .open(item.getUrl(), 5)
//                        .subscribe(new Action1<MjpegInputStream>() {
//                            @Override
//                            public void call(MjpegInputStream mjpegInputStream) {
//                                mjpegView.setSource(mjpegInputStream);
//                                mjpegView.setDisplayMode(DisplayMode.BEST_FIT);
//                                mjpegView.showFps(true);
//                            }
//                        });
                break;
            }
            case WebCam.TYPE_JPEG: {

                View content = LayoutInflater.from(context).inflate(R.layout.webcam_jpeg, null);
                final ImageView webCamView = (ImageView) content.findViewById(R.id.iv_webcam);

                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    webCamView.getLayoutParams().width = height * 4 / 3;
                    webCamView.getLayoutParams().height = height;
                } else {
                    webCamView.getLayoutParams().width = width;
                    webCamView.getLayoutParams().height = width * 3 / 4;
                }

                final GlideUrl url;
                if (!TextUtils.isEmpty(item.getAuthUser())) {
                    String authString = item.getAuthUser() + ":" + item.getAuthPass();

                    String authStringEnc;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        authStringEnc = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
                    } else {
                        authStringEnc = de.ebertp.HomeDroid.Connection.Base64.encodeBytes(authString.getBytes());
                    }

                    url = new GlideUrl(item.getUrl(), new LazyHeaders.Builder()
                            .setHeader("Authorization", "Basic " + authStringEnc)
                            .build());
                } else {
                    url = new GlideUrl(item.getUrl());
                }

                final RequestListener<Drawable> listener = new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(hL.TAG, e == null ? "Webcam access failed, no exception" : e.toString());
                        String msg = e == null ? context.getString(R.string.webcam_failed) : e.getLocalizedMessage();
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                };

                final Runnable refreshView = new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(context)
                                .load(url)
                                .fitCenter()
                                .listener(listener)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(webCamView);
                    }
                };

                ImageView btnRefresh = (ImageView) content.findViewById(R.id.btn_refresh);

                Util.setIconColor(PreferenceHelper.isDarkTheme(context), btnRefresh);

                btnRefresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        refreshHandler.post(refreshView);
                    }
                });

                new AlertDialog.Builder(context).setView(content).show();

                refreshHandler.post(refreshView);

                break;
            }
        }
    }

    public static void showCrudDialog(final Context ctx, final WebCam webcam) {
        View content = LayoutInflater.from(ctx).inflate(R.layout.crud_webcam, null);

        final EditText editName = ((EditText) content.findViewById(R.id.edit_name));
        final EditText editUrl = ((EditText) content.findViewById(R.id.edit_url));
        final ImageView ivHelp = ((ImageView) content.findViewById(R.id.iv_help));
        final EditText editUser = ((EditText) content.findViewById(R.id.edit_auth_user));
        final EditText editPass = ((EditText) content.findViewById(R.id.edit_auth_pass));
        final ImageView ivDelete = ((ImageView) content.findViewById(R.id.iv_delete));
        final ImageView ivFav = ((ImageView) content.findViewById(R.id.iv_fav));

        Util.setIconColor(PreferenceHelper.isDarkTheme(ctx), ivHelp);
        Util.setIconColor(PreferenceHelper.isDarkTheme(ctx), ivDelete);
        Util.setIconColor(PreferenceHelper.isDarkTheme(ctx), ivFav);

        final Spinner spinner = (Spinner) content.findViewById(R.id.spinner_type);
        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ctx,
                R.array.webcam_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        final AlertDialog dialog = new AlertDialog.Builder(ctx).setView(content)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (editName.getText().toString().equals("") || editUrl.getText().toString().equals("")) {
                            Toast.makeText(ctx, R.string.imcomplete_input, Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            //If the user supplies a username, he also has to supply a password
                            if (!editUser.getText().toString().equals("") && editPass.getText().toString().equals("")) {
                                Toast.makeText(ctx, R.string.missing_password, Toast.LENGTH_LONG).show();
                                return;
                            }

                            try {

                                WebCam updatedWebcam = new WebCam(editName.getText().toString(), editUrl.getText().toString(), spinner.getSelectedItemPosition());

                                if (!TextUtils.isEmpty(editUser.getText().toString()) && !TextUtils.isEmpty(editPass.getText().toString())) {
                                    updatedWebcam.setAuthUser(editUser.getText().toString());
                                    updatedWebcam.setAuthPass(editPass.getText().toString());
                                }

                                if (webcam != null && webcam.getId() != null) {
                                    updatedWebcam.setId(webcam.getId());
                                }

                                try {
                                    HomeDroidApp.db().getWebCamDao().createOrUpdate(updatedWebcam);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                BroadcastHelper.refreshUI();
                                dialog.dismiss();

                            } catch (NumberFormatException e) {
                                Toast.makeText(ctx, R.string.invalid_input, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (webcam != null) {
            editName.setText(webcam.getName());
            editUrl.setText(webcam.getUrl());
            spinner.setSelection(webcam.getType());

            if (webcam.getAuthUser() != null && webcam.getAuthPass() != null) {
                editUser.setText(webcam.getAuthUser());
                editPass.setText(webcam.getAuthPass());
            }

            ivDelete.setVisibility(View.VISIBLE);
            ivDelete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    try {
                        HomeDroidApp.db().getWebCamDao().delete(webcam);
                        HomeDroidApp.db().favRelationsDbAdapter.deleteItem(webcam.getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    BroadcastHelper.refreshUI();
                    dialog.dismiss();
                }
            });

            ivFav.setVisibility(View.VISIBLE);
            ivFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (HomeDroidApp.db().favRelationsDbAdapter.fetchItem(webcam.getId()).getCount() == 0) {
                        Toast.makeText(ctx, R.string.added_to_favs, Toast.LENGTH_LONG).show();
                        HomeDroidApp.db().favRelationsDbAdapter.createItem(webcam.getId(), FavRelationsDbAdapter.WEBCAMS_ID + PreferenceHelper.getPrefix(ctx) * BaseDbAdapter.PREFIX_OFFSET);
                        BroadcastHelper.refreshUI();
                    } else {
                        Toast.makeText(ctx, R.string.remove_from_favs, Toast.LENGTH_LONG).show();
                        HomeDroidApp.db().favRelationsDbAdapter.deleteItem(webcam.getId());
                        BroadcastHelper.refreshUI();
                    }


                    dialog.dismiss();
                }
            });


        } else {
            //editUrl.setText("http://webcam.datainstituttet.no/axis-cgi/jpg/image.cgi");
            //editUrl.setText("http://browserspy.dk/password.php");
        }

        ivHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ctx).setMessage(R.string.webcam_info).show();
            }
        });

        dialog.show();
    }
}
