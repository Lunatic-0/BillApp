package com.example.week4_bim;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//通过id获取对象
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        EditText w=findViewById(R.id.weight);
        EditText h=findViewById(R.id.height);
        TextView bmi=findViewById(R.id.bmi);
        Button btn=findViewById(R.id.btn);
        TextView suggestion=findViewById(R.id.suggestion);

        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                String healthAdvice="";
                try {
                    double weight=Double.parseDouble(w.getText().toString());
                    double height=Double.parseDouble(h.getText().toString());
                    double bmiValue=weight/(height*height);
                    DecimalFormat df=new DecimalFormat("0.00");
                    String bmiFormatted=df.format(bmiValue);

                    bmi.setText("您的BMI指数是："+bmiFormatted);

                    if(bmiValue <18.5){
                        healthAdvice="体重过轻，建议增加营养。";
                    } else if (bmiValue >=18.5 && bmiValue <24.9) {
                        healthAdvice="体重正常，继续保持。";
                    } else if (bmiValue >=25 && bmiValue <29.9) {
                        healthAdvice="超重，建议加强运动。";
                    } else  {
                        healthAdvice="肥胖，建议咨询医生。";
                    }
                    suggestion.setText(healthAdvice);
                } catch (NumberFormatException e) {
                    bmi.setText("请输入有效数字！");
                    suggestion.setText("");
                }
            }

        });

    }
}