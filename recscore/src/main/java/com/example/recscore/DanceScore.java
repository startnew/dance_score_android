package com.example.recscore;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DanceScore {

    private List<Map<String,Integer>> SCORE_RELU = new ArrayList<>();


    DanceScore(){
        Map<String,Integer> score_relu1 = new HashMap();
        Map<String,Integer> score_relu4 = new HashMap();
        Map<String,Integer> score_relu5 = new HashMap();
        Map<String,Integer> score_relu6 = new HashMap();
        Map<String,Integer> score_relu7 = new HashMap();
        Map<String,Integer> score_relu3 = new HashMap();
        Map<String,Integer> score_relu2 = new HashMap();
        Map<String,Integer> score_relu8 = new HashMap();
        Map<String,Integer> score_relu9 = new HashMap();
        Map<String,Integer> score_relu10 = new HashMap();


        score_relu1.put("st",0);
        score_relu1.put("ed",3);
        score_relu1.put("score",100);
        SCORE_RELU.add(score_relu1);

        score_relu2.put("st",5);
        score_relu2.put("ed",10);
        score_relu2.put("score",89);
        SCORE_RELU.add(score_relu2);

        score_relu3.put("st",10);
        score_relu3.put("ed",15);
        score_relu3.put("score",80);
        SCORE_RELU.add(score_relu3);

        score_relu4.put("st",15);
        score_relu4.put("ed",20);
        score_relu4.put("score",70);
        SCORE_RELU.add(score_relu4);
        score_relu5.put("st",20);
        score_relu5.put("ed",30);
        score_relu5.put("score",60);
        SCORE_RELU.add(score_relu5);
        score_relu6.put("st",30);
        score_relu6.put("ed",50);
        score_relu6.put("score",20);
        SCORE_RELU.add(score_relu1);
        score_relu7.put("st",90);
        score_relu7.put("ed",180);
        score_relu7.put("score",2);


        SCORE_RELU.add(score_relu7);
        score_relu8.put("st",-30);
        score_relu8.put("ed",0);
        score_relu8.put("score",20);
        SCORE_RELU.add(score_relu8);
        score_relu9.put("st",-90);
        score_relu9.put("ed",-30);
        score_relu9.put("score",2);
        SCORE_RELU.add(score_relu9);
        score_relu10.put("st",-180);
        score_relu10.put("ed",-90);
        score_relu10.put("score",20);
        SCORE_RELU.add(score_relu10);
        this.SCORE_RELU = SCORE_RELU;

    }


    //计算身体部位的夹角版本二：使用每个部位的开始点与结束点计算其与垂直正上方的夹角
    public List<Map> getAngleByInfos_v2(List<Map> infs){
        if (infs.size()>0) {
            for (int i = 0; i < infs.size(); i++) {
                double dx1 = (double)((int)infs.get(i).get("line_ed_x")-(int)infs.get(i).get("line_st_x"));
                double dy1 = (double)((int)infs.get(i).get("line_ed_y")-(int)infs.get(i).get("line_st_y"));
                double cosx1x2 = (dx1)/(Math.sqrt(Math.pow(dx1,2)+Math.pow(dy1,2)));
                try{
                    int cobb;


                    cobb = (int)((dy1/Math.abs(dy1))* (Math.acos(cosx1x2) * 180 / Math.PI + 0.5));


                    Log.e("angles",String.valueOf(cobb));
                    infs.get(i).put("angles", (float) cobb);
                }catch (Exception e){
                    System.out.println(e);
                    continue;
                }


                }




            }
            return infs;
        }







    //计算夹角
    public List<Map> getAngleByInfos(List<Map> infs){
        if (infs.size()>0){
            for (int i = 0;i<infs.size();i++){
                List<Map> inf__ = new ArrayList<>();
                inf__.clear();
                infs = helpAngleByInfos(infs,inf__,i,"st","ed","st","ed","ed");
                infs = helpAngleByInfos(infs,inf__,i,"st","st","ed","st","ed");
                infs = helpAngleByInfos(infs,inf__,i,"ed","ed","st","ed","st");


            }

        }
        return infs;
    }
    public List<Map> helpAngleByInfos(List<Map> infs, List<Map> inf__,Integer i,String st,String ed,String st1,String mid1,String ed1){

        for (int j = 0;j<infs.size();j++){
            if (infs.get(j).get(st) == infs.get(i).get(ed) && i!=j){
                inf__.add(infs.get(j));
            }
        }
        if (inf__.size()>0){
            for (int k = 0;k<inf__.size();k++){

                double dx1 = (double)((int)inf__.get(k).get("line_ed_x")-(int)inf__.get(k).get("line_st_x"));
                double dy1 = (double) ((int)inf__.get(k).get("line_ed_y")-(int)inf__.get(k).get("line_st_y"));
                double dx = (double) ((int)infs.get(i).get("line_ed_x")-(int)infs.get(i).get("line_st_x"));
                double dy = (double) ((int)infs.get(i).get("line_ed_y")-(int)infs.get(i).get("line_st_y"));
                double cosx1x2 = (dx1*dx+dy1*dy)/(Math.sqrt(Math.pow(dx1,2)+Math.pow(dy1,2))*Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2)));
                try {
                    int cobb = (int)(((dy1/Math.abs(dy1)))* (Math.acos(cosx1x2) * 180 / Math.PI + 0.5));
                    if (infs.get(i).containsKey("angles")) {
                        //List<Map> angles = (List<Map>) infs.get(i).get("angles");

                        Map<String, Float> angle = new HashMap();
                        angle.put("degree", (float) cobb);
                        angle.put("st", Float.valueOf( infs.get(i).get(st1).toString()));
                        angle.put("mid", Float.valueOf( infs.get(i).get(mid1).toString()));
                        angle.put("ed", Float.valueOf( inf__.get(k).get(ed1).toString()));
                        ((List<Map>) infs.get(i).get("angles")).add(angle);

                    } else {

                        List<Map> angles = new ArrayList<>();

                        Map<String, Float> angle = new HashMap();
                        angle.put("degree", (float) cobb);
                        angle.put("st", Float.valueOf( infs.get(i).get(st1).toString()));
                        angle.put("mid", Float.valueOf( infs.get(i).get(mid1).toString()));
                        angle.put("ed", Float.valueOf( inf__.get(k).get(ed1).toString()));
                        angles.add(angle);
                        infs.get(i).put("angles", angles);

                    }
                } catch (Exception e){
                    System.out.println(e);
                    continue;
                }

            }
        }
        return infs;
    }

    //计算得分的第二个版本：v2 2018 0821
    public float getScoreByInfos_v2(List<Map> infsmother,List<Map> infsson,boolean debug,String summays){
        Log.d("学生视频长度danceScore",""+infsson.size());
        Log.d("标准视频长度danceScore",""+infsmother.size());
        Map<String,Double> scoreinf = new HashMap();
        Map<String,Double>  scoreStd = new HashMap();
        //{ {1, 2}, {1, 5}, {2, 3}, {3, 4}, {5, 6}, {6, 7}, {1, 8}, {8, 9}, {9, 10}, {1, 11},
        //                {11, 12}, {12, 13}, {1, 0}, {0, 14}, {14, 16}, {0, 15}, {15, 17}}
        //身体各个部位的权重赋值
        //脖子-肩膀
        scoreStd.put("0.0",0.1);
        scoreStd.put("1.0",0.1);
        //肩膀到胳膊,手
        scoreStd.put("2.0",0.3);
        scoreStd.put("3.0",4.0);
        //
        scoreStd.put("4.0",0.3);
        scoreStd.put("5.0",4.0);
        //
        scoreStd.put("6.0",0.01);
        scoreStd.put("7.0",0.3);
        scoreStd.put("8.0",4.0);
        //
        scoreStd.put("9.0",0.05);
        scoreStd.put("10.0",0.5);
        scoreStd.put("11.0",4.0);
        //
        scoreStd.put("12.0",0.1);
        scoreStd.put("13.0",0.1);
        scoreStd.put("14.0",0.05);
        scoreStd.put("15.0",0.05);
        scoreStd.put("16.0",0.01);
        scoreStd.put("17.0",0.01);





        if (infsmother.size()>0 && infsson.size()>0){
            for (int i=0;i<infsmother.size();i++){
                Map inf =infsmother.get(i);
                int sign = 0;
                for(int j = 0;j<infsson.size();j++){
                    if ( Math.abs(Float.valueOf(infsson.get(j).get("pair_ord").toString())- Float.valueOf(inf.get("pair_ord").toString()))<0.8){
                        //todo:根据不同的部位给与不同的权重得分
                        if (inf.containsKey("angles")) {
                            Log.d("没有缺失的部位",""+inf.get("pair_ord")+""+infsson.get(j).get("pair_ord"));

                            sign = 1;
                        }
                    }
                }

                if (sign < 0.8){
                    if (inf.containsKey("angles") ){



                            if((inf.get("pair_ord").toString() == "3.0") || inf.get("pair_ord").toString()== "3"){
                                scoreinf.put( inf.get("pair_ord").toString() , (double)30);
                                Log.d("有一个手部位子视频没有角度不参与计算", "" + inf.get("pair_ord")+ "30分");

                            }
                            else if ((inf.get("pair_ord").toString() == "5.0") || inf.get("pair_ord").toString()== "5"){
                            scoreinf.put( inf.get("pair_ord").toString() , (double)30);
                            Log.d("有一个右手部位子视频没有角度不参与计算", "" + inf.get("pair_ord")+ "30分");

                        }
                        else if((inf.get("pair_ord").toString() == "11.0") || inf.get("pair_ord").toString()== "11"){
                                scoreinf.put( inf.get("pair_ord").toString() , (double)30);
                                Log.d("有一个左脚部位子视频没有角度不参与计算", "" + inf.get("pair_ord")+ "30分");

                            }
                            else if((inf.get("pair_ord").toString() == "8.0") || inf.get("pair_ord").toString()== "8"){
                                scoreinf.put( inf.get("pair_ord").toString() , (double)30);
                                Log.d("有一个右脚部位子视频没有角度不参与计算", "" + inf.get("pair_ord")+ "30分");

                            }
                            else{
                            scoreinf.put( inf.get("pair_ord").toString() , (double)5);

                            Log.d("有一个部位子视频没有角度不参与计算", "" + inf.get("pair_ord")+ "30分");}

                        }
                    }
                    Log.d("缺失一个部位",""+inf);
                    Log.d("缺失第几个部位",""+inf.get("pair_ord"));
                }
                Log.d("缺失部位的得分","scoreinf"+scoreinf);}

            for (int i=0;i<infsson.size();i++){
                Map inf =infsson.get(i);
                List<Map<String,Map>> inf_m = new ArrayList();

                for(int j = 0;j<infsmother.size();j++){
                    if ( Math.abs(Float.valueOf(infsmother.get(j).get("pair_ord").toString())- Float.valueOf(infsson.get(i).get("pair_ord").toString()))<0.8){
                        //todo:根据不同的部位给与不同的权重得分
                        inf_m.add(infsmother.get(j));
                    }
                }
                Log.d("相同部位的得分","inf_m"+inf_m);
                if (inf_m.size()>0){
                    for (int z=0;z<inf_m.size();z++){
                        Map infm_ = inf_m.get(z);
                        if (infm_.containsKey("angles") && inf.containsKey("angles")){




                                //Log.d("角度得分",""+angle_m);

                                    double score = 0;
                                    int degree_ = (int) Math.abs(Float.valueOf(infm_.get("angles").toString())-Float.valueOf(inf.get("angles").toString()));
                                    for (int zk =0;zk<SCORE_RELU.size();zk++){
                                        if (SCORE_RELU.get(zk).get("st")<= degree_ && SCORE_RELU.get(zk).get("ed")>degree_){
                                            score =  Double.valueOf(SCORE_RELU.get(zk).get("score").toString());
                                            scoreinf.put(inf.get("pair_ord").toString(),score);
                                            Log.d("相同部位的得分","scoreinf"+scoreinf);
                                        }
                                    }



                            }
                        }

                    }
                }










        Log.d("danceScore","20180614");
        Log.d("得分长度danceScore",""+scoreinf.size());
        Log.d("得分danceScore",""+scoreinf);
        Log.d("标准视频长度danceScore",""+infsmother.size());
        Log.d("标准视频danceScore",""+infsmother);
        Log.d("学生视频长度danceScore",""+infsson.size());
        Log.d("学生视频danceScore",""+infsson);

        if (scoreinf.size()>0){
            float scores = 0;
            double allscore = 0;
            double num = 0;
            if (summays=="avg"){

                Iterator iter = scoreinf.entrySet().iterator();
                while (iter.hasNext()){
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = String.valueOf(entry.getKey());
                    String key2 = key;
                    if (key.contains(".")){

                    }
                    else{

                        key = key.concat(String.valueOf('.'));
                        key = key.concat(String.valueOf('0'));

                    }
                    if (scoreStd.containsKey(key)){
                        double ratio = Double.valueOf(scoreStd.get(key));
                        double score = Double.valueOf(scoreinf.get(key2));

                        allscore = allscore+ ratio*score;
                        num += ratio;


                    }
                    else{
                        continue;
                    }




                }
                Log.d("计算得分：",""+allscore+" "+num);
                scores = (float)(allscore/num);
                return scores;

            }
            else{
                return -1;
            }


        }
        else{

            return -1;}
    }

    //计算得分

    public float getScoreByInfos(List<Map> infsmother,List<Map> infsson,boolean debug,String summays){
        Log.d("学生视频长度danceScore",""+infsson.size());
        Log.d("标准视频长度danceScore",""+infsmother.size());
        List<ArrayList<Float>> list1 = new ArrayList<>();
        List<Float> list1_1 = new ArrayList<>();
        List<Float> list1_2 = new ArrayList<>();
        List<Float> list1_3 = new ArrayList<>();
        List<Float> list1_4 = new ArrayList<>();
        List<Float> list1_5 = new ArrayList<>();
        List<Float> list1_6 = new ArrayList<>();
        List<Float> list1_7 = new ArrayList<>();
        List<Float> list1_8 = new ArrayList<>();
        List<Float> list1_9 = new ArrayList<>();
        List<Float> list1_10 = new ArrayList<>();
        List<Float> list1_11 = new ArrayList<>();
        List<Float> list1_12 = new ArrayList<>();
        List<Float> list1_13 = new ArrayList<>();
        List<Float> list1_14 = new ArrayList<>();
        List<Float> list1_15 = new ArrayList<>();
        List<Float> list1_16 = new ArrayList<>();
        List<Float> list1_17 = new ArrayList<>();
        List<Float> list1_18 = new ArrayList<>();
        List<Float> list1_19 = new ArrayList<>();
        List<Float> list1_20 = new ArrayList<>();
        List<Float> list1_21 = new ArrayList<>();
        List<Float> list1_22 = new ArrayList<>();
        List<Float> list1_23 = new ArrayList<>();
        List<Float> list1_24 = new ArrayList<>();
        List<Float> list1_25 = new ArrayList<>();
        List<Float> list1_26 = new ArrayList<>();
        List<Float> list1_27 = new ArrayList<>();
        List<Float> list1_28 = new ArrayList<>();
        List<Float> list1_29 = new ArrayList<>();
        List<Float> list1_30 = new ArrayList<>();
        List<Float> list1_31 = new ArrayList<>();
        List<Float> list1_32 = new ArrayList<>();


//        list1_1.add((float) 11);
//        list1_1.add((float) 12);
//        list1_1.add((float) 13);
//        list1_2.add((float) 8);
//        list1_2.add((float) 9);
//        list1_2.add((float) 10);
        list1_3.add((float) 1);
        list1_3.add((float) 2);
        list1_3.add((float) 3);
        list1_4.add((float) 2);
        list1_4.add((float) 3);
        list1_4.add((float) 4);
        list1_5.add((float) 1);
        list1_5.add((float) 5);
        list1_5.add((float) 6);
        list1_6.add((float) 5);
        list1_6.add((float) 6);
        list1_6.add((float) 7);
        list1_7.add((float) 2);
        list1_7.add((float) 8);
        list1_7.add((float) 9);
        list1_8.add((float) 2);
        list1_8.add((float) 9);
        list1_8.add((float) 10);
        list1_9.add((float) 2);
        list1_9.add((float) 11);
        list1_9.add((float) 12);
        list1_10.add((float) 2);
        list1_10.add((float) 12);
        list1_10.add((float) 13);
        list1_11.add((float) 3);
        list1_11.add((float) 9);
        list1_11.add((float) 10);
        list1_12.add((float) 3);
        list1_12.add((float) 12);
        list1_12.add((float) 13);
        list1_13.add((float) 3);
        list1_13.add((float) 8);
        list1_13.add((float) 9);
        list1_14.add((float) 3);
        list1_14.add((float) 11);
        list1_14.add((float) 12);
        list1_15.add((float) 3);
        list1_15.add((float) 12);
        list1_15.add((float) 13);
        list1_16.add((float) 4);
        list1_16.add((float) 8);
        list1_16.add((float) 9);
        list1_17.add((float) 4);
        list1_17.add((float) 9);
        list1_17.add((float) 10);
        list1_18.add((float) 4);
        list1_18.add((float) 11);
        list1_18.add((float) 12);
        list1_19.add((float) 4);
        list1_19.add((float) 12);
        list1_19.add((float) 13);
        list1_20.add((float) 5);
        list1_20.add((float) 8);
        list1_20.add((float) 9);
        list1_21.add((float) 5);
        list1_21.add((float) 9);
        list1_21.add((float) 10);
        list1_22.add((float) 5);
        list1_22.add((float) 11);
        list1_22.add((float) 12);
        list1_23.add((float) 5);
        list1_23.add((float) 12);
        list1_23.add((float) 13);
        list1_24.add((float) 6);
        list1_24.add((float) 9);
        list1_24.add((float) 10);
        list1_25.add((float) 6);
        list1_25.add((float) 12);
        list1_25.add((float) 13);
        list1_26.add((float) 6);
        list1_26.add((float) 8);
        list1_26.add((float) 9);
        list1_27.add((float) 6);
        list1_27.add((float) 11);
        list1_27.add((float) 12);
        list1_28.add((float) 6);
        list1_28.add((float) 12);
        list1_28.add((float) 13);
        list1_29.add((float) 7);
        list1_29.add((float) 8);
        list1_29.add((float) 9);
        list1_30.add((float) 7);
        list1_30.add((float) 9);
        list1_30.add((float) 10);
        list1_31.add((float) 7);
        list1_31.add((float) 11);
        list1_31.add((float) 12);
        list1_32.add((float) 7);
        list1_32.add((float) 12);
        list1_32.add((float) 13);
//        list1.add((ArrayList<Float>) list1_1);
//        list1.add((ArrayList<Float>) list1_2);
        list1.add((ArrayList<Float>) list1_3);
        list1.add((ArrayList<Float>) list1_4);
        list1.add((ArrayList<Float>) list1_5);
        list1.add((ArrayList<Float>) list1_6);
        list1.add((ArrayList<Float>) list1_7);
        list1.add((ArrayList<Float>) list1_8);
        list1.add((ArrayList<Float>) list1_9);
        list1.add((ArrayList<Float>) list1_10);
        list1.add((ArrayList<Float>) list1_11);
        list1.add((ArrayList<Float>) list1_12);
        list1.add((ArrayList<Float>) list1_13);
        list1.add((ArrayList<Float>) list1_14);
        list1.add((ArrayList<Float>) list1_15);
        list1.add((ArrayList<Float>) list1_16);
        list1.add((ArrayList<Float>) list1_17);
        list1.add((ArrayList<Float>) list1_18);
        list1.add((ArrayList<Float>) list1_19);
        list1.add((ArrayList<Float>) list1_20);
        list1.add((ArrayList<Float>) list1_21);
        list1.add((ArrayList<Float>) list1_22);
        list1.add((ArrayList<Float>) list1_23);
        list1.add((ArrayList<Float>) list1_24);
        list1.add((ArrayList<Float>) list1_25);
        list1.add((ArrayList<Float>) list1_26);
        list1.add((ArrayList<Float>) list1_27);
        list1.add((ArrayList<Float>) list1_28);
        list1.add((ArrayList<Float>) list1_29);
        list1.add((ArrayList<Float>) list1_30);
        list1.add((ArrayList<Float>) list1_31);
        list1.add((ArrayList<Float>) list1_32);

        List<ArrayList<Float>> list2 = new ArrayList<>();
        List<Float> list2_1 = new ArrayList<>();
        List<Float> list2_2 = new ArrayList<>();
        List<Float> list2_3 = new ArrayList<>();
        List<Float> list2_4 = new ArrayList<>();
        List<Float> list2_5 = new ArrayList<>();
        List<Float> list2_6 = new ArrayList<>();

        list2_1.add((float) 1);
        list2_1.add((float) 11);
        list2_1.add((float) 12);
        list2_2.add((float) 1);
        list2_2.add((float) 8);
        list2_2.add((float) 9);
        list2_5.add((float) 8);
        list2_5.add((float) 9);
        list2_5.add((float) 10);
        list2_6.add((float) 11);
        list2_6.add((float) 12);
        list2_6.add((float) 13);


        list2.add((ArrayList<Float>) list2_1);
        list2.add((ArrayList<Float>) list2_2);
        list2.add((ArrayList<Float>) list2_5);
        list2.add((ArrayList<Float>) list2_6);

        List<ArrayList<Float>> list3 = new ArrayList<>();
        list2_3.add((float) 0);
        list2_3.add((float) 1);
        list2_3.add((float) 2);
        list2_4.add((float) 0);
        list2_4.add((float) 1);
        list2_4.add((float) 5);
        list3.add((ArrayList<Float>) list2_3);
        list3.add((ArrayList<Float>) list2_4);

        Map scoreinf = new HashMap();
        if (infsmother.size()>0 && infsson.size()>0){
            for (int i=0;i<infsmother.size();i++){
                Map inf =infsmother.get(i);
                int sign = 0;
                for(int j = 0;j<infsson.size();j++){
                    if ( Math.abs(Float.valueOf(infsson.get(j).get("pair_ord").toString())- Float.valueOf(inf.get("pair_ord").toString()))<0.8){
                        //todo:根据不同的部位给与不同的权重得分
                        if (inf.containsKey("angles")) {
                            Log.d("没有缺失的部位",""+inf.get("pair_ord")+""+infsson.get(j).get("pair_ord"));

                            sign = 1;
                        }
                    }
                }

            if (sign < 0.8){
                if (inf.containsKey("angles") ){

                    for (int xz = 0; xz < ((List) inf.get("angles")).size(); xz++) {


                        //scoreinf.put( (Map)((List)(inf.get("angles"))).get(xz) , (float)5);

                        Log.d("有一个部位子视频没有角度", "" + ((List)(inf.get("angles"))).get(xz) + "30分"+xz);
                    }
                }
                Log.d("缺失一个部位",""+inf);
                Log.d("缺失第几个部位",""+inf.get("pair_ord"));
        }
                Log.d("缺失部位的得分","scoreinf"+scoreinf);}

            for (int i=0;i<infsson.size();i++){
                Map inf =infsson.get(i);
                List<Map<String,Map>> inf_m = new ArrayList();

                for(int j = 0;j<infsmother.size();j++){
                    if ( Math.abs(Float.valueOf(infsmother.get(j).get("pair_ord").toString())- Float.valueOf(infsson.get(i).get("pair_ord").toString()))<0.8){
                        //todo:根据不同的部位给与不同的权重得分
                        inf_m.add(infsmother.get(j));
                    }
                }
                Log.d("相同部位的得分","inf_m"+inf_m);
                if (inf_m.size()>0){
                    for (int z=0;z<inf_m.size();z++){
                        Map infm_ = inf_m.get(z);
                        if (infm_.containsKey("angles") && inf.containsKey("angles")){
                            for (int zx=0;zx<((List)inf.get("angles")).size();zx++){
                                Map angle_son = (Map)((List)(inf.get("angles"))).get(zx);
                                List <Map> angle_m = new ArrayList<>();
                                for (int yx =0;yx<((List)(infm_.get("angles"))).size();yx++) {
                                    Map x = (Map)((List)infm_.get("angles")).get(yx);
                                    if (Math.abs(Float.valueOf(x.get("mid").toString()) - Float.valueOf(angle_son.get("mid").toString()))<0.8 &&
                                            Math.abs(Float.valueOf(x.get("st").toString()) - Float.valueOf(angle_son.get("st").toString()))<0.8 &&
                                            Math.abs(Float.valueOf(x.get("ed").toString()) - Float.valueOf(angle_son.get("ed").toString()))<0.8) {
                                        angle_m.add(x);
                                    }
                                }
                                //Log.d("角度得分",""+angle_m);
                                if (angle_m.size()>0){
                                    float score = 0;
                                    int degree_ = (int) Math.abs(Float.valueOf(angle_m.get(0).get("degree").toString())-Float.valueOf(angle_son.get("degree").toString()));
                                    int temp1 = scoreinf.size();
                                    for (int zk =0;zk<SCORE_RELU.size();zk++){
                                        if (SCORE_RELU.get(zk).get("st")<= degree_ && SCORE_RELU.get(zk).get("ed")>degree_){
                                            score =  Float.valueOf(SCORE_RELU.get(zk).get("score").toString());
                                            scoreinf.put(angle_son,score);
                                        }
                                    }
                                    int temp = scoreinf.size();
                                    if (temp1==temp){
                                        Log.d("无新增得分",""+temp+"");
                                    }
                                }
//                                else{
//                                    //scoreinf.put((Map)((List)(inf.get("angles"))).get(0),70);
//                                    System.out.println("母模板中有个没有角度");
//                                }

                            }
                        }
//                        else{
//                            if (infm_.containsKey("angles")){
//                                //scoreinf.put((Map)((List)(infm_.get("angles"))).get(0),50);
//                                continue;
//                            }
//                            System.out.println("子母模板中至少一个没有识别到角度");
//                        }
                    }
                }




            }
        }





        Log.d("danceScore","20180614");
        Log.d("得分长度danceScore",""+scoreinf.size());
        Log.d("得分danceScore",""+scoreinf);
        Log.d("标准视频长度danceScore",""+infsmother.size());
        Log.d("标准视频danceScore",""+infsmother);
        Log.d("学生视频长度danceScore",""+infsson.size());
        Log.d("学生视频danceScore",""+infsson);

        if (scoreinf.size()>0){
            float scores = 0;
            float allscore = 0;
            float num = 0;
            if (summays=="avg"){

                Iterator iter = scoreinf.entrySet().iterator();
                while (iter.hasNext()){
                    Map.Entry entry = (Map.Entry) iter.next();
                    Map key = (Map) entry.getKey();
                    if (Math.abs(Float.valueOf(key.get("st").toString())-Float.valueOf(key.get("ed").toString()))>=0.8){

                        for (int z=0;z<list1.size();z++){

                            if ((list1.get(z).contains(Float.valueOf(key.get("st").toString()))) &&(list1.get(z).contains(Float.valueOf(key.get("mid").toString()))) && (list1.get(z).contains(Float.valueOf(key.get("ed").toString())))){
                                float val = Float.valueOf( entry.getValue().toString());
                                Log.e("debug:Score100%",key.get("st")+"\t"+key.get("mid")+"\t"+key.get("ed")+"\t"+key.get("degree")+"\t"+"Score:"+val);
                                if (Math.abs(val - 5.0f )<0.1){
                                    val += 5*val;
                                    Log.d("经常预测不到的部位,又重要的部位",""+"");
                                }

                                allscore +=val;
                                num += 1;
                            }
                        }
                        for (int z=0;z<list2.size();z++){
                            if ((list2.get(z).contains(Float.valueOf(key.get("st").toString()))) &&(list2.get(z).contains(Float.valueOf(key.get("mid").toString()))) && (list2.get(z).contains(Float.valueOf(key.get("ed").toString())))){
                                float val = Float.valueOf( entry.getValue().toString());
                                Log.e("debug:Score80%",key.get("st")+"\t"+key.get("mid")+"\t"+key.get("ed")+"\t"+key.get("degree")+"\t"+"Score:"+val);
                                allscore +=0.18*val*0.95;
                                num += 0.18;
                            }
                        }
                        for (int z=0;z<list3.size();z++){
                            if ((list3.get(z).contains(Float.valueOf(key.get("st").toString()))) &&(list3.get(z).contains(Float.valueOf(key.get("mid").toString()))) && (list3.get(z).contains(Float.valueOf(key.get("ed").toString())))){
                                float val = Float.valueOf( entry.getValue().toString());
                                allscore +=0.03*val;
                                Log.e("debug:Score30%",key.get("st")+"\t"+key.get("mid")+"\t"+key.get("ed")+"\t"+key.get("degree")+"\t"+"Score:"+val);
                                num += 0.03;
                            }
                        }
                        float val = Float.valueOf( entry.getValue().toString());
                        allscore += 0.001*val*0.99;
                        num += 0.001;
                        Log.e("debug:Score1%",key.get("st")+"\t"+key.get("mid")+"\t"+key.get("ed")+"\t"+key.get("degree")+"\t"+"Score:"+val);

                    }
                    else{
                        continue;
                    }




                }
                Log.d("计算得分：",""+allscore+" "+num);
                scores = allscore/num;
                return scores;

            }
            else{
                return -1;
            }


        }
        else{

        return -1;}
    }

}
