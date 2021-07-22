package com.codepath.bookself;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.UsersBookProgress;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private ArrayList<UsersBookProgress> progressesList;
    private Context context;

    public ProfileAdapter(ArrayList<UsersBookProgress> booksList, Context context) {
        this.progressesList = booksList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProfileAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.books_progresses_item, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {
        UsersBookProgress progress = progressesList.get(position);
        holder.bind(progress);
    }

    @Override
    public int getItemCount() {
        return progressesList.size();
    }

    public void updateAdapter(ArrayList<UsersBookProgress> mProgress) {
        this.progressesList = mProgress;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvBookTitle;
        TextView tvDate;
        TextView tvAuthor;
        TextView tvProgressBook;
        ProgressBar progressBar;
        ImageView ivBookImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            ivBookImage = itemView.findViewById(R.id.ivBookImage);
            tvDate = itemView.findViewById(R.id.tvDate);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgressBook = itemView.findViewById(R.id.tvProgressBook);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                UsersBookProgress progress = progressesList.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, DetailsActivity.class);
                // serialize the movie using parceler, use its short name as a key
                intent.putExtra("Progress", true);
                intent.putExtra(UsersBookProgress.class.getSimpleName(), Parcels.wrap(progress));
                // show the activity
                context.startActivity(intent);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void bind(UsersBookProgress progress) {
            BooksParse book = progress.getBook();
            assert book != null;
            double currentProgress = ((double) progress.getCurrentPage()/ (double) book.getPageCount()) * 100;
            int newCurrentProgress = (int )Math.round(currentProgress);
            progressBar.setProgress(newCurrentProgress);
            tvProgressBook.setText(String.valueOf(newCurrentProgress) + "%");
            assert book != null;
            tvBookTitle.setText(book.getTitle());
            tvAuthor.setText(String.join(", " , book.getAuthors()));
            DateFormat df = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            Log.i("ProfileAdapter", "Date: " +progress.getUpdatedAt());
            String lastReadAt = df.format(progress.getUpdatedAt());
            tvDate.setText("Last read: " +lastReadAt);
            String httpLink = book.getThumbnail();
            if (!httpLink.equals("")) {
                String httpsLink = httpLink.substring(0,4) + "s" + httpLink.substring(4);
                Log.i("Something", "Link: " + httpsLink);
                Glide.with(context).load(httpsLink).transform(new RoundedCornersTransformation(30, 10)).into(ivBookImage);
            }
        }
    }
}
