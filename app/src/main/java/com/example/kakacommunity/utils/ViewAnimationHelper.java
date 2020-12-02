package com.example.kakacommunity.utils;

import android.animation.Animator;
import android.view.View;
import android.view.ViewPropertyAnimator;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class ViewAnimationHelper {

    private boolean isAnimating;

    /**
     * 并行执行动画
     */
    private ViewPropertyAnimator viewPropertyAnimator;

    private View view;


    public void bindView(View view){
        if(view == null){
            throw new NullPointerException("The view is cannot null");
        }
        this.view = view;
        if (viewPropertyAnimator == null){  //设置多个view动画
            viewPropertyAnimator = view.animate();
            viewPropertyAnimator.setDuration(300);
            viewPropertyAnimator.setInterpolator(new LinearOutSlowInInterpolator());  //设置插值器
        }
    }


    public boolean isAnimating(){
        return this.isAnimating;
    }


    public void hideFloatActionButton(){
        viewPropertyAnimator.scaleX(0.0f).scaleY(0.0f).alpha(0.0f).setListener(animationListener);
    }


    public void showFloatActionButton(){
        view.setVisibility(View.VISIBLE);
        viewPropertyAnimator.scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setListener(null);
    }


    private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animator) {
            isAnimating = true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            isAnimating = false;
            view.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    };
}
