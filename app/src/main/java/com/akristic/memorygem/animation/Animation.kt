package com.akristic.memorygem.animation


import android.os.Handler
import android.view.View
import android.widget.ImageView
import com.akristic.memorygem.utility.Tile
import com.akristic.memorygem.screens.game.GameViewModel
import com.akristic.memorygem.sound.Sound

class Animation {
    var isAnimationShowing = false

    enum class Duration(val milliseconds: Long) {
        ANIMATION_DELAY(700),
        ANIMATION_DURATION(400),
        ANIMATION_LAST_TILE_DELAY(1200)
    }

    /**
     * Animation after we pass a level
     */
    fun showAnimationLevelComplete(buttonsList: ArrayList<Tile>, sound: Sound) {
        //set this to true and later after sequence animation for next level it will be set to false
        isAnimationShowing = true
        for (i in buttonsList.indices) {
            val handler = Handler()
            val delay = Duration.ANIMATION_DURATION.milliseconds * (i + 1)
            handler.postDelayed({
                sound.playGemSound(i)
                buttonsList.get(i).imageViewButton.animate().setDuration(Duration.ANIMATION_DURATION.milliseconds)
                    .alpha(0f)
                    .scaleY(0f)
                    .scaleX(0f).start()
            }, delay)
        }
        for (i in buttonsList.indices) {
            val handler = Handler()
            val delay =
                Duration.ANIMATION_DURATION.milliseconds * buttonsList.size + Duration.ANIMATION_DELAY.milliseconds
            handler.postDelayed({
                buttonsList.get(i).imageViewButton.animate().setDuration(Duration.ANIMATION_DURATION.milliseconds)
                    .alpha(1.0f)
                    .scaleY(1.0f).scaleX(1.0f).start()
            }, delay)

        }
   }

    /**
     * Animation that display us a sequence of buttons that we have to repeat
     */
    fun showAnimationSequence(
        buttonsSequence: ArrayList<Int>?,
        buttonsList: ArrayList<Tile>,
        sound: Sound,
        viewModel: GameViewModel
    ) {
        if (buttonsSequence != null) {
            isAnimationShowing = true
            viewModel.stopTimer()
            for (i in buttonsSequence.indices) {
                val handler = Handler()
                val buttonNumber = buttonsSequence.get(i)
                val delay = Duration.ANIMATION_DELAY.milliseconds * (i + 1)
                handler.postDelayed({
                    buttonClickedAnimation(buttonsList, buttonNumber)
                    sound.playGemSound(buttonNumber)
                }, delay)
            }
            val durationOfAnimationDelay = Duration.ANIMATION_DELAY.milliseconds * buttonsSequence.size
            val startHandler = Handler()
            startHandler.postDelayed({
                isAnimationShowing = false
                viewModel.startTimer()
            }, durationOfAnimationDelay)
        }
    }




    //simple animation when single button is clicked
    fun buttonClickedAnimation(buttonsList: ArrayList<Tile>, buttonNumber: Int) {
        buttonsList[buttonNumber].imageViewButton.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .alpha(0.5f)
            .setDuration(Duration.ANIMATION_DURATION.milliseconds / 2).start()
        val handler1Shrink = Handler()
        handler1Shrink.postDelayed({
            buttonsList[buttonNumber].imageViewButton.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(Duration.ANIMATION_DURATION.milliseconds / 2).start()
        }, (Duration.ANIMATION_DURATION.milliseconds / 2))
     }

    fun animateMonsterImage(monsterImage: ImageView, sound: Sound) {
        //set monster image visible and than BUUMMM make a prank animation
        monsterImage.visibility = View.VISIBLE
        sound.playMonsterSound()
        monsterImage.animate()
            .alpha(0.0f).scaleY(0.1f).scaleX(0.1f).start()
        monsterImage.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .alpha(1.0f)
            .setDuration(Duration.ANIMATION_DURATION.milliseconds)
            .start()

        //wait for a moment and than fade monster image out
        val handlerFade = Handler()
        handlerFade.postDelayed({
            monsterImage.animate().alpha(0.0f).setDuration(Duration.ANIMATION_DURATION.milliseconds)
        }, Duration.ANIMATION_DURATION.milliseconds * 4)
    }

}