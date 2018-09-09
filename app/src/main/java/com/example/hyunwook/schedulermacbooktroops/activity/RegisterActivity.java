package com.example.hyunwook.schedulermacbooktroops.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hyunwook.schedulermacbooktroops.R;

public class RegisterActivity extends Activity {
    Button regBtn;
    EditText editEmail, editPw, editName, editPhone, editBirth;

    String strEmail, strPw, strName, strPhone, strBirth;

    static final String TAG = RegisterActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        editEmail = (EditText) findViewById(R.id.emailInput);
        editPw = (EditText) findViewById(R.id.pwInput);
        editName = (EditText) findViewById(R.id.nameInput);
        editPhone = (EditText) findViewById(R.id.phoneInput);
        editBirth = (EditText) findViewById(R.id.birthInput);


        regBtn = (Button) findViewById(R.id.registFinBtn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                strEmail = editEmail.getText().toString();
                strPw = editPw.getText().toString();
                strName = editName.getText().toString();
                strPhone = editPhone.getText().toString();
                strBirth = editBirth.getText().toString();

                Log.d(TAG, "Regist info -->" + strEmail + "--" + strPw + "--" + strName
                        + "--" + strPhone + "--" + strBirth);

                //id, pw, name, phone, birth 한개라도 비어있으면 토스트.
                if (strEmail.equals("") || strPw.equals("") || strName.equals("") || strPhone.equals("") || strBirth.equals("")) {
                    Toast.makeText(getApplicationContext(), "비어있는 항목을 채워주세요.", Toast.LENGTH_LONG).show();
                }
                else {
                    savePreferences();
                }
            }
        });
    }

    //값 저장
    private void savePreferences() {
        SharedPreferences pref = getSharedPreferences("registInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("prefEmail", strEmail);
        editor.putString("prefPw", strPw);
        editor.putString("prefName", strName);
        editor.putString("prefPhone", strPhone);
        editor.putString("prefBirth", strBirth);

        editor.commit();

        finish();
    }
}
