package net.honarnama.sell.fragments;

import com.parse.ParseFile;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.sell.R;
import net.honarnama.sell.widget.ImageSelector;
import net.honarnama.utils.ParseIO;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class EditItemFragment extends Fragment implements View.OnClickListener {

    private Button mSaveButton;
    private EditText mProductTitle;
    private EditText mProductDescription;
    private TextView mItemImageHint;

    private ImageSelector[] itemImages;

    public static EditItemFragment newInstance() {
        return new EditItemFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_item, container, false);

        mProductTitle = (EditText) rootView.findViewById(R.id.editProductTitle);
        mProductDescription = (EditText) rootView.findViewById(R.id.editProductDescription);
        mItemImageHint = (TextView) rootView.findViewById(R.id.itemImageHint);

        ImageSelector.OnImageSelectedListener onImageSelectedListener =
                new ImageSelector.OnImageSelectedListener() {
                    @Override
                    public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                        mItemImageHint.setError(null);
                        return true;
                    }

                    @Override
                    public boolean onImageRemoved() {
                        return false;
                    }

                    @Override
                    public void onImageSelectionFailed() {

                    }
                };

        itemImages = new ImageSelector[]{
                (ImageSelector) rootView.findViewById(R.id.itemImage1),
                (ImageSelector) rootView.findViewById(R.id.itemImage2),
                (ImageSelector) rootView.findViewById(R.id.itemImage3),
                (ImageSelector) rootView.findViewById(R.id.itemImage4)
        };
        for (ImageSelector imageSelector : itemImages) {
            imageSelector.setActivity(this.getActivity());
            imageSelector.restore(savedInstanceState);
            imageSelector.setOnImageSelectedListener(onImageSelectedListener);
        }

        mSaveButton = (Button) rootView.findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
                saveItem();
                break;
            default:
                break;
        }
    }

    private void saveItem() {
        String title = mProductTitle.getText().toString();
        String description = mProductDescription.getText().toString();

        boolean noImage = true;
        for (ImageSelector imageSelector : itemImages) {
            if (imageSelector.getFinalImageUri() != null) {
                noImage = false;
                break;
            }
        }
        if (noImage) {
            mItemImageHint.requestFocus();
            mItemImageHint.setError(getString(R.string.error_edit_item_no_image));
            return;
        }

        if (title.trim().length() == 0) {
            mProductTitle.requestFocus();
            mProductTitle.setError(getString(R.string.error_edit_item_title_is_empty));
            return;
        }

        if (description.trim().length() == 0) {
            mProductDescription.requestFocus();
            mProductDescription.setError(getString(R.string.error_edit_item_description_is_empty));
            return;
        }

        try {
            ArrayList<ParseFile> images = new ArrayList<ParseFile>();
            for (ImageSelector imageSelector : itemImages) {
                if (imageSelector.getFinalImageUri() != null) {
                    images.add(ParseIO.getParseFileFromFile(
                            "Item" + imageSelector.getImageSelectorIndex() + ".jpeg",
                            new File(imageSelector.getFinalImageUri().getPath())
                    ));
                }
            }

            // TODO: save images in background!
            // TODO: save the item!

        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                        "Failed on preparing item images.", e);
            } else {
                Log.e(HonarNamaBaseApp.PRODUCTION_TAG, "Failed on preparing item images. e="
                        + e.getMessage());
            }
            // TODO: feedback to user
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (ImageSelector imageSelector : itemImages) {
            if (imageSelector.onActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (ImageSelector imageSelector : itemImages) {
            imageSelector.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        for (ImageSelector imageSelector : itemImages) {
            imageSelector.onDestroy();
        }
        super.onDestroy();
    }
}
