package com.dtw_asr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class CustomizeKwNameAddActivity extends Activity {
    ImageView quitImg;
    EditText kwNameText;
    Button kwNxtBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.customize_kw_name_add);
        quitImg =findViewById(R.id.customize_kw_add_quit);
        quitImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomizeKwManager.getInstance().clrCustomizeKwItem();
                finish();
            }
        });
        kwNameText =findViewById(R.id.customize_kw_add_txt);
        kwNxtBtn = findViewById(R.id.customize_kw_add_nxt);
        kwNxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serch_textinfo =kwNameText.getText().toString();
                if(serch_textinfo!=null && !"".equals(serch_textinfo)){
                    CustomizeKwManager.getInstance().setCustomizeKwItem(serch_textinfo);
                    Intent intent=new Intent(CustomizeKwNameAddActivity.this,CustomizeKwVoiceRecordActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
//                    Toast.makeText(CustomizeKwNameAddActivity.this,"唤醒词不能为空",Toast.LENGTH_SHORT).show();
                    Toast toast=Toast.makeText(CustomizeKwNameAddActivity.this,"唤醒词不能为空",Toast.LENGTH_SHORT);
                    toast.setText("唤醒词不能为空");
                    toast.show();
                }


            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK){
            CustomizeKwManager.getInstance().clrCustomizeKwItem();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
