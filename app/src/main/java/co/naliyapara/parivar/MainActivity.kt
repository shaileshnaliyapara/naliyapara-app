package co.naliyapara.parivar

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_home)
        }

        // ---------- Top bar ----------
        val topBar = TextView(this).apply {
            text = "નળીયાપરા પરિવાર"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setBackgroundColor(getColor(R.color.blue))
            minHeight = dp(56)
        }
        root.addView(topBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // ---------- Scroll content ----------
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(14), dp(14), dp(14), dp(20))
        }

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.logo_parivar)
            adjustViewBounds = true
        }
        content.addView(logo, LinearLayout.LayoutParams(dp(130), dp(130)))

        val title = TextView(this).apply {
            text = "નળીયાપરા પરિવાર"
            setTextColor(getColor(R.color.text_dark))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        content.addView(title)

        val tagline = TextView(this).apply {
            text = "એક પરિવાર... એક એપ... બધા માટે..."
            setTextColor(getColor(R.color.text_dark))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(14))
        }
        content.addView(tagline)

        // ---------- Grid (3 columns) ----------
        val grid = GridLayout(this).apply {
            columnCount = 3
            useDefaultMargins = false
        }
        for (tile in TILES) grid.addView(buildTile(tile))
        content.addView(grid, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val footer = TextView(this).apply {
            text = "આપણા પરિવારની એકતા એ જ અમારી શક્તિ"
            setTextColor(getColor(R.color.blue_dark))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(6))
        }
        content.addView(footer)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)

        // status bar / nav bar niche content na chhupai
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, bars.bottom)
            insets
        }
    }

    private fun buildTile(tile: Tile): View {
        val cell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(4), dp(10), dp(4), dp(10))
            isClickable = true
            isFocusable = true
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
            setOnClickListener { open(tile) }
        }

        val box = TextView(this).apply {
            text = tile.emoji
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(16).toFloat()
                setColor(tile.color)
            }
            elevation = dp(3).toFloat()
        }
        cell.addView(box, LinearLayout.LayoutParams(dp(68), dp(68)))

        val label = TextView(this).apply {
            text = tile.title
            setTextColor(getColor(R.color.text_dark))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            maxLines = 2
            setPadding(0, dp(7), 0, 0)
        }
        cell.addView(label, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val screenW = resources.displayMetrics.widthPixels - dp(28)
        cell.layoutParams = GridLayout.LayoutParams().apply {
            width = screenW / 3
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        return cell
    }

    private fun open(tile: Tile) {
        startActivity(Intent(this, WebActivity::class.java).apply {
            putExtra(WebActivity.EXTRA_TITLE, tile.title)
            putExtra(WebActivity.EXTRA_URL, Config.BASE_URL + (tile.path ?: ""))
            putExtra(WebActivity.EXTRA_TAB, tile.tabLabel)
        })
    }
}
