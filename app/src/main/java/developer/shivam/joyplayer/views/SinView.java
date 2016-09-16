package developer.shivam.joyplayer.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SinView extends View {

    private Context mContext;
    private int deviceWidth = 0;
    private int x_coordinate = 0;
    private Paint waveColor;
    private int amplitude = 100;
    private int y = 0;
    private int top;
    private int bottom;
    private int center;
    private boolean toTop = true;

    public SinView(Context context) {
        super(context);
        init(context, null);
    }

    public SinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        waveColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        waveColor.setColor(Color.BLACK);
        waveColor.setStrokeWidth(5);
        waveColor.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 'deviceWidth' is the width of the device
         *  'amplitude' is the height of wave
         */
        deviceWidth = canvas.getWidth();
        center = canvas.getHeight()/2;
        /*y = center;
        top = canvas.getHeight()/2 - amplitude;
        bottom = canvas.getHeight()/2 + amplitude;

        while (x_coordinate < deviceWidth) {
            canvas.drawPoint(x_coordinate, getY(y), waveColor);
            x_coordinate += 1;
        }*/

        for (int i = 0; i < deviceWidth; i = i + 10)
        {
            canvas.drawLine (i, center + amplitude * (float)Math.sin(i/180.0*Math.PI), i + 10, center + amplitude * (float)Math.sin ((i + 10)/180.0*Math.PI), waveColor);
        }
    }

    public void setAmplitude(int amplitude) {
        this.amplitude = amplitude;
        x_coordinate = 0;
        y = center;
    }

    public int getY(int y_coordinate) {
        if (y_coordinate <= center) {
            if (y_coordinate == top) {
                toTop = false;
            }
            if (toTop && y > top) {
                y -= 2;
            } else if (!toTop && y < bottom){
                y += 2;
            }
        } else {
            if (y_coordinate == bottom) {
                toTop = true;
            }
            if (toTop && y > top) {
                y -= 2;
            } else if (!toTop && y < bottom){
                y += 2;
            }
        }
        return y;
    }
}
