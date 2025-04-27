package eki.dore.flabbygame.ui.screen.game_over

import android.view.View
import androidx.lifecycle.lifecycleScope
import eki.dore.flabbygame.databinding.FragmentGameOverBinding
import eki.dore.flabbygame.ui.screen.main.MainActivity.Companion.KEY_SCORE
import eki.dore.flabbygame.ui.core.BaseFragment
import eki.dore.flabbygame.util.AnimUtil.startAnimBounce
import eki.dore.flabbygame.util.Constant
import eki.dore.flabbygame.util.SharedPrefs
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
