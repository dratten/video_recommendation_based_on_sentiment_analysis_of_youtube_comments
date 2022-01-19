package com.dalzai.findmyexpert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    TypedArray tabIcons;
    TypedArray seltabIcons;
    TabLayout tabLayout;
    ViewPager viewPager;
    pagerAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TabLayout implementation
        tabLayout = findViewById(R.id.home_menu);
        //Removing Tab Selected Indicator
        tabLayout.setSelectedTabIndicator(0);
        //Retrieving the tab icons and texts
        final String[] tabLabels = getResources().getStringArray(R.array.navLabel);
        tabIcons = getResources().obtainTypedArray(R.array.navIcon);
        seltabIcons = getResources().obtainTypedArray(R.array.navIconSelected);
        //Setting the custom view for each tab and adding it to the tabLayout
        for (int i = 0; i < tabLabels.length; i++)
        {
            RelativeLayout tab = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.nav_tab,null);
            ImageView tab_icon = tab.findViewById(R.id.menu_icon);
            RelativeLayout border = tab.findViewById(R.id.tab_border);
            if(i == 0)//For the homepage
            {
                tab_icon.setImageResource(seltabIcons.getResourceId(i,0));
            }
            else
            {
                tab_icon.setImageResource(tabIcons.getResourceId(i,0));
            }
            //Adding custom tab menu view to tabLayout
            tabLayout.addTab(tabLayout.newTab().setCustomView(tab));
        }
        viewPager = findViewById(R.id.home_view);
        pageAdapter = new pagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                //Highlighting selected tab
                View tabView = tab.getCustomView();
                ImageView tab_icon = tabView.findViewById(R.id.menu_icon);
                tab_icon.setImageResource(seltabIcons.getResourceId(tab.getPosition(),0));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //Unhighlighting selected tab
                View tabView = tab.getCustomView();
                ImageView tab_icon = tabView.findViewById(R.id.menu_icon);
                tab_icon.setImageResource(tabIcons.getResourceId(tab.getPosition(),0));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void search(View view)
    {
        viewPager.setCurrentItem(1);
    }

    public void change_password(View view)
    {
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
        finish();
    }

    public void edit_profile(View view) 
    {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void video(View view)
    {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
        finish();
    }
}
