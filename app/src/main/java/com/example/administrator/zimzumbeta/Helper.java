package com.example.administrator.zimzumbeta;

import android.os.Bundle;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Helper {

    public static Set<String> allowedTypes;
    public static List<String> types = Arrays.asList("button","textview");
    public static void init(){
        allowedTypes = new HashSet<>();
        allowedTypes.addAll(types);

    }
    public static Message msgCreator(String type,Integer id,String text){
        if(allowedTypes == null){
            init();
        }
        Bundle b = new Bundle();


        b.putString("type",type);
        b.putInt("id",id);
        b.putString("text",text);
        Message msg = new Message();
        msg.setData(b);
        return msg;
    }
}
