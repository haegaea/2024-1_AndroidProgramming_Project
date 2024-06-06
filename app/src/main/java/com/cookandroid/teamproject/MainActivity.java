package com.cookandroid.teamproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> ipList = new ArrayList<>();
    StringBuilder logBuilder = new StringBuilder();
    ScrollView scrollView;
    LinearLayout logContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = findViewById(R.id.scrollView);
        logContainer = findViewById(R.id.logContainer);
        Button connectBtn = findViewById(R.id.connectBtn);
        Button saveBtn = findViewById(R.id.savebtn);
        Button introBtn = findViewById(R.id.introbtn);
        EditText editText = findViewById(R.id.editext);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>( // 어댑터 생성
                this,
                android.R.layout.simple_list_item_1,
                ipList
        );
        ListView listView = findViewById(R.id.list); // 리스트뷰 생성
        listView.setAdapter(arrayAdapter); // 리스뷰와 어댑터 연결

        introBtn.setOnClickListener(new View.OnClickListener() { // 다른 액티비티로 전환 (사용방법)
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() { // 입력 받은 ip주소 리스트에 저장
            @Override
            public void onClick(View v) {
                for(int i = 0; i < ipList.size(); i ++) {
                    if(editText.getText().toString().equals(ipList.get(i))) { // 이미 존재하는 데이터 저장 시
                        Toast.makeText(getApplicationContext(), "이미 존재하는 데이터입니다.", Toast.LENGTH_SHORT).show(); // 오류 메시지 띄우고
                        return; // 메소드 탈출 (저장하지 않음)
                    }
                }
                if(!editText.getText().toString().equals("")) {
                    ipList.add(editText.getText().toString()); // 리스트에 데이터 저장
                    arrayAdapter.notifyDataSetChanged(); // 어댑터에 반영
                }
                else {
                    Toast.makeText(getApplicationContext(), "데이터가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editText.getText().toString().equals("")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appendLog("Connecting...", android.R.color.holo_orange_light);
                        }
                    });

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL Url = new URL("http://" + editText.getText().toString() + "/on"); // 문자열을 URL형식으로 변환
                                HttpURLConnection connection = (HttpURLConnection) Url.openConnection();

                                connection.setRequestMethod("GET"); // GET방식으로 요청
                                connection.setConnectTimeout(2000); // 연결 지연 2초까지 기다림

                                BufferedReader reader;

                                int resCode = connection.getResponseCode();
                                if(resCode == 200) { // http 상태 코드가 200이면 성공
                                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); // 서버에서 데이터를 받음 (입력스트림 생성)
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "연결 성공", Toast.LENGTH_SHORT).show(); // 연결 성공했다고 띄움
                                            appendLog("Successfully Connected", android.R.color.holo_green_light);
                                        }
                                    });
                                } else {
                                    throw new IOException("HTTP error code: " + resCode);
                                }
                            } catch (Exception e) { // 예외 발생시
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "ip주소를 다시 확인하십시오.", Toast.LENGTH_SHORT).show(); // 연결 실패 메시지 띄움
                                        appendLog("Connection Failed", android.R.color.holo_red_light);
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // 리스트뷰 아이템 이벤트 처리
                String selectIP = ipList.get(position); // position값에 따라 인덱스 구분
                editText.setText(selectIP); // ip주소 입력란에 선택한 ip주소를 자동으로 입력
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) { // 리스트뷰 아이템 길게 클릭하면
                ipList.remove(position); // postion값에 따라 눌린 item 지움
                arrayAdapter.notifyDataSetChanged(); // 어댑터에 지워진 데이터 업데이트
                return false;
            }
        });
    }

    private void appendLog(String message, int colorResId) {
        String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String formattedMessage = String.format("[%s] %s", timestamp, message);

        TextView logEntry = new TextView(this);
        logEntry.setText(formattedMessage);
        logEntry.setTextColor(getResources().getColor(colorResId));

        View separator = new View(this);
        separator.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        logContainer.addView(logEntry);
        logContainer.addView(separator);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
