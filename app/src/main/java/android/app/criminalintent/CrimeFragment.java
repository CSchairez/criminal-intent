package android.app.criminalintent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import database.CrimeCursorWrapper;

public class CrimeFragment extends Fragment{
    // request code
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;


    private Crime mCrime;           // a crime object reference.
    private EditText mTitleField;   // an editText reference
    private Button mDateButton;    // a Button reference
    private CheckBox mSolvedCheckBox;  // CheckBox reference
    // an argument to add to the bundle
    private static final String ARG_CRIME_ID = "crime_id";
    // DatePickerFragment's tag
    private static final String DIALOG_DATE = "DialogDate";

    private static final String DIALOG_TIME = "DialogTime";
    private Button mTimeButton;
    public static final String TAG = "CrimeFragment";

    private Button mReportButton; // a ref to the report crime button
    private Button mSuspectButton; // ref to pick suspect button;

    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;

    private Dialog customDialog; // ref for the dialog imageview
    private Button imageClose;  // ref button that close image dialog
    private ImageView zoomView; // ref to zoomed image of thumbnail



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID)getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);   // Instantiate a new Crime object.
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){

        View v = inflater.inflate(
                R.layout.fragment_crime,       // layout resource id
                container,                     // the view's parent
                false);            // view gets added in view activity's code.

        // Get a ref to the Activitys PackageManager
        // so we can talk to it
        PackageManager pm = getActivity().getPackageManager();
        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charS,
                                          int start,
                                          int count,
                                          int after) {
                // required to override this method, but we're not using it.

            }
            @Override
            public void onTextChanged(CharSequence charS,
                                      int start,
                                      int count,
                                      int after) {
                mCrime.setTitle(charS.toString());

            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mDateButton = (Button)v.findViewById(R.id.crime_date);
        SimpleDateFormat formatter = new SimpleDateFormat(
                "MM/dd/yyyy");
        mDateButton.setText((mCrime.getDate()).toString());
        //mDateButton.setEnabled(true);

        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(
                            CompoundButton buttonView,
                            boolean isChecked) {
                            mCrime.setSolved(true);
                    }
                });

        mDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                FragmentManager fm = getFragmentManager();

                //DatePickerFragment dialog = new DatePickerFragment();
                //dialog.show(fm, DIALOG_DATE);

                DatePickerFragment dialog =
                        new DatePickerFragment().newInstance(mCrime.getDate());
                // set the target fragment
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        mTimeButton = (Button)v.findViewById(R.id.crime_time);
        mTimeButton.setEnabled(true);
        mTimeButton.setText(R.string.set_time);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager tfm = getFragmentManager();
                TimePickerFragment dial = new TimePickerFragment().newInstance(mCrime.getTime());
                dial.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dial.show(tfm, DIALOG_TIME);

            }
        });

        // listener for the report crime button
        mReportButton = (Button)v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setChooserTitle(getString(R.string.send_report))
                        .setText(getCrimeReport())
                        .startChooser();

/*
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);

 */

            }
        });

        mPhotoButton = (ImageButton)v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView)v.findViewById(R.id.crime_photo);
        // We intent to take a picture, dammit.
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Is there a place to store the photo and is there an app to take it?
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(pm) != null;
        // Now, create a listener for the button to take the picture, if its enabled
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "android.app.criminalintent.fileprovider",
                        mPhotoFile);
                // specify the extra where to put the picture
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // get a list of possible activities to take the pictures.
                List<ResolveInfo> cameraActivities =
                        getActivity().getPackageManager().queryIntentActivities(
                                captureImage, PackageManager.MATCH_DEFAULT_ONLY);
                // grant write permissions to any activity that can take a picture
                for(ResolveInfo activity : cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                // start the activity to take a picture
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        // Build dialog when user clicks on photo thumbnail
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogAlert();
            }
        });

        // make intent
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton = (Button)v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if(mCrime.getSuspect() != null)
            mSuspectButton.setText(mCrime.getSuspect());

        // Get a ref to the Activitys PackageManager
        // so we can talk to it

        // search for an activity matching the intent we gave,
        // and match only activities with the CATEGORY_DEFAULT flag.
        // if it cant find a match, deactivate the button.
        if(pm.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null)
            mSuspectButton.setEnabled(false);

        mPhotoButton = (ImageButton)v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView)v.findViewById(R.id.crime_photo);

        updatePhotoView();
        return v;
         }

    // Override the appropriate callback
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.fragment_crime, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onPause(){
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        UUID crimeId = (UUID)getArguments().getSerializable(ARG_CRIME_ID);
    

        switch(id){
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).removeCrime(crimeId);
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK)
            return;

        if(requestCode == REQUEST_DATE & data != null){
            Date date = (Date) data.getSerializableExtra(
                    DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "MM/dd/yyyy");
            try {
                formatter.parse((formatter.format(date)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mDateButton.setText("DATE: " + formatter.format(date));
        }

        else if(requestCode == REQUEST_TIME && data != null){
            Time time = (Time) data.getSerializableExtra(
                    TimePickerFragment.EXTRA_TIME);
            mCrime.setTime(time);
            mTimeButton.setText("TIME: " + mCrime.getTime().toString());
        }


        else if (requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            // we want the query to return values for these fields
            String [] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // do the query, the URI is our "where" clause
            Cursor c = getActivity().getContentResolver().query(
                    contactUri, queryFields, null,null,null);
            try{
                if(c.getCount() == 0) // did we get a result?
                    return;
                // get the field -- the suspects name
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            }
            finally{
                c.close();
            }
        }

        else if(requestCode == REQUEST_PHOTO){
            updatePhotoView();

            // Since we're done taking the picture now, revoke the permission
            // to write files
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "android.app.criminalintent.fileprovider",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }


    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // helper method to generate report
    private String getCrimeReport() {
        String solvedString;

        // each argument after the first in getString(...) replaces a placeholder.
        if(mCrime.isSolved())
            solvedString = getString(R.string.crime_report_solved);
        else
            solvedString = getString(R.string.crime_report_unsolved);

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null)
            suspect = getString(R.string.crime_report_no_suspect);
        else
            suspect = getString(R.string.crime_report_suspect);

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);
        return report;

    }

    // helper method to load the picture into the imageview widget
    private void updatePhotoView(){
        // if the picture doesnt exist, clears the ImageView widgest so
        // you dont see anything
        if(mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        }
        else{
            Bitmap bm = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bm);
        }
    }



    @SuppressLint("ResourceType")
    private void customDialogAlert(){
        Bitmap bm = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());

        customDialog = new Dialog(getActivity());
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View layout2 = layoutInflater.inflate(R.layout.dialog_bitmap, null);
        customDialog.setContentView(layout2);

        zoomView = (ImageView)layout2.findViewById(R.id.zoomImage);
        zoomView.setImageBitmap(bm);

        customDialog.show();

        imageClose = (Button)customDialog.findViewById(R.id.closeImage);
        imageClose.setEnabled(true);
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.cancel();
            }
        });
    }



}
