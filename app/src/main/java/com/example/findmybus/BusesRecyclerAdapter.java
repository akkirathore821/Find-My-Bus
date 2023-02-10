package com.example.findmybus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class BusesRecyclerAdapter extends RecyclerView.Adapter<BusesRecyclerAdapter.ViewHolder>{

    private ArrayList<Bus> mBuses = new ArrayList<>();
    private Context mContext;

    public BusesRecyclerAdapter(ArrayList<Bus> buses, Context context) {
        this.mBuses = buses;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_bus_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ((ViewHolder)holder).busTitle.setText(mBuses.get(position).getBusNo());
        Bus bus = mBuses.get(position);

        ((ViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(mContext, "Bus Id : " + bus.getId(), Toast.LENGTH_LONG).show();
                Intent chatIntent = new Intent(mContext, UserMapActivity.class);
                chatIntent.putExtra("bus",bus.getBusId());
                mContext.startActivity(chatIntent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mBuses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView busTitle;
        public ViewHolder(View itemView) {
            super(itemView);
            busTitle = itemView.findViewById(R.id.bus_title);
        }

    }


}
