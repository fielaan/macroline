package com.fielaan.macroline.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fielaan.macroline.MacrolineAccessibilityService;
import com.fielaan.macroline.R;
import com.fielaan.macroline.model.Macros;

import java.util.List;

public class MacrosAdapter extends RecyclerView.Adapter<MacrosAdapter.MacrosViewHolder>{

    Context context;
    List<Macros> macroses;

    public MacrosAdapter(Context context, List<Macros> macroses) {
        this.context = context;
        this.macroses = macroses;
    }

    @NonNull
    @Override
    public MacrosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View macrosItems = LayoutInflater.from(context).inflate(R.layout.macros_item, parent, false);
        return new MacrosViewHolder(macrosItems);
    }

    @Override
    public void onBindViewHolder(@NonNull MacrosViewHolder holder, int position) {

        switch (macroses.get(position).getType()){
            case MACROS:
                holder.icon.setImageResource(R.drawable.ic_baseline_description_24);
                break;
            case FOLDER:
                holder.icon.setImageResource(R.drawable.ic_baseline_folder_24);
                break;
            case BACK_BUTTON:
                holder.icon.setImageResource(R.drawable.ic_baseline_more_horiz_24);
        }

        holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));


        Macros thisMacros = macroses.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String func = holder.macrosTitle.getText().toString();
                Log.d("CLICK:", func == null? "NULL": func);
                if(thisMacros.getMacrolineAccessibilityService() == null) thisMacros.getModulesFragment().OnClick(func, thisMacros.getType());
                else thisMacros. getMacrolineAccessibilityService().OnClick(func, thisMacros.getType());
            }
        });
        holder.macrosTitle.setText(macroses.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return macroses.size();
    }

    public static final class MacrosViewHolder extends RecyclerView.ViewHolder{

        TextView macrosTitle;
        ImageView icon;

        public MacrosViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.macrosIcon);
            macrosTitle = itemView.findViewById(R.id.macrosTitle);

        }
    }

}
