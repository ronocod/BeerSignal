package com.starstorm.beer.fragment;

import android.app.Fragment;

/**
 * Created by Conor on 17/09/2014.
 */
public abstract class BaseFragment extends Fragment {

    protected void setMenuWhirrerVisible(boolean visible) {
        // need implementation of this as setSupportProgressBarIndeterminateVisibility has been removed
//        if (getActivity() != null) {
//            ((BaseActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(visible);
//        }
    }
}
