package com.fitgreat.airfacerobot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.model.PositionEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.ViewHolder> {
    private static final String TAG = "PositionAdapter";
    private Context mcontext;
    private List<LocationEntity> mlist = new ArrayList<>();

    public static final String EVE_SELECTED_POSITION = "eve_selected_position";


    public PositionAdapter(Context context,List<LocationEntity> positionList){
        mcontext = context;
        mlist = positionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.position_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_name.setText(mlist.get(position).getF_Name());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PositionEvent posEve = new PositionEvent();
                posEve.setType(EVE_SELECTED_POSITION);
                posEve.setSelected(position);
                EventBus.getDefault().post(posEve);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = (TextView)itemView.findViewById(R.id.tv_name);
        }
    }
}
