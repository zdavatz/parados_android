package com.ywesee.parados

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class GameAdapter(
    private val games: List<GameInfo>,
    private val onGameClick: (String) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    private val buttonColors = listOf(
        "#43a047", "#1e88e5", "#ff9800", "#e53935", "#8e24aa"
    )

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.game_title)
        val players: TextView = view.findViewById(R.id.game_players)
        val description: TextView = view.findViewById(R.id.game_description)
        val buttonsContainer: LinearLayout = view.findViewById(R.id.buttons_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.title.text = game.title
        holder.players.text = game.players
        holder.description.text = game.description

        holder.buttonsContainer.removeAllViews()

        if (game.variants.isEmpty()) {
            val btn = createButton(holder.itemView.context, "Play", buttonColors[0], 1)
            btn.setOnClickListener { onGameClick(game.filename) }
            holder.buttonsContainer.addView(btn)
        } else {
            val count = game.variants.size
            game.variants.forEachIndexed { index, variant ->
                val color = buttonColors[index % buttonColors.size]
                val btn = createButton(holder.itemView.context, variant.label, color, count)
                btn.setOnClickListener { onGameClick(variant.filename) }
                holder.buttonsContainer.addView(btn)
                // Remove right margin from last button
                if (index == count - 1) {
                    val params = btn.layoutParams as LinearLayout.LayoutParams
                    params.setMargins(0, 0, 0, 0)
                }
            }
        }
    }

    override fun getItemCount() = games.size

    private fun createButton(context: android.content.Context, text: String, bgColor: String, count: Int): MaterialButton {
        val btn = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonStyle)
        btn.text = text
        btn.setBackgroundColor(Color.parseColor(bgColor))
        btn.setTextColor(if (bgColor == "#ff9800") Color.parseColor("#263238") else Color.WHITE)
        btn.cornerRadius = 20
        btn.isAllCaps = false
        btn.minimumWidth = 0
        btn.minWidth = 0
        btn.setPadding(16, 0, 16, 0)

        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        if (count > 1) {
            params.setMargins(0, 0, 12, 0)
        }
        btn.layoutParams = params

        return btn
    }
}
