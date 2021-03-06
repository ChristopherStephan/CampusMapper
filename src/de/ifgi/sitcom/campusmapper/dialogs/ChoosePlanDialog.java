package de.ifgi.sitcom.campusmapper.dialogs;



import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.ifgi.sitcom.campusmapper.R;
import de.ifgi.sitcom.campusmapper.indoordata.FloorPlan;
import de.ifgi.sitcom.campusmapper.io.RDFReader;

/*
 * dialog shown after click on stairs/ elevator/ entrance destination button
 */
public class ChoosePlanDialog extends SherlockDialogFragment {
	
    
    // Use this instance of the interface to deliver action events
	private ChoosePlanDialogListener mListener;
	
	private LinearLayout mLlProgressCircle;
	private ListView mPlanList;
	
	private LoadPlansFromLODUM mLoadPlansFromLODUM;

	private ArrayList<FloorPlan> mFloorPlans;
	private int mSelectedIndex = -1;
	
	private FloorPlan mBasicFloorPlan;
	
	
    public interface ChoosePlanDialogListener {
        public void onChoosePlanDialogPositiveClick(ChoosePlanDialog dialog);
        public void onChoosePlanDialogNegativeClick(ChoosePlanDialog dialog);
    }
    
    public void setFloorPlans(ArrayList<FloorPlan> floorPlans){
    	this.mFloorPlans = floorPlans;
    }
    
    private String[] stringArrayFromFloorPlans(){
    	String [] result = new String [mFloorPlans.size()];
    	
    	for (int i = 0; i < mFloorPlans.size(); i++){    		
    		result[i] = mFloorPlans.get(i).getId();
    	}
    	
    	
    	return result;
    }
        
    
    
	public FloorPlan getBasicFloorPlan() {
		return mBasicFloorPlan;
	}

	public void setBasicFloorPlan(FloorPlan basicFloorPlan) {
		this.mBasicFloorPlan = basicFloorPlan;
	}

	public void attachHandler(Activity stairsLocationActivity) {

		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (ChoosePlanDialogListener) stairsLocationActivity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(stairsLocationActivity.toString()
					+ " must implement NoticeDialogListener");
		}

	}
    
    
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View dialogView = inflater.inflate(R.layout.dialog_choose_plan, null);
	    builder.setView(dialogView)
	    // Add title
	    .setTitle(R.string.title_choose_plan)
	    // Add action buttons
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   if (mListener != null)
	                   mListener.onChoosePlanDialogNegativeClick(ChoosePlanDialog.this);
	               }
	           }); 

	    
		mLlProgressCircle = (LinearLayout) dialogView.findViewById(R.id.ll_progress_circle);
	    mPlanList = (ListView) dialogView.findViewById(R.id.listview_plans);
	    

	    if(mFloorPlans != null) showList(stringArrayFromFloorPlans());
	    else {
		    /*
		     * It might be that the async task of loading person names is still running.
		     * For this reason use .executeOnExecutor instead of .execute. This way we can have multiple async tasks at the same time.
		     */
		    mLoadPlansFromLODUM = new LoadPlansFromLODUM();
		    mLoadPlansFromLODUM.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");	    	
	    }
	  		
	    return builder.create();
	}
	
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);		
		if (mLoadPlansFromLODUM != null) mLoadPlansFromLODUM.cancel(true);
	}
	
	
	private void showList(String [] listItems){

    	if( getSherlockActivity() != null){
        	
        	mLlProgressCircle.setVisibility(View.GONE);
        	ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, listItems);
        	mPlanList.setAdapter(arrayAdapter);
        	mPlanList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> a, View view,
						int position, long id) {
					
					mSelectedIndex = position;
					ChoosePlanDialog.this.getDialog().cancel();
	                   mListener.onChoosePlanDialogPositiveClick(ChoosePlanDialog.this);
					
				}
			});        		
    	}
	}
	
	public FloorPlan getSelectedPlan(){
		
		if (mSelectedIndex >= 0){
			return mFloorPlans.get(mSelectedIndex);
		} else {
			return null;
		}
		
	}
	
	
	private class LoadPlansFromLODUM extends AsyncTask<String, Void, String[]> {

        @Override
        protected String [] doInBackground(String... params) {

        	mFloorPlans = getFloorPlans();
        	return stringArrayFromFloorPlans();
        }
        
    	private ArrayList<FloorPlan> getFloorPlans(){
    		
    		RDFReader rdfReader = new RDFReader();
    		ArrayList<FloorPlan> floorPlans = 
    				rdfReader.getFloorPlanList(mBasicFloorPlan.getBuildingURI(), 
    						Integer.toString(mBasicFloorPlan.getFloorNumber()), "");
    				
    		return floorPlans;
    	}

        @Override
		protected void onPostExecute(String[] result) {
        	showList(result);
		}

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
        
	 }



}
