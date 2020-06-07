package ml.chiragkhandhar.retagged;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExploreViewHolder extends RecyclerView.ViewHolder {
    TextView name, type, address, distance;
    ImageView icon;

    public ExploreViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        type = itemView.findViewById(R.id.type);
        address = itemView.findViewById(R.id.address);
        distance = itemView.findViewById(R.id.distance);
        icon = itemView.findViewById(R.id.icon);
    }
}
