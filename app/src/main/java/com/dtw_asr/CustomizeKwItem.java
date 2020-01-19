package com.dtw_asr;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomizeKwItem {
    String name;
    public String firstVideoName;
    public String secondVideoName;
    public String thirdVideoName;
    String label;
    public int nameLen;
    boolean isPlaying=false;
    boolean isDeleted=false;

    public CustomizeKwItem(){


    }

    public CustomizeKwItem(String name,String label) {
        this.name=name;
        this.label=label;
        firstVideoName="kw_"+PinyinUtil.cn2Pinyin(name)+label+"1.pcm";
        secondVideoName="kw_"+PinyinUtil.cn2Pinyin(name)+label+"2.pcm";
        thirdVideoName="kw_"+PinyinUtil.cn2Pinyin(name)+label+"3.pcm";
        nameLen=counthanzi(name);

    }

    int counthanzi(String text){
        int amount = 0;
        for(int i = 0;i<text.length();i++){
            boolean matches = Pattern.matches("^[\u4E00-\u9Fa5]{0,}$", text.charAt(i)+"");
            if(matches){
                amount ++;
            }
        }
        return amount;
    }


//    boolean isChinese(String str) {
//        String regex = "[\\u4e00-\\u9fa5]";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(str);
//        return matcher.find();
//
//    }
//
//     String chineneToSpell( String chineseStr) {
//        if (isChinese(chineseStr)) {
//            String pinying = PinyinUtil.cn2Pinyin(chineseStr);
//            return pinying.toUpperCase();
//        }
//        return chineseStr;
//    }

}
