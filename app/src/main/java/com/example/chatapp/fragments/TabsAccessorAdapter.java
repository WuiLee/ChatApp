package com.example.chatapp.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.chatapp.fragments.AnnouncementFragment;
import com.example.chatapp.fragments.ChatsFragment;
import com.example.chatapp.fragments.GroupsFragment;
import com.example.chatapp.fragments.RequestsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    } //end of Tabs AccessorAdapter

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 3:
                AnnouncementFragment announcementFragment = new AnnouncementFragment();
                return announcementFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4 ;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";

            case 1:
                return "Groups";

            case 2:
                return "Requests";

            case 3:
                return "News";

            default:
                return null;
        }
    }
}
