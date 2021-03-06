package com.posn.main.main.friends.asynctasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.posn.constants.Constants;
import com.posn.datatypes.RequestedFriend;
import com.posn.email.EmailSender;
import com.posn.exceptions.POSNCryptoException;
import com.posn.main.main.MainActivity;
import com.posn.main.main.friends.UserFriendsFragment;
import com.posn.managers.AppDataManager;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * This AsyncTask class implements the functionality for the first of three phases of the friend request process, where the user sends the friend request to a desired friend
 * <ul><li>Takes in the new requested friend object and creates a temporal file for the desired friend and uploads it to the cloud
 * <li>Builds a friend request URI with the following user data: id, name, email, public key, URL to the temporal file, and nonce value
 * <li>Creates a new email containing the URI and sends it to the desired friend</ul>
 **/
public class NewFriendInitialAsyncTask extends AsyncTask<String, String, String>
   {
      private ProgressDialog pDialog;
      private RequestedFriend requestedFriend;
      private UserFriendsFragment friendFrag;
      private MainActivity main;
      private AppDataManager dataManager;

      public NewFriendInitialAsyncTask(UserFriendsFragment frag, RequestedFriend requestedFriend)
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
                  // create empty temporal friend wall file on device to upload to cloud
                  String fileName = requestedFriend.nonce + "_temp_friend_file.txt";
                  String deviceFilepath = Constants.friendsFilePath;
                  dataManager.createTemporalFriendFile(null, deviceFilepath, fileName);

                  // upload group wall to cloud and get direct link
                  String temporalFileLink = main.cloud.uploadFileToCloud(Constants.friendDirectory, fileName, deviceFilepath + "/" + fileName);

                  // create the URI
                  String URI = createURIData(temporalFileLink);

                  // send the email
                  sendEmailToFriend(URI);

                  // add pending friend to request friends list
                  dataManager.friendManager.addNewPendingFriend(requestedFriend);

                  // save the updated friends list to the device
                  dataManager.saveFriendListAppFile(false);
               }
            catch (JSONException | IOException | POSNCryptoException error)
               {
                  error.printStackTrace();
               }

            return null;
         }


      // After completing background task Dismiss the progress dialog
      protected void onPostExecute(String file_url)
         {
            // notify the adapter that the data changed
            friendFrag.updateFriendList();

            // dismiss the dialog once done
            pDialog.dismiss();
         }


      private String createURIData(String temporalFileLink) throws UnsupportedEncodingException
         {
            // replace all + chars in the key to the hex value
            String publicKey = dataManager.userManager.publicKey;
            publicKey = publicKey.replace("+", "%2B");

            // encode the key to maintain special chars
            publicKey = URLEncoder.encode(publicKey, "UTF-8");

            // encode temporal URL to maintain special chars
            String encodedURL = URLEncoder.encode(temporalFileLink, "UTF-8");

            // create URI containing the user data
            return "http://posn.com/request/" + dataManager.userManager.ID + "/" + dataManager.userManager.email + "/" + dataManager.userManager.firstName + "/" + dataManager.userManager.lastName.trim()
                       + "/" + publicKey + "/" + encodedURL + "/" + requestedFriend.nonce;
         }


      private void sendEmailToFriend(String URI)
         {
            // BAD TO HARDCODE EMAIL ADDRESS AND PASSWORD
            EmailSender email = new EmailSender("projectcloudbook@gmail.com", "cnlpass!!");
            String body = email.emailBodyFormatter(dataManager.userManager.firstName + " " + dataManager.userManager.lastName + " wants to be your friend in POSN!", URI, "Click to Respond to the Request");
            email.sendMail("POSN - New Friend Request", body, "POSN", requestedFriend.email);
         }
   }