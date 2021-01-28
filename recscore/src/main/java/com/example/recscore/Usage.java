package com.example.recscore;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.List;
import java.util.Map;

public class Usage {
    HumanPoseGet worker = new HumanPoseGet();
    public void usage(Context context,Bitmap bitmap){

        TensorFlowInferenceInterface inferenceInterface = worker.getModelInterface(context);
        List humans = worker.getHumans(bitmap,inferenceInterface);
        //绘制图形关节点
        Bitmap bitmap1 = worker.draw_humans(bitmap,humans,false,0.0);
        //获取关节信息
        List<Map> infs = worker.getHumanAngle(humans,bitmap);
        //打分,分别传入学生与老师的关节信息进行打分。
        Float score = worker.getDanceScore(infs,infs);



    }
}
