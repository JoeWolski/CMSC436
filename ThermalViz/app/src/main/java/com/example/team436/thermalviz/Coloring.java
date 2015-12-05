package com.example.team436.thermalviz;

import android.util.Log;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import com.example.team436.thermalviz.Point;

public class Coloring {
    //Assuming topleft is (0,0)
    final int height = 480; //y
    final int width = 640; //x

    //2D array for temperature (row , column) (y,x)
    //0.0 by default... contructor sets the intial to -300.0
    float[][] temperatures = new float[height][width];

    //O = highest accuracy...increases as it gets farther way from initial point
    //This will keep the spread from overwriting values that are more accurate
    //Initial value is 5000
    int[][] certainity  = new int[height][width];


    //2D array for holding points
    //0 = no point there, 1 = point there
    int[][] points = new int[height][width];

    //Queue for points
    LinkedList<Point> queue = new LinkedList<Point>();

    //Average Temperature
    float average = 0;

    public Coloring(){
        //initializing temperatures to -300.0
        for(int i = 0; i < 480; i++) {
            for(int j = 0; j < 640; j++) {
                temperatures[i][j] = -300;
            }
        }
        //Arrays.fill(temperatures, (float) -300.0);

        //intializing certainity factor
        for(int i = 0; i < 480; i++) {
            for(int j = 0; j < 640; j++) {
                certainity[i][j] = 5000;
            }
        }
        //Arrays.fill(certainity, 5000);

    }

    public void collectTemp(float[][] temp){
        int count = 0;
        for(int y=0; y < height; y++){
            for(int x=0; x < height; x++){
                if(temp[y][x] > -300.0){
                    average = average+ temp[y][x];
                    count = count + 1;
                    Point newpoint = new Point(x,y,temp[y][x]);
                    queue.add(newpoint);
                }
            }
        }

        average = (float) average/(float)count;
    }

    public void run() { //post processing

        while (queue.peek() != null){      //until queue is empty
            Point next = queue.remove();

            float takenTemp = next.temperature;
            int takenX = next.x;
            int takenY = next.y;

            temperatures[takenY][takenX] = takenTemp;
            points[takenY][takenX] = 1;
            certainity[takenY][takenX] = 0;
            int pointsInRange = rangedPoints(takenY, takenX);

            int range = 50 - (2 * (pointsInRange - 1));

            //Cell averaging all the points within range of initial point
            for (int i = 1; i <= range; i++) {
                //check if in bound
                boolean lowerX = true;
                boolean upperX = true;
                if(takenX - i < 0){ lowerX = false;}
                if(takenX + i >= width){ upperX = false;}

                boolean lowerY = true;
                boolean upperY = true;
                if(takenY - i <0){ lowerY =false;}
                if(takenY + i >= height){upperY = false;}

                for(int j = 0; j <= i; i++){ //fills spread of right and left spaces
                    boolean upper = true; //for y+j is in boundary
                    boolean lower = true;
                    if(takenY - j <0){ upper =false;}
                    if(takenY + j >= height){lower = false;}

                    if(upperX){
                        if(upper && i < certainity[takenY+j][takenX+i]){
                            cellAverage(takenY+j,takenX+i);
                            certainity[takenY+j][takenX+i] = i;
                        }
                        if(lower && i < certainity[takenY-j][takenX+i]){
                            cellAverage(takenY-j,takenX+i);
                            certainity[takenY-j][takenX+i] = i;
                        }
                    }
                    if(lowerX){
                        if(upper && i < certainity[takenY+j][takenX-i]){
                            cellAverage(takenY+j,takenX-i);
                            certainity[takenY+j][takenX-i] = i;
                        }
                        if(lower && i < certainity[takenY-j][takenX-i]){
                            cellAverage(takenY-j,takenX-i);
                            certainity[takenY+j][takenX-i] = i;
                        }
                    }
                }

                for(int k = 0; k <= i-1; i++){ //do not need to check corner again, fills top and bottom
                    boolean upper = true; //for y+j is in boundary
                    boolean lower = true;
                    if(takenX - k <0){ upper =false;}
                    if(takenX + k >= width){lower = false;}

                    if(upperY){
                        if(upper && i < certainity[takenY+i][takenX+k]){
                            cellAverage(takenY+i,takenX+k);
                            certainity[takenY+i][takenX+k] = i;
                        }
                        if(lower && i < certainity[takenY+i][takenX-k]){
                            cellAverage(takenY+i,takenX-k);
                            certainity[takenY+i][takenX-k] = i;
                        }
                    }
                    if(lowerY){
                        if(upper && i < certainity[takenY-i][takenX+k]){
                            cellAverage(takenY-i,takenX+k);
                            certainity[takenY-i][takenX+k] = i;

                        }
                        if(lower && i < certainity[takenY-i][takenX+k]){
                            cellAverage(takenY-i,takenX-k);
                            certainity[takenY-i][takenX+k] = i;
                        }
                    }
                }
            }

        }

        //fill in remaining space with average
        for(int y=0; y < height; y++){
            for(int x=0; x < height; x++){
                if(temperatures[y][x] <= -299.0){
                    temperatures[y][x] = average;
                }
            }
        }
    }

    //Computes the average based on temperature around it
    private void cellAverage(int y, int x){
        int count = 0;
        float total = 0.0f;
        boolean lowerX = true;
        boolean upperX = true;
        if(x - 1 < 0){ lowerX = false;}
        if(x + 1 >= width){ upperX = false;}

        boolean lowerY = true;
        boolean upperY = true;
        if(y - 1 <0){ lowerY =false;}
        if(y + 1 >= height){upperY = false;}


        //All these if statements are checking the 8 pixel around..so 8 if statements
        if(lowerX){
            if(temperatures[y][x-1] != 0){
                total += temperatures[y][x-1];
                count++;
            }
            if(lowerY){
                if(temperatures[y-1][x-1] != 0){
                    total += temperatures[y-1][x-1];
                    count++;
                }
            }

            if(upperY){
                if(temperatures[y+1][x-1] != 0){
                    total += temperatures[y+1][x-1];
                    count++;
                }
            }
        }

        if(upperX){
            if(temperatures[y][x+1] != 0){
                total += temperatures[y][x+1];
                count++;
            }
            if(lowerY){
                if(temperatures[y-1][x+1] != 0){
                    total += temperatures[y-1][x+1];
                    count++;
                }
            }

            if(upperY){
                if(temperatures[y+1][x+1] != 0){
                    total += temperatures[y+1][x+1];
                    count++;
                }
            }
        }

        if(lowerY){
            if(temperatures[y-1][x] != 0){
                total += temperatures[y-1][x];
                count++;
            }
        }

        if(upperY){
            if(temperatures[y+1][x] != 0){
                total += temperatures[y+1][x];
                count++;
            }
        }

        if(count > 0){
            temperatures[y][x] = total/((float)count);
        }
    }

    //Returns the number of points within 25 pixels of (x,y)
    //Note: to make this simpler for now, it is a square area rather circle
    //Might change this in future....
    private int rangedPoints(int y, int x) {
        int count = 0;
        int rangeY;
        int rangeX;
        int startX;
        int startY;

        //checking if it hits any boundary
        if (y - 25 < 0) { //hits upper
            rangeY = y + 25;
            startY = 0;
        } else if (y + 25 >= height) { //hits lower
            rangeY = height - 1 - y + 25;
            startY = y - 25;
        } else {
            rangeY = 50;
            startY = y - 25;
        }

        if (x - 25 < 0) { //hits left
            rangeX = x + 25;
            startX = 0;
        } else if (x + 25 >= width){//hits right
            rangeX = width -1 -x +25;
            startX = x-25;
        }else{
            rangeX = 50;
            startX = x - 25;
        }


        //Find points
        Log.v("THING", Integer.toString(startX + rangeX - 1));
        Log.v("THING", Integer.toString(startY + rangeY - 1));
        for(int i = startX; i < startX + rangeX - 1; i++){
            for(int j = startY; i < startY+ rangeY - 1; j++){
                if(points[j][i] == 1){
                    count++;
                }
            }
        }

        return count;
    }

}