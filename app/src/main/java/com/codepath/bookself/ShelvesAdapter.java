package com.codepath.bookself;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;

import org.parceler.Parcels;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ShelvesAdapter extends RecyclerView.Adapter<ShelvesAdapter.ViewHolder>{

    private ArrayList<Shelves> shelvesList;
    private Context context;

    public ShelvesAdapter(ArrayList<Shelves> shelvesList, Context context) {
        this.shelvesList = shelvesList;
        this.context = context;
    }

    @NonNull
    @Override
    public ShelvesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ShelvesAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.shelf_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShelvesAdapter.ViewHolder holder, int position) {
        Shelves shelf = shelvesList.get(position);
        holder.bind(shelf);
    }

    @Override
    public int getItemCount() {
        return shelvesList.size();
    }

    public void updateAdapter(ArrayList<Shelves> mShelves) {
        this.shelvesList = mShelves;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvNameShelf;
        TextView tvAmountBooks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameShelf = itemView.findViewById(R.id.tvNameShelf);
            tvAmountBooks = itemView.findViewById(R.id.tvAmountBooks);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                Shelves shelf = shelvesList.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, ShelveDetailsActivity.class);
                // serialize the movie using parceler, use its short name as a key
                intent.putExtra(Shelves.class.getSimpleName(), Parcels.wrap(shelf));
                // show the activity
                context.startActivity(intent);
            }
        }

        public void bind(Shelves shelf) {
            tvNameShelf.setText(shelf.getNameShelf());
            tvAmountBooks.setText(String.valueOf(shelf.getAmountBooks()));
        }
    }
}