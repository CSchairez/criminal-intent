package android.app.criminalintent;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static android.app.criminalintent.CrimeFragment.TAG;


// Create Fragment to hold RecyclerView and ViewHolders //
public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private Adapter mAdapter;
    private boolean mSubtitleVisible;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity()));

         if(savedInstanceState != null)
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);

        updateUI();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }

    // Override the appropriate callback
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if(mSubtitleVisible)
            subtitleItem.setTitle(R.string.hide_subtitle);
        else
            subtitleItem.setTitle(R.string.show_subtitle);
    }

    // Override the callback to handle the user's menu selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId())
        {
            case R.id.new_crime:            // this is the id of the menu option selected
                Crime crime = new Crime();  // create the crime
                CrimeLab.get(getActivity()).addCrime(crime);    // Add the crime to crime lab
                // Create the new intent to pass the needed info
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);  // start the activity, passing it the intent it needs
                return true;    // return true tells OS we're done here

            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default: // If the selected option isnt found, defer to the superclass
                return super.onOptionsItemSelected(item);


        }
    }

    public void updateUI() {
        CrimeLab crimelab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimelab.getCrimes();

        if(mAdapter == null){
            mAdapter = new Adapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }
        else{
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }


    ////////////// -------------------------------------------------------------------------- //////////////////////////////////////////////
    private class CrimeHolder extends RecyclerView.ViewHolder{
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Crime mCrime;
        private ImageView mSolvedImageView;

        public CrimeHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
        }
        public void bind(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);

        }

        public void onClick(View view){
            Intent intent = CrimePagerActivity.newIntent(
                    getActivity(), mCrime.getId());
            startActivity(intent);
        }

    }


    private class PoliceHolder extends RecyclerView.ViewHolder {
        private Button mPoliceButton;
        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        public PoliceHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
            mPoliceButton = (Button) itemView.findViewById(R.id.police_button);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mPoliceButton.setText(R.string.police_button_text);
        }
    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<Crime> mCrimes;
        private static final int REQUIRES_POLICE = 1;
        private static final int NO_POLICE = 0;
        private static final String ARG_CRIME_ID = "crime_id";
        private Crime mCrime;

        public Adapter(List<Crime> crimes) {
            mCrimes = crimes;

        }

        @Override
        public int getItemViewType(int position) {
            int itemType = mCrimes.get(position).getRequiresPolice();
            if (itemType == 0) {
                return NO_POLICE;
            } else {
                return REQUIRES_POLICE;
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            RecyclerView.ViewHolder viewHolder;
            if (viewType == NO_POLICE) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_crime, parent, false);
                viewHolder = new CrimeHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_crime_police, parent, false);
                viewHolder = new PoliceHolder(view);
            }
            return viewHolder;

        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final Crime crime = mCrimes.get(position);

            if (holder.getItemViewType() == NO_POLICE) {
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "MM/dd/yyyy");
                final CrimeHolder itemHolder = (CrimeHolder) holder;
                itemHolder.mTitleTextView.setText(crime.getTitle());
                itemHolder.mDateTextView.setText("DATE OF CRIME: " + formatter.format(crime.getDate()) + "TIME: " + crime.getTime());
                itemHolder.mSolvedImageView.setVisibility(View.GONE);
                if(crime.isSolved()){
                    itemHolder.mSolvedImageView.setVisibility(View.VISIBLE);
                }
                itemHolder.mTitleTextView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                        startActivity(intent);
                    }

                });


            } else {
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "MM/dd/yyyy");
                PoliceHolder itemHolder = (PoliceHolder) holder;
                itemHolder.mTitleTextView.setText(crime.getTitle());
                itemHolder.mDateTextView.setText("DATE: " + formatter.format(crime.getDate()));
                itemHolder.mPoliceButton.setText(R.string.police_button_text);
                itemHolder.mSolvedImageView.setVisibility(View.GONE);
                if(crime.isSolved()){
                    itemHolder.mSolvedImageView.setVisibility(View.VISIBLE);
                }
                itemHolder.mPoliceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), R.string.CALL_POLICE, Toast.LENGTH_LONG).show();
                    }
                });
                itemHolder.mTitleTextView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                        startActivity(intent);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }


    }
    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        // Build string to display
        String subtitle = getString(R.string.subtitle_format, crimeCount);
        if(!mSubtitleVisible) // get rid of the string object if we dont need it.
            subtitle = null;
        // get a ref to the host activity
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }
}
