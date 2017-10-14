package app.com.flighter.foo.gettix;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kourotchkinalex on 10/14/17.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
{

    private List<Show> values;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {



        TextView showName;
        TextView showDateAndTime;
        TextView showLocation;
        ImageView imageView;

        public View layout;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            showName = (TextView) itemView.findViewById(R.id.show_name);
            showDateAndTime = (TextView) itemView.findViewById(R.id.show_date_and_time);
            showLocation = (TextView) itemView.findViewById(R.id.show_location);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    public void add(int position, Show item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    public RecyclerViewAdapter(List<Show> dataSet, Context c) {
        values = dataSet;
        context = c;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(R.layout.list_item, viewGroup, shouldAttachToParentImmediately);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Show show = values.get(position);

        holder.showName.setText(show.getTitle());
        holder.showDateAndTime.setText(show.getDate() + show.getTime());
        holder.showLocation.setText(show.getLocation());

        int id = context.getResources().getIdentifier(show.getDrawNmae(), "mipmap",
                context.getPackageName());

        holder.imageView.setImageResource(id);

        // Setting the image on the top of the page
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("I", "was clicked");
                Context contextO = v.getContext();
                Intent intent = new Intent(contextO, DetailActivity.class);
                intent.putExtra("showObject", show);
                contextO.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return values.size();
    }

}
