package com.posn.main.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.posn.Constants;
import com.posn.R;
import com.posn.adapters.FriendGroupArrayAdapter;
import com.posn.application.POSNApplication;
import com.posn.datatypes.Group;
import com.posn.datatypes.RequestedFriend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class AddFriendsActivity extends FragmentActivity implements OnClickListener
   {

      // declare variables
      Button addFriend;
      EditText name, email;
      ListView lv;

      ArrayList<Group> groupList;
      int type;

      RequestedFriend requestedFriend;

      POSNApplication app;

      FriendGroupArrayAdapter adapter;


      @Override
      protected void onCreate(Bundle savedInstanceState)
         {
            super.onCreate(savedInstanceState);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            // get the group list from the intent extras
            groupList = getIntent().getExtras().getParcelableArrayList("groups");
            type = getIntent().getExtras().getInt("type");

            // get the XML layout
            if (type == Constants.TYPE_FRIEND_INFO)
               {
                  setContentView(R.layout.activity_add_friends);
                  requestedFriend = new RequestedFriend();
               }
            else
               {
                  setContentView(R.layout.activity_add_groups);
                  requestedFriend = (RequestedFriend) getIntent().getExtras().get("requestedFriend");
               }

            // sort the grouplist by group name
            Collections.sort(groupList, new Comparator<Group>()
               {
                  public int compare(Group o1, Group o2)
                     {
                        return o1.name.compareTo(o2.name);
                     }
               });

            // get the listview from the layout
            lv = (ListView) findViewById(R.id.listView1);

            // get the EditText from the layout
            name = (EditText) findViewById(R.id.name_text);
            email = (EditText) findViewById(R.id.email_text);

            // get the buttons from the layout
            addFriend = (Button) findViewById(R.id.add_friend_button);

            // set onclick listener for each button
            addFriend.setOnClickListener(this);

            // get all the phone contacts.

            lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            lv.setItemsCanFocus(true);


            // create a custom adapter for each contact item in the listview
            adapter = new FriendGroupArrayAdapter(this, groupList, requestedFriend);

            // set the adapter to the listview
            lv.setAdapter(adapter);

            // set onItemClick listener
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
               {

                  @Override
                  public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
                     {
                        CheckBox currentCheckBox = (CheckBox) view.findViewById(R.id.checkBox1);
                        currentCheckBox.toggle();

                        Group group = (Group) parent.getItemAtPosition(position);

                        // get the contact that was click and toggle the check box
                        adapter.updateSelectedGroupList(group);

                        // refresh the listview
                        //  adapter.notifyDataSetChanged();
                     }

               });

            // get the action bar and set the title
            ActionBar actionBar = getActionBar();
            if (actionBar != null)
               {
                  actionBar.setTitle("Add New Friend");
               }

            app = (POSNApplication) getApplication();

            //            friendList = app.friendList;
         }


      @Override
      public void onClick(View v)
         {
            switch (v.getId())
               {
                  case R.id.add_friend_button:

                     System.out.println("GROUP SIZE: " + requestedFriend.groups.size());

                     if (type == Constants.TYPE_FRIEND_INFO)
                        {
                           if ((!isEmpty(email)))
                              {
                                 // change the status to pending
                                 requestedFriend.status = Constants.STATUS_PENDING;

                                 // get the friend's name from the edit text
                                 requestedFriend.name = name.getText().toString();

                                 // get the friend's email from the edit text
                                 requestedFriend.email = email.getText().toString();

                                 // create nonce
                                 requestedFriend.nonce = Integer.toString((int) (System.currentTimeMillis() / 1000));

                                 Intent resultIntent = new Intent();
                                 setResult(Activity.RESULT_OK, resultIntent);
                                 resultIntent.putExtra("requestedFriend", requestedFriend);
                                 finish();
                              }
                           else
                              {
                                 Toast.makeText(this, "You must add at least one friend.", Toast.LENGTH_SHORT).show();
                              }
                        }
                     else
                        {
                           requestedFriend.nonce2 = Integer.toString((int) (System.currentTimeMillis() / 1000));

                           Intent resultIntent = new Intent();
                           setResult(Activity.RESULT_OK, resultIntent);
                           resultIntent.putExtra("requestedFriend", requestedFriend);
                           finish();
                        }




                     break;
               }
         }


      private boolean isEmpty(EditText etText)
         {
            if ((etText.getText().toString().trim().length() > 0))
               {
                  return false;
               }

            return true;
         }
   }
