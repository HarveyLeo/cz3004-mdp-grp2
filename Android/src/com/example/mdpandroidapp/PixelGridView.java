package com.example.mdpandroidapp;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


public class PixelGridView extends ImageView
{
    private static int numColumns, numRows;
    private static int cellWidth, cellHeight;
    private Paint whitePaint = new Paint();
    private Paint blackPaint = new Paint();
    private Paint bluePaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint headPaint = new Paint();
    private boolean[][] cellChecked;
    private static MapDecoder md = new MapDecoder();

    

    public PixelGridView(Context context)
    {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        //blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        blackPaint.setColor(Color.BLACK);
        bluePaint.setColor(Color.BLUE);
        redPaint.setColor(Color.RED);
        greenPaint.setColor(Color.GREEN);
        headPaint.setColor(Color.MAGENTA);
    }

    public void setNumColumns(int numColumns)
    {
        this.numColumns = numColumns;
        calculateDimensions();
    }

    public int getNumColumns()
    {
        return numColumns;
    }

    public void setNumRows(int numRows)
    {
        this.numRows = numRows;
        calculateDimensions();
    }

    public int getNumRows()
    {
        return numRows;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions()
    {
        if (numColumns == 0 || numRows == 0)
            return;

        cellWidth = getWidth() / numColumns;
        cellHeight = getHeight() / numRows;


        cellChecked = new boolean[numColumns][numRows];

        invalidate();
    }
    
    public void updateRobotPosition(int xcoord, int ycoord, int orientation){
    	
    }
    
    public void updateDemoArenaMap(String obstacleMapDes){
    	
    }
    
    public void updateDemoRobotPos(String robotPos){
    	md.updateDemoRobotPos(robotPos);
    }
    
    public void clearMap(){
    	md.clearMapArray();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
    	//md.setSampleMap();
    	int[][] testMap = md.decodeMapDescriptor();
    	

        canvas.drawColor(Color.BLACK);

        if (numColumns == 0 || numRows == 0)
            return;

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < numColumns; i++)
        {
            for (int j = 0; j < numRows ; j++)
            {
                if (testMap[j][i] == 0)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, blackPaint);
                }
                if (testMap[j][i] == 1)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
                }
                if (testMap[j][i] == 2)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, redPaint);
                }
                if (testMap[j][i] == 3)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, bluePaint);
                }
                if (testMap[j][i] == 4)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, headPaint);
                }
            }
        }    

        for (int i = 1; i < numColumns; i++)
        {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, whitePaint);
        }

        for (int i = 1; i < numRows; i++)
        {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, whitePaint);
        }
        
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return true;

        int column = (int)(event.getX() / cellWidth);
        int row = (int)(event.getY() / cellHeight);

        cellChecked[column][row] = !cellChecked[column][row];
        invalidate();

        return true;
    }*/
}