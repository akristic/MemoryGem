package com.akristic.memorygem.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.akristic.memorygem.R


class Sound(private val mContext: Context?) {
    var gameSoundPool: SoundPool
    internal var gemSound01Id: Int = 0
    internal var gemSound02Id: Int = 0
    internal var gemSound03Id: Int = 0
    internal var gemSound04Id: Int = 0
    internal var gemSound05Id: Int = 0
    internal var monsterSoundId: Int = 0

    init {
        gameSoundPool = createSounds()
        gemSound01Id = gameSoundPool.load(mContext, R.raw.gem_sound01, 1)
        gemSound02Id = gameSoundPool.load(mContext, R.raw.gem_sound02, 1)
        gemSound03Id = gameSoundPool.load(mContext, R.raw.gem_sound03, 1)
        gemSound04Id = gameSoundPool.load(mContext, R.raw.gem_sound04, 1)
        gemSound05Id = gameSoundPool.load(mContext, R.raw.gem_sound05, 1)
        monsterSoundId = gameSoundPool.load(mContext, R.raw.monster_sound, 1)
    }

    private fun createSounds(): SoundPool {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val soundPool: SoundPool = SoundPool.Builder()
                .setMaxStreams(7)
                .setAudioAttributes(audioAttributes)
                .build()
            return soundPool
        } else {
            val soundPool = SoundPool(6, AudioManager.STREAM_MUSIC, 0)
            return soundPool
        }
    }


    fun playGemSound1() {
        gameSoundPool.play(gemSound01Id, 0.3f, 0.3f, 1, 0, 1f)
    }

    fun playGemSound2() {
        gameSoundPool.play(gemSound02Id, 0.3f, 0.3f, 1, 0, 1f)
    }

    fun playGemSound3() {
        gameSoundPool.play(gemSound03Id, 0.3f, 0.3f, 1, 0, 1f)
    }

    fun playGemSound4() {
        gameSoundPool.play(gemSound04Id, 0.3f, 0.3f, 1, 0, 1f)
    }

    fun playGemSound5() {
        gameSoundPool.play(gemSound05Id, 0.3f, 0.3f, 1, 0, 1f)
    }

    fun playMonsterSound() {
        gameSoundPool.play(monsterSoundId, 1f, 1f, 1, 0, 1f)
    }

    /**
     * Play sound for passed gem
     *
     * @param gemNumber is from 0 for gem1 to 4 for gem5
     */
    fun playGemSound(gemNumber: Int) {
        if (gemNumber == 0) {
            playGemSound1()
        }
        if (gemNumber == 1) {
            playGemSound2()
        }
        if (gemNumber == 2) {
            playGemSound3()
        }
        if (gemNumber == 3) {
            playGemSound4()
        }
        if (gemNumber == 4) {
            playGemSound5()
        }
    }

    fun release(){
        gameSoundPool.release()
    }
}
