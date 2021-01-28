package com.example.recscore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.max;


public class HumanPoseGet  {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static final String MODEL_FILE = "file:///android_asset/graph_opt.pb";
    private static final String INPUT_NAME = "image:0";
    private static final String OUTPUT_NAME = "Openpose/concat_stage7:0";
    private static final int NUM_CLASSES = 57;  //样本集的类别数量
    private static final int HEIGHT = 368;
    private static final int WIDTH = 432;
    private static final int CHANNEL = 3;    //输入图片的通道数：RGB

    private int logit;   //输出数组中最大值的下标
    private float[] inputs_data = new float[WIDTH * HEIGHT * CHANNEL];
    private float[] outputs_data = new float[WIDTH / 8 * HEIGHT / 8 * NUM_CLASSES];
    //private TensorFlowInferenceInterface inferenceInterface;


    public TensorFlowInferenceInterface getModelInterface(Context context){
        TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
        return inferenceInterface;
    }

    public List getHumans  (Bitmap bitmap,TensorFlowInferenceInterface inferenceInterface) {
        //inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
        //System.out.println(inferenceInterface.graph());
        bitmap = stylizeImage(bitmap);
        Trace.beginSection("feed");
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long startTime = System.currentTimeMillis();
        inferenceInterface.feed(INPUT_NAME, inputs_data, 1,HEIGHT, WIDTH , CHANNEL);

        Trace.endSection();
        Trace.beginSection("run");
        inferenceInterface.run(new String[]{OUTPUT_NAME});
        Trace.endSection();
        Trace.beginSection("fetch");

        inferenceInterface.fetch(OUTPUT_NAME, outputs_data);

        Trace.endSection();
        logit = 0;
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        double NMS_Threshold = 0.15;
        NMS_Threshold = (float) NMS_Threshold;
        int PAF_Count_Threshold = 5;
        int Part_Count_Threshold = 4;
        float Part_Score_Threshold = (float) 4.5;


        float [][][] outputs = new float[HEIGHT/8][WIDTH/8][NUM_CLASSES];
        float[][][] temppafMat = new float[HEIGHT / 8][WIDTH / 8][NUM_CLASSES - 19];
        float[][][] tempheatMats = new float[HEIGHT / 8][WIDTH / 8][19];
        int num = 0;
        float[][] resized_cntMat = new float[HEIGHT / 8][WIDTH / 8];
        for (int i = 0; i < resized_cntMat.length; i++) {
            for (int j = 0; j < resized_cntMat[i].length; j++) {
                resized_cntMat[i][j] = (float) Math.pow(10, -12)+1;
            }
        }
        for (int i = 0;i< outputs.length;i++){
            for(int j = 0;j<outputs[i].length;j++){
                for (int k = 0;k< outputs[i][j].length;k++){
                    outputs[i][j][k] = outputs_data[num];
                    if (k<tempheatMats[0][0].length)
                        tempheatMats[i][j][k] = Math.max(outputs[i][j][k],0);
                    else temppafMat[i][j][k-tempheatMats[0][0].length]=(float) (outputs[i][j][k]/(Math.log((double)resized_cntMat[i][j])+1));

                    num += 1;
                }
            }
        }


        float[][][] resizepafMat = new float[NUM_CLASSES - 19][HEIGHT / 8][WIDTH / 8];
        float[][][] resizeheatMat = new float[19][HEIGHT / 8][WIDTH / 8];
        resizepafMat = roallaxis(temppafMat, 2, 0);
        resizeheatMat = roallaxis(tempheatMats, 2, 0);



        float[][][] maxheatMat = new float[19][HEIGHT / 8][WIDTH / 8];
        List<List> coords1 = new ArrayList<>();
        int[][][] coords = new int[19][54*46][3];
        for (int i = 0;i<coords.length;i++){
            for (int j =0;j<coords[i].length;j++){
                for (int k =0;k<coords[i][j].length;k++){
                    coords[i][j][k] = -1;

                }
            }
        }
        int tempnum = 0;
        for (int i = 0; i < resizeheatMat.length; i++) {
            List<Integer> indexmap1 = new ArrayList<>();
            List<Integer> indexmap2 = new ArrayList<>();

            indexmap1.clear();
            indexmap2.clear();
            tempnum = 0;
            for (int j = 0; j < resizeheatMat[i].length; j++) {
                for (int k = 0; k < resizeheatMat[i][j].length; k++) {

                    List<Float> list1 = new ArrayList<Float>();
                    list1.clear();


                    for (int tj = j+2; tj >= j - 2; tj--) {
                        for (int tk = k+2; tk >= 0 && tk >= k - 2 ; tk--) {
                            if (tk<0 || tk >=resizeheatMat[i][j].length)
                                continue;


                            if (tj < 0 || tj>=resizeheatMat[i].length) {
                                continue;
                            }


                            list1.add(resizeheatMat[i][tj][tk]);

                        }


                    }

                    maxheatMat[i][j][k] = max(list1);
                    if (max(list1) > NMS_Threshold && max(list1) ==resizeheatMat[i][j][k] ){

                        coords[i][tempnum][0] = i;
                        coords[i][tempnum][1] = j;
                        coords[i][tempnum][2] = k;

                        indexmap1.add(j);
                        indexmap2.add(k);


                        tempnum++;

                    }



                }
            }
            coords1.add(indexmap1);
            coords1.add(indexmap2);
        }

        int[][] CocoPairs_ = new int[19][2];
        CocoPairs_[0][0] = 1;
        CocoPairs_[0][1] = 2;
        CocoPairs_[1][0] = 1;
        CocoPairs_[1][1] = 5;
        CocoPairs_[2][0] = 2;
        CocoPairs_[2][1] = 3;
        CocoPairs_[3][0] = 3;
        CocoPairs_[3][1] = 4;
        CocoPairs_[4][0] = 5;
        CocoPairs_[4][1] = 6;
        CocoPairs_[5][0] = 6;
        CocoPairs_[5][1] = 7;
        CocoPairs_[6][0] = 1;
        CocoPairs_[6][1] = 8;
        CocoPairs_[7][0] = 8;
        CocoPairs_[7][1] = 9;
        CocoPairs_[8][0] = 9;
        CocoPairs_[8][1] = 10;
        CocoPairs_[9][0] = 1;
        CocoPairs_[9][1] = 11;
        CocoPairs_[10][0] = 11;
        CocoPairs_[10][1] = 12;
        CocoPairs_[11][0] = 12;
        CocoPairs_[11][1] = 13;
        CocoPairs_[12][0] = 1;
        CocoPairs_[12][1] = 0;
        CocoPairs_[13][0] = 0;
        CocoPairs_[13][1] = 14;
        CocoPairs_[14][0] = 14;
        CocoPairs_[14][1] = 16;
        CocoPairs_[15][0] = 0;
        CocoPairs_[15][1] = 15;
        CocoPairs_[16][0] = 15;
        CocoPairs_[16][1] = 17;
        CocoPairs_[17][0] = 2;
        CocoPairs_[17][1] = 16;
        CocoPairs_[18][0] = 5;
        CocoPairs_[18][1] = 17;

        int[][] CocoPairsNetwork_ = new int[19][2];
        CocoPairsNetwork_[0][0] = 12;
        CocoPairsNetwork_[0][1] = 13;
        CocoPairsNetwork_[1][0] = 20;
        CocoPairsNetwork_[1][1] = 21;
        CocoPairsNetwork_[2][0] = 14;
        CocoPairsNetwork_[2][1] = 15;
        CocoPairsNetwork_[3][0] = 16;
        CocoPairsNetwork_[3][1] = 17;
        CocoPairsNetwork_[4][0] = 22;
        CocoPairsNetwork_[4][1] = 23;
        CocoPairsNetwork_[5][0] = 24;
        CocoPairsNetwork_[5][1] = 25;
        CocoPairsNetwork_[6][0] = 0;
        CocoPairsNetwork_[6][1] = 1;
        CocoPairsNetwork_[7][0] = 2;
        CocoPairsNetwork_[7][1] = 3;
        CocoPairsNetwork_[8][0] = 4;
        CocoPairsNetwork_[8][1] = 5;
        CocoPairsNetwork_[9][0] = 6;
        CocoPairsNetwork_[9][1] = 7;
        CocoPairsNetwork_[10][0] = 8;
        CocoPairsNetwork_[10][1] = 9;
        CocoPairsNetwork_[11][0] = 10;
        CocoPairsNetwork_[11][1] = 11;
        CocoPairsNetwork_[12][0] = 28;
        CocoPairsNetwork_[12][1] = 29;
        CocoPairsNetwork_[13][0] = 30;
        CocoPairsNetwork_[13][1] = 31;
        CocoPairsNetwork_[14][0] = 34;
        CocoPairsNetwork_[14][1] = 35;
        CocoPairsNetwork_[15][0] = 32;
        CocoPairsNetwork_[15][1] = 33;
        CocoPairsNetwork_[16][0] = 36;
        CocoPairsNetwork_[16][1] = 37;
        CocoPairsNetwork_[17][0] = 18;
        CocoPairsNetwork_[17][1] = 19;
        CocoPairsNetwork_[18][0] = 26;
        CocoPairsNetwork_[18][1] = 27;




        float [][] connections = new float [999][11];
        int[] index_array = new int[3];
        int indx1 = 0;
        for (int i = 0; i < CocoPairsNetwork_.length; i++) {
            //每一个关节点
            int paf_x_idx = CocoPairsNetwork_[i][0];
            int paf_y_idx = CocoPairsNetwork_[i][1];
            int part_idx1 = CocoPairs_[i][0];
            int part_idx2 = CocoPairs_[i][1];
            double rescale_x = 1.0 / maxheatMat[0][0].length;
            double rescale_y = 1.0 / maxheatMat[0].length;
            float[][] connection_temp = new float[999][11];
            float[][] connection = new float[1][connection_temp[0].length];
            List<Integer> coord_list1 = new ArrayList<>();
            List<Integer> coord_list11 = new ArrayList<>();
            List<Integer> coord_list2 = new ArrayList<>();
            List<Integer> coord_list22 = new ArrayList<>();
            coord_list1 = coords1.get(part_idx1*2);
            coord_list11 = coords1.get(part_idx1*2+1);
            coord_list2 = coords1.get(part_idx2*2);
            coord_list22 = coords1.get(part_idx2*2+1);
            float[][] paf_mat_x = resizepafMat[paf_x_idx];
            float[][] paf_mat_y = resizepafMat[paf_y_idx];
            int cnt = 0;
            int idx = 0;


            for (int j = 0; j < coord_list1.size(); j++) {
                int y1 = coord_list1.get(j);
                int x1 = coord_list11.get(j);

                for (int k = 0; k < coord_list22.size(); k++) {
                    int y2 = coord_list2.get(k);
                    int x2 = coord_list22.get(k);
                    float[] result = new float[2];
                    result = get_score(x1, y1, x2, y2, paf_mat_x, paf_mat_y);
                    float score = result[0];
                    int count = (int) result[1];
                    cnt += 1;
                    if ((count < PAF_Count_Threshold) || (score <= 0.0)) {
                        Log.d("部位小于一阈值:",""+part_idx1+" "+part_idx2+" "+score+x1+" "+y1+" "+x2+" "+y2);
                        continue;
                    } else {
                        connection_temp[idx][0] = score;
                        connection_temp[idx][1] = part_idx1;
                        connection_temp[idx][2] = part_idx2;
                        connection_temp[idx][3] = j;
                        connection_temp[idx][4] = k;
                        connection_temp[idx][5] = (float) (x1 * rescale_x);
                        connection_temp[idx][6] = (float) (y1 * rescale_y);
                        connection_temp[idx][7] = (float) (x2 * rescale_x);
                        connection_temp[idx][8] = (float) (y2 * rescale_y);
                        connection_temp[idx][9] = resizeheatMat[part_idx1][y1][x1];
                        connection_temp[idx][10] = resizeheatMat[part_idx2][y1][x2];
                        idx += 1;
                        Log.d("部位大于阈值:",""+part_idx1+" "+part_idx2+" "+score+x1+" "+y1+" "+x2+" "+y2);

                    }



                }

            }
            if (idx > 0) {
                Log.d("idx",""+idx);
                float[][] connection_temp_ = new float[idx][11];
                for (int z = 0; z < connection_temp_.length; z++) {
                    connection_temp_[z] = connection_temp[z];
                }
                connection_temp = connection_temp_;
                float[][] connection_ = new float[idx][connection_temp[0].length];
                int start = 0;
                int end = idx - 1;
                sort(connection_temp, 0, true, start, end);

                Set used_idx1 = new HashSet();
                Set used_idx2 = new HashSet();
                int indx = 0;
                for (int z = 0; z < connection_temp.length; z++) {
                    if (connection_temp[z][0] > 0) {
                        if (used_idx1.contains(connection_temp[z][3]) || used_idx2.contains(connection_temp[z][4])) {
                            continue;
                        } else {
                            used_idx1.add(connection_temp[z][3]);
                            used_idx2.add(connection_temp[z][4]);
                            connection_[indx] = connection_temp[z];
                            indx += 1;
                        }

                    }
                    else break;

                }
                if (indx > 0) {
                    int indx1_copy = 0;
                    float[][] connection_1 = new float[indx][11];
                    for (int z_ = 0;z_<connection_1.length;z_++){
                        connection_1[z_] = connection_[z_];


                    }

                    connection = connection_1;
                    int sign = 0;
                    for (int z_ = 0;z_<connection.length;z_++){
                        if (connection[z_][0] > 0){
                            connections[z_+indx1] = connection_[z_];
                            indx1_copy +=1;
                            sign = 1;

                        }
                    }
                    if (sign>0) indx1+=indx1_copy;
                }






            }




        }
        Log.d("indx", "indx"+connections[0][0]);
        if (indx1>0) {
            float[][] connections_ = new float[indx1][11];
            for (int i = 0; i < connections_.length; i++) {

                for (int j = 0; j < connections[i].length; j++) {
                    if (connections[i][0] > 0) {
                        connections_[i][j] = connections[i][j];


                    }


                }

            }
            connections = connections_;
        }







        List<Human> humans= new ArrayList<Human>();
        for (int i=0;i<connections.length;i++){
            //System.out.print(connections[i].length);
            if (connections[i][0] >0){
                float[][] tempcon = new float[1][11];
                tempcon[0] = connections[i];

                humans.add(new Human(tempcon));
                Log.d("关系处理", "indx1"+i);
            }

        }
        Log.d("human 几个",""+humans.size());
        System.out.print("人体各个部位");
        if (humans.size()> 0){
            Log.d("各个部位的描述",""+humans.get(0).__str__());
            Log.d("共识别几个部位",""+humans.get(0).part_count());}

        while (true){
            Human [] merge_items = new Human [2];
            outer : for (int i = 0;i<humans.size();i++){
                for (int j = 0;j<humans.size();j++){
                    if (i!=j){
                        Human k1 = humans.get(i);
                        Human k2 = humans.get(j);
                        if (k1.is_connected(k2)){
                            merge_items[0] = k1;
                            merge_items[1] = k2;
                            break outer;

                        }

                    }
                }
            }
            if (merge_items[0] != null)
            {
                merge_items[0].merge(merge_items[1]);
                humans.remove(merge_items[1]);
                merge_items[0] = null;
            }
            else
                break;
        }

        List<Human> humans_temp = new ArrayList<Human>();
        for (int i = 0;i<humans.size();i++){
            if (humans.get(i).part_count() >= PAF_Count_Threshold && humans.get(i).get_max_score()>=Part_Score_Threshold){
                humans_temp.add(humans.get(i));
            }
        }
        Log.d("humantemp 几个",""+humans_temp.size());
        humans = humans_temp;
        if (humans.size()>0){
            for (int i =0;i<humans.size();i++){
                Log.d("描述", ""+humans.get(i).__str__()+humans.get(i).part_count());
            }
        }
        long endTime = System.currentTimeMillis();    //获取结束时间
        Log.d("模型预测人物用时",""+(endTime-startTime));
        return humans;
    }
    public Bitmap drawhumans(List humans,Bitmap bitmap,double score){

        Bitmap bitmapd = draw_humans(bitmap,humans,false,score);

        return bitmapd;


    }
    public List<Map> getHumanAngle(List humans,Bitmap bitmap){
        long st = System.currentTimeMillis();
        List infs = getHumansInf(bitmap,humans);

        DanceScore worker = new DanceScore();
        List<Map> infs_angle = worker.getAngleByInfos((List<Map>) infs);
        long ed = System.currentTimeMillis();
        Log.d("获取关节角度用时",""+(ed-st));
        return infs_angle;

    }
    public Float getDanceScore(List<Map> infs_angle_teacher,List<Map> infs_angle_student){
        long st = System.currentTimeMillis();
        DanceScore worker = new DanceScore();
        float score = worker.getScoreByInfos(infs_angle_teacher,infs_angle_student,false,"avg");
        long ed = System.currentTimeMillis();
        Log.d("对比打分用时",""+(ed-st));
        return score;
    }




    private Bitmap stylizeImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) WIDTH) / width;
        float scaleHeight = ((float) HEIGHT) / height;

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);


        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, false);
        float[] pixels = new float[HEIGHT * WIDTH * CHANNEL];


        int k = 0;
        for (int x = 0; x < HEIGHT; x++) {
            for (int y = 0; y < WIDTH; y++) {
                final int val = resizedBitmap.getPixel(y, x);

                pixels[k * 3 + 2] = ((val >> 16) & 0xFF);
                pixels[k * 3 + 1] = ((val >> 8) & 0xFF);
                pixels[k * 3 ] = (val & 0xFF);
                k++;
            }


        }
        for (int i = 0; i < pixels.length; i++) {
            inputs_data[i] = (float) pixels[i];

        }
        return bitmap;
    }

    //将数组的指定的维度转到另一个维度 ,只针对三维数组
    // 实现与python numpy包中rollaxis类似的功能。如：a = np.ones((3,4,5,6)) np.rollaxis(a, 3, 1).shape ==>(3, 6, 4, 5)
    private float[][][] roallaxis(float[][][] array, int axis, int start) {
        int firstdim = array.length;
        int seconddim = array[0].length;
        int thirddim = array[0][0].length;
        //System.out.println(firstdim);

        float[][][] temparray = new float[thirddim][firstdim][seconddim];
        for (int i = 0; i < firstdim; i++) {
            for (int j = 0; j < seconddim; j++) {
                for (int k = 0; k < thirddim; k++) {
                    temparray[k][i][j] = array[i][j][k];
                }
            }
        }

        //System.out.println( temparray.length);
        return temparray;


    }

    //计算得分
    private float[] get_score(int x1, int y1, int x2, int y2, float[][] paf_mat_x, float[][] paf_mat_y) {
        int __num_inter = 10;
        float[] SCORE = new float[2];
        float __num_inter_f = (float) __num_inter;
        int dx = x2 - x1;
        int dy = y2 - y1;
        float normVec = (float) Math.sqrt(dx * dx + dy * dy);
        if (normVec < 0.0001) {
            SCORE[0] = (float) 0.0;
            SCORE[1] = (float) 0;
            return SCORE;

        }
        float vx = dx / normVec;
        float Local_PAF_Threshold= (float)0.2;
        float vy = dy / normVec;
        int[] xs = new int[__num_inter];
        int[] ys = new int[__num_inter];
        float[] pafXs = new float[__num_inter];
        float[] pafYs = new float[__num_inter];
        float[] local_scores = new float[__num_inter];
        int thidxs = 0;
        float scores = 0;
        for (int i = 0; i < __num_inter; i++) {
            xs[i] = (int) ((dx / __num_inter_f) * (i + 1)+x1 + 0.5);
            ys[i] = (int) ((dy / __num_inter_f) * (i + 1)+y1 + 0.5);
            pafXs[i] = 0;
            pafYs[i] = 0;
            pafXs[i] = paf_mat_x[ys[i]][xs[i]];
            pafYs[i] = paf_mat_y[ys[i]][xs[i]];
            local_scores[i] = pafXs[i] * vx +pafYs[i]*vy;
            if (local_scores[i]>Local_PAF_Threshold){
                thidxs+=1;
                scores += local_scores[i];
            }
        }
        SCORE[0] =scores;
        SCORE[1] = (float) thidxs;
        return SCORE;








    }

    //排序
    public void sort(float[][] array1,int index,boolean reverce,int low,int high) {

        int end = high;
        int start = low;
        if (end <= start)
            return ;
        float key = array1[low][index];
        float[] temp = array1[low];


        while (end > start) {
            if (reverce) {
                while (end > start && array1[end][index] < key)
                    end--;
                if (end > start) {
                    //float[] temp = array1[end];

                    array1[start++] = array1[end];
                    //start++;

                }
                //从前往后比较
                while (end > start && array1[start][index] >= key)//如果没有比关键值大的，比较下一个，直到有比关键值大的交换位置
                    start++;
                if (end > start) {

                    array1[end--] = array1[start];
                    //end --;

                }
                //此时第一次循环比较结束，关键值的位置已经确定了。左边的值都比关键值小，右边的值都比关键值大，但是两边的顺序还有可能是不一样的，进行下面的递归调用
            }



        }
        array1[start] = temp;

        //递归
        sort(array1,index,reverce,low,start-1);
        sort(array1,index,reverce,start+1 ,high);
    }
    //bitmap图像缩放的问题

    //存储文件
    public void save(String data,String filename){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        String SDCarePath= Environment.DIRECTORY_DCIM;
        File file = createFile(SDCarePath + filename);

        try {

            out = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //根据人物信息来画图
    public Bitmap draw_humans(Bitmap npimg,List<Human> humans,boolean imgcopy,double score){
        long st = System.currentTimeMillis();

        if (imgcopy){
            Bitmap npimg1 = npimg.copy(Bitmap.Config.ARGB_8888,true);
        }

        Bitmap npimg1 = npimg.copy(Bitmap.Config.ARGB_8888,true);
        int[][] CocoColors = new int[][]{{255, 0, 0}, {255, 85, 0}, {255, 170, 0}, {255, 255, 0}, {170, 255, 0}, {85, 255, 0}, {0, 255, 0},
                {0, 255, 85}, {0, 255, 170}, {0, 255, 255}, {0, 170, 255}, {0, 85, 255}, {0, 0, 255}, {85, 0, 255},
                {170, 0, 255}, {255, 0, 255}, {255, 0, 170}, {255, 0, 85}};
        int[][] CocoCairsRender = new int[][]{ {1, 2}, {1, 5}, {2, 3}, {3, 4}, {5, 6}, {6, 7}, {1, 8}, {8, 9}, {9, 10}, {1, 11},
                {11, 12}, {12, 13}, {1, 0}, {0, 14}, {14, 16}, {0, 15}, {15, 17}};
        int width = npimg.getWidth();
        int height = npimg.getHeight();
        Canvas canvas = new Canvas(npimg1);// 初始化画布绘制的图像到npimg上
        Paint photoPaint = new Paint(); // 建立画笔
        photoPaint.setAntiAlias(true);
        photoPaint.setColor(Color.rgb(255,255,255));
        photoPaint.setTextSize(50);
        //canvas.drawRect(new Rect(0, 0, width, height), photoPaint);



        //绘制点
        Map <Integer,List> centers = new HashMap<>();
        for (int i=0;i<humans.size();i++){
            for (int k=0;k<18;k++){
                if (humans.get(i).body_parts.containsKey((float)k)){
                    BodyPart bodypart =  humans.get(i).body_parts.get((float)k);
                    int centerx = 0;
                    int centery = 0;
                    List<Integer> center = new ArrayList<>();
                    center.clear();

                    centerx = (int)(bodypart.getX()*width + 0.5);
                    centery = (int)(bodypart.getY()*height + 0.5);
                    center.add(centerx);
                    center.add(centery);
                    centers.put(k,center);
                    //画中心点
                    photoPaint.setColor(Color.rgb(CocoColors[k][0],CocoColors[k][1],CocoColors[k][2]));
                    canvas.drawCircle(centerx,centery,3,photoPaint);
                }
            }
            for (int j = 0;j<CocoCairsRender.length;j++){
                if (humans.get(i).body_parts.containsKey((float)CocoCairsRender[j][0]) && humans.get(i).body_parts.containsKey((float)CocoCairsRender[j][1])){
                    photoPaint.setColor(Color.rgb(CocoColors[j][0],CocoColors[j][1],CocoColors[j][2]));
                    try {
                        int stx = (int) centers.get(CocoCairsRender[j][0]).get(0);
                        int sty = (int) centers.get(CocoCairsRender[j][0]).get(1);
                        int edx = (int) centers.get(CocoCairsRender[j][1]).get(0);
                        int edy = (int) centers.get(CocoCairsRender[j][1]).get(1);
                        canvas.drawLine(stx, sty, edx, edy, photoPaint);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        if (score > 0.1 ){
            canvas.drawText(String.valueOf(score), 100, 100, photoPaint);


        }
        canvas.drawBitmap(npimg1,0,0,photoPaint);




        //show.setImageBitmap(npimg1);
        long ed = System.currentTimeMillis();
        Log.d("绘制人物骨骼用时",""+(ed-st));

        return npimg1;
    }
    public List<Map<String,Integer>> getHumansInf(Bitmap npimg,List<Human> humans){
        long st = System.currentTimeMillis();
        int[][] CocoColors = new int[][]{{255, 0, 0}, {255, 85, 0}, {255, 170, 0}, {255, 255, 0}, {170, 255, 0}, {85, 255, 0}, {0, 255, 0},
                {0, 255, 85}, {0, 255, 170}, {0, 255, 255}, {0, 170, 255}, {0, 85, 255}, {0, 0, 255}, {85, 0, 255},
                {170, 0, 255}, {255, 0, 255}, {255, 0, 170}, {255, 0, 85},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},
                {0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}};
        int[][] CocoCairsRender = new int[][]{ {1, 2}, {1, 5}, {2, 3}, {3, 4}, {5, 6}, {6, 7}, {1, 8}, {8, 9}, {9, 10}, {1, 11},
                {11, 12}, {12, 13}, {1, 0}, {0, 14}, {14, 16}, {0, 15}, {15, 17},{2,8},{2,11},{5,8},{5,11},{2,9},{2,12},{5,9},{5,12},{4,10},{4,13},{7,10},{7,13},
                {3,8},{3,9},{3,11},{3,12},{3,13},{4,8},{4,12},{4,11},{4,12},{6,8},{6,9},{6,11},{6,12},{6,13},{3,10},{6,10},{7,8},{7,12},{7,11},{7,12}};
        int width = npimg.getWidth();
        int height = npimg.getHeight();
        //绘制点
        Map <Integer,List> centers = new HashMap<>();
        List<Map<String,Integer>> infolist = new ArrayList();
        for (int i=0;i<humans.size();i++){
            for (int k=0;k<50;k++){
                if (humans.get(i).body_parts.containsKey((float)k)){
                    BodyPart bodypart =  humans.get(i).body_parts.get((float)k);
                    int centerx = 0;
                    int centery = 0;
                    List<Integer> center = new ArrayList<>();
                    center.clear();

                    centerx = (int)(bodypart.getX()*width + 0.5);
                    centery = (int)(bodypart.getY()*height + 0.5);
                    center.add(centerx);
                    center.add(centery);
                    centers.put(k,center);
                }
            }
            for (int j = 0;j<CocoCairsRender.length;j++){
                if (humans.get(i).body_parts.containsKey((float)CocoCairsRender[j][0]) && humans.get(i).body_parts.containsKey((float)CocoCairsRender[j][1])){
                    try{
                    int stx = (int) centers.get(CocoCairsRender[j][0]).get(0);
                    int sty = (int) centers.get(CocoCairsRender[j][0]).get(1);
                    int edx = (int) centers.get(CocoCairsRender[j][1]).get(0);
                    int edy = (int) centers.get(CocoCairsRender[j][1]).get(1);
                    Map<String,Integer>  infos= new HashMap<>();
                    infos.clear();
                    infos.put("line_st_x",stx);
                    infos.put("line_st_y", sty);
                    infos.put("line_ed_x",edx);
                    infos.put("line_ed_y", edy);
                    infos.put("pair_ord",j);
                    infos.put("st", CocoCairsRender[j][0]);
                    infos.put("ed",CocoCairsRender[j][1]);
                    infos.put("color_r",CocoColors[j][0]);
                    infos.put("color_g",CocoColors[j][1]);
                    infos.put("color_b",CocoColors[j][2]);
                    infolist.add(infos);}
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }




        //show.setImageBitmap(npimg1);
        long ed = System.currentTimeMillis();
        Log.d("获取人物inf list用时:",""+(ed-st));


        return infolist;
    }
    public File createFile(String fileName) {
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}





