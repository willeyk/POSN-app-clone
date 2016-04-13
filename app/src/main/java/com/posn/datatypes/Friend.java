package com.posn.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.posn.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class Friend implements Parcelable
   {
      public String id;
      public String name;

      public String phone;
      public String email;
      public String image_uri;

      // holds the list of groups as group IDs
      public ArrayList<String> groups = new ArrayList<>();

      public String publicKey;

      public String friendFileLink;

      public boolean selected;
      public int status;

      public Friend()
         {
            selected = false;
         }

      public Friend(String name)
         {
            this.name = name;
            selected = false;
         }

      public Friend(RequestedFriend friend)
         {
            status = Constants.STATUS_ACCEPTED;
            id = friend.ID;
            name = friend.name;
            publicKey = friend.publicKey;
            friendFileLink = friend.fileLink;
            groups.addAll(friend.groups);
            selected = false;
         }


      public Friend(String name, String email, int status)
         {
            this.name = name;
            this.email = email;
            this.status = status;

            final HashCode hashCode = Hashing.sha1().hashString(email, Charset.defaultCharset());
            id = hashCode.toString();

            phone = "0";
            image_uri = "asd";
            selected = false;
         }

      public JSONObject createJSONObject()
         {
            JSONObject obj = new JSONObject();

            try
               {
                  obj.put("id", id);
                  obj.put("name", name);
                  obj.put("email", email);
                  obj.put("status", status);
                  obj.put("publicKey", publicKey);
                  obj.put("friendFileLink", friendFileLink);

                  JSONArray jsArray = new JSONArray(groups);
                  obj.put("groups", jsArray);
               }
            catch (JSONException e)
               {
                  e.printStackTrace();
               }

            return obj;
         }

      public void parseJSONObject(JSONObject obj)
         {
            try
               {
                  status = obj.getInt("status");
                  id = obj.getString("id");
                  name = obj.getString("name");
                  email = obj.getString("email");

                  if (obj.has("publicKey"))
                     {
                        publicKey = obj.getString("publicKey");
                     }
                  if (obj.has("friendFileLink"))
                     {
                        friendFileLink = obj.getString("friendFileLink");
                     }

                  if (obj.has("groups"))
                     {
                        JSONArray groupMemberList = obj.getJSONArray("groups");
                        for (int n = 0; n < groupMemberList.length(); n++)
                           {
                              String groupID = groupMemberList.getString(n);
                              groups.add(groupID);
                           }
                     }
               }
            catch (JSONException e)
               {
                  e.printStackTrace();
               }
         }

      @Override
      public boolean equals(Object o)
         {
            if (!(o instanceof Friend))
               {
                  return false;
               }
            Friend other = (Friend) o;
            System.out.println(name + " | " + other.name);
            return name.equalsIgnoreCase(other.name);
         }


      // Parcelling part
      public Friend(Parcel in)
         {
            this.id = in.readString();
            this.name = in.readString();
            this.email = in.readString();
            this.publicKey = in.readString();
            this.friendFileLink = in.readString();
            this.status = in.readInt();

            in.readStringList(groups);
         }


      @Override
      public void writeToParcel(Parcel dest, int flags)
         {
            dest.writeString(this.id);
            dest.writeString(this.name);
            dest.writeString(this.email);
            dest.writeString(this.publicKey);
            dest.writeString(this.friendFileLink);
            dest.writeInt(this.status);
            dest.writeStringList(groups);
         }

      public static final Parcelable.Creator<Friend> CREATOR = new Parcelable.Creator<Friend>()
         {
            public Friend createFromParcel(Parcel in)
               {
                  return new Friend(in);
               }

            public Friend[] newArray(int size)
               {
                  return new Friend[size];
               }
         };

      @Override
      public int describeContents()
         {
            return 0;
         }
   }