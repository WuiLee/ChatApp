package com.example.chatapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chatapp.R;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context){
        this.context = context;
    }

    public int[] slide_images = {
            R.drawable.doctor,
            R.drawable.security,
            R.drawable.nurse
    };

    public String[] slide_headings = {
            "Mental Health Crisis Hotline",
            "Sunway Security Emergency Hotline",
            "Nightingale Bay / Nurse"
    };

    public String[] slide_phone_no = {
            "Call : 018 - 389 3220",
            "Call : 03 - 7491 8777",
            "Call : 03 - 7491 8670"
    };

    public String[] slide_office_hours = {
            "Office Hours : 24 hours",
            "Office Hours : 24 hours",
            "Office Hours : 8.30 a.m. - 5.30 p.m."
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return (view == o);
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_contact_list, container, false);

        ImageView slideImageView= (ImageView)view.findViewById(R.id.slide_image);
        TextView slideHeading = (TextView) view.findViewById(R.id.slide_headings);
        TextView slidePhoneNo = (TextView) view.findViewById(R.id.slide_phone_no);
        TextView slideOfficeHour = (TextView) view.findViewById(R.id.slide_office_hours);

        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slidePhoneNo.setText(slide_phone_no[position]);
        slideOfficeHour.setText(slide_office_hours[position]);


        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView ((RelativeLayout)object);
    }
}
