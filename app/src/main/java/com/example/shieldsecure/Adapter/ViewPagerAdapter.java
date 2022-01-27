package com.example.shieldsecure.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.shieldsecure.Fragments.DecryptFragment;
import com.example.shieldsecure.Fragments.EncryptFragment;
import com.example.shieldsecure.Fragments.MainFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                MainFragment mainFragment = new MainFragment();
                return mainFragment;
            case 1:
                EncryptFragment encryptFragment = new EncryptFragment();
                return encryptFragment;
            case 2:
                DecryptFragment decryptFragment = new DecryptFragment();
                return decryptFragment;


        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
