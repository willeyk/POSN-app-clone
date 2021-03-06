package com.posn.main.main.friends.asynctasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.posn.constants.Constants;
import com.posn.datatypes.Friend;
import com.posn.datatypes.RequestedFriend;
import com.posn.exceptions.POSNCryptoException;
import com.posn.managers.AppDataManager;
import com.posn.main.main.MainActivity;
import com.posn.main.main.friends.UserFriendsFragment;
import com.posn.utility.AsymmetricKeyHelper;
import com.posn.managers.DeviceFileManager;
import com.posn.utility.SymmetricKeyHelper;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * This AsyncTask class implements the functionality for the last phase of the friend request process, where the initiating creates a friend file and sends the information
 * to the new friend and updates the temporal file.
 * <ul><li>Updates the friends list from pending to accepted
 * <li>Creates a friend file for the new friend and uploads it to the cloud
 * <li>Creates a new URI containing the friend file info to send to the user directly and updates the temporal file in the cloud
 * <li>Fetches the friend file for the new friend</ul>
 **/
public class NewFriendFinalAsyncTask extends AsyncTask<String, String, String>
   {
      private ProgressDialog pDialog;
      private RequestedFriend requestedFriend;
      private UserFriendsFragment friendFrag;
      private MainActivity main;
      private AppDataManager dataManager;

      public NewFriendFinalAsyncTask(UserFriendsFragment frag, RequestedFriend requestedFriend )
         {
            super();
            friendFrag = frag;
            this.requestedFriend = requestedFriend;
            main = friendFrag.activity;
            dataManager = main.dataManager;
         }


      // Before starting background thread Show Progress Dialog
      @Override
      protected void onPreExecute()
         {
            super.onPreExecute();
            pDialog = new ProgressDialog(main);
            pDialog.setMessage("Adding New Friend...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
         }


      // Checking login in background
      protected String doInBackground(String... params)
         {
            try
               {
                  // create a new friend object from the requested friend
                  Friend newFriend = dataManager.friendManager.addNewAcceptedFriend(requestedFriend, Constants.STATUS_ACCEPTED);

                  // add friend to groups
                  dataManager.userGroupManager.addFriendToGroups(newFriend);

                  // create friend file with all friend group data
                  String fileName = newFriend.ID + "_friend_file.txt";
                  String deviceFilepath = Constants.friendsFilePath;
                  dataManager.createFriendFile(newFriend.ID, deviceFilepath, fileName);

                  // upload group wall to cloud and get direct link
                  String friendFileLink = main.cloud.uploadFileToCloud(Constants.friendDirectory, fileName, deviceFilepath + "/" + fileName);

                  // create URI contain the friend file info
                  String URI = createURI(newFriend, friendFileLink);


                  // SEND AS DIRECT MESSAGE (NEED TO IMPLEMENT)

                  // update the temporal file and upload it to the cloud
                  fileName = requestedFriend.nonce + "_temp_friend_file.txt";
                  deviceFilepath = Constants.friendsFilePath;
                  dataManager.createTemporalFriendFile(URI, deviceFilepath, fileName);
                  main.cloud.uploadFileToCloud(Constants.friendDirectory, fileName, deviceFilepath + "/" + fileName);


                  // fetch the accepted friend's friend file for the user
                  fileName = newFriend.ID + "_friend_user_file.txt";
                  deviceFilepath = Constants.wallFilePath;
                  DeviceFileManager.downloadFileFromURL(newFriend.friendFileLink, deviceFilepath, fileName);

                  // load the friend file into the application
                  dataManager.loadFriendFile(newFriend.ID, deviceFilepath, fileName);

                  dataManager.friendManager.updateFriend(newFriend);

                  // save the friends list to the device
                  dataManager.saveFriendListAppFile(false);
                  dataManager.saveUserGroupListAppFile();
               }
            catch (POSNCryptoException | IOException | JSONException e)
               {
                  e.printStackTrace();
               }
            return null;
         }


      // After completing background task Dismiss the progress dialog
      protected void onPostExecute(String file_url)
         {
            // notify the adapter that the data changed
            friendFrag.updateFriendList();

            // update the user group fragment
            main.tabsAdapter.notifyUserGroupFragmentOnNewDataChange();

            // dismiss the dialog once done
            pDialog.dismiss();
         }


      private String createURI(Friend newFriend, String friendFileLink) throws POSNCryptoException, UnsupportedEncodingException
         {
            // encode the friend file URL and user friend file key to maintain special chars
            String encodedURL = URLEncoder.encode(friendFileLink, "UTF-8");
            String encodedKey = URLEncoder.encode(newFriend.userFriendFileKey, "UTF-8");

            // create URI with appropriate data
            String URI = dataManager.userManager.ID + "/" + encodedURL + "/" + encodedKey + "/" + requestedFriend.nonce2;

            // generate symmetric key to encrypt the URI
            String key = SymmetricKeyHelper.createRandomKey();
            String encryptedURI = SymmetricKeyHelper.encrypt(key, URI);

            // encrypt the symmetric key will the friend's public key
            String encryptedKey = AsymmetricKeyHelper.encrypt(newFriend.publicKey, key);

            // build final URI to send to friend
            return encryptedKey + "/" + encryptedURI;
         }
   }