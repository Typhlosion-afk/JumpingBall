package com.example.jumpingball.ui.screen.game_over

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.jumpingball.databinding.FragmentGameOverBinding
import com.example.jumpingball.ui.screen.main.MainActivity.Companion.KEY_SCORE
import com.example.jumpingball.ui.core.BaseFragment
import com.example.jumpingball.util.AnimUtil.startAnimBounce
import com.example.jumpingball.util.Constant
import com.example.jumpingball.util.SharedPrefs
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class GameOverFragment : BaseFragment<FragmentGameOverBinding>(FragmentGameOverBinding::inflate) {
    private var clickEvent: EventClick? = null
    private var score: Int? = null
    override fun onViewReady() {
        initAction()
        subscribeUi()
    }

    private fun subscribeUi(){

        arguments?.let {
            score = arguments?.get(KEY_SCORE) as Int
        }

        binding.txtRestart.visibility = View.GONE
        lifecycleScope.launch {
            delay(500L)
            binding.root.visibility = View.VISIBLE
            delay(200L)
            binding.txtRestart.visibility = View.VISIBLE
            binding.txtRestart.startAnimBounce()
        }
        binding.bestScore.text = SharedPrefs[Constant.BEST_SCORE, Int::class.java].toString()
        binding.lastScore.text = score?.toString()
    }

     private fun initAction(){
        binding.txtRestart.setOnClickListener{
            clickEvent?.onRestartClick()
        }
    }

    fun setClickEvent(clickEvent: EventClick){
        this.clickEvent = clickEvent
    }

    class EventClick(val event: () -> Unit){
        fun onRestartClick() = event()
    }
}
