package edu.califer.workout

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_exercise.*
import kotlinx.android.synthetic.main.dialog_custom_back_confirmation.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class ExerciseActivity : AppCompatActivity() , TextToSpeech.OnInitListener {

    //RestStateProgressBar
    private var restTimer : CountDownTimer? = null
    private var restProgress = 0
    private var restTimerDuration : Long = 10

    //Exercise ProgressBar
    private var exerciseTimer : CountDownTimer? = null
    private var exerciseProgress = 0 // Variable for exercise timer progress. As initial the exercise progress is set to 0. As we are about to start.
    private var exerciseTimerDuration : Long = 30

    //SetUp the Exercise Details
    private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    //Text To Speech
    private var tts : TextToSpeech? = null

    //Media Player
    private var player : MediaPlayer? = null

    //Recycler View
    private var exerciseAdapter : ExerciseStatusAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        setSupportActionBar(toolbar_exercise_activity)
        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        toolbar_exercise_activity.setNavigationOnClickListener{
            onBackPressed()
        }
        exerciseList = Constant.defaultExerciseList()

        tts = TextToSpeech(this,this)

        setupRestView()

        setUpExerciseStatusRecyclerView()


    }

    /**
     * Here is Destroy function we will reset the rest timer to initial if it is running.
     */
    public override fun onDestroy()
    {
        if (restTimer != null)
        {
            restTimer!!.cancel()
            restProgress = 0
        }

        if (exerciseTimer != null)
        {
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }

        if (tts !=null)
        {
            tts!!.stop()
            tts!!.shutdown()
        }

        if (player!=null)
        {
            player!!.stop()
        }

        super.onDestroy()
    }

    /**
     * Function is used to set the timer for REST.
     */
    private fun setupRestView()
    {
        try
        {
            //val soundUri = Uri.parse("android:resource://edu.califer.workout/"+R.raw.press_start) //alternative way to use
            player = MediaPlayer.create(applicationContext , R.raw.press_start)
            player!!.isLooping = false
            player!!.start()
        }catch (e:Exception)
        {
            e.printStackTrace()
        }


        llRestView.visibility = View.VISIBLE
        llExerciseView.visibility = View.GONE

        /**
         * Here firstly we will check if the timer is running the and it is not null then cancel the running timer and start the new one.
         * And set the progress to initial which is 0.
         */
        if (restTimer != null)
        {
            restTimer!!.cancel()
            restProgress = 0
        }

        speakOut("Upcoming Exercise is "+exerciseList!![currentExercisePosition + 1].getName())

        tvUpcomingExerciseName.text = exerciseList!![currentExercisePosition + 1].getName()


        // This function is used to set the progress details.
        setRestProgressBar()


    }

    /**
     * Function is used to set the progress of timer using the progress
     */
    private fun setRestProgressBar()
    {
        progressBar.max = restTimerDuration.toInt()

        progressBar.progress = restProgress // Sets the current progress to the specified value.

        /**
         * @param millisInFuture The number of millis in the future from the call
         *   to {#start()} until the countdown is done and {#onFinish()}
         *   is called.
         * @param countDownInterval The interval along the way to receive
         *   {#onTick(long)} callbacks.
         */
        // Here we have started a timer of 10 seconds so the 10000 is milliseconds is 10 seconds and the countdown interval is 1 second so it 1000.
        restTimer = object : CountDownTimer(restTimerDuration*1000 , 1000)
        {
            override fun onTick(millisUntilFinished: Long)
            {
                restProgress++ // It is increased to ascending order
                progressBar.progress = restTimerDuration.toInt() - restProgress // Indicates progress bar progress
                tvTimer.text = (restTimerDuration.toInt() - restProgress).toString()  // Current progress is set to text view in terms of seconds.
            }

            override fun onFinish()
            {
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)

                exerciseAdapter!!.notifyDataSetChanged()
                // START
                setupExerciseView()
                // END
            }
        }.start()
    }

    // START
    /**
     * Function is used to set the progress of the timer using the progress for Exercise View.
     */
    private fun setupExerciseView()
    {

        // Here according to the view make it visible as this is Exercise View so exercise view is visible and rest view is not.
        llRestView.visibility = View.GONE
        llExerciseView.visibility = View.VISIBLE

        /**
         * Here firstly we will check if the timer is running and it is not null then cancel the running timer and start the new one.
         * And set the progress to the initial value which is 0.
         */
        if (exerciseTimer != null)
        {
            exerciseTimer!!.cancel()
            exerciseProgress = 0
        }

        speakOut(exerciseList!![currentExercisePosition].getName()+" Started")

        setExerciseProgressBar()

        ivImage.setImageResource(exerciseList!![currentExercisePosition].getImage())
        tvExerciseName.text = exerciseList!![currentExercisePosition].getName()

    }
    // END

    /**
     * Function is used to set the progress of the timer using the progress for Exercise View for 30 Seconds
     */
    private fun setExerciseProgressBar()
    {
        progressBarExercise.max = exerciseTimerDuration.toInt()

        progressBarExercise.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(exerciseTimerDuration*1000, 1000)
        {
            override fun onTick(millisUntilFinished: Long)
            {
                    exerciseProgress++
                    progressBarExercise.progress = exerciseTimerDuration.toInt() - exerciseProgress
                    tvExerciseTimer.text = (exerciseTimerDuration.toInt() - exerciseProgress).toString()
            }

            override fun onFinish()
            {
               if (currentExercisePosition < exerciseList?.size!! - 1)
               {

                   exerciseList!![currentExercisePosition].setIsSelected(false)
                   exerciseList!![currentExercisePosition].setIsCompleted(true)

                   exerciseAdapter!!.notifyDataSetChanged()

                   setupRestView()
               }
                else
               {
                   finish()
                   val intent = Intent(this@ExerciseActivity , FinishActivity::class.java)
                   startActivity(intent)
               }
            }
        }.start()
    }

    /**
     * Text To Speech onInit()
     */
    override fun onInit(status: Int)
    {
       if (status == TextToSpeech.SUCCESS)
       {
           val result = tts?.setLanguage(Locale.US)
           if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
           {
               Log.e("TTS" , "Language specified is not supported")
           }
       }
        else
       {
           Log.e("TTS" , "Initialization failed")
       }
    }

    private fun speakOut(text : String)
    {
        tts!!.speak(text , TextToSpeech.QUEUE_FLUSH , null , "")
    }

    private fun setUpExerciseStatusRecyclerView()
    {
        rvExerciseStatus.layoutManager = LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false)
        exerciseAdapter = ExerciseStatusAdapter(exerciseList!! , this)
        rvExerciseStatus.adapter = exerciseAdapter
    }


    //TODO Add pause Timer when called
    private fun customDialogForBackButton()
    {
        val customDialog = Dialog(this)

//        if (restTimer != null)
//        {
//            restTimer!!.cancel()
//        }
//
//        if (exerciseTimer != null)
//        {
//            exerciseTimer!!.cancel()
//        }

        customDialog.setContentView(R.layout.dialog_custom_back_confirmation)

        customDialog.tvYes.setOnClickListener {
            finish()
            customDialog.dismiss()
        }
        customDialog.tvNo.setOnClickListener {
            customDialog.dismiss()
//            if (restTimer != null)
//            {
//                restTimer!!.start()
//            }
//
//            if (exerciseTimer != null)
//            {
//                exerciseTimer!!.start()
//            }
        }
        customDialog.show()
    }

    override fun onBackPressed() {
        customDialogForBackButton()
    }
}