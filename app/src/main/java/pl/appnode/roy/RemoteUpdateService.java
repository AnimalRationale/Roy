package pl.appnode.roy;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

public class RemoteUpdateService extends Service {

    private Firebase f = new Firebase("https://somedemo.firebaseio-demo.com/");
    private ValueEventListener handler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
