package com.example.hardwork.ui;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.hardwork.Bean.DefineTimer;
import com.example.hardwork.Bean.Question;
import com.example.hardwork.R;
import com.example.hardwork.database.DataGet;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private int count;
    //当前显示的题目
    private int current;
    //问题
    private TextView tv_title;
    //选项
    RadioButton[] mRadioButton = new RadioButton[4];
    //上一题
    private Button btn_up;
    //下一题
    private Button btn_down;
    //详情
    private TextView tv_result;
    //容器
    private RadioGroup mRadioGroup;
    //是否进入错题模式
    private boolean wrongMode;
    private DefineTimer countDownTimer;
    private TextView timer;
    Handler handler;
    List<Question> list = new ArrayList<>();
    int minute,second;
    List<Integer> wrongList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                List<Map<String,Object>> listitem = (List<Map<String, Object>>) bundle.getSerializable("list");
                for(int i=0;i<listitem.size();i++){
                    list.add((Question)listitem.get(i));
                }
                initDB();
            }
        };
        SendByHttpClient("http://129.204.21.235:8080/app-Test/question");
        initView();
    }

    private long currentTime = 1000*60L;

    public void startTimer() {
        countDownTimer = new DefineTimer(currentTime, 1000) {//2700 45分钟
            @Override
            public void onTick(long l) {
                currentTime = l;
                int allSecond = (int) l / 1000;//秒
                minute = allSecond / 60;
                second = allSecond - minute * 60;
                timer.setText("倒计时 " + minute + ":" + second);
            }

            @Override
            public void onFinish() {
               // saveHistory();
                wrongList = checkAnswer(list);

                new AlertDialog.Builder(TestActivity.this).setTitle("答题时间到！")
                        .setMessage("本次得分为：" + (list.size() - wrongList.size())*20 + "分" )
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
                Log.d("测试","time is over"+wrongList.size());
            }
        };
        countDownTimer.start();
    }


   @Override
    protected void onPause() {
        if (true) {
            countDownTimer.cancel();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (true) {
            startTimer();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (true) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    private void initView() {

        wrongMode = false;

        tv_title = (TextView) findViewById(R.id.ques_question);

        mRadioButton[0] = (RadioButton) findViewById(R.id.ques_an1);
        mRadioButton[1] = (RadioButton) findViewById(R.id.ques_an2);
        mRadioButton[2] = (RadioButton) findViewById(R.id.ques_an3);
        mRadioButton[3] = (RadioButton) findViewById(R.id.ques_an4);

        btn_down = (Button) findViewById(R.id.btn_previous);
        btn_up = (Button) findViewById(R.id.btn_next);

        tv_result = (TextView) findViewById(R.id.ques_explain);

        mRadioGroup = (RadioGroup) findViewById(R.id.ques_radiogroup);
        timer = findViewById(R.id.question_countdown);
    }

    private void initDB() {
        count = list.size();
        current = 0;

        Question q = list.get(0);
        tv_title.setText(q.getQuestion());

        mRadioButton[0].setText(q.getAnswerA());
        mRadioButton[1].setText(q.getAnswerB());
        mRadioButton[2].setText(q.getAnswerC());
        mRadioButton[3].setText(q.getAnswerD());

        //上一题
        btn_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current > 0) {
                    current--;

                    Question q = list.get(current);

                    tv_title.setText(q.getQuestion());

                    mRadioButton[0].setText(q.getAnswerA());
                    mRadioButton[1].setText(q.getAnswerB());
                    mRadioButton[2].setText(q.getAnswerC());
                    mRadioButton[3].setText(q.getAnswerD());

                    tv_result.setText(q.getExplaination());

                    mRadioGroup.clearCheck();

                    //设置选中
                    if (q.getSelectedAnswer() != -1) {
                        mRadioButton[q.getSelectedAnswer()].setChecked(true);
                    }
                }

            }
        });

        //下一题
        btn_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否为最后一题
                if (current < count - 1) {
                    current++;
                    Question q = list.get(current);

                    tv_title.setText(q.getQuestion());

                    mRadioButton[0].setText(q.getAnswerA());
                    mRadioButton[1].setText(q.getAnswerB());
                    mRadioButton[2].setText(q.getAnswerC());
                    mRadioButton[3].setText(q.getAnswerD());

                    tv_result.setText(q.getExplaination());

                    mRadioGroup.clearCheck();

                    //设置选中
                    if (q.getSelectedAnswer() != -1) {
                        mRadioButton[q.getSelectedAnswer()].setChecked(true);
                    }
                } else if (current == count - 1 && wrongMode == true) {

                    new AlertDialog.Builder(TestActivity.this).setTitle("提示").setMessage("已经到达最后一道题，是否退出？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).setNegativeButton("取消",null).show();

                } else if (current == count - 1 && wrongMode == false){

                    new AlertDialog.Builder(TestActivity.this).setTitle("提示").setMessage("已经到达最后一道题，提交？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    countDownTimer.cancel();  //时钟停止
                                    wrongList = checkAnswer(list);

                                    if(wrongList.size()==0){
                                        new AlertDialog.Builder(TestActivity.this).setTitle("恭喜，答题完成！")
                                                .setMessage("本次得分为：" + (list.size() - wrongList.size())*20 + "分" )
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }).show();
                                    }else{
                                        new AlertDialog.Builder(TestActivity.this).setTitle("恭喜，答题完成！")
                                                .setMessage("本次得分为：" + (list.size() - wrongList.size())*20 + "分" + "\n"
                                                        + "是否查看错题？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                wrongMode = true;
                                                List<Question> newList = new ArrayList<Question>();
                                                for (int i = 0; i < wrongList.size(); i++) {
                                                    newList.add(list.get(wrongList.get(i)));
                                                }
                                                list.clear();
                                                for (int i = 0; i < newList.size(); i++) {
                                                    list.add(newList.get(i));
                                                }
                                                current = 0;
                                                count = list.size();

                                                //更新当前显示的内容
                                                Question q = list.get(current);

                                                tv_title.setText(q.getQuestion());

                                                mRadioButton[0].setText(q.getAnswerA());
                                                mRadioButton[1].setText(q.getAnswerB());
                                                mRadioButton[2].setText(q.getAnswerC());
                                                mRadioButton[3].setText(q.getAnswerD());

                                                tv_result.setText(q.getExplaination());
                                                //显示结果
                                                tv_result.setVisibility(View.VISIBLE);
                                            }
                                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }).show();
                                    }
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            }
        });

        //答案选中
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < 4; i++) {
                    if (mRadioButton[i].isChecked() == true) {
                        list.get(current).setSelectedAnswer(i);
                        break;
                    }
                }
            }
        });
    }

    //判断是否答题正确
    private List<Integer> checkAnswer(List<Question> list) {
        List<Integer> wrongList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            //判断对错
            if (list.get(i).getAnswer() != list.get(i).getSelectedAnswer()){
                wrongList.add(i);
            }
        }
        return wrongList;
    }

    public void SendByHttpClient(final String urlPath){
        final ArrayList<Question> qlist = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = new DataGet().readParse(urlPath);

                    JSONArray jsonArray = new JSONArray(result);
                    for(int i=0;i<jsonArray.length();i++){
                        Question question = new Question();
                        //ID
                        question.setID(jsonArray.getJSONObject(i).getInt("id"));
                        question.setQuestion(jsonArray.getJSONObject(i).getString("field1"));
                        question.setAnswerA(jsonArray.getJSONObject(i).getString("field2"));
                        question.setAnswerB(jsonArray.getJSONObject(i).getString("field3"));
                        question.setAnswerC(jsonArray.getJSONObject(i).getString("field4"));
                        question.setAnswerD(jsonArray.getJSONObject(i).getString("field5"));
                        question.setAnswer(jsonArray.getJSONObject(i).getInt("field6"));
                        question.setExplaination(jsonArray.getJSONObject(i).getString("field7"));

                        //设置为没有选择任何选项
                        question.setSelectedAnswer(-1);
                        qlist.add(question);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("list",qlist);
                Message message=new Message();
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }).start();

    }

}
