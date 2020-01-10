package com.mandeep.toastlibrary;

import android.content.Context;
import android.widget.Toast;

public class ToastMessage {

    public static void showToast(Context context, String message){

        if (message==null){
            message = "You passed null as message";
        }
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show();

    }
}
