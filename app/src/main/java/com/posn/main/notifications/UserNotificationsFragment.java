package com.posn.main.notifications;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.posn.R;
import com.posn.application.POSNApplication;
import com.posn.datatypes.Notification;
import com.posn.main.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class UserNotificationsFragment extends Fragment implements OnClickListener
   {
      int TYPE_COMMENT = 0;
      int TYPE_FRIEND_REQUEST = 1;
      int TYPE_FRIEND_ACCEPTED = 2;

      // declare variables
      Context context;
      ArrayList<Notification> notificationsList;
      ListView lv;
      POSNApplication app;
      NotificationArrayAdapter adapter;
      MainActivity main;

      @Override
      public void onResume()
         {
            super.onResume();
         }


      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
         {
            super.onCreate(savedInstanceState);

            // load the system tab layout
            View view = inflater.inflate(R.layout.fragment_user_notifications, container, false);
            context = getActivity();

            // get the application
            main = (MainActivity) getActivity();
            app = (POSNApplication) getActivity().getApplication();

            // get the listview from the layout
            lv = (ListView) view.findViewById(R.id.listView1);

            notificationsList = main.notificationData;

            // fill with fake data
            //createNotifications();
            //loadNotifications();

            adapter = new NotificationArrayAdapter(getActivity(), notificationsList);
            lv.setAdapter(adapter);

            return view;
         }


      @Override
      public void onActivityCreated(Bundle savedInstanceState)
         {
            super.onActivityCreated(savedInstanceState);
            onResume();
         }


      @Override
      public void onAttach(Activity activity)
         {
            super.onAttach(activity);
            context = getActivity();
         }


      @Override
      public void onClick(View arg0)
         {
         }


      public void createNotificationsList()
         {
            JSONArray notifications = new JSONArray();

            try
               {

                  Notification notification = new Notification(TYPE_COMMENT, "ec3591b0907170cc48c6759c013333f712141eb8", "Jan 19, 2015 at 1:45 pm");
                  notifications.put(notification.createJOSNObject());

                  notification = new Notification(TYPE_FRIEND_ACCEPTED, "413e990ba1e5984d8fd41f1a1acaf3d154b21cab", "Jan 19, 2015 at 1:45 pm");
                  notifications.put(notification.createJOSNObject());

                  notification = new Notification(TYPE_FRIEND_REQUEST, "dc66ae1b5fa5c84cf12b82e2ec07f6b91233e8d4", "Jan 19, 2015 at 1:45 pm");
                  notifications.put(notification.createJOSNObject());

                  JSONObject studentsObj = new JSONObject();
                  studentsObj.put("notifications", notifications);

                  String jsonStr = studentsObj.toString();

                  FileWriter fw = new FileWriter(app.wallFilePath + "/user_notifications.txt");
                  BufferedWriter bw = new BufferedWriter(fw);
                  bw.write(jsonStr);
                  bw.close();

               }
            catch (JSONException | IOException e)
               {
                  e.printStackTrace();
               }
         }

      public void updateNotifications()
         {
            //createNotificationsList();

            // notify the adapter about the data change
            adapter.notifyDataSetChanged();
         }
   }