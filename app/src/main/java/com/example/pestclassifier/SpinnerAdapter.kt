package com.example.pestclassifier

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull

class SpinnerAdapter : ArrayAdapter<String> {
    lateinit var layoutInflater : LayoutInflater;

    constructor (context : Context, resource : Int, pest: Array<String>) : super(context, resource, pest) {
        layoutInflater = LayoutInflater.from(context)
    }

    @NonNull
    @Override
    public override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView : View = layoutInflater.inflate(R.layout.custom_spinner_adapter, null, true)
        val pest : String? = getItem(position)
        val drawables = arrayOf(R.drawable.se, R.drawable.sl, R.drawable.tu, R.drawable.ba, R.drawable.mp, R.drawable.ha, android.R.drawable.ic_menu_help)
        val textView : TextView = rowView.findViewById(R.id.pestTextView)
        val imageView : ImageView = rowView.findViewById(R.id.pestIcon)
        textView.setText(pest)
        imageView.setImageResource(drawables[position])
        return rowView;
    }

    @Override
    public override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var _convertView = convertView
        if(_convertView == null) {
            _convertView = layoutInflater.inflate(R.layout.custom_spinner_adapter, parent, false)
        }

        val pest : String? = getItem(position)
        val drawables = arrayOf(R.drawable.se, R.drawable.sl, R.drawable.tu, R.drawable.ba, R.drawable.mp, R.drawable.ha, android.R.drawable.ic_menu_help)
        val textView : TextView = _convertView!!.findViewById(R.id.pestTextView)
        val imageView : ImageView = _convertView.findViewById(R.id.pestIcon)
        textView.setText(pest)
        imageView.setImageResource(drawables[position])
        return _convertView!!;
    }



}