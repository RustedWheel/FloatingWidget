package com.rustedWheel.floatingwidget

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.layout_floating_widget.view.*

class FloatingViewService : Service(), View.OnTouchListener {

    private val windowManager: WindowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }
    private val params by lazy {
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }
    private var floatingView: View? = null
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        params.gravity = Gravity.TOP or Gravity.START        //Initially view will be added to top-left corner
        params.x = 0
        params.y = 200

        windowManager.addView(floatingView, params)

        val context = this
        floatingView?.run {
            closeBtn.setOnClickListener {
                stopSelf()
            }

            playBtn.setOnClickListener {
                Toast.makeText(context, "Playing the song.", Toast.LENGTH_LONG).show()
            }

            nextBtn.setOnClickListener {
                Toast.makeText(context, "Playing next song.", Toast.LENGTH_LONG).show()
            }

            prevBtn.setOnClickListener {
                Toast.makeText(context, "Playing previous song.", Toast.LENGTH_LONG).show()
            }

            closeButton.setOnClickListener {
                collapseView.isVisible = true
                expandedContainer.isVisible = false
            }

            openButton.setOnClickListener {
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                //close the service and remove view from the view hierarchy
                stopSelf()
            }

            rootContainer.setOnTouchListener(context)
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                //remember the initial position.
                initialX = params.x
                initialY = params.y

                //get the touch location
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                //Calculate the X and Y coordinates of the view.
                params.x = initialX + (event.rawX - initialTouchX).toInt()
                params.y = initialY + (event.rawY - initialTouchY).toInt()

                //Update the layout with new X & Y coordinate
                windowManager.updateViewLayout(floatingView, params)
                return true
            }
            MotionEvent.ACTION_UP -> {
                val xDiff = (event.rawX - initialTouchX).toInt()
                val yDiff = (event.rawY - initialTouchY).toInt()

                //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                //So that is click event.
                if (xDiff < 10 && yDiff < 10) {
                    if (isViewCollapsed()) {
                        floatingView?.let {
                            it.collapseView.isVisible = false
                            it.expandedContainer.isVisible = true
                        }
                    }
                }
                return true
            }

        }
        return false
    }

    private fun overlayParam() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
        WindowManager.LayoutParams.TYPE_PHONE;
    }

    private fun isViewCollapsed() = floatingView?.collapseView?.isVisible ?: false

    override fun onDestroy() {
        super.onDestroy()
        if(floatingView != null) windowManager.removeView(floatingView)
    }
}