package developer.shivam.joyplayer.activity;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import developer.shivam.joyplayer.views.SinView;

public class CustomView extends AppCompatActivity {

    private SinView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new SinView(CustomView.this);
        view.setAmplitude(100);
        setContentView(view);

        ValueAnimator animator = ValueAnimator.ofInt(0, 500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                view.setAmplitude(value);
                view.invalidate();
            }
        });
        animator.setDuration(5000);
        animator.start();
    }
}
